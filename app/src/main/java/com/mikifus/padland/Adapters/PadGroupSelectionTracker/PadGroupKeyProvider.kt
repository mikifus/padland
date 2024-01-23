package com.mikifus.padland.Adapters.PadGroupSelectionTracker

import androidx.recyclerview.selection.ItemKeyProvider
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.Adapters.PadGroupAdapter

class PadGroupKeyProvider(private val adapter: PadGroupAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long =
        adapter.data[position].padGroup.mId

    override fun getPosition(key: Long): Int =
        adapter.data.indexOfFirst { it.padGroup.mId == key }
}