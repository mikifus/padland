package com.mikifus.padland.Adapters.PadSelectionTracker

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.View
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.Adapters.PadDetailsLookup
import com.mikifus.padland.Adapters.PadKeyProvider

interface IMakesPadSelectionTracker {
    var padSelectionTrackers: MutableList<SelectionTracker<Long>>?
    fun makePadSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padAdapter: PadAdapter): SelectionTracker<Long>
    fun clearSelection()
}
class MakesPadSelectionTrackerImpl: IMakesPadSelectionTracker {
    override var padSelectionTrackers: MutableList<SelectionTracker<Long>>? = null

    override fun makePadSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padAdapter: PadAdapter): SelectionTracker<Long> {
        val padSelectionTracker: SelectionTracker<Long> = SelectionTracker.Builder(
            "padListTracker",
            recyclerView,
            PadKeyProvider(padAdapter),
            PadDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            )
            .withOnItemActivatedListener { item, event ->
                if(activity.mActionMode != null) {
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

                if (activity.mActionMode == null) {
                    activity.mActionMode = activity.startSupportActionMode(activity)
                }

                var selectionCount = 0
                padSelectionTrackers?.forEach {
                    selectionCount += it.selection.size()
                }
                if (selectionCount > 0) {
                    activity.mActionMode?.title = selectionCount.toString()
                } else {
                    activity.mActionMode?.finish()
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

    override fun clearSelection() {
        padSelectionTrackers?.forEach {
            it.clearSelection()
        }
    }
}