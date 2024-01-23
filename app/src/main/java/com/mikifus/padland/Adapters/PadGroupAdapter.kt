package com.mikifus.padland.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.R


class PadGroupAdapter(context: Context, listener: IDragAndDropListener):
    RecyclerView.Adapter<PadGroupAdapter.PadGroupViewHolder>() {

    private val activityContext: Context

    private val mInflater: LayoutInflater
    var data: List<PadGroupsWithPadList> = listOf()
    private val dragAndDropListener: IDragAndDropListener
    var tracker: SelectionTracker<Long>? = null

    init {
        mInflater = LayoutInflater.from(context)
        activityContext = context
        dragAndDropListener = listener
    }

    class PadGroupViewHolder(itemView: View, context: AppCompatActivity, listener: IDragAndDropListener) :
        RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView
        val itemLayout: ConstraintLayout
        private val padListRecyclerView: RecyclerView

        val padAdapter: PadAdapter

        var padGroupId: Long = 0

        init {
            titleTextView = itemView.findViewById(R.id.text_recyclerview_item_name)
            padListRecyclerView = itemView.findViewById(R.id.recyclerview_padgroup)
            itemLayout = itemView.findViewById(R.id.pad_list_recyclerview_item_padgroup)
            itemLayout.isActivated = true
            padListRecyclerView.layoutManager = LinearLayoutManager(context)
            padAdapter = PadAdapter(context, listener)

            initListView()
            padAdapter.tracker = (context as PadListActivity).makePadSelectionTracker(context, padListRecyclerView, padAdapter)

            initEvents()
        }

        private fun initListView() {
            itemView.setOnDragListener(padAdapter.getDragInstance())
            padListRecyclerView.setOnDragListener(padAdapter.getDragInstance())
            padListRecyclerView.adapter = padAdapter
        }

        fun initEvents() {
            itemView.setOnClickListener {
                toggle()
            }
        }

        private fun toggle() {
            val transition: Transition = AutoTransition()
            transition.duration = 400
            transition.addTarget(padListRecyclerView)
            TransitionManager.beginDelayedTransition(padListRecyclerView.rootView as ViewGroup, transition)

            itemLayout.isActivated = !itemLayout.isActivated
            padListRecyclerView.visibility = if (itemLayout.isActivated){
                View.VISIBLE
            } else{
                View.GONE
            }
        }

        fun getItem(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition
                override fun getSelectionKey(): Long = padGroupId
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PadGroupViewHolder {
        val itemView: View = mInflater.inflate(R.layout.pad_list_recyclerview_item_padgroup, parent, false)
        return PadGroupViewHolder(itemView, activityContext as AppCompatActivity, dragAndDropListener)
    }

    override fun onBindViewHolder(holder: PadGroupViewHolder, position: Int) {
        val current: PadGroupsWithPadList = data[position]
        holder.titleTextView.text = current.padGroup.mName
        holder.itemLayout.tag = current.padGroup.mId
        holder.padGroupId = current.padGroup.mId
        holder.padAdapter.padGroupId = current.padGroup.mId
        holder.padAdapter.data = current.padList

        holder.padAdapter.notifyDataSetChanged()

        tracker?.let {
            holder.itemLayout.isSelected = it.isSelected(current.padGroup.mId)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


}