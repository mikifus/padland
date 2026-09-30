package com.mikifus.padland.Utils.Export

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.mikifus.padland.Utils.Export.TypeAdapters.SqlDateTypeAdapter
import java.sql.Date

/**
 * Skips imported records identical to existing ones.
 *
 * Records are compared as exported (e.g. dates by day),
 * without the fields reset by [IImportMatchable.toImportMatch].
 */
object ImportMatcher {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, SqlDateTypeAdapter)
        .create()

    /**
     * [items] not found in [existing], without repetitions.
     */
    fun <T : IImportMatchable> filterNew(items: List<T>, existing: List<T>): List<T> {
        val keys = existing.mapTo(HashSet()) { key(it) }
        return items.filter { keys.add(key(it)) }
    }

    private fun key(item: IImportMatchable): JsonElement = gson.toJsonTree(item.toImportMatch())
}

