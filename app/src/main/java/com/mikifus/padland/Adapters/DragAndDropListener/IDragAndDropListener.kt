package com.mikifus.padland.Adapters.DragAndDropListener

import android.view.DragEvent
import android.view.View

interface IDragAndDropListener {
//    fun setEmptyListTop(visibility: Boolean)
//    fun setEmptyListBottom(visibility: Boolean)

    fun onEnteredView(view: View, event: DragEvent)

    fun onExitedView(view: View, event: DragEvent)

    fun notifyChange(padGroupId: Long, padId: Long, position: Int)
}