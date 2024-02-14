package com.mikifus.padland.Dialogs.Managers

import android.content.DialogInterface.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesDeletePadDialog {
    var padGroupViewModel: PadGroupViewModel?
    var padViewModel: PadViewModel?
    var ids: List<Long>
    fun showDeletePadDialog(activity: AppCompatActivity, ids: List<Long>)
}

class ManagesDeletePadDialog: ManagesDialog(), IManagesDeletePadDialog {
    override val DIALOG_TAG: String = "DIALOG_DELETE_PAD"

    override val dialog by lazy { ConfirmDialog() }

    override var padGroupViewModel: PadGroupViewModel? = null
    override var padViewModel: PadViewModel? = null
    override var ids: List<Long> = listOf()

    override fun showDeletePadDialog(activity: AppCompatActivity, ids: List<Long>) {
        initViewModels(activity)
        initEvents(activity)

        this.ids = ids

        dialog.setTitle(activity.getString(R.string.delete))
        dialog.setMessage(activity.getString(R.string.sure_to_delete_pad))
        dialog.positiveButtonText = activity.getString(R.string.delete)

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(activity)[PadViewModel::class.java]
        }
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.positiveButtonCallback = OnClickListener { dialog, which ->
            confirmDeletePadDialog(activity)
            this.ids = listOf()
        }
    }

    private fun confirmDeletePadDialog(activity: AppCompatActivity) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            ids.forEach {
                padGroupViewModel!!.deletePadGroupsAndPadList(it)
                padViewModel!!.deletePad(it)
            }
        }
        dialog.dismiss()
    }
}