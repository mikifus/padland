package com.mikifus.padland.Utils.Sorting

/**
 * Available sort criteria for the pad list (used both for groups and documents).
 *
 * [preferenceValue] is the stable value persisted in the shared preferences,
 * so enum constants can be renamed without breaking stored user choices.
 */
enum class PadListSortOrder(val preferenceValue: String) {
    ALPHABETICAL("alphabetical"),
    LAST_ACCESS("last_access");

    companion object {
        val DEFAULT = ALPHABETICAL

        fun fromPreferenceValue(value: String?): PadListSortOrder =
            entries.firstOrNull { it.preferenceValue == value } ?: DEFAULT
    }
}

