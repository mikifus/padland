package com.mikifus.padland.Dialogs.Managers;

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Activities.PadListActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.Dialogs.EditPadGroupDialog
import com.mikifus.padland.Dialogs.NewPadGroupDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

interface IManagesEditPadGroupDialog {
    var padGroupViewModel: PadGroupViewModel?
    fun showEditPadGroupDialog(activity: AppCompatActivity,
                               id: Long,
                               animationOriginView: View? = null)
}

public class ManagesEditPadGroupDialog: ManagesDialog(), IManagesEditPadGroupDialog {
    override val DIALOG_TAG: String = "DIALOG_EDIT_PADGROUP"

    override val dialog by lazy { EditPadGroupDialog() }

    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showEditPadGroupDialog(activity: AppCompatActivity,
                                        id: Long,
                                        animationOriginView: View?) {
        showDialog(activity)
        initViewModels(activity)
        initEvents(activity, id)
        initAnimations(animationOriginView)
        setData(activity, id)
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
            closeDialog(activity)
        }
    }

    private fun initAnimations(animationOriginView: View?) {
        animationOriginView.let {
            dialog.animationOriginView = animationOriginView
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
    }
}
