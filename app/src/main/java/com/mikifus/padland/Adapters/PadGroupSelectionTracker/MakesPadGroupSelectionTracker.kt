package com.mikifus.padland.Adapters.PadGroupSelectionTracker

import android.content.ClipData
import android.content.ClipDescription
import android.os.Build
import android.view.View
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Adapters.PadGroupAdapter

interface IMakesPadGroupSelectionTracker {
    var padGroupSelectionTracker: SelectionTracker<Long>?
    fun makePadGroupSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padGroupAdapter: PadGroupAdapter): SelectionTracker<Long>
    fun clearPadGroupSelection()
}
class MakesMakesPadGroupSelectionTrackerImpl: IMakesPadGroupSelectionTracker {
    override var padGroupSelectionTracker: SelectionTracker<Long>? = null

    override fun makePadGroupSelectionTracker(activity: PadListActivity, recyclerView: RecyclerView, padGroupAdapter: PadGroupAdapter): SelectionTracker<Long> {
        padGroupSelectionTracker = SelectionTracker.Builder(
            "padGroupSelectionTracker",
            recyclerView,
            PadGroupKeyProvider(padGroupAdapter),
            PadGroupDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            )
            .withOnItemActivatedListener { item, event ->
                if(activity.mActionMode != null) {
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

                if (activity.mActionMode == null) {
                    activity.mActionMode = activity.startSupportActionMode(activity)
                }

                val selectionCount = padGroupSelectionTracker!!.selection.size()
                if (selectionCount > 0) {
                    activity.mActionMode?.title = selectionCount.toString()
                } else {
                    activity.mActionMode?.finish()
                }
            }
        })

        return padGroupSelectionTracker!!
    }

    override fun clearPadGroupSelection() {
        padGroupSelectionTracker?.clearSelection()
    }
}