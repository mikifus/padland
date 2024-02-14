package com.mikifus.padland.Dialogs.Managers;

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialogs.GroupPadDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesGroupPadDialog {
    var padViewModel: PadViewModel?
    var padGroupViewModel: PadGroupViewModel?
    fun showGroupPadDialog(activity: AppCompatActivity, ids: List<Long>)
}

public class ManagesGroupPadDialog: ManagesDialog(), IManagesGroupPadDialog {
    override val DIALOG_TAG: String = "DIALOG_EDIT_PAD"

    override  val dialog by lazy { GroupPadDialog() }
    override var padViewModel: PadViewModel? = null
    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showGroupPadDialog(activity: AppCompatActivity, ids: List<Long>) {
        showDialog(activity)
        initViewModels(activity)
        initEvents(activity, ids)
        setData(activity, ids)
    }

    private fun setData(activity: AppCompatActivity, ids: List<Long>) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val padGroupsAndPadList = padGroupViewModel?.getPadGroupsAndPadListByPadIds(ids)

            var mainGroup: Long = 0
            padGroupsAndPadList?.forEach {
                mainGroup = it.mGroupId
            }

            val data = HashMap<String, Any>()
            data["group_id"] = mainGroup

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

    private fun initEvents(activity: AppCompatActivity, ids: List<Long>) {
        dialog.setPositiveButtonCallback { data ->
            saveGroupPadDialog(activity, ids, data)
            dialog.clearForm()
            closeDialog(activity)
        }
    }

    private fun saveGroupPadDialog(activity: AppCompatActivity, ids: List<Long>, data: Map<String, Any>) {
        val groupId = data["group_id"] as Long
        activity.lifecycleScope.launch(Dispatchers.IO) {
            ids.forEach {
                padGroupViewModel?.deletePadGroupsAndPadList(it)
                if(data["group_id"] as Long > 0) {
                    padGroupViewModel?.insertPadGroupsAndPadList(
                        PadGroupsAndPadList(
                            mGroupId = groupId,
                            mPadId = it,
                        )
                    )
                }
            }
        }
    }
}
