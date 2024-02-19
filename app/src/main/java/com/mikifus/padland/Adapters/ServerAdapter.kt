package com.mikifus.padland.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads.ServerPayload
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by mikifus on 29/05/16.
 */
class ServerAdapter (
    private val activity: AppCompatActivity,
    private val onClickListener: View.OnClickListener? = null) :
    RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    var tracker: SelectionTracker<Long>? = null

    var data: List<Server> = listOf()
        set(value) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val diffResult = computeDataSetChanged(field, value)
                withContext(Dispatchers.Main) {
                    field = value
                    diffResult.dispatchUpdatesTo(this@ServerAdapter)
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

    class ServerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView
        val urlTextView: TextView
        val itemLayout: ConstraintLayout

        var serverId: Long = 0

        init {
            nameTextView = itemView.findViewById(R.id.text_recyclerview_item_name)
            urlTextView = itemView.findViewById(R.id.text_recyclerview_item_url)
            itemLayout = itemView.findViewById(R.id.server_list_recyclerview_item_server)
        }

        fun getItem(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = serverId
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val itemView: View = mInflater.inflate(R.layout.server_list_recyclerview_item_server, parent, false)

        onClickListener?.let { itemView.setOnClickListener(onClickListener) }

        return ServerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val current: Server = data[position]
        holder.bindName(current.mName)
        holder.bindUrl(current.mUrl)

        holder.itemLayout.tag = current.mId
        holder.serverId = current.mId

        tracker?.let {
            holder.bindSelected(it.isSelected(current.mId))
        }
    }

    override fun onBindViewHolder(
        holder: ServerViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        when (val latestPayload = payloads.lastOrNull()) {
            is ServerPayload.NameUrl -> {
                holder.bindName(latestPayload.name)
                holder.bindUrl(latestPayload.url)
            }
            is ServerPayload.Url -> holder.bindUrl(latestPayload.url)
            SelectionTracker.SELECTION_CHANGED_MARKER -> holder.bindSelected(
                tracker?.isSelected(holder.serverId) == true)
            else -> onBindViewHolder(holder, position)
        }
    }
    private fun computeDataSetChanged(
        oldValue: List<Server>,
        newValue: List<Server>): DiffUtil.DiffResult {

        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldValue.size
            override fun getNewListSize(): Int = newValue.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldValue[oldItemPosition].mId == newValue[newItemPosition].mId
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return !oldValue[oldItemPosition].isPartiallyDifferentFrom(newValue[newItemPosition])
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
                return when {
                    oldValue[oldItemPosition].mName != newValue[newItemPosition].mName -> {
                        ServerPayload.NameUrl(
                            newValue[newItemPosition].mName,
                            newValue[newItemPosition].mUrl)
                    }
                    else -> {
                        ServerPayload.Url(newValue[newItemPosition].mUrl)
                    }
                }
            }
        }, true)
    }
}