package com.mikifus.padland.Utils.Download

import android.content.ActivityNotFoundException
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Saves the files the WebView wants to download (document exports) with the
 * Storage Access Framework, so the user picks the location and no storage
 * permission is needed.
 *
 * - http(s) URLs are downloaded with the WebView cookies and user agent.
 * - data: URLs are decoded.
 * - blob: URLs only exist inside the page, so their content is read with
 *   javascript and sent back through a [JavascriptInterface]. The blob may belong
 *   to an iframe (CryptPad sandbox), so the request is posted to every frame.
 *
 * Must be created before the activity is started, see [ExportHelper][com.mikifus.padland.Utils.Export.ExportHelper].
 */
class DownloadHelper(
    private val activity: AppCompatActivity,
    private val callback: ((done: Boolean) -> Unit) = {}) : DownloadListener {

    private data class BlobDownload(val token: String, val fileName: String)

    private var webView: WebView? = null
    private var blobDownload: BlobDownload? = null
    private var pendingWrite: (suspend (output: OutputStream) -> Unit)? = null

    private val launcher =
        activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument(DEFAULT_MIME_TYPE)
        ) {
            val write = pendingWrite
            pendingWrite = null
            if (it == null || write == null) {
                return@registerForActivityResult
            }

            activity.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = activity.contentResolver.openOutputStream(it)
                            ?: throw IOException("The output file could not be opened")
                        output.use { stream -> write(stream) }
                    }
                    callback(true)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    deleteDocument(it)
                    ErrorReporter.reportNonFatal(e, activity)
                    callback(false)
                }
            }
        }

    fun attach(webView: WebView) {
        // The web view settings are made again when the server list changes
        if (this.webView === webView) {
            return
        }
        this.webView = webView
        webView.setDownloadListener(this)
        webView.addJavascriptInterface(JavascriptBridge(), JAVASCRIPT_INTERFACE_NAME)

        // Makes the blob reader available in every frame, iframes included
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(webView, BLOB_READER_SCRIPT, setOf("*"))
        }
    }

    override fun onDownloadStart(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        val fileName = guessFileName(url, contentDisposition, mimeType)

        when {
            URLUtil.isNetworkUrl(url) -> {
                val cookies = CookieManager.getInstance().getCookie(url)
                requestLocation(fileName) { output ->
                    downloadUrl(url, userAgent, cookies, output)
                }
            }
            URLUtil.isDataUrl(url) -> {
                requestLocation(fileName) { output ->
                    output.write(decodeDataUrl(url))
                }
            }
            url.startsWith(BLOB_URL_PREFIX) -> {
                readBlobUrl(url, fileName)
            }
            else -> {
                Log.e(TAG, "Unsupported download URL scheme")
                callback(false)
            }
        }
    }

    private fun requestLocation(fileName: String, write: suspend (output: OutputStream) -> Unit) {
        pendingWrite = write
        try {
            launcher.launch(fileName)
        } catch (e: ActivityNotFoundException) {
            // No document picker available on the device
            pendingWrite = null
            ErrorReporter.reportNonFatal(e, activity)
            callback(false)
        }
    }

    private fun readBlobUrl(url: String, fileName: String) {
        val token = UUID.randomUUID().toString()
        blobDownload = BlobDownload(token, fileName)

        // The reader is added again in case document start scripts are not supported
        webView?.evaluateJavascript(
            BLOB_READER_SCRIPT + makeBlobRequestScript(url, token),
            null
        )

        // No frame may answer, i.e. the blob belongs to an iframe without the reader
        webView?.postDelayed({
            onBlobError(token, "No frame could read the blob")
        }, BLOB_READ_TIMEOUT)
    }

    private fun onBlobRead(token: String, dataUrl: String) {
        val download = blobDownload
        if (download == null || download.token != token) {
            return
        }
        blobDownload = null

        requestLocation(download.fileName) { output ->
            output.write(decodeDataUrl(dataUrl))
        }
    }

    private fun onBlobError(token: String, message: String) {
        if (blobDownload?.token != token) {
            return
        }
        blobDownload = null

        ErrorReporter.reportNonFatal(IOException("The blob could not be read: $message"), activity)
        callback(false)
    }

    private fun downloadUrl(url: String, userAgent: String?, cookies: String?, output: OutputStream) {
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

            connection.inputStream.use { it.copyTo(output) }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Format: data:[<mediatype>][;base64],<data>
     */
    private fun decodeDataUrl(url: String): ByteArray {
        val commaIndex = url.indexOf(',')
        if (commaIndex < 0) {
            throw IOException("Invalid data URL")
        }

        val header = url.substring(0, commaIndex)
        val data = url.substring(commaIndex + 1)

        return if (header.endsWith(";base64")) {
            Base64.decode(data, Base64.DEFAULT)
        } else {
            Uri.decode(data).toByteArray()
        }
    }

    /**
     * blob: and data: URLs have no useful name, so the page title is used instead.
     */
    private fun guessFileName(url: String, contentDisposition: String?, mimeType: String?): String {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        if (URLUtil.isNetworkUrl(url) || contentDisposition?.contains("filename", true) == true) {
            return fileName
        }

        val title = webView?.title?.replace(INVALID_FILE_NAME_CHARS, "_")?.trim()
        if (title.isNullOrBlank()) {
            return fileName
        }

        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return if (extension.isNullOrBlank()) title else "$title.$extension"
    }

    private fun deleteDocument(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(activity.contentResolver, uri)
        } catch (e: Exception) {
            Log.w(TAG, "The incomplete file could not be deleted", e)
        }
    }

    private fun makeBlobRequestScript(url: String, token: String): String {
        return """
            (function(message) {
                function post(target) {
                    try { target.postMessage(message, '*'); } catch (e) {}
                    for (var i = 0; i < target.frames.length; i++) {
                        post(target.frames[i]);
                    }
                }
                post(window);
            })({ padLandBlobToken: ${JSONObject.quote(token)}, url: ${JSONObject.quote(url)} });
        """.trimIndent()
    }

    /**
     * Called from javascript, not from the main thread.
     */
    private inner class JavascriptBridge {
        @JavascriptInterface
        fun onBlobRead(token: String, dataUrl: String) {
            activity.runOnUiThread { this@DownloadHelper.onBlobRead(token, dataUrl) }
        }

        @JavascriptInterface
        fun onBlobError(token: String, message: String) {
            activity.runOnUiThread { this@DownloadHelper.onBlobError(token, message) }
        }
    }

    companion object {
        const val TAG: String = "DOWNLOAD_HELPER"

        private const val DEFAULT_MIME_TYPE = "application/octet-stream"
        private const val BLOB_URL_PREFIX = "blob:"
        private const val JAVASCRIPT_INTERFACE_NAME = "PadLandDownloadHelper"
        private const val CONNECTION_TIMEOUT = 120_000
        private const val BLOB_READ_TIMEOUT = 120_000L
        private val INVALID_FILE_NAME_CHARS = Regex("""[\\/:*?"<>|]""")

        /**
         * Listens for blob requests and reads them when the blob belongs to the
         * frame. Only reads blobs, the token is checked on the app side.
         */
        private val BLOB_READER_SCRIPT = """
            (function() {
                if (window.padLandBlobReader || typeof $JAVASCRIPT_INTERFACE_NAME === 'undefined') {
                    return;
                }
                window.padLandBlobReader = true;

                window.addEventListener('message', function(event) {
                    var data = event.data;
                    if (!data || typeof data.padLandBlobToken !== 'string' || typeof data.url !== 'string') {
                        return;
                    }
                    if (data.url.indexOf('$BLOB_URL_PREFIX' + window.location.origin + '/') !== 0) {
                        return;
                    }

                    var token = data.padLandBlobToken;
                    var request = new XMLHttpRequest();
                    request.open('GET', data.url, true);
                    request.responseType = 'blob';
                    request.onload = function() {
                        var reader = new FileReader();
                        reader.onloadend = function() {
                            if (reader.error) {
                                $JAVASCRIPT_INTERFACE_NAME.onBlobError(token, String(reader.error));
                                return;
                            }
                            $JAVASCRIPT_INTERFACE_NAME.onBlobRead(token, reader.result);
                        };
                        reader.readAsDataURL(request.response);
                    };
                    request.onerror = function() {
                        $JAVASCRIPT_INTERFACE_NAME.onBlobError(token, 'Request failed');
                    };
                    request.send();
                });
            })();
        """.trimIndent() + "\n"
    }
}







