package com.mikifus.padland.Adapters

import androidx.recyclerview.selection.ItemKeyProvider

class PadKeyProvider(private val adapter: PadAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    //2
    override fun getKey(position: Int): Long =
        adapter.data[position].mId

    //3
    override fun getPosition(key: Long): Int =
        adapter.data.indexOfFirst { it.mId == key }
}