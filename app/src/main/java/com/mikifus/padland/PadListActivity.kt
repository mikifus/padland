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
package com.mikifus.padland

import android.app.LoaderManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.ClipboardManager
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ExpandableListView
import android.widget.ListView
import android.widget.Toast
import com.mikifus.padland.Adapters.PadListAdapter
import com.mikifus.padland.Dialog.NewPadGroup
import com.mikifus.padland.Models.PadModel
import com.mikifus.padland.Utils.WhiteListMatcher

/**
 * This activity displays a list of previously checked documents.
 * Here documents can be deleted via Intent.
 * It handles as well the sharing intent to the app.
 *
 * @author mikifus
 * @since 0.1
 */
class PadListActivity : PadLandDataActivity(), ActionMode.Callback, LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * mActionMode defines behaviour of the action-bar
     */
    protected var mActionMode: ActionMode? = null

    /**
     * expandableListView
     */
    private var expandableListView: ExpandableListView? = null

    /**
     * Adapter to play with the expandableListView
     */
    private var adapter: PadListAdapter? = null

    /**
     * Override onCreate
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Layout
        super.onCreate(savedInstanceState)

        // Intent
        _actionFromIntent()
        setContentView(R.layout.activity_padlist)

        // Intent
        _textFromIntent()

        // Loader
        initLoader(this)

        // Init list view
        _initListView()
        _detectItemFocus()
    }

    /**
     * If there is a share intent this function gets the extra text
     * and copies it into clipboard.
     * If it is an URL then it can be opened in the PadView as far as
     * it is in the whitelist.
     */
    private fun _textFromIntent() {
        var extra_text = intent.getStringExtra(Intent.EXTRA_TEXT)
        if (extra_text != null) {
            extra_text = extra_text.trim { it <= ' ' }
            if (WhiteListMatcher.checkValidUrl(extra_text)
                    && WhiteListMatcher.isValidHost(extra_text, serverWhiteList)) {
                val i = Intent(this@PadListActivity, PadViewActivity::class.java)
                i.action = Intent.ACTION_VIEW
                i.data = Uri.parse(extra_text)
                //                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(extra_text));
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                //                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                //                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setPackage("com.mikifus.padland")
                //                finish();
                try {
                    startActivity(i)
                } catch (e: ActivityNotFoundException) {
//                    i.setPackage(null);
                    startActivity(i)
                }
                return
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.text = extra_text
            } else {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", extra_text)
                clipboard.setPrimaryClip(clip)
            }
            Toast.makeText(this, getString(R.string.activity_padlist_implicitintent_text_copied), Toast.LENGTH_LONG).show()
        }
    }

    private fun _actionFromIntent() {}

    /**
     * Override _deletePad() to let the adapter know about the
     * changes.
     *
     * @param pad_id_list
     */
    override fun _deletePad(pad_id_list: ArrayList<String>) {
        super._deletePad(pad_id_list)
        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun _detectItemFocus() {
        val extras = intent.extras ?: return
        val pad_id = extras.getLong(INTENT_FOCUS_PAD, 0)
        if (pad_id > 0) {
//            Bundle position = adapter.getPosition(pad_id);
//            expandableListView.smoothScrollToPosition(position.getInt("groupPosition"), position.getInt("childPosition"));
//            expandableListView.setSelectedChild(position.getInt("groupPosition"), position.getInt("childPosition"), true);
        }
    }

    /**
     * When the list is empty a message with a button is shown.
     * This handles the button onClick.
     *
     * @param view
     */
    fun onNewPadClick(view: View?) {
        val newPadIntent = Intent(this, NewPadActivity::class.java)
        startActivity(newPadIntent)
    }

    /**
     * When the list is empty a message with a button is shown.
     * This handles the button onClick.
     *
     * @param view
     */
    fun onNewPadgroupClick(view: View?) {
        showNewPadgroupDialog()
    }

    private fun showNewPadgroupDialog() {
        val fm = supportFragmentManager
        val dialog = NewPadGroup()
        dialog.show(fm, "dialog_new_padgroup")
    }

    /**
     * Makes an empty ListView and returns it.
     *
     * @return ListView
     */
    private fun _initListView() {
        expandableListView = findViewById<View>(R.id.listView) as ExpandableListView
        expandableListView!!.isTextFilterEnabled = true
        expandableListView!!.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        expandableListView!!.emptyView = findViewById(android.R.id.empty)

        // Set the data
        setAdapter()

        // events
        _setListViewEvents()
        adapter!!.notifyDataSetChanged()
    }

    /**
     * This function adds events listeners for a ListView object to provide usage of the ActionBar
     */
    private fun _setListViewEvents() {
        expandableListView!!.setOnChildClickListener { parent, v, groupPosition, childPosition, id ->
            Log.d(TAG, "child click: $id")

            // DO NOT try to handle selection with this listener, you will hurt yourself
            val padViewIntent = Intent(this@PadListActivity, PadInfoActivity::class.java)
            padViewIntent.putExtra("pad_id", id)
            startActivity(padViewIntent)
            true
        }
        expandableListView!!.onItemLongClickListener = OnItemLongClickListener { parent, view, position, id -> //  convert the input flat list position to a packed position
            val packedPosition = expandableListView!!.getExpandableListPosition(position)
            val itemType = ExpandableListView.getPackedPositionType(packedPosition)
            val groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition)
            val childPosition = ExpandableListView.getPackedPositionChild(packedPosition)
            Log.d(TAG, "Longclick: item: $itemType childpos:$childPosition pos:$position id:$id")

            //  GROUP-item clicked
            if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                val groupId = adapter!!.getGroupId(groupPosition)
                if (groupId > 0) {
                    menu_delete_group(groupId)
                    return@OnItemLongClickListener true
                }
            } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                startActionMode()
                val checked = expandableListView!!.isItemChecked(position)
                expandableListView!!.setItemChecked(position, !checked)
                view.isSelected = !checked
                if (expandableListView!!.checkedItemCount == 0) {
                    mActionMode!!.finish()
                }
                // Return true as we are handling the event.
                return@OnItemLongClickListener true
            }
            false
        }

        // If there was something selected, just in case, we start the actionmode to allow
        // the user cancel the previous selection at least.
        if (expandableListView!!.checkedItemCount > 0) {
            startActionMode()
        }
    }

    /**
     * Check an item and set is as selected.
     *
     */
    fun startActionMode() {
//        Log.d(TAG, "SELECTION NEW: pos:" + String.valueOf(position) + " id:" + String.valueOf(id));
//
        if (mActionMode == null) {
//            // Start the CAB using the ActionMode.Callback defined above
            this@PadListActivity.startActionMode(this@PadListActivity)
        }
    }

    /**
     * Gets an adapter for the expandableListView with the contents from the database
     *
     * @return
     */
    private fun setAdapter() {
//        ArrayList<HashMap<String, ArrayList>> group_data = getGroupsForAdapter();
//        HashMap<Long, ArrayList<String>> padlist_data = _getPadListData();
        adapter = PadListAdapter(this)

        // Bind to adapter.
        expandableListView!!.setAdapter(adapter)

        // Expand all groups by default
        for (i in 0 until adapter!!.groupCount) {
            expandableListView!!.expandGroup(i)
        }
    }
    //    private HashMap<Long, ArrayList<String>> _getPadListData()
    //    {
    //        Uri padlist_uri = Uri.parse(getString(R.string.request_padlist));
    //        Cursor cursor = getContentResolver()
    //                .query(padlist_uri,
    //                        new String[]{PadContentProvider._ID, PadContentProvider.NAME, PadContentProvider.LOCAL_NAME, PadContentProvider.URL},
    //                        null,
    //                        null,
    //                        PadContentProvider.LAST_USED_DATE + " ASC");
    //
    //        HashMap<Long, ArrayList<String>> result = new HashMap<>();
    //
    //        if (cursor == null || cursor.getCount() == 0) {
    //            return result;
    //        }
    //
    //        HashMap<Long, ArrayList<String>> pad_data = new HashMap<>();
    //
    //        cursor.moveToFirst();
    //        while (!cursor.isAfterLast())
    //        {
    //            long id = cursor.getLong(0);
    //            String name = cursor.getString(1);
    //            String local_name = cursor.getString(2);
    //            String url = cursor.getString(3);
    //
    //            ArrayList<String> pad_strings = new ArrayList<String>();
    //            pad_strings.add(name);
    //            pad_strings.add(local_name);
    //            pad_strings.add(url);
    //
    //            pad_data.put(id, pad_strings);
    //
    //            // do something
    //            cursor.moveToNext();
    //        }
    //        cursor.close();
    //
    //        return pad_data;
    //    }
    /**
     * Data loader initial event
     *
     * @param id
     * @param args
     * @return
     */
    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        val projection = arrayOf<String>(PadContentProvider.Companion._ID, PadModel.Companion.NAME, PadModel.Companion.URL)
        return CursorLoader(this, PadContentProvider.Companion.PADLIST_CONTENT_URI, projection, null, null, null)
    }

    /**
     * Data loader finish event
     *
     * @param loader
     * @param data
     */
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        setAdapter()
    }

    /**
     * Data loader event
     *
     * @param loader
     */
    override fun onLoaderReset(loader: Loader<Cursor>) {
        // data is not available anymore, delete reference
//        setAdapter();
        adapter = null
        expandableListView.setAdapter(adapter)
    }

    /**
     * Called when the action mode is created; startActionMode() was called
     *
     * @param mode
     * @param menu
     * @return boolean
     */
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        // Inflate a menu resource providing context menu items
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.rowselection, menu)
        mActionMode = mode
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
    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false // Return false if nothing is done
    }

    /**
     * Called when the user selects a contextual menu item
     *
     * @param mode
     * @param item
     * @return
     */
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuitem_group -> {
                menu_group(checkedItemIds)
                // Action picked, so close the CAB
                mode.finish()
                true
            }

            R.id.menuitem_copy -> {
                menu_copy(checkedItemIds)
                // Action picked, so close the CAB
                mode.finish()
                true
            }

            R.id.menuitem_edit -> {
                menu_edit(checkedItemIds)
                // Action picked, so close the CAB
                mode.finish()
                true
            }

            R.id.menuitem_delete -> {
                AskDelete(checkedItemIds)
                // Action picked, so close the CAB
                mode.finish()
                true
            }

            R.id.menuitem_share -> {
                menu_share(checkedItemIds)
                // Action picked, so close the CAB
                mode.finish()
                true
            }

            else -> false
        }
    }

    //        HashMap<Long, ArrayList<String>> padlist_data = _getPadListData();
    private val checkedItemIds: ArrayList<String?>
        private get() {
            val selectedItems = ArrayList<String?>()
            //        HashMap<Long, ArrayList<String>> padlist_data = _getPadListData();
            val positions = expandableListView!!.checkedItemPositions
            Log.d(TAG, "selectedItemsPositions: $positions")
            for (i in 0 until positions.size()) {
                val position = positions.keyAt(i)
                if (positions.valueAt(i)) {
                    val packed_position = expandableListView!!.getExpandableListPosition(position)
                    val group = ExpandableListView.getPackedPositionGroup(packed_position)
                    val child = ExpandableListView.getPackedPositionChild(packed_position)
                    Log.d(TAG, "selectedItemsPositions: g: " + group + "c: " + child)
                    if (child == -1) {
                        continue
                    }
                    selectedItems.add(adapter!!.getChildId(group, child).toString())
                }
            }
            Log.d(TAG, "selectedItemsIds: $selectedItems")
            return selectedItems
        }

    /**
     * Called when the user exits the action mode
     *
     * @param mode
     */
    override fun onDestroyActionMode(mode: ActionMode) {
        mActionMode = null
        uncheckAllItems()
    }

    private fun uncheckAllItems() {
        if (expandableListView == null) {
            return
        }
        val checked = expandableListView!!.checkedItemPositions
        for (i in 0 until checked.size()) {
            // Item position in adapter
            val position = checked.keyAt(i)
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i)) {
                expandableListView!!.setItemChecked(position, false)
            }
        }
    }

    /**
     * backbutton event
     */
    override fun onBackPressed() {
        onDestroyActionMode(mActionMode!!)
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu, R.menu.pad_list)
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    fun notifyDataSetChanged() {
        if (expandableListView != null) {
            setAdapter()
            //            ((BaseExpandableListAdapter) expandableListView.getExpandableListAdapter()).notifyDataSetChanged();
        }
    }

    override fun onDialogDismiss() {
        super.onDialogDismiss()
        notifyDataSetChanged()
    }

    companion object {
        const val TAG = "PadListActivity"
        const val INTENT_FOCUS_PAD = "focus_pad"
    }
}