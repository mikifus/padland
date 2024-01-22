/*
 * Copyleft PadLand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mikifus.padland.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.Adapters.PadDetailsLookup
import com.mikifus.padland.Adapters.PadGroupAdapter
import com.mikifus.padland.Adapters.PadKeyProvider
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadListEntity
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialog.NewPadGroup
import com.mikifus.padland.NewPadActivity
import com.mikifus.padland.R
import com.mikifus.padland.Utils.DragAndDropListener.DragAndDropListenerInterface
import kotlinx.coroutines.launch


/**
 * This activity displays a list of previously checked documents.
 * Here documents can be deleted via Intent.
 * It handles as well the sharing intent to the app.
 *
 * @author mikifus
 * @since 0.1
 */
class PadListActivity: AppCompatActivity(), ActionMode.Callback, DragAndDropListenerInterface {

    /**
     * mActionMode defines behaviour of the action-bar
     */
    var mActionMode: ActionMode? = null
//    protected var padSelectionTracker: SelectionTracker<Long>? = null
    protected var padSelectionTrackers: MutableList<SelectionTracker<Long>>? = null

    var padGroupViewModel: PadGroupViewModel? = null
    var padViewModel: PadViewModel? = null

    private var mainList: List<PadGroupsWithPadList>? = null
    private var unclassifiedList: List<Pad>? = null

    private var recyclerView: RecyclerView? = null
    private var recyclerViewUnclassified: RecyclerView? = null

    private var adapter: PadGroupAdapter? = null
    private var padAdapter: PadAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pad_list_activity)

        //initializing all the UI elements
        recyclerView = findViewById(R.id.recyclerview)
        recyclerViewUnclassified = findViewById(R.id.recyclerview_unclassified)

        padGroupViewModel = ViewModelProvider(this).get(PadGroupViewModel::class.java)
        adapter = PadGroupAdapter(this, this)

        padViewModel = ViewModelProvider(this).get(PadViewModel::class.java)
        padAdapter = PadAdapter(this, this)

        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(this)

        recyclerViewUnclassified!!.adapter = padAdapter
        recyclerViewUnclassified!!.layoutManager = LinearLayoutManager(this)
        recyclerViewUnclassified!!.setOnDragListener(padAdapter!!.getDragInstance())

        initListView()
        initEvents()
    }

    /*
    This method shall be used to initialize the list view using observer,
    here onChanged shall be triggered realtime as the data changes
     */
    private fun initListView() {
        initPadSelectionTrackers()

        padGroupViewModel!!.getPadGroupsWithPadList.observe(this) { currentList ->
            mainList = currentList ?: listOf()
            adapter!!.data = mainList!!
            adapter!!.notifyDataSetChanged()
        }

        padViewModel!!.getAll.observe(this) { currentList ->
            unclassifiedList = currentList ?: listOf()
//            padAdapter!!.setData(unclassifiedList)
            padAdapter!!.data = unclassifiedList!!
            padAdapter!!.notifyDataSetChanged()
        }
    }

    private fun initPadSelectionTrackers() {
        val padSelectionTracker = SelectionTracker.Builder<Long>(
            "padListTracker",
            recyclerViewUnclassified!!,
            PadKeyProvider(padAdapter!!),
            PadDetailsLookup(recyclerViewUnclassified!!),
            StorageStrategy.createLongStorage()
        )
            .withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            )
//            .withOnItemActivatedListener { item, e ->
//                Log.d("TAG", "Selected ItemId: $item")
//                true
//            }
//            .withOnDragInitiatedListener {
//                Log.d("TAG", "onDragInitiated")
//                true
//            }
            .build()

        padAdapter!!.tracker = padSelectionTracker

        registerTracker(padSelectionTracker)

