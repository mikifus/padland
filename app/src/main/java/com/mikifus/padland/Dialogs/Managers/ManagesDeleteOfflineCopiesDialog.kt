package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.Offline.OfflinePadStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IManagesDeleteOfflineCopiesDialog {
    /**
     * Asks for confirmation and deletes all the offline copies.
     * The pads keep their offline access, a new copy is saved when leaving them.
     * [onDeletedCallback] is called on the main thread once they are deleted.
     */
    fun showDeleteOfflineCopiesDialog(activity: AppCompatActivity,
                                      onDeletedCallback: (() -> Unit)? = null)
}

class ManagesDeleteOfflineCopiesDialog: ManagesDialog(), IManagesDeleteOfflineCopiesDialog {
    override val DIALOG_TAG: String = "DIALOG_DELETE_OFFLINE_COPIES"

    override val dialog by lazy { ConfirmDialog() }

    override fun showDeleteOfflineCopiesDialog(activity: AppCompatActivity,
                                               onDeletedCallback: (() -> Unit)?) {
        if(dialog.isAdded || activity.supportFragmentManager.isDestroyed) {
            return
        }

        initEvents(activity, onDeletedCallback)

        dialog.setTitle(activity.getString(R.string.offline_copies_delete_all))
        dialog.setMessage(activity.getString(R.string.offline_copies_dialog_sure_to_delete))
        dialog.positiveButtonText = activity.getString(R.string.delete)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initEvents(activity: AppCompatActivity, onDeletedCallback: (() -> Unit)?) {
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            confirmDeleteOfflineCopies(activity, onDeletedCallback)
        }
    }

    private fun confirmDeleteOfflineCopies(activity: AppCompatActivity,
                                           onDeletedCallback: (() -> Unit)?) {
        val offlinePadStore = OfflinePadStore(activity)
        activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                offlinePadStore.clear()
            }
            onDeletedCallback?.let { it() }
        }
        dialog.dismiss()
    }
}

