package com.mikifus.padland.Adapters.ServerSelectionTracker

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.ServerAdapter

class ServerDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

    //2
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        //3
        val view = recyclerView.findChildViewUnder(event.x, event.y)
        if (view != null) {
            //4
            return (recyclerView.getChildViewHolder(view) as ServerAdapter.ServerViewHolder).getItem()
        }
        return null
    }
}