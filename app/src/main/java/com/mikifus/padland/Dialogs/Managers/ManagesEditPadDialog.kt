package com.mikifus.padland.Dialogs.Managers;

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Activities.PadViewActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadListEntity
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialogs.EditPadDialog
import com.mikifus.padland.Dialogs.NewPadDialog
import com.mikifus.padland.Utils.PadServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesEditPadDialog {
    var padViewModel: PadViewModel?
    var padGroupViewModel: PadGroupViewModel?
    fun showEditPadDialog(activity: AppCompatActivity, id: Long)
}

public class ManagesEditPadDialog: IManagesEditPadDialog {

    override var padViewModel: PadViewModel? = null
    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showEditPadDialog(activity: AppCompatActivity, id: Long) {
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
            val pad = padViewModel?.getById(id)
            val padGroup = padGroupViewModel?.getById(id)

            val data = HashMap<String, Any>()
            pad?.mName?.let {
                data["name"] = it
            }
            pad?.mLocalName?.let {
                data["local_name"] = it
            }
//            pad?.mServer?.let {
//                data["server"] = it
//            }
            pad?.mUrl?.let {
                data["server"] = PadServer.Builder().padUrl(it).build().server!!
            }
            data["group_id"] = padGroup?.mId ?: 0

            activity.lifecycleScope.launch {
                dialog.setFormData(data)
            }
        }
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(activity)[PadViewModel::class.java]
        }
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity, id: Long) {
        dialog.setPositiveButtonCallback { data ->
            saveEditPadDialog(activity, id, data)
            dialog.clearForm()
        }
    }

    private fun saveEditPadDialog(activity: AppCompatActivity, padId: Long, data: Map<String, Any>) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val pad = padViewModel?.getById(padId)!!

            val savePad = pad.copy(
                mName = data["name"].toString(),
                mLocalName = data["local_name"].toString(),
                mServer = data["server"].toString(),
                mUrl = data["url"].toString()
            )

            padViewModel?.updatePad(savePad)

            padGroupViewModel?.deletePadGroupsAndPadList(padId)
            if(data["group_id"] as Long > 0) {
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
        private const val DIALOG_TAG: String = "DIALOG_EDIT_PAD"

        private val dialog by lazy { EditPadDialog() }
    }
}
