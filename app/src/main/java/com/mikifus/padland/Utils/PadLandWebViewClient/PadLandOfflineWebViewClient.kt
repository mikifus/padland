package com.mikifus.padland.Utils.PadLandWebViewClient

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.ByteArrayInputStream

/**
 * Shows an offline copy: nothing is loaded from outside the copy
 * (only inline data: resources) and the copy is not left.
 * Touched links are opened in the user's browser, or the app for the link.
 */
class PadLandOfflineWebViewClient : WebViewClient() {

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        return if (URLUtil.isDataUrl(request.url.toString())) {
            null
        } else {
            WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url

        // Anchors within the copy (its base URL is about:blank)
        if (url.scheme == ABOUT_SCHEME && url.fragment != null) {
            return false
        }

        // Only links touched by the user, i.e. not a redirect of the copy
        if (request.hasGesture() && url.scheme?.lowercase() in EXTERNAL_SCHEMES) {
            try {
                view.context.startActivity(Intent(Intent.ACTION_VIEW, url))
            } catch (e: ActivityNotFoundException) {
                Log.w(TAG, "No app to open $url", e)
            }
        }
        return true
    }

    companion object {
        const val TAG: String = "OFFLINE_WEB_VIEW_CLIENT"

        private const val ABOUT_SCHEME = "about"
        private val EXTERNAL_SCHEMES = listOf("http", "https", "mailto")
    }
}

