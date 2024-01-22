package com.mikifus.padland.Utils.DragAndDropListener

interface DragAndDropListenerInterface {
    fun setEmptyListTop(visibility: Boolean)
    fun setEmptyListBottom(visibility: Boolean)

    fun notifyChange(padGroupId: Long, padId: Long, position: Int)
}