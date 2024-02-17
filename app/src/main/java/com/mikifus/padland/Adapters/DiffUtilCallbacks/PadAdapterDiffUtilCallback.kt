package com.mikifus.padland.Adapters.DiffUtilCallbacks

import androidx.recyclerview.widget.DiffUtil
import com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads.PadGroupPayload
import com.mikifus.padland.Database.PadModel.Pad

open class PadAdapterDiffUtilCallback(
    private val oldValue: List<Pad>,
    private val newValue: List<Pad>)
    : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldValue.size
    override fun getNewListSize(): Int = newValue.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldValue[oldItemPosition].mId == newValue[newItemPosition].mId
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        return !oldValue[oldItemPosition].isPartiallyDifferentFrom(newValue[newItemPosition])
    }
}