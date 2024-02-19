package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.text.TextPaint
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R

interface IManagesWhitelistServerDialog {
    var serverViewModel: ServerViewModel?
    fun showWhitelistServerDialog(activity: AppCompatActivity,
                                  url: String,
                                  onAddCallback: (dialogUrl: String) -> Unit,
                                  onNegativeCallback: (dialogUrl: String) -> Unit,
                                  onIgnoreCallback: (dialogUrl: String) -> Unit)
}
class ManagesWhitelistServerDialog: ManagesDialog(), IManagesWhitelistServerDialog {
    override val DIALOG_TAG: String = "DIALOG_WHITELIST_SERVER"

    override val dialog by lazy { ConfirmDialog() }
    override var serverViewModel: ServerViewModel? = null

    override fun showWhitelistServerDialog(activity: AppCompatActivity,
                                           url: String,
                                           onAddCallback: (dialogUrl: String) -> Unit,
                                           onNegativeCallback: (dialogUrl: String) -> Unit,
                                           onIgnoreCallback: (dialogUrl: String) -> Unit) {
        initViewModels(activity)
        initEvents(activity, url, onAddCallback, onNegativeCallback, onIgnoreCallback)

        dialog.setTitle(activity.getString(R.string.whitelist_server_dialog_title))
        dialog.setMessage(activity.getString(
            R.string.padview_toast_blacklist_url,
            ellipsizeUrl(url, 80)
        ))
        dialog.positiveButtonText = activity.getString(R.string.serverlist_dialog_new_server_title)
        dialog.negativeButtonText = activity.getString(R.string.whitelist_server_dialog_open_browser)
        dialog.neutralButtonText = activity.getString(R.string.ignore)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    @Suppress("SameParameterValue")
    private fun ellipsizeUrl(url: String, maxLength: Int): String {
        return if (url.length <= maxLength) {
            url
        } else {
            url.take(
                maxLength - (maxLength / 2) - 2
            ) +
            Typography.ellipsis +
            url.takeLast(
                maxLength - (maxLength / 2) - 1
            )
        }
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(activity)[ServerViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity,
                           url: String,
                           onAddCallback: (dialogUrl: String) -> Unit,
                           onNegativeCallback: (dialogUrl: String) -> Unit,
                           onIgnoreCallback: (dialogUrl: String) -> Unit) {
        dialog.neutralButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            onIgnoreCallback(url)
        }
        dialog.negativeButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            dialog.dismiss()
            onNegativeCallback(url)
        }
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            onAddCallback(url)
        }
        dialog.isCancelable = false
    }
}