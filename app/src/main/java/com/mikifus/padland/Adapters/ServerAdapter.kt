package com.mikifus.padland.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.R

/**
 * Created by mikifus on 29/05/16.
 */
class ServerAdapter(
    context: AppCompatActivity,
    private val onClickListener: View.OnClickListener? = null) :
    RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    private val mInflater: LayoutInflater
    var data: List<Server> = listOf()
    var tracker: SelectionTracker<Long>? = null

    init {
        mInflater = LayoutInflater.from(context)
    }

    class ServerViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val url: TextView
        val itemLayout: ConstraintLayout

        var serverId: Long = 0

        init {
            name = itemView.findViewById(R.id.text_recyclerview_item_name)
            url = itemView.findViewById(R.id.text_recyclerview_item_url)
            itemLayout = itemView.findViewById(R.id.server_list_recyclerview_item_server)
        }

        fun getItem(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = serverId
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val itemView: View = mInflater.inflate(R.layout.server_list_recyclerview_item_server, parent, false)

        onClickListener?.let { itemView.setOnClickListener(onClickListener) }

        return ServerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val current: Server = data[position]
        holder.name.text = current.mName
        holder.url.text = current.mUrl

        holder.itemLayout.tag = current.mId
        holder.serverId = current.mId

        tracker?.let {
            holder.itemLayout.isSelected = it.isSelected(current.mId)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}