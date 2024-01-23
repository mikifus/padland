package com.mikifus.padland.Adapters.PadSelectionTracker.DragAndDropListener

import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.R

class DragAndDropListener internal constructor(listener: IDragAndDropListener) : OnDragListener {
    private var isDropped = false
    private val listener: IDragAndDropListener

    init {
        this.listener = listener
    }

    override fun onDrag(view: View, event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DROP -> {
                isDropped = true
                var positionTarget = 0
                val viewSource = event.localState as View
                val viewId = view.id
//                val tvEmptyListTop: Int = R.id.tvEmptyListTop
//                val tvEmptyListBottom: Int = R.id.tvEmptyListBottom
                val padItemViewId: Int = R.id.pad_list_recyclerview_item_pad
                val padGroupRvViewId: Int = R.id.recyclerview_padgroup
                val unclassifiedViewId: Int = R.id.recyclerview_unclassified
                val padGroupViewId: Int = R.id.pad_list_recyclerview_item_padgroup

                when (viewId) {
                    padItemViewId, padGroupRvViewId, unclassifiedViewId, padGroupViewId -> {
//                        val target: RecyclerView
                        val targetGroupId: Long
                        when (viewId) {
                            padItemViewId -> {
                                targetGroupId =
                                    (
                                        (view.parent
                                                as RecyclerView)
                                            .getChildViewHolder(view)
                                                as PadAdapter.PadViewHolder
                                    )
                                    .padGroupId

                                positionTarget = (view.parent as RecyclerView).getChildAdapterPosition(view)
                            }

                            padGroupRvViewId -> {
                                val itemView = (view as RecyclerView).findChildViewUnder(event.x, event.y)
                                if(itemView != null) {
                                    targetGroupId = (
                                                view.getChildViewHolder(itemView)
                                                        as PadAdapter.PadViewHolder
                                            ).padGroupId

                                    positionTarget = view.getChildAdapterPosition(itemView)
                                } else {
                                    targetGroupId = -1
                                }
                            }

                            unclassifiedViewId -> {
                                targetGroupId = 0
                            }

                            padGroupViewId -> {
                                targetGroupId = view.tag as Long
                            }

                            else -> {
                                targetGroupId = -1
                            }
                        }
//                        if (viewSource != null) {
//                            val source = viewSource.parent as RecyclerView
//                            val adapterSource: PadAdapter? = source.adapter as PadAdapter?
//                            val positionSource = viewSource.tag as Int
//                            val sourceId = source.id
//                            val list: Pad = adapterSource!!.data.get(positionSource)
//                            val listSource: MutableList<Pad> = adapterSource.data.toMutableList()
//                            listSource.removeAt(positionSource)
////                            adapterSource.setData(listSource)
//                            adapterSource.data = listSource
//                            adapterSource.notifyItemRemoved(positionSource)
//
//                            val adapterTarget: PadAdapter? = target.adapter as PadAdapter?
//                            val customListTarget: MutableList<Pad> = adapterTarget!!.data.toMutableList()
//                            if (positionTarget >= 0) {
//                                customListTarget.add(positionTarget, list)
//                            } else {
//                                customListTarget.add(list)
//                            }
//
////                            adapterTarget.setData(customListTarget)
//                            adapterTarget.data = customListTarget
//                            adapterTarget.notifyDataSetChanged()

                            if(targetGroupId > -1) {
                                listener.notifyChange(
                                    targetGroupId,
                                    (event.clipData.getItemAt(0).text as String).toLong(),
                                    positionTarget
                                )
                                return true
                            } else {
                                return false
                            }


//                            if (sourceId == rvBottom && adapterSource.getItemCount() < 1) {
//                                listener.setEmptyListBottom(true)
//                            }
//                            if (sourceId == rvTop && adapterSource.getItemCount() < 1) {
//                                listener.setEmptyListTop(true)
//                            }
//                        }
                    }
                }
            }
        }
//        if (!isDropped && event.localState != null) {
//            (event.localState as View).visibility = View.VISIBLE
//        }
        return true
    }
}