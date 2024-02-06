package com.mikifus.padland.Dialogs.Managers;

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadListEntity
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialogs.NewPadDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesNewPadDialog {
    var padViewModel: PadViewModel?
    var padGroupViewModel: PadGroupViewModel?
    fun showNewPadDialog(activity: AppCompatActivity)
}

public class ManagesNewPadDialog: IManagesNewPadDialog {

    override var padViewModel: PadViewModel? = null
    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showNewPadDialog(activity: AppCompatActivity) {
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
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(activity)[PadViewModel::class.java]
        }
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.setPositiveButtonCallback { data ->
            saveNewPadDialog(activity, data)
            dialog.clearForm()
        }
    }

    private fun saveNewPadDialog(activity: AppCompatActivity, data: Map<String, Any>) {
        val pad: Pad = Pad.fromData(data).value!!
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val padId = padViewModel?.insertPad(pad)

            if(padId != null && data["group_id"] as Long > 0) {
                padGroupViewModel?.insertPadGroupsAndPadList(
                    PadGroupsAndPadListEntity(
                        mGroupId = data["group_id"] as Long,
                        mPadId = padId,
                    )
                )
            }
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_NEW_PAD"

        private val dialog by lazy { NewPadDialog() }
    }
}
