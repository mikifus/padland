package com.mikifus.padland.SaferWebView

import android.os.Build
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.mikifus.padland.Utils.WhiteListMatcher
import java.io.ByteArrayInputStream

/**
 * Implements whitelisting on host name
 *
 */
open class PadLandSaferWebViewClient(protected var hostsWhitelist: List<String>) : WebViewClient() {

    private val webResourceResponseFromString: WebResourceResponse
        get() {
            Log.w("SaferWebViewClient", "Blocked a JS request to an external domain.")
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

        return WhiteListMatcher.isValidHost(url, hostsWhitelist)
    }

    private fun onUnsafeUrlProtocol(url: String?) {}
}