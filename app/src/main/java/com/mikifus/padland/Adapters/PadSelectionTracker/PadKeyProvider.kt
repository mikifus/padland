package com.mikifus.padland.Adapters.PadSelectionTracker

import androidx.recyclerview.selection.ItemKeyProvider
import com.mikifus.padland.Adapters.PadAdapter

class PadKeyProvider(private val adapter: PadAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long =
        adapter.data[position].mId!!

    override fun getPosition(key: Long): Int =
        adapter.data.indexOfFirst { it.mId == key }
}