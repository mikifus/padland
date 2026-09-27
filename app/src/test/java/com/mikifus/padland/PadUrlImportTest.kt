package com.mikifus.padland

import com.mikifus.padland.Utils.Import.PadUrlImport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PadUrlImportTest {

    @Test
    fun parse_onePerLine_skipsBlankLines_andTrims() {
        val result = PadUrlImport.parse(
            "  https://pad.riseup.net/p/one  \r\n\n\nhttps://pad.riseup.net/p/two\n   \n"
        )

        assertEquals(
            listOf("https://pad.riseup.net/p/one", "https://pad.riseup.net/p/two"),
            result.validUrls
        )
        assertEquals(0, result.duplicateCount)
        assertEquals(0, result.invalidCount)
    }

    @Test
    fun parse_requiresFullyQualifiedHttpUrls() {
        val result = PadUrlImport.parse(
            listOf(
                "just some text",
                "pad.riseup.net/p/no-scheme",
                "ftp://pad.riseup.net/p/wrong-scheme",
                "https://",
                "https://pad.riseup.net/p/two urls https://pad.riseup.net/p/x",
                "https://pad.riseup.net/p/ok",
            ).joinToString("\n")
        )

        assertEquals(listOf("https://pad.riseup.net/p/ok"), result.validUrls)
        assertEquals(5, result.invalidCount)
    }

    @Test
    fun parse_noValidUrls_returnsEmptyList() {
        val result = PadUrlImport.parse("hello\nworld")

        assertEquals(emptyList<String>(), result.validUrls)
        assertEquals(2, result.invalidCount)
    }

    @Test
    fun parse_countsDuplicates_caseInsensitiveHost_keepsFirst() {
        val result = PadUrlImport.parse(
            listOf(
                "https://pad.riseup.net/p/doc",
                "HTTPS://PAD.RISEUP.NET/p/doc",
                "https://pad.riseup.net/p/doc",
                "https://pad.riseup.net/p/Doc", // path is case sensitive: different pad
            ).joinToString("\n")
        )

        assertEquals(
            listOf("https://pad.riseup.net/p/doc", "https://pad.riseup.net/p/Doc"),
            result.validUrls
        )
        assertEquals(2, result.duplicateCount)
    }

    @Test
    fun normalize_keepsFragment_forCryptPadKeys() {
        assertNotEquals(
            PadUrlImport.normalize("https://cryptpad.fr/pad/#/2/pad/edit/aaa/"),
            PadUrlImport.normalize("https://cryptpad.fr/pad/#/2/pad/edit/bbb/")
        )
    }

    @Test
    fun normalize_ignoresBareTrailingSlash() {
        assertEquals(
            PadUrlImport.normalize("https://pad.example.org"),
            PadUrlImport.normalize("https://pad.example.org/")
        )
        assertNull(PadUrlImport.normalize("not a url"))
    }

    @Test
    fun classify_splitsImportAlreadySavedAndUnknownHosts() {
        val classification = PadUrlImport.classify(
            validUrls = listOf(
                "https://pad.riseup.net/p/new",
                "https://pad.riseup.net/p/saved",
                "https://unknown.example.org/p/x",
                "https://Pad.Disroot.org/p/upper",
            ),
            existingUrls = listOf("HTTPS://pad.riseup.net/p/saved"),
            hostsWhitelist = listOf("pad.riseup.net", "pad.disroot.org"),
        )

        assertEquals(
            listOf("https://pad.riseup.net/p/new", "https://Pad.Disroot.org/p/upper"),
            classification.toImport
        )
        assertEquals(1, classification.alreadySavedCount)
        assertEquals(listOf("https://unknown.example.org/p/x"), classification.unknownHostUrls)
    }

    @Test
    fun classify_hostMatchIsExact_noLookalikeDomains() {
        val classification = PadUrlImport.classify(
            validUrls = listOf(
                "https://pad.riseup.net.evil.com/p/x",
                "https://evilpad.riseup.net/p/y",
            ),
            existingUrls = emptyList(),
            hostsWhitelist = listOf("pad.riseup.net"),
        )

        assertEquals(emptyList<String>(), classification.toImport)
        assertEquals(2, classification.unknownHostUrls.size)
    }

    @Test
    fun hostOf_isLowercase() {
        assertEquals("pad.riseup.net", PadUrlImport.hostOf("https://PAD.riseup.NET/p/x"))
        assertNull(PadUrlImport.hostOf("mailto:someone@example.org"))
    }
}

