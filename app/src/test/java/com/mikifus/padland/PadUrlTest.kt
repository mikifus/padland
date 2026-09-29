package com.mikifus.padland

import com.mikifus.padland.Utils.PadUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class PadUrlTest {

    @Test
    fun etherpadExportUrl() {
        assertEquals("https://pad.riseup.net/p/test/export/html",
            PadUrl.etherpadExportUrl("https://pad.riseup.net/p/test", "html"))
        assertEquals("https://pad.riseup.net/p/test/export/html",
            PadUrl.etherpadExportUrl("https://pad.riseup.net/p/test/", "html"))
    }

    @Test
    fun etherpadExportUrl_dropsQueryAndKeepsPrefixAndPort() {
        assertEquals("http://example.org:9001/etherpad/p/my%20pad/export/html",
            PadUrl.etherpadExportUrl(
                "http://example.org:9001/etherpad/p/my%20pad?userName=me&userColor=%23ff0000", "html"))
    }
}

