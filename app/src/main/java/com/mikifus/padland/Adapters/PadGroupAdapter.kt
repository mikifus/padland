package com.mikifus.padland.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.textview.MaterialTextView
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads.PadGroupPayload
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PadGroupAdapter(private val activity: AppCompatActivity,
                      private val dragAndDropListener: IDragAndDropListener,
                      private val onClickListener: OnClickListener,
                      private val onClickInfoListener: OnClickListener? = null):
    RecyclerView.Adapter<PadGroupAdapter.PadGroupViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private var padAdapterTouchListener: OnTouchListener? = null
    var tracker: SelectionTracker<Long>? = null

    var data: List<PadGroupsWithPadList> = listOf()
        set(value) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val diffResult = computeDataSetChanged(field, value)
                withContext(Dispatchers.Main) {
                    field = value
                    diffResult.dispatchUpdatesTo(this@PadGroupAdapter)
                }
            }
        }

    init {
        setHasStableIds(true)
        initEvents()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return data[position].padGroup.mId
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
        activity: AppCompatActivity,
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
            mPadListRecyclerView.layoutManager = LinearLayoutManager(activity)
            padAdapter = PadAdapter(activity, listener, onClickListener, onClickInfoListener)

            initListView()
            padAdapter.tracker = (activity as PadListActivity).makePadSelectionTracker(activity, mPadListRecyclerView, padAdapter)

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
            transition.duration = 200
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

        fun bindTitle(title: String) {
            titleTextView.text = title
        }

        fun bindPadList(padList: List<Pad>) {
            padAdapter.data = padList

            mEmptyView.visibility = if (padList.isEmpty() && itemLayout.isActivated){
                View.VISIBLE
            } else{
                View.GONE
            }
        }

        fun bindSelected(selected: Boolean) {
            itemLayout.isSelected = selected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PadGroupViewHolder {
        val itemView: View = mInflater.inflate(R.layout.pad_list_recyclerview_item_padgroup, parent, false)
        return PadGroupViewHolder(itemView, activity, dragAndDropListener, onClickListener, onClickInfoListener)
    }

    override fun onBindViewHolder(holder: PadGroupViewHolder, position: Int) {
        val current: PadGroupsWithPadList = data[position]
        holder.bindTitle(makeGroupTitle(current))
        holder.itemLayout.tag = current.padGroup.mId
        holder.padGroupId = current.padGroup.mId

        holder.padAdapter.padGroupId = current.padGroup.mId
        holder.padAdapter.onTouchListener = padAdapterTouchListener
        holder.bindPadList(current.padList)

        tracker?.let {
            holder.bindSelected(it.isSelected(current.padGroup.mId))
        }
    }

    override fun onBindViewHolder(
        holder: PadGroupViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }

        when (val latestPayload = payloads.lastOrNull()) {
            is PadGroupPayload.Title -> holder.bindTitle(latestPayload.title)
            is PadGroupPayload.TitlePadList -> {
                holder.bindTitle(latestPayload.title)
                holder.bindPadList(latestPayload.padList)
            }
            "Selection-Changed" -> holder.bindSelected(
                tracker?.isSelected(holder.padGroupId) == true)
            else -> onBindViewHolder(holder, position)
        }
    }

    private fun computeDataSetChanged(
        oldValue: List<PadGroupsWithPadList>,
        newValue: List<PadGroupsWithPadList>): DiffUtil.DiffResult {

        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldValue.size
            override fun getNewListSize(): Int = newValue.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldValue[oldItemPosition].padGroup.mId == newValue[newItemPosition].padGroup.mId
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return !oldValue[oldItemPosition].isPartiallyDifferentFrom(newValue[newItemPosition])
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any {
                return when {
                    oldValue[oldItemPosition].padGroup.mName != newValue[newItemPosition].padGroup.mName -> {
                        PadGroupPayload.Title(makeGroupTitle(newValue[newItemPosition]))
                    }
                    else -> {
                        PadGroupPayload.TitlePadList(
                            makeGroupTitle(newValue[newItemPosition]),
                            newValue[newItemPosition].padList)
                    }
                }
            }
        }, true)
    }

    fun makeGroupTitle(current: PadGroupsWithPadList): String {
        return activity.getString(
            R.string.show_padgroup_title,
            current.padList.size,
            current.padGroup.mName
        )
    }
}