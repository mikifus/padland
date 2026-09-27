package com.mikifus.padland

import com.mikifus.padland.Utils.ErrorReporting.ErrorReportSanitizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ErrorReportSanitizerTest {

    private fun sanitize(text: String) = ErrorReportSanitizer.removeDocumentUrls(text)

    @Test
    fun url_keepsOnlySchemeAndHost() {
        assertEquals(
            "Failed to load https://pad.riseup.net/[removed] now",
            sanitize("Failed to load https://pad.riseup.net/p/my-secret-pad now"))
    }

    @Test
    fun url_keepsPort_andDropsCredentials() {
        assertEquals(
            "http://pad.example.org:9001/[removed]",
            sanitize("http://user:password@pad.example.org:9001/p/secret"))
    }

    @Test
    fun url_removesQueryAndFragment_evenWithoutPath() {
        assertEquals("https://pad.example.org/[removed]", sanitize("https://pad.example.org?token=abc"))
        assertEquals("https://pad.example.org/[removed]", sanitize("https://pad.example.org#secret"))
    }

    @Test
    fun cryptPadUrl_removesTheKeyInTheFragment() {
        val sanitized = sanitize("https://cryptpad.fr/pad/#/2/pad/edit/Xy7kLmNoPqRsTuVw/")
        assertEquals("https://cryptpad.fr/[removed]", sanitized)
        assertFalse(sanitized.contains("Xy7kLmNoPqRsTuVw"))
    }

    @Test
    fun url_withOnlyHost_isKept() {
        assertEquals("https://pad.riseup.net", sanitize("https://pad.riseup.net"))
        assertEquals("https://pad.riseup.net/", sanitize("https://pad.riseup.net/"))
    }

    @Test
    fun severalUrls_andQuotes() {
        assertEquals(
            "Can't open \"https://a.example.org/[removed]\" or 'http://b.example.org/[removed]'",
            sanitize("Can't open \"https://a.example.org/p/one\" or 'http://b.example.org/p/two?x=1'"))
    }

    @Test
    fun hostWithPath_withoutScheme() {
        assertEquals(
            "Invalid pad: pad.riseup.net/[removed]",
            sanitize("Invalid pad: pad.riseup.net/p/my-secret-pad"))
    }

    @Test
    fun otherUris_keepOnlyTheirAuthority() {
        assertEquals(
            "content://com.mikifus.padland.padlandcontentprovider/[removed]",
            sanitize("content://com.mikifus.padland.padlandcontentprovider/padlist/1"))
    }

    @Test
    fun stackTraceLines_areNotChanged() {
        val trace = """
            java.lang.IllegalStateException: boom
            	at com.mikifus.padland.Activities.PadViewActivity.loadUrl(PadViewActivity.kt:405)
            	at java.net.URL.<init>(URL.java:611)
            Caused by: java.net.UnknownHostException: Unable to resolve host "pad.riseup.net": No address
            	at /data/app/com.mikifus.padland/base.apk
        """.trimIndent()

        assertEquals(trace, sanitize(trace))
    }

    @Test
    fun isIdempotent() {
        val once = sanitize("See https://user@pad.example.org/p/x and pad.example.org/p/y")
        assertEquals(once, sanitize(once))
    }

    @Test
    fun emptyText() {
        assertEquals("", sanitize(""))
    }
}

