package com.mikifus.padland.Adapters.PadSelectionTracker.DragAndDropListener

interface IDragAndDropListener {
    fun setEmptyListTop(visibility: Boolean)
    fun setEmptyListBottom(visibility: Boolean)

    fun notifyChange(padGroupId: Long, padId: Long, position: Int)
}