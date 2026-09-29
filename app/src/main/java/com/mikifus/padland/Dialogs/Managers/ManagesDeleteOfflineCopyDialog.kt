package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IManagesDeleteOfflineCopyDialog {
    /**
     * [onDeletedCallback] is called on the main thread.
     */
    fun showDeleteOfflineCopyDialog(activity: AppCompatActivity,
                                    padId: Long,
                                    onDeletedCallback: (() -> Unit)? = null)
}

class ManagesDeleteOfflineCopyDialog: ManagesDialog(), IManagesDeleteOfflineCopyDialog {
    override val DIALOG_TAG: String = "DIALOG_DELETE_OFFLINE_COPY"

    override val dialog by lazy { ConfirmDialog() }

    override fun showDeleteOfflineCopyDialog(activity: AppCompatActivity,
                                             padId: Long,
                                             onDeletedCallback: (() -> Unit)?) {
        if(dialog.isAdded || activity.supportFragmentManager.isDestroyed) {
            return
        }

        initEvents(activity, padId, onDeletedCallback)

        dialog.setTitle(activity.getString(R.string.offline_copy_delete))
        dialog.setMessage(activity.getString(R.string.offline_copy_dialog_delete_sure_to_delete))
        dialog.positiveButtonText = activity.getString(R.string.delete)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initEvents(activity: AppCompatActivity,
                           padId: Long,
                           onDeletedCallback: (() -> Unit)?) {
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            confirmDeleteOfflineCopy(activity, padId, onDeletedCallback)
        }
    }

    private fun confirmDeleteOfflineCopy(activity: AppCompatActivity,
                                         padId: Long,
                                         onDeletedCallback: (() -> Unit)?) {
        val offlinePadStore = OfflinePadFetcher.getStore(activity)
        activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                offlinePadStore.delete(padId)
            }
            onDeletedCallback?.let { it() }
        }
        dialog.dismiss()
    }
}

