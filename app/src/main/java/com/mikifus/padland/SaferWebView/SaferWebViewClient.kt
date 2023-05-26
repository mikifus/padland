package com.mikifus.padland.SaferWebView

import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import java.io.ByteArrayInputStream

/**
 * Implements whitelisting on host name
 */
open class SaferWebViewClient(protected var hostsWhitelist: Array<String?>) : WebViewClient() {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val isValid = isValidHost(request.url.toString())
        return if (isValid) {
            super.shouldInterceptRequest(view, request)
        } else {
            webResourceResponseFromString
        }
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val isValid = isValidHost(url)
        return if (isValid) {
            super.shouldInterceptRequest(view, url)
        } else {
            webResourceResponseFromString
        }
    }

    protected open val webResourceResponseFromString: WebResourceResponse?
        get() {
            Log.w("SaferWebViewClient", "Blocked a JS request to an external domains.")
            return getUtf8EncodedWebResourceResponse(ByteArrayInputStream("alert('!NO!')".toByteArray()))
        }

    protected fun getUtf8EncodedWebResourceResponse(data: ByteArrayInputStream?): WebResourceResponse {
        return WebResourceResponse("text/css", "UTF-8", data)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        return isValidHost(url)
    }

    protected open fun isValidHost(url: String?): Boolean {
        if (!TextUtils.isEmpty(url)) {
            val host = Uri.parse(url).host
            for (whitelistedHost in hostsWhitelist!!) {
                if (whitelistedHost.equals(host, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}