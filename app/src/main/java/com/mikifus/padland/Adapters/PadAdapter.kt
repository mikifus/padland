package com.mikifus.padland.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R
import com.mikifus.padland.Adapters.PadSelectionTracker.DragAndDropListener.DragAndDropListener
import com.mikifus.padland.Adapters.PadSelectionTracker.DragAndDropListener.IDragAndDropListener


class PadAdapter(context: Context, listener: IDragAndDropListener): RecyclerView.Adapter<PadAdapter.PadViewHolder>(){

    private val mInflater: LayoutInflater
    var data: List<Pad> = listOf()
    var padGroupId: Long = 0
    private val dragAndDropListener: IDragAndDropListener
    var tracker: SelectionTracker<Long>? = null

    init {
        mInflater = LayoutInflater.from(context);
        dragAndDropListener = listener
    }

    class PadViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val url: TextView
        val itemLayout: ConstraintLayout

        var padId: Long = 0
        var padGroupId: Long = 0

        init {
            name = itemView.findViewById<TextView>(R.id.text_recyclerview_item_name)
            url = itemView.findViewById<TextView>(R.id.text_recyclerview_item_url)
            itemLayout = itemView.findViewById(R.id.pad_list_recyclerview_item_pad)
        }

        fun getItem(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = padId
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PadViewHolder {
        val itemView: View = mInflater.inflate(R.layout.pad_list_recyclerview_item_pad, parent, false)
        return PadViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PadViewHolder, position: Int) {
        val current: Pad = data[position]
        holder.name.text = current.mName
        holder.url.text = current.mUrl

        holder.padGroupId = padGroupId
        holder.itemLayout.tag = current.mId
        holder.padId = current.mId

        tracker?.let {
            holder.itemLayout.isSelected = it.isSelected(current.mId)
        }
    }

    override fun getItemCount(): Int {
        return data.size;
    }


    fun getDragInstance(): DragAndDropListener? {
        return if (dragAndDropListener != null) {
            DragAndDropListener(dragAndDropListener)
        } else {
            Log.e("ListAdapter", "Listener wasn't initialized!")
            null
        }
    }

}