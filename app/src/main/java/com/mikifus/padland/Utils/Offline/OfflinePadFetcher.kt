package com.mikifus.padland.Utils.Offline

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebSettings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import com.mikifus.padland.Utils.PadServer
import com.mikifus.padland.Utils.PadUrl
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

    private const val EXPORT_FORMAT = "html"
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
     * Offline copies are only available for pads of Etherpad Lite servers (for now).
     */
    suspend fun isAvailable(context: Context, pad: Pad): Boolean {
        val servers = PadListDatabase.getInstance(context).serverDao().getAllList()
        return isAvailable(pad, servers, context.resources)
    }

    /**
     * The server of the pad is the one with the same base URL (URL and pad prefix):
     * the saved servers set up with the Etherpad Lite jQuery plugin, or the
     * built-in ones that are not CryptPad.
     * Pads of unknown servers are not available.
     *
     * @param servers the saved servers, they come first: they may be
     * set up differently than a built-in one
     */
    fun isAvailable(pad: Pad, servers: List<Server>, resources: Resources): Boolean {
        val baseUrl = try {
            PadServer.Builder().padUrl(pad.mUrl).build().baseUrl
        } catch (e: MalformedURLException) {
            return false
        }

        val server = servers.firstOrNull {
            PadServer.Builder().padUrl(it.mUrl + it.mPadprefix).build().baseUrl == baseUrl
        }
        if (server != null) {
            return server.mJquery && !server.mCryptPad
        }

        val index = resources.getStringArray(R.array.etherpad_servers_url_padprefix)
            .indexOfFirst { PadServer.Builder().padUrl(it).build().baseUrl == baseUrl }
        if (index < 0) {
            return false
        }

        val cryptPadInfo = resources.obtainTypedArray(R.array.etherpad_servers_cryptpad)
        try {
            return !cryptPadInfo.getBoolean(index, false)
        } finally {
            cryptPadInfo.recycle()
        }
    }

    fun isUpdating(padId: Long): Boolean = synchronized(lock) { padId in runningPadIds }

    /**
     * Brings the offline copy in line with the saved pad, in the background:
     * downloads it if the pad has offline access and its server supports it.
     * Otherwise the previous copy is kept: it belongs to the pad, not to the
     * server or the offline access setting. It is only deleted with the pad
     * (or from the settings).
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
            if (pad == null) {
                // The copy belongs to the pad, it goes with it
                offlinePadStore.delete(padId)
                return false
            }
            // The previous copy is kept, even if the server is gone or changed
            if (!pad.mOfflineAccess || !isAvailable(context, pad)) {
                return false
            }

            val exportUrl = PadUrl.etherpadExportUrl(pad.mUrl, EXPORT_FORMAT)
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

