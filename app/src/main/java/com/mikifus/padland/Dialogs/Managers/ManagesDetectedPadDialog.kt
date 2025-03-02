package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R

interface IManagesDetectedPadDialog {
    fun showDetectedPadDialog(activity: AppCompatActivity,
                           url: String,
                           onCancelCallback: (() -> Unit)? = {},
                           onConfirmCallback: (() -> Unit)? = {})
}
class ManagesDetectedPadDialog: ManagesDialog(), IManagesDetectedPadDialog {
    override val DIALOG_TAG: String = "DIALOG_SSL_ERROR"

    override val dialog by lazy { ConfirmDialog() }

    override fun showDetectedPadDialog(activity: AppCompatActivity,
                                    url: String,
                                    onCancelCallback: (() -> Unit)?,
                                       onConfirmCallback: (() -> Unit)?) {

        dialog.setTitle(activity.getString(R.string.padview_dialog_detected_pad))
        dialog.setMessage(activity.getString(R.string.padview_dialog_detected_pad_desc))
        dialog.positiveButtonText = activity.getString(R.string.save_pad)
        dialog.negativeButtonText = activity.getString(R.string.cancel)

        if(dialog.isAdded && !activity.supportFragmentManager.isDestroyed) {
            dialog.dismiss()
        }

        initEvents(activity, url, onCancelCallback, onConfirmCallback)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initEvents(activity: AppCompatActivity,
                           url: String,
                           onCancelCallback: (() -> Unit)?,
                           onConfirmCallback: (() -> Unit)?) {
        dialog.negativeButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            onCancelCallback?.let { it() }
        }
        dialog.onDismissCallback = {
            onCancelCallback?.let { it() }
        }
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            onConfirmCallback?.let { it() }
        }
    }

    private fun confirmBrowser(activity: AppCompatActivity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity.startActivity(intent)
    }

}