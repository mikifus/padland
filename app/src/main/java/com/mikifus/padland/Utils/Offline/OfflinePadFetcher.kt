package com.mikifus.padland.Utils.Offline

import android.content.Context
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebSettings
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Utils.CryptPad.CryptPadUtils
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.Collections

/**
 * Updates the offline copy of Etherpad Lite pads with their public HTML export,
 * the same file the "Export → HTML" button gives (no API key needed):
 *
 * ```
 * https://server/p/<pad>/export/html
 * ```
 *
 * Updates run in their own scope, so they finish even if the screen that
 * started them is closed.
 */
object OfflinePadFetcher {
    private const val TAG = "OFFLINE_PAD_FETCHER"

    private const val EXPORT_PATH = "/export/html"
    private const val CONNECTION_TIMEOUT = 30_000
    private const val MAX_SIZE = 10 * 1024 * 1024

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val updatingPadIds: MutableSet<Long> = Collections.synchronizedSet(mutableSetOf())

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

    fun isUpdating(padId: Long): Boolean = updatingPadIds.contains(padId)

    /**
     * Downloads the copy in the background if the pad has offline access.
     * Network errors are only logged, the previous copy is kept.
     *
     * @param callback called on the main thread when done, not called if
     * an update of the same pad is already running
     */
    fun update(context: Context, pad: Pad, callback: ((updated: Boolean) -> Unit)? = null) {
        if (!pad.mOfflineAccess || !isAvailable(pad)) return
        if (!updatingPadIds.add(pad.mId)) return

        val applicationContext = context.applicationContext

        scope.launch {
            var updated = false
            try {
                val exportUrl = makeExportUrl(pad.mUrl)
                val html = download(
                    exportUrl,
                    WebSettings.getDefaultUserAgent(applicationContext),
                    CookieManager.getInstance().getCookie(exportUrl)
                )

                if (!OfflinePadStore(applicationContext).save(pad.mId, html)) {
                    throw IOException("The offline copy could not be saved")
                }
                updated = true
                Log.d(TAG, "Offline copy updated for pad ${pad.mId}")
            } catch (e: IOException) {
                Log.w(TAG, "The offline copy could not be updated for pad ${pad.mId}", e)
            } catch (e: Exception) {
                ErrorReporter.reportNonFatal(e)
            } finally {
                updatingPadIds.remove(pad.mId)
            }

            callback?.let {
                withContext(Dispatchers.Main) { it(updated) }
            }
        }
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

