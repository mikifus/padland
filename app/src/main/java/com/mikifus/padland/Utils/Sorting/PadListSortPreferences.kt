package com.mikifus.padland.Utils.Sorting

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Persists the user's pad list sort choices in the app default shared preferences.
 */
class PadListSortPreferences(context: Context) {

    private val preferences: SharedPreferences = context.applicationContext
        .getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)

    var groupSortOrder: PadListSortOrder
        get() = PadListSortOrder.fromPreferenceValue(
            preferences.getString(KEY_GROUP_SORT_ORDER, null))
        set(value) = preferences.edit { putString(KEY_GROUP_SORT_ORDER, value.preferenceValue) }

    var padSortOrder: PadListSortOrder
        get() = PadListSortOrder.fromPreferenceValue(
            preferences.getString(KEY_PAD_SORT_ORDER, null))
        set(value) = preferences.edit { putString(KEY_PAD_SORT_ORDER, value.preferenceValue) }

    companion object {
        const val KEY_GROUP_SORT_ORDER = "padlist_group_sort_order"
        const val KEY_PAD_SORT_ORDER = "padlist_pad_sort_order"
    }
}