//        padSelectionTracker!!.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
////            override fun onItemStateChanged(key: Long, selected: Boolean) {
////                super.onItemStateChanged(key, selected)
////            }
////
////            override fun onSelectionRefresh() {
////                super.onSelectionRefresh()
////            }
//
//            override fun onSelectionChanged() {
//                super.onSelectionChanged()
//
//                if (mActionMode == null) {
//                    val currentActivity = this@PadListActivity
//                    mActionMode = currentActivity.startSupportActionMode(this@PadListActivity)
//
//                    recyclerView!!.clearFocus()
//                    recyclerView!!.isEnabled = false
//                }
//
//                val selectionCount = padSelectionTracker!!.selection.size()
//                if (selectionCount > 0) {
////                    mActionMode?.title = getString(R.string.action_selected, items)
//                    mActionMode?.title = selectionCount.toString()
//                } else {
//                    mActionMode?.finish()
//                }
//            }
////
////            override fun onSelectionRestored() {
////                super.onSelectionRestored()
////            }
//        })
    }

    fun initEvents() {
        val newPadGroupButton = findViewById<FloatingActionButton>(R.id.new_pad_group_button)
        newPadGroupButton.setOnClickListener(View.OnClickListener {
            showNewPadgroupDialog()
        })

        val newPadButton = findViewById<FloatingActionButton>(R.id.new_pad_button)
        newPadButton.setOnClickListener(View.OnClickListener {
            val newPadIntent = Intent(this@PadListActivity, NewPadActivity::class.java)
            startActivity(newPadIntent)
        })
    }

    fun registerTracker(tracker: SelectionTracker<Long>) {
        if(padSelectionTrackers == null) {
            padSelectionTrackers = mutableListOf(tracker)
        } else {
            padSelectionTrackers?.add(tracker)
        }


        tracker.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()

                if (mActionMode == null) {
                    val currentActivity = this@PadListActivity
                    mActionMode = currentActivity.startSupportActionMode(this@PadListActivity)

//                    recyclerView!!.clearFocus()
//                    recyclerView!!.isEnabled = false
                }

//                val selectionCount = tracker.selection.size()
                var selectionCount = 0
                padSelectionTrackers!!.forEach {
                    selectionCount += it.selection.size()
                }
                if (selectionCount > 0) {
                    mActionMode?.title = selectionCount.toString()
                } else {
                    mActionMode?.finish()
                }
            }
        })
    }

    private fun showNewPadgroupDialog() {
        val fm = supportFragmentManager
        val dialog = NewPadGroup()
        dialog.show(fm, "dialog_new_padgroup")
    }

    /**
     * Called when the action mode is created; startActionMode() was called
     *
     * @param mode
     * @param menu
     * @return boolean
     */
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        // Inflate a menu resource providing context menu items
        if(mode != null) {
            val inflater = mode.menuInflater
            inflater.inflate(R.menu.rowselection, menu)
            mActionMode = mode
        }
        return true
    }

    /**
     * Called each time the action mode is shown. Always called after onCreateActionMode, but
     * may be called multiple times if the mode is invalidated.
     *
     * @param mode
     * @param menu
     * @return boolean
     */
    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return false // Return false if nothing is done
    }

    /**
     * Called when the user selects a contextual menu item
     *
     * @param mode
     * @param item
     * @return
     */
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.menuitem_group -> {
//                menuGroup(checkedItemIds)
//                // Action picked, so close the CAB
//                mode.finish()
//                true
//            }
//
//            R.id.menuitem_copy -> {
//                menuCopy(checkedItemIds)
//                // Action picked, so close the CAB
//                mode.finish()
//                true
//            }
//
//            R.id.menuitem_edit -> {
//                menuEdit(checkedItemIds)
//                // Action picked, so close the CAB
//                mode.finish()
//                true
//            }
//
//            R.id.menuitem_delete -> {
//                askDelete(checkedItemIds)
//                // Action picked, so close the CAB
//                mode.finish()
//                true
//            }
//
//            R.id.menuitem_share -> {
//                menuShare(checkedItemIds)
//                // Action picked, so close the CAB
//                mode.finish()
//                true
//            }
//
            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        mActionMode = null
//        uncheckAllItems()
        padSelectionTrackers!!.forEach {
            it.clearSelection()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.pad_list, menu)
        return true
    }

    override fun setEmptyListTop(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setEmptyListBottom(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun notifyChange(padGroupId: Long, padId: Long, position: Int) {
        lifecycleScope.launch {
            padGroupViewModel!!.deletePadGroupsAndPadList(padId)
            if(padGroupId > 1) {
                padGroupViewModel!!.insertPadGroupsAndPadList(
                    PadGroupsAndPadListEntity(
                        mGroupId = padGroupId,
                        mPadId = padId,
                    )
                )
//                padViewModel!!.updatePadPosition(padId, position)
            }
        }
    }

}