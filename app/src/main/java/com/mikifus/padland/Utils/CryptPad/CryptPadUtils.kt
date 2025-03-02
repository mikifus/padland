package com.mikifus.padland.Utils.CryptPad

class CryptPadUtils {
    companion object {
        private val CRYPTPAD_PAD_URL = Regex("https?://[^/]+/[^/]+/#/\\d+/[^/]+/[^/]+/[^/]+/")
        private val CRYPTPAD_REPLACE_TYPE_NAME_REGEX = Regex("(\\d)/[a-z]+(/?)")
        private const val CRYPTPAD_REPLACE_TYPE_NAME_REGEX_SUBSTITUTION = "$1/__type__$2"
        private val CRYPTPAD_REPLACE_TYPE_PATH_REGEX = Regex("(/?)[a-z]+/#/(\\d)/[a-z]+(/?)")
        private const val CRYPTPAD_REPLACE_TYPE_PATH_REGEX_SUBSTITUTION = "$1__type__/#/$2/__type__$3"
        private val CRYPTPAD_PAD_URL_NEW_REGEX = Regex("(https?://[^/]+/)([a-z]+)/#.*")
        private const val CRYPTPAD_PAD_URL_NEW_REGEX_SUBSTITUTION = "$1$2"

        fun replaceCryptPadType(nameOrUrl: String, type: String): String {
            var typeString = type
            val typeFormatIndex = type.indexOf("/#")
            if (typeFormatIndex > -1) {
                typeString = typeString.substring(1, typeFormatIndex)
            }
            val isName = CRYPTPAD_REPLACE_TYPE_NAME_REGEX.containsMatchIn(nameOrUrl)
            val regex = if (isName) CRYPTPAD_REPLACE_TYPE_NAME_REGEX else CRYPTPAD_REPLACE_TYPE_PATH_REGEX
            val substitution = if (isName) CRYPTPAD_REPLACE_TYPE_NAME_REGEX_SUBSTITUTION else CRYPTPAD_REPLACE_TYPE_PATH_REGEX_SUBSTITUTION
            return nameOrUrl.replace(regex, substitution.replace("__type__", typeString))
        }

        fun makePadPrefixFromServerAndType(server: String, type: String): String {
            var serverString = server
            var typeString = type
            if (serverString.endsWith("/")) {
                serverString = serverString.substringBeforeLast("/")
            }
            if (!typeString.endsWith("/#/")) {
                typeString = "/$type/#/"

            }
            return serverString + typeString
        }

        fun seemsCrpytPadUrl(url: String): Boolean {
            return CRYPTPAD_PAD_URL.matches(url)
        }

        fun applyNewPadUrl(url: String): String {
            return url.replace(
                CRYPTPAD_PAD_URL_NEW_REGEX,
                CRYPTPAD_PAD_URL_NEW_REGEX_SUBSTITUTION
            )
        }
    }
}