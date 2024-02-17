package com.mikifus.padland.Adapters

import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewKeyProvider(private val recyclerView: RecyclerView) : ItemKeyProvider<Long>(SCOPE_CACHED) {

    override fun getKey(position: Int): Long =
        recyclerView.adapter?.getItemId(position)
            ?: throw IllegalStateException("RecyclerView adapter is not set!")

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}