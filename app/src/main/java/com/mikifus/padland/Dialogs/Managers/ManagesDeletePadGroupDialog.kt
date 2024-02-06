package com.mikifus.padland.Dialogs.Managers

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesDeletePadGroupDialog {
    var padGroupViewModel: PadGroupViewModel?
    var ids: List<Long>
    fun showDeletePadGroupDialog(activity: AppCompatActivity, ids: List<Long>)
}

class ManagesDeletePadGroupDialog: IManagesDeletePadGroupDialog {
    override var padGroupViewModel: PadGroupViewModel? = null
    override var ids: List<Long> = listOf()

    override fun showDeletePadGroupDialog(activity: AppCompatActivity, ids: List<Long>) {
        initViewModels(activity)
        initEvents(activity)

        this.ids = ids

        dialog.setTitle(activity.getString(R.string.delete))
        dialog.setMessage(activity.getString(R.string.sure_to_delete_group))
        dialog.setPositiveButtonText(activity.getString(R.string.delete))

        dialog.show(activity.supportFragmentManager, DIALOG_TAG)
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.setPositiveButtonCallback { dialog, which ->
            confirmDeletePadGroupDialog(activity)
            this.ids = listOf()
        }
    }

    private fun confirmDeletePadGroupDialog(activity: AppCompatActivity) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            padGroupViewModel!!.deletePadGroup(ids)
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_DELETE_PADGROUP"

        private val dialog by lazy { ConfirmDialog() }
    }
}