package com.mikifus.padland.Dialogs.Managers

import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.Dialogs.ErrorReportDialog

interface IManagesErrorReportDialog {
    fun showErrorReportDialog(activity: AppCompatActivity, id: String)
}

/**
 * Shows a saved error report from a screen (e.g. the saved errors list).
 * Popups for new errors are shown by the ErrorReporter itself.
 */
class ManagesErrorReportDialog: ManagesDialog(), IManagesErrorReportDialog {
    // Shared with ErrorReporter, so popups never stack over this dialog
    override val DIALOG_TAG: String = ErrorReportDialog.DIALOG_TAG

    override val dialog by lazy { ErrorReportDialog() }

    override fun showErrorReportDialog(activity: AppCompatActivity, id: String) {
        val fragmentManager = activity.supportFragmentManager
        if(dialog.isAdded || fragmentManager.isDestroyed || fragmentManager.isStateSaved
            || fragmentManager.findFragmentByTag(DIALOG_TAG) != null) {
            return
        }

        dialog.setReportIds(listOf(id), ErrorReportDialog.MODE_DETAIL)
        dialog.show(fragmentManager, DIALOG_TAG)
    }
}

