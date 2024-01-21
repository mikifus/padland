package com.mikifus.padland.Adapters

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R
import com.mikifus.padland.Utils.DragAndDropListener.DragAndDropListener
import com.mikifus.padland.Utils.DragAndDropListener.DragAndDropListenerInterface


class PadAdapter(context: Context, listener: DragAndDropListenerInterface): /*View.OnDragListener,*/View.OnLongClickListener,View.OnTouchListener, RecyclerView.Adapter<PadAdapter.PadViewHolder>(){

    private val mInflater: LayoutInflater
    var data: List<Pad> = listOf()
    private val dragAndDropListener: DragAndDropListenerInterface
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

        init {
            name = itemView.findViewById<TextView>(R.id.text_recyclerview_item_name)
            url = itemView.findViewById<TextView>(R.id.text_recyclerview_item_url)
            itemLayout = itemView.findViewById(R.id.pad_list_recyclerview_item_pad)
        }

        fun getItem(): ItemDetailsLookup.ItemDetails<Long> =

            //1
            object : ItemDetailsLookup.ItemDetails<Long>() {

                //2
                override fun getPosition(): Int = bindingAdapterPosition

                //3
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

//        holder.itemLayout.tag = position;
        holder.itemLayout.tag = current.mId;
//        holder.itemLayout.setOnTouchListener(this);
//        holder.itemLayout.setOnLongClickListener(this);
//        holder.itemLayout.setOnDragListener(DragAndDropListener(dragAndDropListener));
        holder.padId = current.mId

        tracker?.let {
            if (it.isSelected(current.mId)) {
                holder.itemLayout.setBackgroundColor(
                    ContextCompat.getColor(holder.itemLayout.context, R.color.design_default_color_primary))
            } else {
                holder.itemLayout.background = null
            }
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

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                view.performClick()
                return true
            }

//            MotionEvent.ACTION_UP -> {
//                if(allowDrag) {
//                    allowDrag = false
//                    return true
//                }
//                return false
//            }

            MotionEvent.ACTION_MOVE -> {
                val item = ClipData.Item(view.tag.toString())
                val data = ClipData(
                    view.tag.toString(),
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )
                val shadowBuilder = DragShadowBuilder(view)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, view, 0)
                } else {
                    view.startDrag(data, shadowBuilder, view, 0)
                }
                return true
            }

            else -> return false
        }
    }

    override fun onLongClick(view: View): Boolean {
//        val item = ClipData.Item(view.tag.toString())
//        val data = ClipData(
//            view.tag.toString(),
//            arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
//            item
//        )
//        val shadowBuilder = DragShadowBuilder(view)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            view.startDragAndDrop(data, shadowBuilder, view, 0)
//        } else {
//            view.startDrag(data, shadowBuilder, view, 0)
//        }
//        view.visibility = View.INVISIBLE;
        return true
    }

}