package com.mikifus.padland.Adapters.ServerSelectionTracker

import androidx.recyclerview.selection.ItemKeyProvider
import com.mikifus.padland.Adapters.ServerAdapter

class ServerKeyProvider(private val adapter: ServerAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long =
        adapter.data[position].mId

    override fun getPosition(key: Long): Int =
        adapter.data.indexOfFirst { it.mId == key }
}