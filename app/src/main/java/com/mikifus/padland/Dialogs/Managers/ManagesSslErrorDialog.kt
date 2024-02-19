package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R

interface IManagesSslErrorDialog {
    fun showSslErrorDialog(activity: AppCompatActivity,
                           url: String,
                           error: String,
                           onCancelCallback: (() -> Unit)? = {},
                           onIgnoreCallback: (() -> Unit)? = {})
}
class ManagesSslErrorDialog: ManagesDialog(), IManagesSslErrorDialog {
    override val DIALOG_TAG: String = "DIALOG_SSL_ERROR"

    override val dialog by lazy { ConfirmDialog() }

    override fun showSslErrorDialog(activity: AppCompatActivity,
                                    url: String,
                                    error: String,
                                    onCancelCallback: (() -> Unit)?,
                                    onIgnoreCallback: (() -> Unit)?) {

        dialog.setTitle(activity.getString(R.string.ssl_error))
        dialog.setMessage(error)
        dialog.positiveButtonText = activity.getString(R.string.ignore)
        dialog.neutralButtonText = activity.getString(R.string.whitelist_server_dialog_open_browser)

        if(dialog.isAdded && !activity.supportFragmentManager.isDestroyed) {
            dialog.dismiss()
        }

        initEvents(activity, url, onCancelCallback, onIgnoreCallback)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initEvents(activity: AppCompatActivity,
                           url: String,
                           onCancelCallback: (() -> Unit)?,
                           onIgnoreCallback: (() -> Unit)?) {
        dialog.negativeButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            onCancelCallback?.let { it() }
        }
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            onIgnoreCallback?.let { it() }
        }
        dialog.neutralButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            confirmBrowser(activity, url)
        }
        dialog.onDismissCallback = {
            onIgnoreCallback?.let { it() }
        }
    }

    private fun confirmBrowser(activity: AppCompatActivity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
    }

}