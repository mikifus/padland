package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IManagesDeleteErrorReportsDialog {
    /**
     * Asks for confirmation and deletes all the saved error reports.
     * [onDeletedCallback] is called on the main thread once they are deleted.
     */
    fun showDeleteErrorReportsDialog(activity: AppCompatActivity,
                                     onDeletedCallback: (() -> Unit)? = null)
}

class ManagesDeleteErrorReportsDialog: ManagesDialog(), IManagesDeleteErrorReportsDialog {
    override val DIALOG_TAG: String = "DIALOG_DELETE_ERROR_REPORTS"

    override val dialog by lazy { ConfirmDialog() }

    override fun showDeleteErrorReportsDialog(activity: AppCompatActivity,
                                              onDeletedCallback: (() -> Unit)?) {
        if(dialog.isAdded || activity.supportFragmentManager.isDestroyed) {
            return
        }

        initEvents(activity, onDeletedCallback)

        dialog.setTitle(activity.getString(R.string.error_report_list_delete_all))
        dialog.setMessage(activity.getString(R.string.error_report_list_dialog_delete_sure_to_delete))
        dialog.positiveButtonText = activity.getString(R.string.delete)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initEvents(activity: AppCompatActivity, onDeletedCallback: (() -> Unit)?) {
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            confirmDeleteErrorReports(activity, onDeletedCallback)
        }
    }

    private fun confirmDeleteErrorReports(activity: AppCompatActivity,
                                          onDeletedCallback: (() -> Unit)?) {
        val errorReportStore = ErrorReporter.getStore(activity)
        activity.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                errorReportStore.clear()
            }
            onDeletedCallback?.let { it() }
        }
        dialog.dismiss()
    }
}

