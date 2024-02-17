package com.mikifus.padland.Adapters

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.textview.MaterialTextView
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R


class PadGroupAdapter(context: Context,
                      private val dragAndDropListener: IDragAndDropListener,
                      private val onClickListener: OnClickListener,
                      private val onClickInfoListener: OnClickListener? = null):
    RecyclerView.Adapter<PadGroupAdapter.PadGroupViewHolder>() {

    private val activityContext: Context
    private val mInflater: LayoutInflater
//    var data: List<PadGroupsWithPadList> = listOf()
//        set(value) {
//            val oldValue = data.toList()
//            field = value
//            computeDataSetChanged(oldValue, value)
//        }

    private var padAdapterTouchListener: OnTouchListener? = null
    var tracker: SelectionTracker<Long>? = null

    private var sortedData: SortedList<PadGroupsWithPadList> = SortedList(
        PadGroupsWithPadList::class.java,
        object: SortedListAdapterCallback<PadGroupsWithPadList>(this@PadGroupAdapter) {
            override fun compare(o1: PadGroupsWithPadList, o2: PadGroupsWithPadList): Int {
                return o1.padGroup.mId.toInt() - o2.padGroup.mId.toInt()
            }

            override fun areItemsTheSame(item1: PadGroupsWithPadList?, item2: PadGroupsWithPadList?): Boolean {
                return item1?.padGroup?.mId == item2?.padGroup?.mId
            }

            override fun areContentsTheSame(oldItem: PadGroupsWithPadList, newItem: PadGroupsWithPadList): Boolean {
                return !oldItem.isPartiallyDifferentFrom(newItem)
            }
        })

    init {
        mInflater = LayoutInflater.from(context)
        activityContext = context
        setHasStableIds(true)

        initEvents()
    }

    override fun getItemId(position: Int): Long {
        return sortedData.get(position).padGroup.mId
    }

    override fun getItemCount(): Int {
        return sortedData.size()
    }

    fun setData(padGroups: List<PadGroupsWithPadList>) {
        sortedData.beginBatchedUpdates()
        sortedData.replaceAll(padGroups)
        sortedData.endBatchedUpdates()
    }

    @SuppressLint("ClickableViewAccessibility") // See the onTouchListener
    private fun initEvents() {
        padAdapterTouchListener = OnTouchListener { view, motionEvent ->
            /**
             * If the user touches a pad inside a padgroup recyclerview
             * the top recyclerview that holds the padgroups is going
             * to manage the event. If we check and that's the case and
             * use requestDisallowInterceptTouchEvent() we can block
             * the top recyclerview from managing the event. Then, when
             * the user stops interacting (or changes interaction), we
             * release the block.
             *
             * Condition applies to children of the pad item layout.
             */
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(
                        // Case for touch on list item direct children
                        view.parent?.parent?.parent?.parent?.parent !== null
                        && view.parent?.parent?.parent?.parent?.parent is RecyclerView
                        && (view.parent.parent.parent.parent.parent as RecyclerView).tag == "recyclerview_padgroups"
                        )
                        view.parent.parent.parent.parent.requestDisallowInterceptTouchEvent(true)
                }
                else -> view.parent.parent.parent.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    class PadGroupViewHolder(
        itemView: View,
        context: Context,
        listener: IDragAndDropListener,
        onClickListener: OnClickListener?,
        onClickInfoListener: OnClickListener?
    ) :
        RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView
        val itemLayout: ConstraintLayout
        private val mPadListRecyclerView: RecyclerView
        val mEmptyView: MaterialTextView

        val padAdapter: PadAdapter

        var padGroupId: Long = 0

        init {
            titleTextView = itemView.findViewById(R.id.text_recyclerview_item_name)
            mPadListRecyclerView = itemView.findViewById(R.id.recyclerview_padgroup_padlist)
            mEmptyView = itemView.findViewById(R.id.recyclerview_padgroup_empty)
            itemLayout = itemView.findViewById(R.id.pad_list_recyclerview_item_padgroup)
            itemLayout.isActivated = true
            mPadListRecyclerView.layoutManager = LinearLayoutManager(context)
            padAdapter = PadAdapter(context, listener, onClickListener, onClickInfoListener)

            initListView()
            padAdapter.tracker = (context as PadListActivity).makePadSelectionTracker(context, mPadListRecyclerView, padAdapter)

            initEvents()
        }

        private fun initListView() {
            itemView.setOnDragListener(padAdapter.getDragInstance())
            mPadListRecyclerView.setOnDragListener(padAdapter.getDragInstance())
            mPadListRecyclerView.adapter = padAdapter
        }

        private fun initEvents() {
            itemView.setOnClickListener {
                toggle()
            }
        }

        private fun toggle() {
            val transition: Transition = AutoTransition()
            transition.duration = 400
            transition.addTarget(mPadListRecyclerView)
            transition.addTarget(mEmptyView)
            TransitionManager.beginDelayedTransition(mPadListRecyclerView.rootView as ViewGroup, transition)
            TransitionManager.beginDelayedTransition(mEmptyView.rootView as ViewGroup, transition)

            itemLayout.isActivated = itemLayout.isActivated.not()
            mPadListRecyclerView.visibility = if (itemLayout.isActivated){
                View.VISIBLE
            } else{
                View.GONE
            }
            mEmptyView.visibility = if (padAdapter.itemCount == 0 && itemLayout.isActivated) {
                View.VISIBLE
            } else {
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
        return PadGroupViewHolder(itemView, activityContext, dragAndDropListener, onClickListener, onClickInfoListener)
    }

    override fun onBindViewHolder(holder: PadGroupViewHolder, position: Int) {
        val current: PadGroupsWithPadList = sortedData.get(position)
        holder.titleTextView.text = activityContext.getString(
            R.string.show_padgroup_title,
            current.padList.size,
            current.padGroup.mName
        )
        holder.itemLayout.tag = current.padGroup.mId
        holder.padGroupId = current.padGroup.mId

        holder.padAdapter.padGroupId = current.padGroup.mId
        holder.padAdapter.setData(current.padList)
        holder.padAdapter.onTouchListener = padAdapterTouchListener

//        holder.padAdapter.notifyDataSetChanged()

        tracker?.let {
            holder.itemLayout.isSelected = it.isSelected(current.padGroup.mId)
        }

        holder.mEmptyView.visibility = if (current.padList.isEmpty() && holder.itemLayout.isActivated){
            View.VISIBLE
        } else{
            View.GONE
        }
    }

//    private fun computeDataSetChanged(oldValue: List<PadGroupsWithPadList>, newValue: List<PadGroupsWithPadList>) {
////        notifyDataSetChanged()
//        var tmpRange = oldValue.size
//        newValue.forEachIndexed { index, padGroup ->
//            if (oldValue.any { it.padGroup.mId == padGroup.padGroup.mId }) {
////                val coincidence = oldValue.find { it.padGroup.mId == newPadGroup.padGroup.mId }!!
////                if(coincidence.padGroup.mName != newPadGroup.padGroup.mName ||
////                    coincidence.padList != newPadGroup.padList
////                ) {
////                    notifyItemChanged(oldValue.indexOf(coincidence))
////                }
//                notifyDataSetChanged()
//                return@computeDataSetChanged
//            } else {
//                notifyItemInserted(index)
//                notifyItemRangeInserted(index, tmpRange++)
//            }
//
//        }
//
//        oldValue.forEachIndexed { index, padGroup ->
//            if (!newValue.any { it.padGroup.mId == padGroup.padGroup.mId }) {
//                notifyItemRemoved(index)
//                notifyItemRangeRemoved(index, tmpRange--)
//            }
//        }
//    }
}