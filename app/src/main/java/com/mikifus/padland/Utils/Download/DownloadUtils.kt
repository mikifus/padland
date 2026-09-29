package com.mikifus.padland.Utils.Download

import java.nio.charset.Charset

object DownloadUtils {
    private const val CHARSET_PARAMETER = "charset="

    /**
     * Charset of a Content-Type header, i.e. `text/html; charset=utf-8`.
     * UTF-8 if missing or unknown.
     */
    fun getCharset(contentType: String?): Charset {
        val charsetName = contentType?.split(';')
            ?.map { it.trim() }
            ?.firstOrNull { it.startsWith(CHARSET_PARAMETER, true) }
            ?.substringAfter('=')
            ?.trim('"', ' ')

        return try {
            charsetName?.let { Charset.forName(it) } ?: Charsets.UTF_8
        } catch (e: Exception) {
            Charsets.UTF_8
        }
    }
}

