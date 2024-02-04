package com.mikifus.padland.Adapters.PadGroupSelectionTracker

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.ActionModes.AnyActionModeActive
import com.mikifus.padland.ActionModes.IAnyActionModeActive
import com.mikifus.padland.ActionModes.PadGroupActionModeCallback
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.PadGroupAdapter

interface IMakesPadGroupSelectionTracker {
    var padGroupSelectionTracker: SelectionTracker<Long>?
    var padGroupActionMode: ActionMode?
    fun makePadGroupSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padGroupAdapter: PadGroupAdapter): SelectionTracker<Long>
    fun getPadGroupSelection(): List<Long>
    fun onDestroyPadGroupActionMode()
    fun getSelectionBlock(): Boolean
    fun setSelectionBlock(value: Boolean)
}
class MakesPadGroupSelectionTracker: IMakesPadGroupSelectionTracker,
    IAnyActionModeActive by AnyActionModeActive() {
    override var padGroupSelectionTracker: SelectionTracker<Long>? = null
    override var padGroupActionMode: ActionMode? = null
    var activity: PadListActivity? = null

    override fun makePadGroupSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padGroupAdapter: PadGroupAdapter): SelectionTracker<Long> {
        this.activity = activity
        padGroupSelectionTracker = SelectionTracker.Builder(
            "padGroupSelectionTracker",
            recyclerView,
            PadGroupKeyProvider(padGroupAdapter),
            PadGroupDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(object: SelectionTracker.SelectionPredicate<Long>() {
                override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
                    if(padGroupActionMode == null && getSelectionBlock()) {
                        return false
                    }
                    return true
                }

                override fun canSetStateAtPosition(position: Int, nextState: Boolean) = true
                override fun canSelectMultiple() = true
            })
            .withOnItemActivatedListener { item, event ->
                if(padGroupActionMode != null) {
                    if(item.selectionKey != null) {
                        padGroupAdapter.tracker!!.select(item.selectionKey!!)
                    }
                    return@withOnItemActivatedListener true
                }
                return@withOnItemActivatedListener false
            }
            .withOnDragInitiatedListener {
                val view = recyclerView.findChildViewUnder(it.x, it.y)!!
                val item = ClipData.Item(view.tag.toString())
                val data = ClipData(
                    view.tag.toString(),
                    arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                    item
                )
                val shadowBuilder = View.DragShadowBuilder(view)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, view, 0)
                } else {
                    @Suppress("DEPRECATION")
                    view.startDrag(data, shadowBuilder, view, 0)
                }
                return@withOnDragInitiatedListener true
            }
            .build()

        padGroupSelectionTracker!!.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if (padGroupActionMode == null) {
                    padGroupActionMode =
                        activity.startSupportActionMode(PadGroupActionModeCallback(activity))
                    setSelectionBlock(true)
                }

                val selectionCount = getPadGroupSelection().size
                if (selectionCount > 0) {
                    padGroupActionMode?.title = selectionCount.toString()
                } else if(padGroupActionMode != null) {
                    finishActionMode()
                }
            }
        })

        return padGroupSelectionTracker!!
    }

    override fun getPadGroupSelection(): List<Long> {
        return padGroupSelectionTracker?.selection?.toList() ?: listOf()
    }

    override fun onDestroyPadGroupActionMode() {
        if(padGroupActionMode != null) {
            padGroupActionMode = null
            padGroupSelectionTracker?.clearSelection()
        }
        setSelectionBlock(false)
    }

    fun finishActionMode() {
        padGroupActionMode?.finish()
    }

    override fun getSelectionBlock(): Boolean {
        return activity?.getSelectionBlock() ?: false
    }

    override fun setSelectionBlock(value: Boolean) {
        activity?.setSelectionBlock(value)
    }
}