package com.mikifus.padland.Utils.PadLandWebViewClient;

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.util.Log
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.URLUtil
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.mikifus.padland.PadViewActivity
import com.mikifus.padland.R
import com.mikifus.padland.SaferWebView.PadLandSaferWebViewClient
import com.mikifus.padland.Utils.WhiteListMatcher
import kotlinx.coroutines.runBlocking

public class PadLandWebViewClient(hostsWhitelist: List<String>, private val callbacks: PadLandWebClientCallbacks) :
    PadLandSaferWebViewClient(hostsWhitelist),
    PadLandWebClientCallbacks by callbacks {

    private var webViewHttpConnections = 0
    private var isLoading: Boolean = false
        set(value) {
            // Callback on start or stop loading
            when(value) {
                true -> onStartLoading()
                else -> onStopLoading()
            }
            field = value
        }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        ++webViewHttpConnections
        isLoading = true
        Log.d(PadViewActivity.TAG, "Added connection $webViewHttpConnections")
    }


    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        --webViewHttpConnections
        Log.d(PadViewActivity.TAG, "Removed connection $webViewHttpConnections")
        if(webViewHttpConnections == 0) {
            isLoading = false
        }
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        --webViewHttpConnections
        Log.d(PadViewActivity.TAG, "Removed connection $webViewHttpConnections")
        if(webViewHttpConnections == 0) {
            isLoading = false
        }
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        super.onReceivedError(view, request, error)
        --webViewHttpConnections
        isLoading = false
        Log.e(PadViewActivity.TAG, "WebView Error $error, Request: $request")
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        @Suppress("DEPRECATION")
        super.onReceivedError(view, errorCode, description, failingUrl)
        --webViewHttpConnections
        isLoading = false
        Log.e(PadViewActivity.TAG, "WebView Error ($errorCode) $description, Request: $failingUrl")
    }

    override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
        onReceivedHttpAuthRequestCallback(view, handler, host, realm)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        @Suppress("DEPRECATION")
        return shouldOverrideUrlLoading(view, request.url.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        if ((URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) &&
            !WhiteListMatcher.isValidHost(url, hostsWhitelist)) {

            // WARNING: Runs blocking, it seems to work as expected
            return runBlocking {
                onExternalHostUrlLoad(url)
            }
        }
        return false
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        val message = when (error.primaryError) {
            SslError.SSL_EXPIRED -> view.context.getString(R.string.error_ssl_expired)
            SslError.SSL_IDMISMATCH -> view.context.getString(R.string.error_ssl_id_mismatch)
            SslError.SSL_NOTYETVALID -> view.context.getString(R.string.error_ssl_not_yet_valid)
            SslError.SSL_UNTRUSTED -> view.context.getString(R.string.error_ssl_untrusted)
            SslError.SSL_DATE_INVALID -> view.context.getString(R.string.error_ssl_date_invalid)
            else -> { error.primaryError.toString() }
        }

        Log.e(PadViewActivity.TAG, "SSL Error received: " + error.primaryError + " - " + message)

        callbacks.onReceivedSslError(handler, error.url, message)
    }
    override suspend fun onUnsafeUrlProtocol(url: String): Boolean {
        // WARNING: Runs blocking, it seems to work as expected
        return runBlocking {
            callbacks.onUnsafeUrlProtocol(url)
        }
    }
}
