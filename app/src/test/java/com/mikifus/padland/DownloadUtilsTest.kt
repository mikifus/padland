package com.mikifus.padland

import com.mikifus.padland.Utils.Download.DownloadUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadUtilsTest {

    @Test
    fun getCharset() {
        assertEquals(Charsets.ISO_8859_1, DownloadUtils.getCharset("text/html; charset=ISO-8859-1"))
        assertEquals(Charsets.UTF_16, DownloadUtils.getCharset("text/html;Charset=\"utf-16\""))
    }

    @Test
    fun getCharset_missingOrUnknown_isUtf8() {
        assertEquals(Charsets.UTF_8, DownloadUtils.getCharset(null))
        assertEquals(Charsets.UTF_8, DownloadUtils.getCharset("text/html"))
        assertEquals(Charsets.UTF_8, DownloadUtils.getCharset("text/html; charset=unknown"))
    }
}

