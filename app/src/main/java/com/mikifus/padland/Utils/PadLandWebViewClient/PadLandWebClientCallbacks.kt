package com.mikifus.padland.Utils.PadLandWebViewClient

import android.net.http.SslError
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebView

interface PadLandWebClientCallbacks {
    fun onStartLoading()
    fun onStopLoading()
    fun onUnsafeUrlProtocol(url: String?)
    fun onReceivedSslError(message: String)

    /**
     * Implemented with runBlocking so it can suspend and
     * ask the user before returning.
     */
    suspend fun onExternalHostUrlLoad(url: String): Boolean
    fun onReceivedHttpAuthRequestCallback(view: WebView, handler: HttpAuthHandler, host: String, realm: String)
}