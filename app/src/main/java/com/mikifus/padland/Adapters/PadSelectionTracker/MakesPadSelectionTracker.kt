package com.mikifus.padland.Adapters.PadSelectionTracker

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.ActionModes.AnyActionModeActive
import com.mikifus.padland.ActionModes.IAnyActionModeActive
import com.mikifus.padland.ActionModes.PadActionModeCallback
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.PadAdapter

interface IMakesPadSelectionTracker {
    var padSelectionTrackers: MutableList<SelectionTracker<Long>>?
    var padActionMode: ActionMode?
    fun makePadSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padAdapter: PadAdapter): SelectionTracker<Long>
    fun getPadSelection(): List<Long>
    fun onDestroyPadActionMode()
    fun getSelectionBlock(): Boolean
    fun setSelectionBlock(value: Boolean)
}
class MakesPadSelectionTracker: IMakesPadSelectionTracker,
    IAnyActionModeActive by AnyActionModeActive() {

    override var padSelectionTrackers: MutableList<SelectionTracker<Long>>? = null
    override var padActionMode: ActionMode? = null
    var activity: PadListActivity? = null

    override fun makePadSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padAdapter: PadAdapter): SelectionTracker<Long> {
        this.activity = activity
        val padSelectionTracker: SelectionTracker<Long> = SelectionTracker.Builder(
            "padSelectionTracker",
            recyclerView,
            PadKeyProvider(padAdapter),
            PadDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(object: SelectionTracker.SelectionPredicate<Long>() {
                override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
                    if(padActionMode == null && getSelectionBlock()) {
                        return false
                    }
                    return true
                }

                override fun canSetStateAtPosition(position: Int, nextState: Boolean) = true
                override fun canSelectMultiple() = true
            })
            .withOnItemActivatedListener { item, event ->
                if(padActionMode != null) {
                    if(item.selectionKey != null) {
                        padAdapter.tracker!!.select(item.selectionKey!!)
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

        padSelectionTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if (padActionMode == null) {
                    padActionMode =
                        activity.startSupportActionMode(PadActionModeCallback(activity))
                    setSelectionBlock(true)
                }

                val selectionCount = getPadSelection().size
                if (selectionCount > 0) {
                    padActionMode?.title = selectionCount.toString()
                } else if(padActionMode != null) {
                    finishActionMode()
                }
            }
        })

        if(padSelectionTrackers != null) {
            padSelectionTrackers!!.add(padSelectionTracker)
        } else {
            padSelectionTrackers = mutableListOf(padSelectionTracker)
        }

        return padSelectionTracker
    }
    override fun getPadSelection(): List<Long> {
        val selection = padSelectionTrackers?.flatMap { it.selection.toList() }
        return selection?: listOf()
    }

    override fun onDestroyPadActionMode() {
        if(padActionMode != null) {
            padActionMode = null
            padSelectionTrackers?.forEach {
                it.clearSelection()
            }
        }
        setSelectionBlock(false)
    }

    fun finishActionMode() {
        padActionMode?.finish()
    }

    override fun getSelectionBlock(): Boolean {
        return activity?.getSelectionBlock() ?: false
    }

    override fun setSelectionBlock(value: Boolean) {
        activity?.setSelectionBlock(value)
    }
}