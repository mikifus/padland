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
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.Adapters.PadGroupAdapter
import com.mikifus.padland.Adapters.PadGroupSelectionTracker.IMakesPadGroupSelectionTracker
import com.mikifus.padland.Adapters.PadGroupSelectionTracker.MakesMakesPadGroupSelectionTrackerImpl
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadListEntity
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialog.NewPadGroup
import com.mikifus.padland.NewPadActivity
import com.mikifus.padland.R
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Adapters.PadSelectionTracker.IMakesPadSelectionTracker
import com.mikifus.padland.Adapters.PadSelectionTracker.MakesPadSelectionTrackerImpl
import kotlinx.coroutines.launch


/**
 * This activity displays a list of previously checked documents.
 * Here documents can be deleted via Intent.
 * It handles as well the sharing intent to the app.
 *
 * @author mikifus
 * @since 0.1
 */
class PadListActivity: AppCompatActivity(),
//    ActionMode.Callback,
    IDragAndDropListener,
    IMakesPadSelectionTracker by MakesPadSelectionTrackerImpl(),
    IMakesPadGroupSelectionTracker by MakesMakesPadGroupSelectionTrackerImpl() {

    /**
     * mActionMode defines behaviour of the action-bar
     */
    var mActionMode: ActionMode? = null

    var padGroupViewModel: PadGroupViewModel? = null
    var padViewModel: PadViewModel? = null

    private var mainList: List<PadGroupsWithPadList>? = null
    private var unclassifiedList: List<Pad>? = null

    private var recyclerView: RecyclerView? = null
    private var recyclerViewUnclassified: RecyclerView? = null
    private var titleViewUnclassified: View? = null

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
        recyclerViewUnclassified!!.isNestedScrollingEnabled = false

        // Unclassified list title
        titleViewUnclassified = findViewById(R.id.unclassified_title)

        initListView()
        initEvents()
    }

    /*
    This method shall be used to initialize the list view using observer,
    here onChanged shall be triggered realtime as the data changes
     */
    private fun initListView() {
        initSelectionTrackers()

        padGroupViewModel!!.getPadGroupsWithPadList.observe(this) { currentList ->
            mainList = currentList ?: listOf()
            adapter!!.data = mainList!!
            adapter!!.notifyDataSetChanged()
        }

        padGroupViewModel!!.getPadsWithoutGroup.observe(this) { currentList ->
            unclassifiedList = currentList ?: listOf()
            padAdapter!!.data = unclassifiedList!!
            padAdapter!!.notifyDataSetChanged()
        }
    }

    private fun initSelectionTrackers() {
        padAdapter!!.tracker = makePadSelectionTracker(this, recyclerViewUnclassified!!, padAdapter!!)
        adapter!!.tracker = makePadGroupSelectionTracker(this, recyclerView!!, adapter!!)
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


        titleViewUnclassified!!.isActivated = true
        titleViewUnclassified!!.setOnClickListener {
            toggleUnclassifiedRecyclerView()
        }
    }

    private fun toggleUnclassifiedRecyclerView() {
        if(recyclerViewUnclassified != null) {
            val transition: Transition = AutoTransition()
            transition.duration = 400
            transition.addTarget(recyclerViewUnclassified!!)
            TransitionManager.beginDelayedTransition(
                recyclerViewUnclassified!!.rootView as ViewGroup,
                transition
            )

            titleViewUnclassified!!.isActivated = !titleViewUnclassified!!.isActivated
            recyclerViewUnclassified!!.visibility = if (titleViewUnclassified!!.isActivated) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun showNewPadgroupDialog() {
        val fm = supportFragmentManager
        val dialog = NewPadGroup()
        dialog.show(fm, "dialog_new_padgroup")
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