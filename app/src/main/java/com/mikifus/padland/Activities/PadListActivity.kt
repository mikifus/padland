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
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
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
import com.mikifus.padland.Adapters.PadGroupSelectionTracker.MakesPadGroupSelectionTracker
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.NewPadActivity
import com.mikifus.padland.R
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Adapters.PadSelectionTracker.IMakesPadSelectionTracker
import com.mikifus.padland.Adapters.PadSelectionTracker.MakesPadSelectionTracker
import com.mikifus.padland.Dialogs.Managers.IManagesNewPadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesNewPadGroupDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewPadGroupDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * This activity displays a list of previously checked documents.
 * Here documents can be deleted via Intent.
 * It handles as well the sharing intent to the app.
 *
 * @author mikifus
 */
class PadListActivity: AppCompatActivity(),
    IDragAndDropListener,
    IMakesPadSelectionTracker by MakesPadSelectionTracker(),
    IMakesPadGroupSelectionTracker by MakesPadGroupSelectionTracker(),
    IManagesNewPadGroupDialog by ManagesNewPadGroupDialog(),
    IManagesNewPadDialog by ManagesNewPadDialog() {

    private var isSelectionBlocked: Boolean = false
    override var padGroupViewModel: PadGroupViewModel? = null
    override var padViewModel: PadViewModel? = null

    private var mainList: List<PadGroupsWithPadList>? = null
    private var unclassifiedList: List<Pad>? = null

    private var recyclerView: RecyclerView? = null
    private var recyclerViewUnclassified: RecyclerView? = null
    private var titleViewUnclassified: View? = null

    private var adapter: PadGroupAdapter? = null
    private var padAdapter: PadAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pad_list)

        //initializing all the UI elements
        recyclerView = findViewById(R.id.recyclerview_padgroups)
        recyclerViewUnclassified = findViewById(R.id.recyclerview_unclassified)

        padGroupViewModel = ViewModelProvider(this)[PadGroupViewModel::class.java]
        adapter = PadGroupAdapter(this, this, getOnItemClickListener())

        padViewModel = ViewModelProvider(this)[PadViewModel::class.java]
        padAdapter = PadAdapter(this, this, getOnItemClickListener())

        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        ViewCompat.setNestedScrollingEnabled(recyclerView!!, false)

        recyclerViewUnclassified!!.adapter = padAdapter
        recyclerViewUnclassified!!.layoutManager = LinearLayoutManager(this)
        recyclerViewUnclassified!!.setOnDragListener(padAdapter!!.getDragInstance())
        ViewCompat.setNestedScrollingEnabled(recyclerViewUnclassified!!, false)

        // Unclassified list title
        titleViewUnclassified = findViewById(R.id.unclassified_title)

        initListView()
        initEvents()
    }

    private fun getOnItemClickListener(): View.OnClickListener {
        return View.OnClickListener{ view: View ->
            val padViewIntent = Intent(this, PadViewActivity::class.java)
            padViewIntent.putExtra("padId", view.tag as Long)
            startActivity(padViewIntent)
        }
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

    private fun initEvents() {
        val newPadGroupButton = findViewById<FloatingActionButton>(R.id.new_pad_group_button)
        newPadGroupButton.setOnClickListener {
            showNewPadGroupDialog(this@PadListActivity)
        }

        val newPadButton = findViewById<FloatingActionButton>(R.id.new_pad_button)
        newPadButton.setOnClickListener {
            val newPadIntent = Intent(this@PadListActivity, NewPadActivity::class.java)
//            startActivity(newPadIntent)
            showNewPadDialog(this@PadListActivity)

        }


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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.pad_list, menu)
        return true
    }

    /**
     * Manage the menu options when selected
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.action_settings -> {
                intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun setEmptyListTop(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setEmptyListBottom(visibility: Boolean) {
        TODO("Not yet implemented")
    }

    override fun notifyChange(padGroupId: Long, padId: Long, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            padGroupViewModel!!.deletePadGroupsAndPadList(padId)
            if(padGroupId > 0) {
                padGroupViewModel!!.insertPadGroupsAndPadList(
                    PadGroupsAndPadList(
                        mGroupId = padGroupId,
                        mPadId = padId,
                    )
                )
//                padViewModel!!.updatePadPosition(padId, position)
            }
        }
    }

    override fun getSelectionBlock(): Boolean {
        return isSelectionBlocked
    }

    override fun setSelectionBlock(value: Boolean) {
        isSelectionBlocked = value
    }

}