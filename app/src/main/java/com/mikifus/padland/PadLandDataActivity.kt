package com.mikifus.padland

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import com.mikifus.padland.Dialog.EditPadDialog
import com.mikifus.padland.Dialog.FormDialog
import com.mikifus.padland.Dialog.FormDialog.FormDialogCallBack
import com.mikifus.padland.Models.Pad
import com.mikifus.padland.Models.PadGroupModel
import com.mikifus.padland.Models.PadModel
import com.mikifus.padland.Models.ServerModel
import java.net.MalformedURLException
import java.net.URL
import java.util.Arrays

/**
 * A data activity inherits from the main activity and provides methods
 * to insert, update and delete the documents data. Each activity that
 * deals with data must either inherit from this or make an intent
 * to another activity which does.
 */
open class PadLandDataActivity : PadLandActivity(), FormDialogCallBack {
    var padlistDb: PadlistDb? = null
    private val meta_groups = ArrayList<HashMap<String, String>>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        padlistDb = PadlistDb(contentResolver)
        Log.d(TAG, "Data activity started running")
        val unclassified_group = HashMap<String, String>()
        unclassified_group[PadContentProvider.Companion._ID] = "0"
        unclassified_group[PadModel.Companion.NAME] = "Unclassified"
        unclassified_group[PadGroupModel.Companion.POSITION] = "999999"
        meta_groups.add(unclassified_group)
    }

    /**
     * It gets the pad id from an intent if there is such info, else 0
     * @return
     */
    open fun _getPadId(): Long {
        val myIntent = intent
        return myIntent.getLongExtra("pad_id", 0)
    }

    /**
     * Gets back a Pad object.
     * @param pad_id
     * @return
     */
    fun _getPad(pad_id: Long): Pad {
        val cursor = padlistDb!!._getPadById(pad_id)
        cursor!!.moveToFirst()
        val pad_data = Pad(cursor)
        cursor.close()
        return pad_data
    }

    /**
     * Gets back a Pad array object.
     * @return
     */
    fun _getPads(): HashMap<Long, Pad> {
        val PadHashMap = HashMap<Long, Pad>()
        val Pads = padlistDb!!._getAllPad()
        for (Pad in Pads) {
            PadHashMap[Pad.id] = Pad
        }
        return PadHashMap
    }

    /**
     * Asks the user to confirm deleting a document.
     * If confirmed, will make an intent to PadListActivity, where the info will be
     * deleted.
     * @param selectedItems
     * @return AlertDialog
     */
    open fun AskDelete(selectedItems: ArrayList<String>): AlertDialog {
        val activity: Context = this
        val DeleteDialogBox = AlertDialog.Builder(this) //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete_pad))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete) { dialog, whichButton ->
                    _deletePad(selectedItems)
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                .create()
        DeleteDialogBox.show()
        return DeleteDialogBox
    }

    /**
     * Delete a pad list when selected.
     *
     * @param pad_id_list
     */
    open fun _deletePad(pad_id_list: ArrayList<String>) {
        if (pad_id_list.size > 0) {
            for (i in pad_id_list.indices) {
                Log.d("DELETE_PAD", "list_get: " + pad_id_list[i])
                val result = padlistDb!!.deletePad(pad_id_list[i].toLong())
                if (result) {
                    Toast.makeText(this, getString(R.string.padlist_document_deleted), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Asks the user to confirm deleting a document.
     * If confirmed, will make an intent to PadListActivity, where the info will be
     * deleted.
     * @return AlertDialog
     */
    protected fun getGroupIdFromAdapterData(groupPosition: Int): Long {
        val model = PadGroupModel(this)
        val padgroups_data = model.getPadgroupAt(groupPosition)
        var id = 0L
        if (padgroups_data!!.size > 0) {
            id = padgroups_data[PadContentProvider.Companion._ID]!!.toLong()
        }
        return id
    }

    protected fun getGroupNameFromAdapterData(groupPosition: Int): String? {
        val model = PadGroupModel(this)
        val padgroups_data = model.getPadgroupAt(groupPosition)
        var name: String? = ""
        if (padgroups_data!!.size > 0) {
            name = padgroups_data[PadModel.Companion.NAME]
        }
        return name
    }

    fun menu_copy(selectedItems: ArrayList<String>) {
        val copy_string = StringBuilder()
        for (pad_id_string in selectedItems) {
            val Pad = _getPad(pad_id_string.toLong())
            if (copy_string.length > 0) {
                copy_string.append("\n")
            }
            copy_string.append(Pad.url)
        }
        val clipboard = baseContext.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Pad urls", copy_string)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this@PadLandDataActivity, getString(R.string.copy_copied), Toast.LENGTH_LONG).show()
    }

    fun menu_edit(selectedItems: ArrayList<String>): FormDialog {
        val fm = supportFragmentManager
        val dialog = EditPadDialog(getString(R.string.padlist_dialog_edit_pad_title), this)
        dialog.editPadId(selectedItems[0].toLong())
        dialog.show(fm, EDIT_PAD_DIALOG)
        return dialog
    }

    fun menu_group(selectedItems: ArrayList<String>): AlertDialog {
        val context = this
        val selectedGroups = ArrayList<Long>()
        val padGroupModel = PadGroupModel(this)
        val group_count = padGroupModel.padgroupsCount
        val group_names = arrayOfNulls<String>(group_count + 1)
        for (i in 0 until group_count) {
            group_names[i] = getGroupNameFromAdapterData(i)
        }

        // Add unclassified group as choice
        group_names[group_count] = getString(R.string.padlist_group_unclassified_name)
        val checkboxStatusArray = BooleanArray(group_count + 1)
        val DeleteDialogBox = AlertDialog.Builder(this) //set message, title, and icon
                .setTitle(R.string.padlist_group_select_dialog)
                .setIcon(R.drawable.ic_group_add)
                .setMultiChoiceItems(group_names, checkboxStatusArray
                ) { dialog, which, isChecked ->
                    if (isChecked) {

                        // Noew clean and set the view
                        val listView = (dialog as AlertDialog).listView
                        //                                SparseBooleanArray checked = listView.getCheckedItemPositions();
                        for (i in checkboxStatusArray.indices) {
                            if (checkboxStatusArray[i] && i != which) {
                                checkboxStatusArray[i] = false
                                listView.setItemChecked(i, false)
                            }
                        }

                        // clean to just select one
                        selectedGroups.clear()

                        // If the user checked the item, add it to the selected items
                        val group_id = context.getGroupIdFromAdapterData(which)
                        selectedGroups.add(group_id)
                    } else if (selectedGroups.contains(which)) {
                        // Else, if the item is already in the array, remove it
//                                selectedGroups.remove(Integer.valueOf(which));
                        (dialog as AlertDialog).listView.setItemChecked(which, true)
                    }
                }
                .setPositiveButton(R.string.ok) { dialog, whichButton ->
                    var save_pad_id: Long
                    val padGroupModel = PadGroupModel(context)
                    for (pad_id_string in selectedItems) {
                        save_pad_id = pad_id_string.toLong()
                        for (save_padgroup_id in selectedGroups) {
                            val saved = context.padlistDb!!.savePadgroupRelation(save_padgroup_id, save_pad_id)
                            Log.d(TAG, "Added to group? $saved")
                        }
                    }
                    (context as PadListActivity).notifyDataSetChanged()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                .create()
        DeleteDialogBox.show()
        return DeleteDialogBox
    }

    fun menu_delete_group(group_id: Long): AlertDialog {
        val context = this
        val DeleteDialogBox = AlertDialog.Builder(this) //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete_group))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete) { dialog, whichButton ->
                    if (context.padlistDb!!.deleteGroup(group_id)) {
                        Toast.makeText(this@PadLandDataActivity, getString(R.string.padlist_group_deleted), Toast.LENGTH_LONG).show()
                    }
                    (context as PadListActivity).notifyDataSetChanged()
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
                .create()
        DeleteDialogBox.show()
        return DeleteDialogBox
    }

    /**
     * Menu to share a document url
     * TODO: Share multiple pads.
     * @param selectedItems
     */
    fun menu_share(selectedItems: ArrayList<String>) {
        Log.d("PadLandDataActivity", selectedItems[0])
        // Only the first one
        val selectedItem_id = selectedItems[0]
        val Pad = _getPad(selectedItem_id.toLong())
        val padUrl = Pad.url
        Log.d("SHARING_PAD", "$selectedItem_id - $padUrl")
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_auto_text) + padUrl)
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.share_document)))
    }

    override fun onCreateOptionsMenu(menu: Menu?, id_menu: Int): Boolean {
        return super.onCreateOptionsMenu(menu, id_menu)
    }

    override fun onDialogDismiss() {}
    override fun onDialogSuccess() {}
    inner class PadlistDb(var contentResolver: ContentResolver) {
        var db: SQLiteDatabase

        init {
            val padlandDbHelper = PadlandDbHelper(this@PadLandDataActivity)
            db = padlandDbHelper.writableDatabase
        }

        /**
         * Self explanatory name.
         * Field to compare must be specified by its identifier. Accepts only one comparation value.
         * @param field
         * @param comparation
         * @return
         */
        private fun _getPadFromDatabase(field: String, comparation: String?): Cursor? {
            val c: Cursor?
            val comparation_set = arrayOf(comparation)

            // I have to use LIKE in order to query by ID. A mistery.
            c = contentResolver.query(
                    PadContentProvider.Companion.PADLIST_CONTENT_URI,
                    PadContentProvider.Companion.getPadFieldsList(),
                    "$field LIKE ?",
                    comparation_set,  // AKA id
                    null
            )
            return c
        }

        /**
         * Self explanatory name.
         * Just get all.
         * @return
         */
        private fun _getPadFromDatabase(): Cursor? {
            val c: Cursor?
            c = contentResolver.query(
                    PadContentProvider.Companion.PADLIST_CONTENT_URI,
                    PadContentProvider.Companion.getPadFieldsList(),
                    null,
                    null,  // AKA id
                    null
            )
            return c
        }

        /**
         * Queries the database and returns all pads
         * @return
         */
        fun _getAllPad(): ArrayList<Pad> {
            val cursor = this._getPadFromDatabase()
            val Pads = ArrayList<Pad>()
            if (cursor == null) {
                return Pads
            }
            if (cursor.count == 0) {
                cursor.close()
                return Pads
            }
            var Pad: Pad
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                // Goes to next by itself
                Pad = Pad(cursor)
                Pads.add(Pad)
                cursor.moveToNext()
            }
            cursor.close()
            return Pads
        }

        /**
         * Queries the database and compares to pad_id
         * @param pad_id
         * @return
         */
        fun _getPadById(pad_id: Long): Cursor? {
            return this._getPadFromDatabase(PadModel.Companion._ID, pad_id.toString())
        }

        /**
         * Queries the database and compares to padUrl
         * @param padUrl
         * @return
         */
        fun _getPadByUrl(padUrl: String?): Cursor? {
            return this._getPadFromDatabase(PadModel.Companion.URL, padUrl)
        }

        val nowDate: Long
            get() = PadContentProvider.Companion.getNowDate()

        /**
         * Gets current pad data and saves the modified values (LAST_USED_DATE and ACCESS_COUNT).
         * I tried to optimize it in such way that there's no need to use _getPad, but it didn't work.
         * @param pad_id
         * @return
         */
        fun accessUpdate(pad_id: Long) {
            if (pad_id > 0) {
                val data = _getPad(pad_id)
                val values = ContentValues()
                values.put(PadContentProvider.Companion.LAST_USED_DATE, nowDate)
                values.put(PadContentProvider.Companion.ACCESS_COUNT, data.accessCount + 1)
                val where_value = arrayOf(pad_id.toString())
                contentResolver.update(PadContentProvider.Companion.PADLIST_CONTENT_URI, values, PadContentProvider.Companion._ID + "=?", where_value)
            }
        }

        fun _debug_relations() {
            val QUERY = "SELECT " + PadContentProvider.Companion._ID_GROUP + ", " + PadContentProvider.Companion._ID_PAD + " FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME
            val values = arrayOf<String>()
            val cursor = db.rawQuery(QUERY, values)
            val hashMap = HashMap<Long, Long>()
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                hashMap[cursor.getLong(0)] = cursor.getLong(1)
                cursor.moveToNext()
            }
            cursor.close()
            Log.d(TAG, hashMap.toString())
        }

        /**
         * Saves a new group if padgroup_id=0 or updates an existing one.
         * @param padgroup_id
         * @param values
         * @return
         */
        fun savePadgroupData(padgroup_id: Long, values: ContentValues): Boolean {
            return if (padgroup_id > 0) {
                val where_value = arrayOf(padgroup_id.toString())
                val result = contentResolver.update(PadContentProvider.Companion.PADGROUPS_CONTENT_URI, values, PadContentProvider.Companion._ID + "=?", where_value)
                result > 0
            } else {
                Log.d("INSERT", "Contents = $values")
                val result = contentResolver.insert(PadContentProvider.Companion.PADGROUPS_CONTENT_URI, values)
                result != null
            }
        }

        /**
         * Saves a new group if padgroup_id=0 or updates an existing one.
         * @param padgroup_id
         * @param pad_id
         * @return
         */
        fun savePadgroupRelation(padgroup_id: Long, pad_id: Long): Boolean {
            removePadFromAllGroups(pad_id)
            if (padgroup_id == 0L) {
                return false
            }
            val contentValues = ContentValues()
            contentValues.put(PadContentProvider.Companion._ID_PAD, pad_id)
            contentValues.put(PadContentProvider.Companion._ID_GROUP, padgroup_id)
            //            _debug_relations();
            return db.insert(PadContentProvider.Companion.RELATION_TABLE_NAME, null, contentValues) > 0
        }

        /**
         * Destroys all possible relation between a pad and any group
         * @param pad_id
         * @return
         */
        fun removePadFromAllGroups(pad_id: Long): Boolean {
            val deleted = db.delete(PadContentProvider.Companion.RELATION_TABLE_NAME, PadContentProvider.Companion._ID_PAD + "=? ", arrayOf<String>(pad_id.toString()))
            return deleted > 0
        }

        /**
         * Deletes a pad by its id, no confirmation, won't be recoverable
         * @param pad_id
         * @return
         */
        fun deletePad(pad_id: Long): Boolean {
            return if (pad_id > 0) {
                val result = contentResolver.delete(PadContentProvider.Companion.PADLIST_CONTENT_URI, PadContentProvider.Companion._ID + "=?", arrayOf<String>(pad_id.toString()))
                result > 0
            } else {
                throw IllegalArgumentException("Pad id is not valid")
            }
        }

        /**
         * Deletes a group by its id, no confirmation, won't be recoverable.
         * The group pads will be moved to the zero-group (Unclassified)
         * @param group_id
         * @return
         */
        fun deleteGroup(group_id: Long): Boolean {
            return if (group_id > 0) {
                emptyGroup(group_id)
                val result = db.delete(PadContentProvider.Companion.PADGROUP_TABLE_NAME, PadContentProvider.Companion._ID + "=?", arrayOf<String>(group_id.toString()))
                result > 0
            } else {
                throw IllegalArgumentException("Group id is not valid")
            }
        }

        /**
         * Erases all relations with stablished with this group
         * @param group_id
         * @return
         */
        fun emptyGroup(group_id: Long): Boolean {
            val result = db.delete(PadContentProvider.Companion.RELATION_TABLE_NAME, PadContentProvider.Companion._ID_GROUP + "=?", arrayOf<String>(group_id.toString()))
            return result > 0
        }

        fun close() {
            if (db.isOpen) {
                db.close()
            }
        }
    }// Load the custom servers

    /**
     * Retrieves a list of all hosts both from the XML default list
     * and the database.
     *
     * @return
     */
    protected val serverWhiteList: Array<String>
        protected get() {
            val server_list: Array<String>
            // Load the custom servers
            val serverModel = ServerModel(this)
            val custom_servers = serverModel.enabledServerList
            val server_names = ArrayList<String>()
            for (server in custom_servers!!) {
                try {
                    val url = URL(server.getUrl())
                    server_names.add(url.host)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
            }
            val collection: MutableCollection<String> = ArrayList()
            collection.addAll(server_names)
            collection.addAll(Arrays.asList(*resources.getStringArray(R.array.etherpad_servers_whitelist)))
            server_list = collection.toTypedArray()
            return server_list
        }

    override fun onDestroy() {
        super.onDestroy()
        if (padlistDb != null) {
            padlistDb!!.close()
        }
    }

    companion object {
        private const val TAG = "PadLandDataActivity"
        private const val EDIT_PAD_DIALOG = "EditPadDialog"
    }
}