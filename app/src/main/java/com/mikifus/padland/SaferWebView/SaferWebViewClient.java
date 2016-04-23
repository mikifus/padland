package com.mikifus.padland.SaferWebView;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.InputStream;
import java.io.StringBufferInputStream;

/**
   * Implements whitelisting on host name
   */
public class SaferWebViewClient extends WebViewClient {

        private String[] hostsWhitelist;

        public SaferWebViewClient(String[] hostsWhitelist){
            super();
            this.hostsWhitelist = hostsWhitelist;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
            if (isValidHost(url)) {
                return super.shouldInterceptRequest(view, url);
            } else {
                return getWebResourceResponseFromString();
            }
        }

        private WebResourceResponse getWebResourceResponseFromString() {
            return getUtf8EncodedWebResourceResponse(new StringBufferInputStream("alert('!NO!')"));
        }

        private WebResourceResponse getUtf8EncodedWebResourceResponse(InputStream data) {
            return new WebResourceResponse("text/css", "UTF-8", data);
        }


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return isValidHost(url);
        }

        private boolean isValidHost(String url){
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