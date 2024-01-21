package com.mikifus.padland.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.R
import com.mikifus.padland.Utils.DragAndDropListener.DragAndDropListenerInterface


class PadGroupAdapter(context: Context, listener: DragAndDropListenerInterface):
    RecyclerView.Adapter<PadGroupAdapter.PadGroupViewHolder>() {

    private val activityContext: Context

    private val mInflater: LayoutInflater
    var data: List<PadGroupsWithPadList> = listOf()
    private val dragAndDropListener: DragAndDropListenerInterface

    init {
        mInflater = LayoutInflater.from(context);
        activityContext = context
        dragAndDropListener = listener
    }

    class PadGroupViewHolder(itemView: View, context: AppCompatActivity, listener: DragAndDropListenerInterface) :
        RecyclerView.ViewHolder(itemView) {
        val dataText: TextView
        val itemLayout: ConstraintLayout
        private val padListRecyclerView: RecyclerView
        var padTracker: SelectionTracker<Long>? = null

        val padAdapter: PadAdapter

        var padGroupId: Long = 0

        init {
            dataText = itemView.findViewById<TextView>(R.id.text_recyclerview_item_name)
            padListRecyclerView = itemView.findViewById(R.id.recyclerview_padgroup)
            itemLayout = itemView.findViewById(R.id.pad_list_recyclerview_item_padgroup)
//            padRecyclerView = itemView.findViewById(R.id.recyclerview)
            padListRecyclerView.layoutManager = LinearLayoutManager(context)
//
//            padViewModel = ViewModelProvider(context)[PadViewModel::class.java]
            padAdapter = PadAdapter(context, listener)
            padAdapter.tracker = padTracker

            initListView()
            initPadSelectionTracker()
        }

        private fun initListView() {
            itemView.setOnDragListener(padAdapter.getDragInstance())
            padListRecyclerView.setOnDragListener(padAdapter.getDragInstance())
            padListRecyclerView.adapter = padAdapter
        }


        fun initPadSelectionTracker() {
            padTracker = SelectionTracker.Builder<Long>(
                "padListTracker",
                padListRecyclerView,
                PadKeyProvider(padAdapter),
                PadDetailsLookup(padListRecyclerView!!),
                StorageStrategy.createLongStorage()
            )
                .withSelectionPredicate(
                    SelectionPredicates.createSelectAnything()
                )
                .build()


            padTracker!!.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()

                    val currentActivity = itemLayout.context as PadListActivity
                    if (currentActivity.mActionMode == null) {
                        currentActivity.mActionMode = currentActivity.startSupportActionMode(currentActivity)

                        padListRecyclerView.clearFocus()
                        padListRecyclerView.isEnabled = false
                    }

                    val selectionCount = padTracker!!.selection.size()
                    if (selectionCount > 0) {
//                    mActionMode?.title = getString(R.string.action_selected, items)
                        currentActivity.mActionMode?.title = selectionCount.toString()
                    } else {
                        currentActivity.mActionMode?.finish()
                    }
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PadGroupViewHolder {
        val itemView: View = mInflater.inflate(R.layout.pad_list_recyclerview_item_padgroup, parent, false)
        return PadGroupViewHolder(itemView, activityContext as AppCompatActivity, dragAndDropListener)
    }

    override fun onBindViewHolder(holder: PadGroupViewHolder, position: Int) {
        val current: PadGroupsWithPadList = data[position]
        holder.dataText.text = current.padGroup.mName
        holder.itemLayout.tag = current.padGroup.mId;
        holder.padGroupId = current.padGroup.mId
//        holder.padAdapter.setData(current.padList)
        holder.padAdapter.data = current.padList
        holder.padAdapter.notifyDataSetChanged()

//        holder.padTracker = padSelectionTracker
    }

    override fun getItemCount(): Int {
        return data.size;
    }

//    fun setData(data: List<PadGroupsWithPadList>?) {
//        this.data = data!!
//        notifyDataSetChanged()
//    }


}