package com.mikifus.padland.Adapters.DragAndDropListener

interface IDragAndDropListener {
    fun setEmptyListTop(visibility: Boolean)
    fun setEmptyListBottom(visibility: Boolean)

    fun notifyChange(padGroupId: Long, padId: Long, position: Int)
}