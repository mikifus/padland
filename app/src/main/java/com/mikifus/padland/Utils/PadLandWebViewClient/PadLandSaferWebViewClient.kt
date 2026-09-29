package com.mikifus.padland.Utils.PadLandWebViewClient

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Utils.WhiteListMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Implements whitelisting on host name
 *
 */
open class PadLandSaferWebViewClient(var hostsWhitelist: List<String>) : WebViewClient() {
    /** Hosts allowed by the Content-Security-Policy of the loaded pages, i.e. the CryptPad sandbox */
    @Volatile
    private var corsPolicyHosts: List<String> = listOf()

    /** Hosts whose policy was read or is being read */
    private val corsPolicyCheckedHosts = mutableSetOf<String>()

    private val webResourceResponseFromString: WebResourceResponse
        get() {
            return getUtf8EncodedWebResourceResponse(ByteArrayInputStream("".toByteArray()))
        }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val isValid = isValidHost(request.url.toString())
        return if (isValid) {
            super.shouldInterceptRequest(view, request)
        } else {
            webResourceResponseFromString
        }
    }

    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val isValid = isValidHost(url)
        return if (isValid) {
            @Suppress("DEPRECATION")
            super.shouldInterceptRequest(view, url)
        } else {
            webResourceResponseFromString
        }
    }

    private fun getUtf8EncodedWebResourceResponse(data: ByteArrayInputStream?): WebResourceResponse {
        return WebResourceResponse("text/css", "UTF-8", data)
    }

    /**
     * Returning false we allow http to https redirects
     * @param view
     * @param request
     * @return
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return false
    }

    @Deprecated("Deprecated in Java", ReplaceWith("shouldOverrideUrlLoading(view, request)"))
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return false
    }

    /**
     * blob: URLs are never intercepted, file: and content: stay blocked.
     */
    private fun isValidHost(url: String?): Boolean {
        // The content is in the URL, nothing is requested
        if (URLUtil.isDataUrl(url)) {
            return true
        }
        if(URLUtil.isHttpUrl(url)) {
            onUnsafeUrlProtocol(url)
        }
        return WhiteListMatcher.isValidHost(url, hostsWhitelist + corsPolicyHosts)
    }

    /**
     * Loads [url] once its security policy is read, so the resources it
     * allows are not blocked while the page loads.
     * If it fails the page is loaded as if the server sent none.
     *
     * Call it from the main thread.
     */
    suspend fun loadUrl(view: WebView, url: String) {
        val userAgent = view.settings.userAgentString
        withContext(Dispatchers.IO) {
            loadCorsPolicy(url, userAgent)
        }
        view.loadUrl(url)
    }

    /**
     * Pages opened from inside the WebView (i.e. links) read it meanwhile.
     */
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (view != null && url != null) {
            val userAgent = view.settings.userAgentString
            view.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
                loadCorsPolicy(url, userAgent)
            }
        }
        super.onPageStarted(view, url, favicon)
    }

    /**
     * Once per host.
     */
    private fun loadCorsPolicy(url: String, userAgent: String?) {
        if (!URLUtil.isNetworkUrl(url)) {
            return
        }
        val host = Uri.parse(url).host ?: return
        synchronized(corsPolicyCheckedHosts) {
            if (!corsPolicyCheckedHosts.add(host)) {
                return
            }
        }

        try {
            val policy = getContentCorsPolicy(url, userAgent) ?: return
            corsPolicyHosts = (corsPolicyHosts + extractHosts(policy)).distinct()
        } catch (e: Exception) {
            Log.w(TAG, "The security policy could not be read", e)
            // Tried again on the next page of the host
            synchronized(corsPolicyCheckedHosts) {
                corsPolicyCheckedHosts.remove(host)
            }
        }
    }

    /**
     * Only the headers are read, as the WebView requests it (cookies and user agent).
     */
    private fun getContentCorsPolicy(url: String, userAgent: String?): String? {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = CORS_POLICY_TIMEOUT
            connection.readTimeout = CORS_POLICY_TIMEOUT
            CookieManager.getInstance().getCookie(url)?.let {
                connection.setRequestProperty("Cookie", it)
            }
            userAgent?.let {
                connection.setRequestProperty("User-Agent", it)
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            // Header names are case insensitive and it may be sent more than once
            return connection.headerFields
                .filterKeys { it.equals(CORS_POLICY_HEADER, true) }
                .values
                .flatten()
                .joinToString(";")
                .ifBlank { null }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Host of every URL in the policy, wildcards included, i.e. `*.example.org`.
     */
    private fun extractHosts(policy: String): List<String> {
        return CORS_POLICY_URL_REGEX.findAll(policy)
            .mapNotNull { Uri.parse(it.value).host }
            .distinct()
            .toList()
    }

    private fun onUnsafeUrlProtocol(url: String?) {}

    companion object {
        private const val TAG = "SAFER_WEB_VIEW_CLIENT"

        private const val CORS_POLICY_HEADER = "Content-Security-Policy"
        private const val CORS_POLICY_TIMEOUT = 10_000
        private val CORS_POLICY_URL_REGEX = Regex("""https?://[^\s;]+""")
    }
}