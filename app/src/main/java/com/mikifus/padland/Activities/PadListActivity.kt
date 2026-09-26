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
import android.view.DragEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mikifus.padland.Adapters.PadAdapter
import com.mikifus.padland.Adapters.PadGroupAdapter
import com.mikifus.padland.Adapters.PadGroupSelectionTracker.IMakesPadGroupSelectionTracker
import com.mikifus.padland.Adapters.PadGroupSelectionTracker.MakesPadGroupSelectionTracker
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Adapters.DragAndDropListener.IDragAndDropListener
import com.mikifus.padland.Adapters.PadSelectionTracker.IMakesPadSelectionTracker
import com.mikifus.padland.Adapters.PadSelectionTracker.MakesPadSelectionTracker
import com.mikifus.padland.Dialogs.Managers.IManagesNewPadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesNewPadGroupDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewPadGroupDialog
import com.mikifus.padland.Dialogs.Managers.IManagesNewServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewServerDialog
import com.mikifus.padland.Utils.Import.PadClipboardImporter
import com.mikifus.padland.Utils.Import.PadUrlImport
import com.mikifus.padland.Utils.Sorting.PadListSortOrder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import androidx.activity.enableEdgeToEdge


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
    IManagesNewPadDialog by ManagesNewPadDialog(),
    IManagesNewServerDialog by ManagesNewServerDialog() {

    private var isSelectionBlocked: Boolean = false
    override var padGroupViewModel: PadGroupViewModel? = null
    override var padViewModel: PadViewModel? = null

    private val clipboardImporter by lazy { PadClipboardImporter(this) }
    private var isImportingFromClipboard: Boolean = false
    private var newPadButton: FloatingActionButton? = null

    private var mainList: List<PadGroupsWithPadList>? = null
    private var unclassifiedList: List<Pad>? = null

    private var recyclerView: RecyclerView? = null
    private var unclassifiedContainer: LinearLayout? = null
    private var recyclerViewUnclassified: RecyclerView? = null
    private var titleViewUnclassified: View? = null
    private var mEmptyLayout: LinearLayoutCompat? = null

    private var adapter: PadGroupAdapter? = null
    private var padAdapter: PadAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pad_list)
        setSupportActionBar(findViewById(R.id.activity_toolbar))

        //initializing all the content UI elements
        recyclerView = findViewById(R.id.recyclerview_padgroups)
        recyclerViewUnclassified = findViewById(R.id.recyclerview_unclassified)
        mEmptyLayout = findViewById(R.id.empty)

        padGroupViewModel = ViewModelProvider(this)[PadGroupViewModel::class.java]
        adapter = PadGroupAdapter(this, this,
            getOnItemClickListener(),
            getOnInfoClickListener())

        padViewModel = ViewModelProvider(this)[PadViewModel::class.java]
        padAdapter = PadAdapter(this, this,
            getOnItemClickListener(),
            getOnInfoClickListener())

        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        ViewCompat.setNestedScrollingEnabled(recyclerView!!, false)

        recyclerViewUnclassified!!.adapter = padAdapter
        recyclerViewUnclassified!!.layoutManager = LinearLayoutManager(this)
        ViewCompat.setNestedScrollingEnabled(recyclerViewUnclassified!!, false)

        // Unclassified container
        unclassifiedContainer = findViewById(R.id.unclassified_container)
        unclassifiedContainer!!.setOnDragListener(padAdapter!!.getDragInstance())
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

    private fun getOnInfoClickListener(): View.OnClickListener {
        return View.OnClickListener{ view: View ->
            val padViewIntent = Intent(this, PadInfoActivity::class.java)
            padViewIntent.putExtra("padId", view.tag as Long)
            startActivity(padViewIntent)
        }
    }

    private fun initListView() {
        initSelectionTrackers()

        padGroupViewModel!!.getSortedPadGroupsWithPadList.observe(this) { currentList ->
            mainList = currentList ?: listOf()
            adapter!!.data = mainList!!
            showHideEmpty()
        }

        padGroupViewModel!!.getSortedPadsWithoutGroup.observe(this) { currentList ->
            unclassifiedList = currentList ?: listOf()
            padAdapter!!.data = unclassifiedList!!
            showHideUnclassified()
            showHideEmpty()
        }
    }

    private fun initSelectionTrackers() {
        padAdapter!!.tracker = makePadSelectionTracker(this, recyclerViewUnclassified!!, padAdapter!!)
        adapter!!.tracker = makePadGroupSelectionTracker(this, recyclerView!!, adapter!!)
    }

    private fun initEvents() {
        val scrollView = findViewById<NestedScrollView>(R.id.scroll_view)
        val newPadGroupButton = findViewById<FloatingActionButton>(R.id.button_new_pad_group)
        val newPadButton = findViewById<FloatingActionButton>(R.id.button_new_pad)
        val emptyButton = findViewById<MaterialButton>(R.id.empty_button_createnew)

        newPadGroupButton.setOnClickListener {
            finishAllActionModes()
            showNewPadGroupDialog(this@PadListActivity, newPadGroupButton)
        }

        newPadButton.setOnClickListener {
            finishAllActionModes()
            showNewPadDialog(this@PadListActivity, newPadButton)
        }

        // Long press: import a list of URLs (one per line) from the clipboard
        this.newPadButton = newPadButton
        newPadButton.setOnLongClickListener {
            finishAllActionModes()
            importPadsFromClipboard()
            true
        }

        emptyButton?.setOnClickListener {
            finishAllActionModes()
            showNewPadDialog(this@PadListActivity, emptyButton)
        }

        titleViewUnclassified!!.isActivated = true
        titleViewUnclassified!!.setOnClickListener {
            toggleUnclassifiedRecyclerView()
        }
    }

    private fun toggleUnclassifiedRecyclerView() {
        if(recyclerViewUnclassified != null) {
            val transition: Transition = AutoTransition()
            transition.duration = 200
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

    private fun showHideUnclassified() {
        if(unclassifiedList?.size == 0) {
            unclassifiedContainer?.visibility = View.GONE
        } else {
            unclassifiedContainer?.visibility = View.VISIBLE
        }
    }

    private fun showHideEmpty() {
        if(unclassifiedList?.size == 0 && mainList?.size == 0) {
            mEmptyLayout?.visibility = View.VISIBLE
        } else {
            mEmptyLayout?.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pad_list, menu)
        menu.findItem(R.id.action_sort)?.subMenu?.let {
            MenuCompat.setGroupDividerEnabled(it, true)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateSortMenuChecks(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Reflects the persisted sort choices in the sort submenu radio items.
     */
    private fun updateSortMenuChecks(menu: Menu) {
        val groupSortOrder = padGroupViewModel?.groupSortOrder?.value ?: PadListSortOrder.DEFAULT
        val padSortOrder = padGroupViewModel?.padSortOrder?.value ?: PadListSortOrder.DEFAULT

        menu.findItem(
            when (groupSortOrder) {
                PadListSortOrder.ALPHABETICAL -> R.id.action_sort_groups_alphabetical
                PadListSortOrder.LAST_ACCESS -> R.id.action_sort_groups_last_access
            }
        )?.isChecked = true

        menu.findItem(
            when (padSortOrder) {
                PadListSortOrder.ALPHABETICAL -> R.id.action_sort_pads_alphabetical
                PadListSortOrder.LAST_ACCESS -> R.id.action_sort_pads_last_access
            }
        )?.isChecked = true
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

            R.id.action_sort_groups_alphabetical -> {
                item.isChecked = true
                padGroupViewModel?.setGroupSortOrder(PadListSortOrder.ALPHABETICAL)
            }

            R.id.action_sort_groups_last_access -> {
                item.isChecked = true
                padGroupViewModel?.setGroupSortOrder(PadListSortOrder.LAST_ACCESS)
            }

            R.id.action_sort_pads_alphabetical -> {
                item.isChecked = true
                padGroupViewModel?.setPadSortOrder(PadListSortOrder.ALPHABETICAL)
            }

            R.id.action_sort_pads_last_access -> {
                item.isChecked = true
                padGroupViewModel?.setPadSortOrder(PadListSortOrder.LAST_ACCESS)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Clipboard import
     */
    private fun importPadsFromClipboard() {
        if (isImportingFromClipboard) return

        // Read now, on the main thread and while focused, as Android requires
        val text = clipboardImporter.readClipboard()
        runImport { clipboardImporter.importText(text) }
    }

    private fun runImport(block: suspend () -> PadClipboardImporter.Result?) {
        if (isImportingFromClipboard) return
        isImportingFromClipboard = true

        lifecycleScope.launch {
            try {
                val result = block()
                if (result == null) {
                    Toast.makeText(this@PadListActivity,
                        getString(R.string.import_clipboard_no_urls), Toast.LENGTH_LONG).show()
                } else {
                    showImportSummary(result)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(this@PadListActivity,
                    getString(R.string.unexpected_error), Toast.LENGTH_LONG).show()
            } finally {
                isImportingFromClipboard = false
            }
        }
    }

    /**
     * Toast with the summary. If some URLs belong to unknown servers, a snackbar
     * with the same summary is shown instead, with an action to add the server.
     */
    private fun showImportSummary(result: PadClipboardImporter.Result) {
        val summary = getString(R.string.import_clipboard_summary,
            result.imported, result.ignored, result.failed)

        val anchor = newPadButton
        if (result.unknownHostUrls.isEmpty() || anchor == null) {
            Toast.makeText(this, summary, Toast.LENGTH_LONG).show()
            return
        }

        val unknownHosts = result.unknownHostUrls
            .mapNotNull { PadUrlImport.hostOf(it) }
            .distinct()
        val message = summary + "\n" + getString(R.string.import_clipboard_unknown_servers,
            result.unknownHostUrls.size, unknownHosts.joinToString(", "))

        Snackbar.make(anchor, message, IMPORT_SNACKBAR_DURATION)
            .setAnchorView(anchor)
            .setTextMaxLines(4)
            .setAction(R.string.import_clipboard_add_server) {
                addServerAndRetryImport(result.unknownHostUrls)
            }
            .show()
    }

    /**
     * Opens the new server dialog prefilled with the first unknown URL. Once saved, the
     * pending URLs are imported again: the ones from the new server get imported and,
     * if other unknown servers remain, the snackbar is offered again.
     */
    private fun addServerAndRetryImport(unknownHostUrls: List<String>) {
        showNewServerDialog(this, unknownHostUrls.first(), onSavedCallBack = {
            runImport { clipboardImporter.importUrls(unknownHostUrls) }
        })
    }

    /**
     * Selection Trackers
     */
    override fun getSelectionBlock(): Boolean {
        return isSelectionBlocked
    }

    override fun setSelectionBlock(value: Boolean) {
        isSelectionBlocked = value
    }

    private fun finishAllActionModes() {
        padActionMode?.finish()
        padGroupActionMode?.finish()
    }


    /**
     * Drag and drop listener.
     */
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

    override fun onEnteredView(view: View, event: DragEvent) {
        view.background = ContextCompat.getDrawable(this, R.drawable.dashed_border)
    }

    override fun onExitedView(view: View, event: DragEvent) {
        view.background = ContextCompat.getDrawable(this, R.drawable.background_selector)
    }

    companion object {
        /** Long enough to read the summary and tap "Add server" */
        private const val IMPORT_SNACKBAR_DURATION = 10_000
    }
}