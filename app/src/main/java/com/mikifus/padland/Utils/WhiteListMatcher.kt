package com.mikifus.padland.Utils

import android.net.Uri
import android.text.TextUtils
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by mikifus on 19/07/16.
 */
object WhiteListMatcher {
    /**
     * Performs a wildcard matching for the text and pattern
     * provided.
     *
     * @param compareText the text to be tested for matches.
     *
     * @param pattern the pattern to be matched for.
     * This can contain the wildcard character '*' (asterisk).
     *
     * @return <tt>true</tt> if a match is found, <tt>false</tt>
     * otherwise.
     *
     * @url http://www.adarshr.com/simple-implementation-of-wildcard-text-matching-using-java
     */
    private fun wildCardMatch(compareText: String, pattern: String?): Boolean {
        // Create the cards by splitting using a RegEx. If more speed
        // is desired, a simpler character based splitting can be done.
        var text = compareText
        val cards = pattern!!.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Iterate over the cards.
        for (card in cards) {
            val idx = text.indexOf(card)

            // Card not detected in the text.
            if (idx == -1) {
                return false
            }

            // Move ahead, towards the right of the text.
            text = text.substring(idx + card.length)
        }
        return true
    }

    /**
     * Checks if the url parameter has a valid host
     * among a list of hosts.
     *
     * @param url
     * @param hostsWhitelist
     * @return
     */
    fun isValidHost(url: String?, hostsWhitelist: List<String?>): Boolean {
        if (!TextUtils.isEmpty(url)) {
            val host = Uri.parse(url).host ?: return false
            for (whitelistedHost in hostsWhitelist) {
                if (wildCardMatch(host, whitelistedHost)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if the URL can be parsed.
     *
     * @param padUrl
     * @return
     */
    fun checkValidUrl(padUrl: String?): Boolean {
        // Check if it is a valid url
        try {
            URL(padUrl)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return false
        }
        return true
    }
}