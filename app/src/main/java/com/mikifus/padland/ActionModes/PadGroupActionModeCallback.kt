package com.mikifus.padland.ActionModes

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Dialogs.Managers.IManagesDeletePadGroupDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDeletePadGroupDialog
import com.mikifus.padland.R

class PadGroupActionModeCallback(activity: PadListActivity):
        ActionMode.Callback,
        IManagesDeletePadGroupDialog by ManagesDeletePadGroupDialog() {

    private var padGrupActionMode: ActionMode? = null
    private var padListActivity: PadListActivity

    init {
        padListActivity = activity
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
            inflater.inflate(R.menu.padgroup_action_mode_menu, menu)
            padGrupActionMode = mode
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
            R.id.menuitem_edit -> {
                menuEdit(padListActivity.getPadGroupSelection())
                mode?.finish()
                true
            }

            R.id.menuitem_delete -> {
                showDeletePadGroupDialog(padListActivity, padListActivity.getPadGroupSelection())
                mode?.finish()
                true
            }

            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        if(padGrupActionMode != null) {
            padGrupActionMode = null
            padListActivity.onDestroyPadGroupActionMode()
        }
    }

    private fun menuEdit(selectedItems: List<Long> = listOf())/*: Fragment*/ {
//        val context = this
//        val selectedGroups = ArrayList<Long>()
//        val padGroupModel = PadGroupModel(this)
//        val groupCount = padGroupModel.padgroupsCount
//        val groupNames = arrayOfNulls<String>(groupCount + 1)
//        for (i in 0 until groupCount) {
//            groupNames[i] = padListActivity.getGroupNameFromAdapterData(i)
//        }
//
//        // Add unclassified group as choice
//        groupNames[groupCount] = getString(R.string.padlist_group_unclassified_name)
//        val checkboxStatusArray = BooleanArray(groupCount + 1)
//        val deleteDialogBox = AlertDialog.Builder(this) //set message, title, and icon
//            .setTitle(R.string.padlist_group_select_dialog)
//            .setIcon(R.drawable.ic_group_add)
//            .setMultiChoiceItems(groupNames, checkboxStatusArray
//            ) { dialog, which, isChecked ->
//                if (isChecked) {
//
//                    // Now clean and set the view
//                    val listView = (dialog as AlertDialog).listView
//                    for (i in checkboxStatusArray.indices) {
//                        if (checkboxStatusArray[i] && i != which) {
//                            checkboxStatusArray[i] = false
//                            listView.setItemChecked(i, false)
//                        }
//                    }
//
//                    // clean to just select one
//                    selectedGroups.clear()
//
//                    // If the user checked the item, add it to the selected items
//                    val groupId = context.getGroupIdFromAdapterData(which)
//                    selectedGroups.add(groupId)
//                } else if (selectedGroups.contains(which.toLong())) {
//                    // Else, if the item is already in the array, remove it
//                    (dialog as AlertDialog).listView.setItemChecked(which, true)
//                }
//            }
//            .setPositiveButton(R.string.ok) { dialog, whichButton ->
//                var savePadId: Long
//                PadGroupModel(context)
//                for (pad_id_string in selectedItems) {
//                    savePadId = pad_id_string!!.toLong()
//                    for (save_padgroup_id in selectedGroups) {
////                        val saved = context.padlistDb!!.savePadgroupRelation(save_padgroup_id, savePadId)
////                        Log.d(PadLandDataActivity.TAG, "Added to group? $saved")
//                    }
//                }
//                (context as com.mikifus.padland.PadListActivity).notifyDataSetChanged()
//                dialog.dismiss()
//            }
//            .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }
//            .create()
//        deleteDialogBox.show()
//        return deleteDialogBox
    }
}