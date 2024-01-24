package com.mikifus.padland.ActionModes

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Dialog.EditPadDialog
import com.mikifus.padland.Dialog.FormDialog
import com.mikifus.padland.PadLandDataActivity
import com.mikifus.padland.R

class PadActionModeCallback(activity: PadListActivity): ActionMode.Callback,
    FormDialog.FormDialogCallBack {

    var padActionMode: ActionMode? = null
    var padListActivity: PadListActivity

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
            inflater.inflate(R.menu.pad_action_mode_menu, menu)
            padActionMode = mode
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
            R.id.menuitem_edit -> {
                menuEdit(padListActivity.getPadSelection())
                // Action picked, so close the CAB
                mode?.finish()
                true
            }
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
        padActionMode = null
        padListActivity.onDestroyPadActionMode()
    }

    fun menuEdit(selection: List<Long>): Fragment {
        val fm = padListActivity.supportFragmentManager
        val dialog = EditPadDialog(padListActivity.getString(R.string.padlist_dialog_edit_pad_title), this)
        selection[0].let { dialog.editPadId(it.toLong()) }
        dialog.show(fm, PadLandDataActivity.EDIT_PAD_DIALOG)
        return dialog
    }

    override fun onDialogDismiss() {
        TODO("Not yet implemented")
    }

    override fun onDialogSuccess() {
        TODO("Not yet implemented")
    }
}