package com.mikifus.padland.SaferWebView

import android.util.Log
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.mikifus.padland.Utils.WhiteListMatcher
import java.io.ByteArrayInputStream

/**
 * Extended class, I
 * Created by mikifus on 23/04/16.
 */
open class PadLandSaferWebViewClient(hostsWhitelist: Array<String?>?) : SaferWebViewClient(hostsWhitelist) {
    protected override val webResourceResponseFromString: WebResourceResponse?
        protected get() {
            Log.w("SaferWebViewClient", "Blocked a JS request to an external domains.")
            return getUtf8EncodedWebResourceResponse(ByteArrayInputStream("".toByteArray()))
        }

    /**
     * Returning false we allow http to https redirects
     * @param view
     * @param url
     * @return
     */
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//        return isValidHost(url);
        return false
    }

    override fun isValidHost(url: String?): Boolean {
        return WhiteListMatcher.isValidHost(url, hostsWhitelist)
    } // TODO: Recognise no-ssl urls and notify the user
}