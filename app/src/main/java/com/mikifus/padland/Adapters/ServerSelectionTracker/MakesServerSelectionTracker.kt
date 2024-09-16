package com.mikifus.padland.Adapters.ServerSelectionTracker

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.ActionModes.ServerActionModeCallback
import com.mikifus.padland.Activities.ServerListActivity
import com.mikifus.padland.Adapters.ServerAdapter
import com.mikifus.padland.R

interface IMakesServerSelectionTracker {
    var serverSelectionTracker: SelectionTracker<Long>?
    var lastSelectedServerView: View?
    var serverActionMode: ActionMode?
    fun makeServerSelectionTracker(activity: ServerListActivity, recyclerView: RecyclerView, serverAdapter: ServerAdapter): SelectionTracker<Long>
    fun getServerSelection(): List<Long>
    fun onDestroyServerActionMode()
}
class MakesServerSelectionTracker: IMakesServerSelectionTracker {

    override var serverSelectionTracker: SelectionTracker<Long>? = null
    override var lastSelectedServerView: View? = null
    override var serverActionMode: ActionMode? = null
    var activity: AppCompatActivity? = null

    override fun makeServerSelectionTracker(activity: ServerListActivity, recyclerView: RecyclerView, serverAdapter: ServerAdapter): SelectionTracker<Long> {
        this.activity = activity
        serverSelectionTracker = SelectionTracker.Builder(
            "padSelectionTracker",
            recyclerView,
            ServerKeyProvider(serverAdapter),
            ServerDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        serverSelectionTracker!!.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if (serverActionMode == null) {
                    serverActionMode =
                        activity.startSupportActionMode(ServerActionModeCallback(activity))
                }

                val selectionCount = getServerSelection().size
                if (selectionCount > 0) {
                    serverActionMode?.title = "$selectionCount " + activity.getString(R.string.model_server)

                    recyclerView
                        .findViewHolderForItemId(
                            getServerSelection().last()
                        )?.let {
                            lastSelectedServerView = (it as ServerAdapter.ServerViewHolder).itemLayout
                        }
                } else if(serverActionMode != null) {
                    finishActionMode()
                }
            }
        })

        return serverSelectionTracker!!
    }

    override fun getServerSelection(): List<Long> {
        return serverSelectionTracker!!.selection.toList()
    }

    override fun onDestroyServerActionMode() {
        if(serverSelectionTracker != null) {
            serverActionMode = null
            serverSelectionTracker!!.clearSelection()
        }
    }

    fun finishActionMode() {
        serverActionMode?.finish()
    }
}