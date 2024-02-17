package com.mikifus.padland.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.mikifus.padland.Adapters.DragAndDropListener.DragAndDropListener
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R


class PadAdapter(
    context: Context,
    private val dragAndDropListener: IDragAndDropListener,
    private val onClickListener: OnClickListener? = null,
    private val onClickInfoListener: OnClickListener? = null):
    RecyclerView.Adapter<PadAdapter.PadViewHolder>() {

    private val mInflater: LayoutInflater
    var padGroupId: Long = 0
    var tracker: SelectionTracker<Long>? = null
    var onTouchListener: OnTouchListener? = null

    private var sortedData: SortedList<Pad> = SortedList(Pad::class.java,
        object: SortedListAdapterCallback<Pad>(this@PadAdapter) {
            override fun compare(o1: Pad, o2: Pad): Int {
                return o1.mId.toInt() - o2.mId.toInt()
            }

            override fun areItemsTheSame(item1: Pad?, item2: Pad?): Boolean {
                return item1?.mId == item2?.mId
            }

            override fun areContentsTheSame(oldItem: Pad, newItem: Pad): Boolean {
                return !oldItem.isPartiallyDifferentFrom(newItem)
            }
        })

    init {
        mInflater = LayoutInflater.from(context)
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return sortedData.get(position).mId
    }

    override fun getItemCount(): Int {
        return sortedData.size()
    }

    fun setData(pads: List<Pad>) {
        sortedData.beginBatchedUpdates()
        sortedData.replaceAll(pads)
        sortedData.endBatchedUpdates()
    }

    class PadViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val url: TextView
        val content: LinearLayout
        val buttonCopy: ImageButton
        val itemLayout: ConstraintLayout

        var padId: Long = 0
        var padGroupId: Long = 0

        init {
            name = itemView.findViewById(R.id.text_recyclerview_item_name)
            url = itemView.findViewById(R.id.text_recyclerview_item_url)
            content = itemView.findViewById(R.id.content)
            buttonCopy = itemView.findViewById(R.id.button_copy)
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PadViewHolder, position: Int) {
        val current: Pad = sortedData.get(position)
        holder.name.text = current.mLocalName.ifBlank { current.mName }
        holder.url.text = current.mUrl

        holder.padGroupId = padGroupId
        holder.itemLayout.tag = current.mId
        holder.content.tag = current.mId
        holder.buttonCopy.tag = current.mId
        holder.padId = current.mId

        onTouchListener?.let {
            // Set on layout children
            holder.itemLayout.children.forEach {
                it.setOnTouchListener(onTouchListener)
            }
        }
        onClickListener?.let {
            holder.content.setOnClickListener(onClickListener)
        }
        onClickInfoListener?.let {
            holder.buttonCopy.setOnClickListener(onClickInfoListener)
        }

        tracker?.let {
            holder.itemLayout.isSelected = it.isSelected(current.mId)
        }
    }

    fun getDragInstance(): DragAndDropListener {
        return DragAndDropListener(dragAndDropListener)
    }

//    private fun computeDataSetChanged(oldValue: List<Pad>, newValue: List<Pad>) {
////        notifyDataSetChanged()
//        var tmpRange = oldValue.size
//        newValue.forEachIndexed { index, pad ->
//            if (oldValue.any { it.mId == pad.mId }) {
//                val coincidence = oldValue.find { it.mId == pad.mId }!!
//                if(
//                    coincidence.isPartiallyDifferentFrom(pad)
//                    ) {
//                    notifyItemChanged(oldValue.indexOf(coincidence))
//                }
////                notifyDataSetChanged()
////                return@computeDataSetChanged
//            } else {
//                notifyItemInserted(index)
//                notifyItemRangeInserted(index, tmpRange++)
//            }
//        }
//
//        oldValue.forEachIndexed { index, pad ->
//            if (!newValue.any { it.mId == pad.mId }) {
//                notifyItemRemoved(oldValue.indexOf(pad))
//                notifyItemRangeRemoved(index, tmpRange--)
//            }
//        }
//    }
}