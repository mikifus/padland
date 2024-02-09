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
    fun onExternalHostUrlLoad(url: String): Boolean
    fun onReceivedHttpAuthRequest(handler: HttpAuthHandler, host: String, realm: String)
}