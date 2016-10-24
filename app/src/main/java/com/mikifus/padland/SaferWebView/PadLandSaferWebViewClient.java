package com.mikifus.padland.SaferWebView;

import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.mikifus.padland.Utils.WhiteListMatcher;

import java.io.ByteArrayInputStream;

/**
 * Extended class, I
 * Created by mikifus on 23/04/16.
 */
public class PadLandSaferWebViewClient extends SaferWebViewClient {
    public PadLandSaferWebViewClient(String[] hostsWhitelist) {
        super(hostsWhitelist);
    }


    @Override
    protected WebResourceResponse getWebResourceResponseFromString() {
        Log.w("SaferWebViewClient", "Blocked a JS request to an external domains.");
        return getUtf8EncodedWebResourceResponse(new ByteArrayInputStream("".getBytes()));
    }

    /**
     * Returning false we allow http to https redirects
     * @param view
     * @param url
     * @return
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        return isValidHost(url);
        return false;
    }

    protected boolean isValidHost(String url){
        return WhiteListMatcher.isValidHost(url, hostsWhitelist);
    }

    // TODO: Recognise no-ssl urls and notify the user
}
