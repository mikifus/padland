package com.mikifus.padland.Utils.PadLandWebViewClient

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.mikifus.padland.Utils.WhiteListMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Implements whitelisting on host name
 *
 */
open class PadLandSaferWebViewClient(var hostsWhitelist: List<String>) : WebViewClient() {
    private var corsDomains: List<String>? = null
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

    private fun isValidHost(url: String?): Boolean {
        if(URLUtil.isHttpUrl(url)) {
            onUnsafeUrlProtocol(url)
        }
        val hostsList: List<String> = corsDomains?.let { (hostsWhitelist + corsDomains as List<String>)  } ?: hostsWhitelist
        return WhiteListMatcher.isValidHost(url, hostsList)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if(corsDomains == null && view != null) {
            corsDomains = listOf()
            view.findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.IO) {
                val headers = url?.let { getUrlHeaders(it) }
                if (headers != null && headers.containsKey("content-security-policy") && headers["content-security-policy"]!!.isNotEmpty()) {
                    corsDomains = extractUniqueUrls(headers["content-security-policy"]!![0] as String)
                }
            }
        }
        super.onPageStarted(view, url, favicon)
    }

    private fun getUrlHeaders(url: String): Map<String, List<String>> {
        var headers = mutableMapOf<String, List<String>>()
        val connectionUrl = URL(url)
        val connection = connectionUrl.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connect()

            // If the response is successful, return headers
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                headers = connection.headerFields
            }
        } finally {
            // Close the connection
            connection.disconnect()
        }
        return headers
    }

    private fun extractUniqueUrls(input: String): List<String> {
        // Regular expression pattern to find URLs
        val urlRegex = Regex("""https?:\/\/([^\s;]+)""")

        // Find all matches in the input string
        val urls = urlRegex.findAll(input)
            .map { it.groups[1]!!.value } // Get the matched value (the hostname)
            .toSet() // Convert to a Set to remove duplicates
            .toList() // Convert back to a List

        return urls
    }

    private fun onUnsafeUrlProtocol(url: String?) {}
}