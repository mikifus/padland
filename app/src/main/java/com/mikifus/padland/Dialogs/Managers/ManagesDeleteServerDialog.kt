package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesDeleteServerDialog {
    var serverViewModel: ServerViewModel?
    var ids: List<Long>
    fun showDeleteServerDialog(activity: AppCompatActivity, ids: List<Long>)
}

class ManagesDeleteServerDialog: IManagesDeleteServerDialog {
    override var serverViewModel: ServerViewModel? = null
    override var ids: List<Long> = listOf()

    override fun showDeleteServerDialog(activity: AppCompatActivity, ids: List<Long>) {
        initViewModels(activity)
        initEvents(activity)

        this.ids = ids

        dialog.setTitle(activity.getString(R.string.delete))
        dialog.setMessage(activity.getString(R.string.serverlist_dialog_delete_sure_to_delete))
        dialog.positiveButtonText = activity.getString(R.string.delete)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(activity)[ServerViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { dialog, which ->
            confirmDeleteServer(activity)
            this.ids = listOf()
        }
    }

    private fun confirmDeleteServer(activity: AppCompatActivity) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            serverViewModel!!.deleteServer(ids)
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_DELETE_SERVER"

        private val dialog by lazy { ConfirmDialog() }
    }
}