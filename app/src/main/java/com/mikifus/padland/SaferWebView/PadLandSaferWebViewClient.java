package com.mikifus.padland.SaferWebView;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.StringBufferInputStream;

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
        return getUtf8EncodedWebResourceResponse(new StringBufferInputStream(""));
    }


    /**
     * Performs a wildcard matching for the text and pattern
     * provided.
     *
     * @param text the text to be tested for matches.
     *
     * @param pattern the pattern to be matched for.
     * This can contain the wildcard character '*' (asterisk).
     *
     * @return <tt>true</tt> if a match is found, <tt>false</tt>
     * otherwise.
     *
     * @url http://www.adarshr.com/simple-implementation-of-wildcard-text-matching-using-java
     */
    public static boolean wildCardMatch(String text, String pattern) {
        // Create the cards by splitting using a RegEx. If more speed
        // is desired, a simpler character based splitting can be done.
        String [] cards = pattern.split("\\*");

        // Iterate over the cards.
        for (String card : cards) {
            int idx = text.indexOf(card);

            // Card not detected in the text.
            if(idx == -1) {
                return false;
            }

            // Move ahead, towards the right of the text.
            text = text.substring(idx + card.length());
        }

        return true;
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
        if (!TextUtils.isEmpty(url)) {
            final String host = Uri.parse(url).getHost();
            for (String whitelistedHost: hostsWhitelist){
                if(wildCardMatch(host, whitelistedHost)) {
                    return true;
                }
            }
        }
        return false;
    }
}
