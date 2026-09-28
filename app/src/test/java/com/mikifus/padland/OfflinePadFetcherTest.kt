package com.mikifus.padland

import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class OfflinePadFetcherTest {

    @Test
    fun makeExportUrl() {
        assertEquals("https://pad.riseup.net/p/test/export/html",
            OfflinePadFetcher.makeExportUrl("https://pad.riseup.net/p/test"))
        assertEquals("https://pad.riseup.net/p/test/export/html",
            OfflinePadFetcher.makeExportUrl("https://pad.riseup.net/p/test/"))
    }

    @Test
    fun makeExportUrl_dropsQueryAndKeepsPrefixAndPort() {
        assertEquals("http://example.org:9001/etherpad/p/my%20pad/export/html",
            OfflinePadFetcher.makeExportUrl(
                "http://example.org:9001/etherpad/p/my%20pad?userName=me&userColor=%23ff0000"))
    }

    @Test
    fun isEtherpadUrl() {
        assertTrue(OfflinePadFetcher.isEtherpadUrl("https://pad.riseup.net/p/test"))
        assertFalse(OfflinePadFetcher.isEtherpadUrl("https://cryptpad.fr/pad/#/2/pad/edit/abcdefghijklmnop/"))
        assertFalse(OfflinePadFetcher.isEtherpadUrl("not an url"))
    }
}

