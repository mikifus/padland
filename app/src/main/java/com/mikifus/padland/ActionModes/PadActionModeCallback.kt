package com.mikifus.padland.ActionModes

import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialog.FormDialog
import com.mikifus.padland.Dialogs.Managers.IManagesDeletePadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesEditPadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesGroupPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDeletePadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesEditPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesGroupPadDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadClipboardHelper
import com.mikifus.padland.Utils.PadShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PadActionModeCallback(activity: PadListActivity): ActionMode.Callback,
    FormDialog.FormDialogCallBack,
    IManagesEditPadDialog by ManagesEditPadDialog(),
    IManagesDeletePadDialog by ManagesDeletePadDialog(),
    IManagesGroupPadDialog by ManagesGroupPadDialog() {

    override var padGroupViewModel: PadGroupViewModel? = null
    override var padViewModel: PadViewModel? = null
    private var padActionMode: ActionMode? = null
    private var padListActivity: PadListActivity

    init {
        padListActivity = activity
        initViewModels(activity)
    }

    private fun initViewModels(activity: PadListActivity) {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(activity)[PadViewModel::class.java]
        }
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
            R.id.menuitem_group -> {
                showGroupPadDialog(padListActivity, padListActivity.getPadSelection())
                mode?.finish()
                true
            }
            R.id.menuitem_copy -> {
                copyToClipboardPads(padListActivity.getPadSelection())
                mode?.finish()
                true
            }
            R.id.menuitem_edit -> {
                showEditPadDialog(padListActivity, padListActivity.getPadSelection()[0])
                mode?.finish()
                true
            }
            R.id.menuitem_delete -> {
                showDeletePadDialog(padListActivity, padListActivity.getPadSelection())
                mode?.finish()
                true
            }
            R.id.menuitem_share -> {
                sharePads(padListActivity.getPadSelection())
                mode?.finish()
                true
            }
            else -> false
        }
    }

    private fun sharePads(ids: List<Long>) {
        padListActivity.lifecycleScope.launch {
            val pads = padViewModel?.getByIds(ids)

            if (!pads.isNullOrEmpty()) {
                padListActivity.lifecycleScope.launch(Dispatchers.Main) {
                    PadShareHelper.share(
                        padListActivity,
                        padListActivity.getString(R.string.share_auto_text),
                        pads.map { it.mUrl }
                    )
                }
            }
        }
    }

    private fun copyToClipboardPads(ids: List<Long>) {
        padListActivity.lifecycleScope.launch {
            val pads = padViewModel?.getByIds(ids)

            if (!pads.isNullOrEmpty()) {
                padListActivity.lifecycleScope.launch(Dispatchers.Main) {
                    PadClipboardHelper.copyToClipboard(padListActivity, pads.map { it.mUrl })

                    Toast.makeText(
                        padListActivity,
                        padListActivity.getString(R.string.copy_copied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        if(padActionMode != null) {
            padActionMode = null
            padListActivity.onDestroyPadActionMode()
        }
    }

    override fun onDialogDismiss() {
        TODO("Not yet implemented")
    }

    override fun onDialogSuccess() {
        TODO("Not yet implemented")
    }
}