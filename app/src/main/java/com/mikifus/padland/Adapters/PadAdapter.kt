package com.mikifus.padland.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.DragAndDropListener.DragAndDropListener
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R


class PadAdapter(
    context: Context,
    private val dragAndDropListener: IDragAndDropListener,
    private val onClickListener: OnClickListener? = null):
    RecyclerView.Adapter<PadAdapter.PadViewHolder>(){

    private val mInflater: LayoutInflater
    var data: List<Pad> = listOf()
    var padGroupId: Long = 0
    var tracker: SelectionTracker<Long>? = null
    var onTouchListener: OnTouchListener? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    class PadViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val url: TextView
        val itemLayout: ConstraintLayout

        var padId: Long = 0
        var padGroupId: Long = 0

        init {
            name = itemView.findViewById(R.id.text_recyclerview_item_name)
            url = itemView.findViewById(R.id.text_recyclerview_item_url)
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

        onClickListener?.let { itemView.setOnClickListener(onClickListener) }

        return PadViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PadViewHolder, position: Int) {
        val current: Pad = data[position]
        holder.name.text = current.mName
        holder.url.text = current.mUrl

        holder.padGroupId = padGroupId
        holder.itemLayout.tag = current.mId
        holder.padId = current.mId

        onTouchListener?.let { holder.itemLayout.setOnTouchListener(onTouchListener) }

        tracker?.let {
            holder.itemLayout.isSelected = it.isSelected(current.mId)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


    fun getDragInstance(): DragAndDropListener {
        return DragAndDropListener(dragAndDropListener)
    }

}