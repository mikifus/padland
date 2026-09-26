package com.mikifus.padland.Utils.Import

import java.net.URI
import java.net.URL

/**
 * Pure (Android-free) logic to import a list of pad URLs.
 *
 * Input format: one fully qualified http(s) URL per line. Blank lines are skipped.
 *
 * Counting rules:
 * - imported: new URLs from whitelisted servers.
 * - ignored: duplicates, either repeated in the list or already saved.
 * - failed: lines that are not a fully qualified URL, or URLs from unknown servers.
 */
object PadUrlImport {

    private val ALLOWED_SCHEMES = setOf("http", "https")

    data class ParseResult(
        /** Valid URLs, trimmed, in input order, without in-list duplicates */
        val validUrls: List<String>,
        /** Valid URLs repeated in the list (after the first occurrence) */
        val duplicateCount: Int,
        /** Non blank lines that are not fully qualified http(s) URLs */
        val invalidCount: Int,
    )

    data class Classification(
        val toImport: List<String>,
        /** URLs that are already saved in the list */
        val alreadySavedCount: Int,
        /** URLs whose host is not whitelisted */
        val unknownHostUrls: List<String>,
    )

    fun parse(text: String): ParseResult {
        val seenKeys = HashSet<String>()
        val validUrls = mutableListOf<String>()
        var duplicateCount = 0
        var invalidCount = 0

        text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { line ->
                val key = normalize(line)
                when {
                    key == null -> invalidCount++
                    !seenKeys.add(key) -> duplicateCount++
                    else -> validUrls.add(line)
                }
            }

        return ParseResult(validUrls, duplicateCount, invalidCount)
    }

    /**
     * Splits valid URLs into the ones to import, the ones already saved
     * and the ones belonging to a server that is not whitelisted.
     *
     * @param existingUrls URLs currently saved (raw, they get normalized here)
     * @param hostsWhitelist allowed hosts, matched exactly (case insensitive)
     */
    fun classify(
        validUrls: List<String>,
        existingUrls: Collection<String>,
        hostsWhitelist: Collection<String>,
    ): Classification {
        val existingKeys = existingUrls.mapNotNull { normalize(it) }.toHashSet()
        val allowedHosts = hostsWhitelist.map { it.trim().lowercase() }.toHashSet()

        val toImport = mutableListOf<String>()
        val unknownHostUrls = mutableListOf<String>()
        var alreadySavedCount = 0

        validUrls.forEach { url ->
            when {
                normalize(url) in existingKeys -> alreadySavedCount++
                hostOf(url) !in allowedHosts -> unknownHostUrls.add(url)
                else -> toImport.add(url)
            }
        }

        return Classification(toImport, alreadySavedCount, unknownHostUrls)
    }

    /**
     * Lowercase host of a fully qualified URL, or null if it is not one.
     */
    fun hostOf(url: String): String? = parseUri(url)?.host?.lowercase()

    /**
     * Comparison key for duplicate detection, or null if [url] is not a fully
     * qualified http(s) URL. Scheme and host are case insensitive and a bare
     * trailing slash on the host is ignored. Path, query and fragment are kept
     * as they are (CryptPad stores keys in the fragment).
     */
    fun normalize(url: String): String? {
        val uri = parseUri(url.trim()) ?: return null
        val authority = uri.rawAuthority.lowercase()
        val path = uri.rawPath.orEmpty().let { if (it == "/") "" else it }
        val query = uri.rawQuery?.let { "?$it" }.orEmpty()
        val fragment = uri.rawFragment?.let { "#$it" }.orEmpty()
        return "${uri.scheme.lowercase()}://$authority$path$query$fragment"
    }

    private fun parseUri(url: String): URI? {
        return try {
            val uri = URI(url)
            if (uri.scheme?.lowercase() !in ALLOWED_SCHEMES) return null
            if (uri.host.isNullOrBlank()) return null
            // The rest of the app parses pads with java.net.URL, it must accept it too
            URL(url)
            uri
        } catch (e: Exception) {
            null
        }
    }
}

