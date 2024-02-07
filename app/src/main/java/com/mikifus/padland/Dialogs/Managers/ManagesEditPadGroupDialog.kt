package com.mikifus.padland.Dialogs.Managers;

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Dialogs.EditPadGroupDialog
import com.mikifus.padland.Dialogs.NewPadGroupDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

interface IManagesEditPadGroupDialog {
    var padGroupViewModel: PadGroupViewModel?
    fun showEditPadGroupDialog(activity: AppCompatActivity, id: Long)
}

public class ManagesEditPadGroupDialog: IManagesEditPadGroupDialog {

    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showEditPadGroupDialog(activity: AppCompatActivity, id: Long) {
        initViewModels(activity)
        initEvents(activity, id)
        setData(activity, id)

        val transaction = activity.supportFragmentManager.beginTransaction()

        // For a polished look, specify a transition animation.
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        // Add to back stack
        transaction.addToBackStack(DIALOG_TAG)

        dialog.show(transaction, DIALOG_TAG)
    }

    private fun setData(activity: AppCompatActivity, id: Long) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val padGroup = padGroupViewModel?.getById(id)

            val data = HashMap<String, Any>()
            padGroup?.mName?.let {
                data["name"] = it
            }

            activity.lifecycleScope.launch {
                dialog.setFormData(data)
            }
        }
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity, id: Long) {
        dialog.setPositiveButtonCallback { data ->
            saveEditPadgroupDialog(activity, id, data["name"].toString())
            dialog.clearForm()
        }
    }

    private fun saveEditPadgroupDialog(activity: AppCompatActivity, id: Long, name: String) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val padGroup = padGroupViewModel?.getById(id)

            padGroup?.mName = name

            if (padGroup != null) {
                padGroupViewModel!!.updatePadGroup(padGroup)
            }
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_EDIT_PADGROUP"

        private val dialog by lazy { EditPadGroupDialog() }
    }
}
