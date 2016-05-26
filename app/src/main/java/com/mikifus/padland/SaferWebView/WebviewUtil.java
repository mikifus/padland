package com.mikifus.padland.SaferWebView;

import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebviewUtil {
public static void disableRiskySettings(WebView webView){

        //javascript could be a vector to exploit your applications
        webView.getSettings().setJavaScriptEnabled(false);

        //default is off, but just in case. plugins could be a vector to exploit your applications process
        webView.getSettings().setPluginState(WebSettings.PluginState.OFF);

        //Should an attacker somehow find themselves in a position to inject script into a WebView, then they could exploit the opportunity to access local resources. This can be somewhat prevented by disabling local file system access. It is enabled by default. The Android WebSettings class can be used to disable local file system access via the public method setAllowFileAccess.
        //This restricts the WebView to loading local resources from file:///android_asset (assets) and file:///android_res (resources).
        webView.getSettings().setAllowFileAccess(false);

        //disable Geolocation API 
        webView.getSettings().setGeolocationEnabled(false);

    }
    
}