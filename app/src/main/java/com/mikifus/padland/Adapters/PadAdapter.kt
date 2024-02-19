package com.mikifus.padland.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.DiffUtilCallbacks.PadAdapterDiffUtilCallback
import com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads.PadPayload
import com.mikifus.padland.Adapters.DragAndDropListener.DragAndDropListener
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PadAdapter(
    private val activity: AppCompatActivity,
    private val dragAndDropListener: IDragAndDropListener,
    private val onClickListener: OnClickListener? = null,
    private val onClickInfoListener: OnClickListener? = null):
    RecyclerView.Adapter<PadAdapter.PadViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    var padGroupId: Long = 0
    var tracker: SelectionTracker<Long>? = null
    var onTouchListener: OnTouchListener? = null

    var data: List<Pad> = listOf()
        set(value) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val diffResult = computeDataSetChanged(field, value)
                withContext(Dispatchers.Main) {
                    field = value
                    diffResult.dispatchUpdatesTo(this@PadAdapter)
                }
            }
        }

    init {
        setHasStableIds(true)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return data[position].mId
    }

    class PadViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView
        val urlTextView: TextView
        val content: LinearLayout
        val buttonCopy: ImageButton
        val itemLayout: ConstraintLayout

        var padId: Long = 0
        var padGroupId: Long = 0

        init {
            nameTextView = itemView.findViewById(R.id.text_recyclerview_item_name)
            urlTextView = itemView.findViewById(R.id.text_recyclerview_item_url)
            content = itemView.findViewById(R.id.content)
            buttonCopy = itemView.findViewById(R.id.button_copy)
            itemLayout = itemView.findViewById(R.id.pad_list_recyclerview_item_pad)
        }

        fun getItem(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = padId
            }

        fun bindName(name: String) {
            nameTextView.text = name
        }
        fun bindUrl(url: String) {
            urlTextView.text = url
        }

        fun bindSelected(selected: Boolean) {
            itemLayout.isSelected = selected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PadViewHolder {
        val itemView: View = mInflater.inflate(R.layout.pad_list_recyclerview_item_pad, parent, false)
        return PadViewHolder(itemView)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PadViewHolder, position: Int) {
        val current: Pad = data[position]
        holder.bindName(current.mLocalName.ifBlank { current.mName })
        holder.bindUrl(current.mUrl)

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
            holder.bindSelected(it.isSelected(current.mId))
        }
    }

    override fun onBindViewHolder(
        holder: PadViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        when (val latestPayload = payloads.lastOrNull()) {
            is PadPayload.NameUrl -> {
                holder.bindName(latestPayload.name)
                holder.bindUrl(latestPayload.url)
            }
            is PadPayload.Url -> holder.bindUrl(latestPayload.url)
            SelectionTracker.SELECTION_CHANGED_MARKER -> holder.bindSelected(
                tracker?.isSelected(holder.padId) == true)
            else -> onBindViewHolder(holder, position)
        }
    }

    private fun computeDataSetChanged(oldValue: List<Pad>, newValue: List<Pad>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object: PadAdapterDiffUtilCallback(oldValue, newValue) {
            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return when {
                    oldValue[oldItemPosition].mName != newValue[newItemPosition].mName ||
                            oldValue[oldItemPosition].mLocalName != newValue[newItemPosition].mLocalName
                    -> {
                        PadPayload.NameUrl(
                            newValue[newItemPosition].mLocalName.ifBlank { newValue[newItemPosition].mName },
                            newValue[newItemPosition].mUrl
                        )
                    }
                    oldValue[oldItemPosition].mUrl != newValue[newItemPosition].mUrl -> {
                        PadPayload.Url(newValue[newItemPosition].mUrl)
                    }
                    else -> super.getChangePayload(oldItemPosition, newItemPosition)
                }
            }
        }, true)
    }

    fun getDragInstance(): DragAndDropListener {
        return DragAndDropListener(dragAndDropListener)
    }
}