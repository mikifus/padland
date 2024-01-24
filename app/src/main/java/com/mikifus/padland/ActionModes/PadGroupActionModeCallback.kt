package com.mikifus.padland.ActionModes

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.R

class PadGroupActionModeCallback(activity: PadListActivity): ActionMode.Callback {

    var padGrupActionMode: ActionMode? = null
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
//        // Inflate a menu resource providing context menu items
//        if(mode != null) {
//            val inflater = mode.menuInflater
//            inflater.inflate(R.menu.rowselection, menu)
//            padGrupActionMode = mode
//        }
//        return true
        return false
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
        padGrupActionMode = null
        padListActivity.clearPadGroupSelection()
    }
}