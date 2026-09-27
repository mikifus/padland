package com.mikifus.padland.Adapters.DiffUtilCallbacks

import androidx.recyclerview.widget.DiffUtil
import com.mikifus.padland.Utils.ErrorReporting.ErrorReport

open class ErrorReportAdapterDiffUtilCallback(
    private val oldValue: List<ErrorReport>,
    private val newValue: List<ErrorReport>)
    : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldValue.size
    override fun getNewListSize(): Int = newValue.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldValue[oldItemPosition].id == newValue[newItemPosition].id
    }

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        // Saved reports never change, same id means same content
        return oldValue[oldItemPosition] == newValue[newItemPosition]
    }
}

