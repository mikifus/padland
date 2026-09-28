package com.mikifus.padland.Utils.Offline

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Utils.CryptPad.CryptPadUtils
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset

/**
 * Updates the offline copy of Etherpad Lite pads with their public HTML export,
 * the same file the "Export → HTML" button gives (no API key needed):
 *
 * ```
 * https://server/p/<pad>/export/html
 * ```
 *
 * Updates run in their own scope, so they finish even if the screen that
 * started them is closed. Only one update per pad runs at a time.
 */
object OfflinePadFetcher {
    private const val TAG = "OFFLINE_PAD_FETCHER"

    private const val EXPORT_PATH = "/export/html"
    private const val CONNECTION_TIMEOUT = 30_000
    private const val MAX_SIZE = 10 * 1024 * 1024

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var store: OfflinePadStore? = null

    private val lock = Any()
    private val runningPadIds = mutableSetOf<Long>()
    private val pendingPadIds = mutableSetOf<Long>()

    private val mUpdatingPadIds = MutableLiveData<Set<Long>>(emptySet())

    /**
     * Pads with an update running, to show the status of the copies.
     */
    val updatingPadIds: LiveData<Set<Long>> = mUpdatingPadIds

    /**
     * The store shared by the whole app, so its writes are synchronized.
     */
    fun getStore(context: Context): OfflinePadStore {
        return store ?: synchronized(this) {
            store ?: OfflinePadStore(context.applicationContext).also { store = it }
        }
    }

    /**
     * Offline copies are only available for Etherpad Lite pads (for now).
     */
    fun isAvailable(pad: Pad): Boolean {
        return isEtherpadUrl(pad.mUrl)
    }

    fun isEtherpadUrl(padUrl: String): Boolean {
        return try {
            URL(padUrl)
            !padUrl.contains("#") && !CryptPadUtils.seemsCrpytPadUrl(padUrl)
        } catch (e: MalformedURLException) {
            false
        }
    }

    /**
     * Query and fragment are dropped, i.e. the username and color added when viewing.
     */
    fun makeExportUrl(padUrl: String): String {
        val url = URL(padUrl)
        val path = url.path.trimEnd('/')
        return URL(url.protocol, url.host, url.port, path + EXPORT_PATH).toString()
    }

    fun isUpdating(padId: Long): Boolean = synchronized(lock) { padId in runningPadIds }

    /**
     * Brings the offline copy in line with the saved pad, in the background:
     * downloads it if the pad has offline access, deletes it otherwise.
     * Network errors are only logged, the previous copy is kept.
     *
     * The pad is read from the database, so call it after saving any change.
     * If the pad is already updating, it is updated once more afterwards.
     *
     * @param delayMillis wait before downloading, i.e. for the last changes
     * to reach the server
     * @param callback called on the main thread when done, not called if
     * an update of the same pad is already running
     */
    fun update(context: Context, padId: Long, delayMillis: Long = 0L,
               callback: ((updated: Boolean) -> Unit)? = null) {
        synchronized(lock) {
            if (padId in runningPadIds) {
                pendingPadIds.add(padId)
                return
            }
            runningPadIds.add(padId)
            mUpdatingPadIds.postValue(runningPadIds.toSet())
        }

        val applicationContext = context.applicationContext

        scope.launch {
            delay(delayMillis)

            var updated: Boolean
            do {
                updated = updateCopy(applicationContext, padId)
            } while (finishOrRepeat(padId))

            callback?.let {
                withContext(Dispatchers.Main) { it(updated) }
            }
        }
    }

    /**
     * @return true if it was requested again meanwhile, the pad keeps updating
     */
    private fun finishOrRepeat(padId: Long): Boolean = synchronized(lock) {
        if (pendingPadIds.remove(padId)) {
            return true
        }
        runningPadIds.remove(padId)
        mUpdatingPadIds.postValue(runningPadIds.toSet())
        false
    }

    private suspend fun updateCopy(context: Context, padId: Long): Boolean {
        val offlinePadStore = getStore(context)
        try {
            val pad = getPad(context, padId)
            if (pad == null || !pad.mOfflineAccess || !isAvailable(pad)) {
                offlinePadStore.delete(padId)
                return false
            }

            val exportUrl = makeExportUrl(pad.mUrl)
            val html = download(
                exportUrl,
                WebSettings.getDefaultUserAgent(context),
                CookieManager.getInstance().getCookie(exportUrl)
            )

            // Changed while downloading, whoever changed it requested another update
            val currentPad = getPad(context, padId)
            if (currentPad == null || !currentPad.mOfflineAccess || currentPad.mUrl != pad.mUrl) {
                return false
            }

            if (!offlinePadStore.save(padId, html)) {
                throw IOException("The offline copy could not be saved")
            }
            Log.d(TAG, "Offline copy updated for pad $padId")
            return true
        } catch (e: IOException) {
            Log.w(TAG, "The offline copy could not be updated for pad $padId", e)
        } catch (e: Exception) {
            ErrorReporter.reportNonFatal(e)
        }
        return false
    }

    private suspend fun getPad(context: Context, padId: Long): Pad? {
        return PadListDatabase.getInstance(context).padDao()
            .getByIds(listOf(padId))
            .firstOrNull()
    }

    private fun download(url: String, userAgent: String?, cookies: String?): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_TIMEOUT
            if (!cookies.isNullOrBlank()) {
                connection.setRequestProperty("Cookie", cookies)
            }
            if (!userAgent.isNullOrBlank()) {
                connection.setRequestProperty("User-Agent", userAgent)
            }

            if (connection.responseCode !in 200..299) {
                throw IOException("Unexpected HTTP response code: ${connection.responseCode}")
            }

            // i.e. a login page instead of the export
            val contentType = connection.contentType ?: ""
            if (!contentType.startsWith("text/html", true)) {
                throw IOException("Unexpected content type: $contentType")
            }

            val bytes = connection.inputStream.use { input ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read = input.read(buffer)
                while (read >= 0) {
                    output.write(buffer, 0, read)
                    if (output.size() > MAX_SIZE) {
                        throw IOException("The export is too big")
                    }
                    read = input.read(buffer)
                }
                output.toByteArray()
            }

            return String(bytes, getCharset(contentType))
        } finally {
            connection.disconnect()
        }
    }

    private fun getCharset(contentType: String): Charset {
        val charsetName = contentType.split(';')
            .map { it.trim() }
            .firstOrNull { it.startsWith("charset=", true) }
            ?.substringAfter('=')
            ?.trim('"', ' ')

        return try {
            charsetName?.let { Charset.forName(it) } ?: Charsets.UTF_8
        } catch (e: Exception) {
            Charsets.UTF_8
        }
    }
}

