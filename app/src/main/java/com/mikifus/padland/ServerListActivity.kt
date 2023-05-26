package com.mikifus.padland

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.mikifus.padland.Adapters.ServerListAdapter
import com.mikifus.padland.Dialog.FormDialog.FormDialogCallBack
import com.mikifus.padland.Dialog.NewServerDialog
import com.mikifus.padland.Models.ServerModel

/**
 * Created by mikifus on 29/05/16.
 */
open class ServerListActivity : PadLandDataActivity(), ActionMode.Callback, FormDialogCallBack {
    /**
     * Multiple choice for all the groups
     */
    private val choiceMode = ListView.CHOICE_MODE_MULTIPLE

    /**
     * mActionMode defines behaviour of the action-bar
     */
    private var mActionMode: ActionMode? = null
    private var mAdapter: ArrayAdapter<*>? = null
    private var listView: ListView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_list)
        mAdapter = ServerListAdapter(this@ServerListActivity, R.layout.serverlist_item)
        listView = findViewById<View>(R.id.listView) as ListView
        if (listView != null) {
            listView!!.choiceMode = choiceMode
            listView!!.emptyView = findViewById(android.R.id.empty)
            listView!!.adapter = mAdapter
            _setListViewEvents()
        }
    }

    /**
     * This function adds events listeners for a ListView object to provide usage of the ActionBar
     */
    private fun _setListViewEvents() {
        // They look similar but they are different.
        listView!!.onItemLongClickListener = OnItemLongClickListener { parent, view, position, id ->
            startActionMode()
            val checked = listView!!.isItemChecked(position)
            listView!!.setItemChecked(position, !checked)
            view.isSelected = !checked
            if (listView!!.checkedItemCount == 0) {
                mActionMode!!.finish()
            }
            // Return true as we are handling the event.
            true
        }
        listView!!.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            if (mActionMode == null) {
                editServer(id)
                listView!!.setItemChecked(position, false)
                view.isSelected = false
            } else {
                val checked = listView!!.isItemChecked(position)
                listView!!.setItemChecked(position, checked)
                view.isSelected = checked
                if (listView!!.checkedItemCount == 0) {
                    mActionMode!!.finish()
                }
            }
            // Return true as we are handling the event.
            return@OnItemClickListener
        }
    }

    fun onNewServerClick(view: View?) {
        val fm = supportFragmentManager
        val dialog = NewServerDialog(getString(R.string.serverlist_dialog_new_server_title), this)
        dialog.show(fm, NEW_SERVER_DIALOG)
    }

    private fun editServer(id: Long) {
        val fm = supportFragmentManager
        val dialog = NewServerDialog(getString(R.string.serverlist_dialog_edit_server_title), this)
        dialog.editServerId(id)
        dialog.show(fm, NEW_SERVER_DIALOG)
    }

    /**
     * Check an item and set is as selected.
     *
     */
    private fun startActionMode() {
//        Log.d(TAG, "SELECTION NEW: pos:" + String.valueOf(position) + " id:" + String.valueOf(id));
//
        if (mActionMode == null) {
//            // Start the CAB using the ActionMode.Callback defined above
            this.startActionMode(this)
        }
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
        inflater.inflate(R.menu.server_list, menu)
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
     * Called when the user exits the action mode
     *
     * @param mode
     */
    override fun onDestroyActionMode(mode: ActionMode) {
        mActionMode = null
        uncheckAllItems()
    }

    private fun uncheckAllItems() {
        if (listView == null) {
            return
        }
        val checked = listView!!.checkedItemPositions
        for (i in 0 until checked.size()) {
            // Item position in adapter
            val position = checked.keyAt(i)
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i)) {
                listView!!.setItemChecked(position, false)
            }
        }
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
            R.id.menuitem_delete -> {
                askDelete(checkedItemIds)
                // Action picked, so close the CAB
                mode.finish()
                true
            }

            else -> false
        }
    }

    //        HashMap<Long, ArrayList<String>> padlist_data = _getPadListData();
    private val checkedItemIds: ArrayList<String?>
        get() {
            val selectedItems = ArrayList<String?>()
            //        HashMap<Long, ArrayList<String>> padlist_data = _getPadListData();
            val positions = listView!!.checkedItemPositions
            //        Log.d(TAG, "selectedItemsPositions: " + positions);
            for (i in 0 until positions.size()) {
                val position = positions.keyAt(i)
                if (positions.valueAt(i)) {
                    selectedItems.add(mAdapter!!.getItemId(position).toString())
                }
            }
            //        Log.d(TAG, "selectedItemsIds: " + selectedItems.toString());
            return selectedItems
        }

    override fun onDialogDismiss() {}
    override fun onDialogSuccess() {
        mAdapter!!.notifyDataSetChanged()
    }

    /**
     * Asks the user to confirm deleting a server.
     * If confirmed the info will be deleted.
     *
     * @param selectedItems
     * @return AlertDialog
     */
    override fun askDelete(selectedItems: ArrayList<String?>): AlertDialog {
        val deleteDialogBox = AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.serverlist_dialog_delete_sure_to_delete))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete) { dialog, whichButton ->
                    val serverModel = ServerModel(baseContext)
                    for (i in selectedItems.indices) {
                        Log.d("DELETE_SERVER", "list_get: " + selectedItems[i])
                        val result = selectedItems[i]?.let { serverModel.deleteServer(it.toLong()) }
                        if (result == true) {
                            Toast.makeText(baseContext, getString(R.string.serverlist_dialog_delete_server_deleted), Toast.LENGTH_LONG).show()
                            mAdapter!!.notifyDataSetChanged()
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                .create()
        deleteDialogBox.show()
        return deleteDialogBox
    }

    companion object {
        private const val NEW_SERVER_DIALOG = "dialog_new_server"
    }
}