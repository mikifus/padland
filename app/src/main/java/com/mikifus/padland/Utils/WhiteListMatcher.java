package com.mikifus.padland.Utils;

import android.net.Uri;
import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by mikifus on 19/07/16.
 */
public class WhiteListMatcher {
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
     * Checks if the url parameter has a valid host
     * among a list of hosts.
     *
     * @param url
     * @param hostsWhitelist
     * @return
     */
    public static boolean isValidHost(String url, String[] hostsWhitelist){
        if (!TextUtils.isEmpty(url)) {
            final String host = Uri.parse(url).getHost();
            if( host == null ) {
                return false;
            }
            for (String whitelistedHost: hostsWhitelist){
                if(wildCardMatch(host, whitelistedHost)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the URL can be parsed.
     *
     * @param padUrl
     * @return
     */
    public static boolean checkValidUrl(String padUrl) {
        // Check if it is a valid url
        try {
            new URL(padUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
