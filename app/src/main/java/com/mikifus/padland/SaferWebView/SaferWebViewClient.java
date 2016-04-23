package com.mikifus.padland.SaferWebView;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.io.StringBufferInputStream;

/**
   * Implements whitelisting on host name
   */
public class SaferWebViewClient extends WebViewClient {

    protected String[] hostsWhitelist;

    public SaferWebViewClient(String[] hostsWhitelist){
        super();
        this.hostsWhitelist = hostsWhitelist;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
        boolean isvalid = isValidHost(url);
        if (isvalid) {
            return super.shouldInterceptRequest(view, url);
        } else {
            return getWebResourceResponseFromString();
        }
    }

    protected WebResourceResponse getWebResourceResponseFromString() {
        Log.w("SaferWebViewClient", "Blocked a JS request to an external domains.");
        return getUtf8EncodedWebResourceResponse(new StringBufferInputStream("alert('!NO!')"));
    }

    protected WebResourceResponse getUtf8EncodedWebResourceResponse(InputStream data) {
        return new WebResourceResponse("text/css", "UTF-8", data);
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return isValidHost(url);
    }

    protected boolean isValidHost(String url){
        if (!TextUtils.isEmpty(url)) {
            final String host = Uri.parse(url).getHost();
            for (String whitelistedHost: hostsWhitelist){
                if (whitelistedHost.equalsIgnoreCase(host)){
                    return true;
                }
            }
        }
        return false;
    }
}