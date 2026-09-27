package com.mikifus.padland.Utils.ErrorReporting

/**
 * Pure (Android-free) logic to remove document addresses from error reports,
 * so they can be shared safely.
 *
 * Only the server (scheme and host, plus the port if any) is kept:
 * - `https://user:pass@pad.example.org:9001/p/secret?x=1` -> `https://pad.example.org:9001/[removed]`
 * - `https://cryptpad.example.org/pad/#/2/pad/edit/key/` -> `https://cryptpad.example.org/[removed]`
 * - `pad.example.org/p/secret` -> `pad.example.org/[removed]`
 *
 * Credentials are always dropped. Applying it more than once gives the same result.
 */
object ErrorReportSanitizer {

    const val REMOVED_MARKER = "[removed]"

    /**
     * scheme://authority followed by anything that is not a blank or a quote.
     * Group 1: scheme, group 2: authority (may contain credentials), group 3: path, query, fragment.
     */
    private val URL_PATTERN = Regex(
        """\b([a-zA-Z][a-zA-Z0-9+.\-]*)://([^\s/?#"'<>(){}\\]*)([^\s"'<>]*)"""
    )

    /**
     * Addresses without scheme: host.tld/path. The look-behind avoids matching
     * again the host of an address already handled by [URL_PATTERN].
     * Group 1: host, group 2: path.
     */
    private val HOST_WITH_PATH_PATTERN = Regex(
        """(?<![\w.@/:\-])((?:[a-zA-Z0-9\-]+\.)+[a-zA-Z]{2,}(?::\d{1,5})?)(/[^\s"'<>]*)"""
    )

    fun removeDocumentUrls(text: String): String {
        val withoutUrls = URL_PATTERN.replace(text) { match ->
            val scheme = match.groupValues[1]
            val host = match.groupValues[2].substringAfterLast('@')
            scheme + "://" + host + removedPath(match.groupValues[3])
        }

        return HOST_WITH_PATH_PATTERN.replace(withoutUrls) { match ->
            match.groupValues[1] + removedPath(match.groupValues[2])
        }
    }

    /**
     * Nothing meaningful after the host (empty or a single slash) is kept as is.
     */
    private fun removedPath(path: String): String {
        return if (path.isEmpty() || path == "/") path else "/$REMOVED_MARKER"
    }
}


