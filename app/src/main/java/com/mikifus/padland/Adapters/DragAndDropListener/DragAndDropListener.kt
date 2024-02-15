package com.mikifus.padland.Adapters.DragAndDropListener

import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.R

class DragAndDropListener internal constructor(listener: IDragAndDropListener) : OnDragListener {
    private val listener: IDragAndDropListener
    private val enteredViews: MutableList<View> = mutableListOf()

    init {
        this.listener = listener
    }

    override fun onDrag(view: View, event: DragEvent): Boolean {
        val viewSource = event.localState as View
        val viewId = view.id
        val padItemViewId: Int = R.id.pad_list_recyclerview_item_pad
        val padGroupRvViewId: Int = R.id.recyclerview_padgroup_padlist
        val unclassifiedContainerId: Int = R.id.unclassified_container
        val padGroupViewId: Int = R.id.pad_list_recyclerview_item_padgroup

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                when (viewId) {
                    padGroupRvViewId -> {
                        val transition: Transition = AutoTransition()
                        transition.duration = 300
                        transition.addTarget(viewSource)
                        TransitionManager.beginDelayedTransition(viewSource.rootView as ViewGroup, transition)

                        viewSource.visibility = View.INVISIBLE
                    }
                }
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                when (viewId) {
                    padGroupRvViewId -> {
                        viewSource.visibility = View.VISIBLE
                    }
                }
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                var padGroupView: View? = null
                when (viewId) {
                    padGroupRvViewId -> {
                        padGroupView = (view.parent.parent as View)
                    }
                    padGroupViewId, unclassifiedContainerId -> {
                        padGroupView = view
                    }
                    else -> return false
                }
                if(padGroupView != null) {
                    listener.onEnteredView(padGroupView, event)
                    enteredViews.add(padGroupView)
                }
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                var padGroupView: View? = null
                when (viewId) {
                    padGroupRvViewId -> {
                        padGroupView = (view.parent.parent as View)
                    }
                    padGroupViewId, unclassifiedContainerId -> {
                        padGroupView = view
                    }
                }
                if(padGroupView != null) {
                    listener.onExitedView(padGroupView, event)
                    enteredViews.remove(padGroupView)
                }
            }
            DragEvent.ACTION_DROP -> {
                var positionTarget = 0
                val targetGroupId: Long
//                var draggedView: View? = null
                when (viewId) {
                    padItemViewId -> {
                        targetGroupId =
                            ((view.parent
                                    as RecyclerView)
                                .getChildViewHolder(view)
                                    as PadAdapter.PadViewHolder
                            ).padGroupId

                        positionTarget = (view.parent as RecyclerView).getChildAdapterPosition(view)
//                        draggedView = view
                    }
                    padGroupRvViewId -> {
                        val itemView = (view as RecyclerView).findChildViewUnder(event.x, event.y)
                        if(itemView != null) {
                            targetGroupId = (
                                        view.getChildViewHolder(itemView)
                                                as PadAdapter.PadViewHolder
                                    ).padGroupId

                            positionTarget = view.getChildAdapterPosition(itemView)
//                            draggedView = itemView
                        } else {
                            targetGroupId = -1
                        }
                    }
                    unclassifiedContainerId -> {
                        targetGroupId = 0
                    }
                    padGroupViewId -> {
                        targetGroupId = view.tag as Long
//                        draggedView = view.findViewById<RecyclerView>(padGroupRvViewId)
//                            .findChildViewUnder(event.x, event.y)
                    }
                    else -> {
                        targetGroupId = -1
                    }
                }

                enteredViews.forEach {
                    listener.onExitedView(it, event)
                }

//                if (viewSource != null) {
//                    val source = viewSource.parent as RecyclerView
//                    val adapterSource: PadAdapter? = source.adapter as PadAdapter?
//                    val positionSource = viewSource.tag as Int
//                    val sourceId = source.id
//                    val list: Pad = adapterSource!!.data.get(positionSource)
//                    val listSource: MutableList<Pad> = adapterSource.data.toMutableList()
//                    listSource.removeAt(positionSource)
////                            adapterSource.setData(listSource)
//                    adapterSource.data = listSource
//                    adapterSource.notifyItemRemoved(positionSource)
//
//                    val adapterTarget: PadAdapter? = target.adapter as PadAdapter?
//                    val customListTarget: MutableList<Pad> = adapterTarget!!.data.toMutableList()
//                    if (positionTarget >= 0) {
//                        customListTarget.add(positionTarget, list)
//                    } else {
//                        customListTarget.add(list)
//                    }

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
            }
        }
        return true
    }
}