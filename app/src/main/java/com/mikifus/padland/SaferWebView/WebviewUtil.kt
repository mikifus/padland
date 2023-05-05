package com.mikifus.padland.SaferWebView

import android.webkit.WebSettings
import android.webkit.WebView

object WebviewUtil {
    fun disableRiskySettings(webView: WebView) {

        //javascript could be a vector to exploit your applications
        webView.settings.javaScriptEnabled = false

        //default is off, but just in case. plugins could be a vector to exploit your applications process
        webView.settings.pluginState = WebSettings.PluginState.OFF

        //Should an attacker somehow find themselves in a position to inject script into a WebView, then they could exploit the opportunity to access local resources. This can be somewhat prevented by disabling local file system access. It is enabled by default. The Android WebSettings class can be used to disable local file system access via the public method setAllowFileAccess.
        //This restricts the WebView to loading local resources from file:///android_asset (assets) and file:///android_res (resources).
        webView.settings.allowFileAccess = false

        //disable Geolocation API 
        webView.settings.setGeolocationEnabled(false)
    }
}