package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R

interface IManagesWhitelistServerDialog {
    var serverViewModel: ServerViewModel?
    fun showWhitelistServerDialog(activity: AppCompatActivity,
                                  url: String,
                                  onAddCallback: (dialogUrl: String) -> Unit,
                                  onIgnoreCallback: (dialogUrl: String) -> Unit)
}
class ManagesWhitelistServerDialog: IManagesWhitelistServerDialog {
    override var serverViewModel: ServerViewModel? = null

    override fun showWhitelistServerDialog(activity: AppCompatActivity,
                                           url: String,
                                           onAddCallback: (dialogUrl: String) -> Unit,
                                           onIgnoreCallback: (dialogUrl: String) -> Unit) {
        initViewModels(activity)
        initEvents(activity, url, onAddCallback, onIgnoreCallback)

        dialog.setTitle(activity.getString(R.string.whitelist_server_dialog_title))
        dialog.setMessage(activity.getString(R.string.padview_toast_blacklist_url))
        dialog.positiveButtonText = activity.getString(R.string.serverlist_dialog_new_server_title)
        dialog.negativeButtonText = activity.getString(R.string.whitelist_server_dialog_open_browser)
        dialog.neutralButtonText = activity.getString(R.string.ignore)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(activity)[ServerViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity,
                           url: String,
                           onAddCallback: (dialogUrl: String) -> Unit,
                           onIgnoreCallback: (dialogUrl: String) -> Unit) {
        dialog.neutralButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            onIgnoreCallback(url)
        }
        dialog.negativeButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            confirmBrowser(activity, url)
        }
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            onAddCallback(url)
        }
        dialog.isCancelable = false
    }

    private fun confirmBrowser(activity: AppCompatActivity, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
        activity.finish()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_WHITELIST_SERVER"

        private val dialog by lazy { ConfirmDialog() }
    }

}