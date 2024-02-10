package com.mikifus.padland.Dialogs.Managers;

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
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

public class ManagesNewPadGroupDialog: IManagesNewPadGroupDialog {

    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showNewPadGroupDialog(activity: AppCompatActivity) {
        if(dialog.isAdded) {
            return
        }

        initViewModels(activity)
        initEvents(activity)

        val transaction = activity.supportFragmentManager.beginTransaction()

        // For a polished look, specify a transition animation.
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        // Add to back stack
        transaction.addToBackStack(DIALOG_TAG)

        dialog.show(transaction, DIALOG_TAG)
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
        }
    }

    private fun saveNewPadgroupDialog(activity: AppCompatActivity, name: String) {
        val padGroup = PadGroup.fromName(name).value!!
        activity.lifecycleScope.launch(Dispatchers.IO) {
            padGroupViewModel!!.insertPadGroup(padGroup)
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_NEW_PADGROUP"

        private val dialog by lazy { NewPadGroupDialog() }
    }
}
