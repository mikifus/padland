package com.mikifus.padland.Dialogs.Managers;

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Dialogs.NewPadGroupDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesNewPadGroupDialog {
    var padGroupViewModel: PadGroupViewModel?
    fun showNewPadGroupDialog(activity: AppCompatActivity)
}

public class ManagesNewPadGroupDialog: ManagesDialog(), IManagesNewPadGroupDialog {
    override val DIALOG_TAG: String = "DIALOG_NEW_PADGROUP"

    override val dialog by lazy { NewPadGroupDialog() }
    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showNewPadGroupDialog(activity: AppCompatActivity) {
        showDialog(activity)
        initViewModels(activity)
        initEvents(activity)
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.setPositiveButtonCallback { data ->
            saveNewPadgroupDialog(activity, data["name"].toString())
            dialog.clearForm()
            closeDialog(activity)
        }
    }

    private fun saveNewPadgroupDialog(activity: AppCompatActivity, name: String) {
        val padGroup = PadGroup.fromName(name).value!!
        activity.lifecycleScope.launch(Dispatchers.IO) {
            padGroupViewModel!!.insertPadGroup(padGroup)
        }
    }
}
