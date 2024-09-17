package com.mikifus.padland.Dialogs.Managers;

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.EditServerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesEditServerDialog {
    var serverViewModel: ServerViewModel?
    fun showEditServerDialog(activity: AppCompatActivity,
                             id: Long,
                             animationOriginView: View? = null)
}

public class ManagesEditServerDialog: ManagesDialog(), IManagesEditServerDialog {
    override val DIALOG_TAG: String = "DIALOG_EDIT_SERVER"

    override val dialog by lazy { EditServerDialog() }

    override var serverViewModel: ServerViewModel? = null

    override fun showEditServerDialog(activity: AppCompatActivity,
                                      id: Long,
                                      animationOriginView: View?) {
        showDialog(activity)
        initViewModels(activity)
        initEvents(activity, id)
        initAnimations(animationOriginView)
        setData(activity, id)
    }

    private fun initAnimations(animationOriginView: View?) {
        animationOriginView.let {
            dialog.animationOriginView = animationOriginView
        }
    }

    private fun setData(activity: AppCompatActivity, id: Long) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val server = serverViewModel?.getById(id)

            val data = HashMap<String, Any>()
            server?.mName?.let {
                data["name"] = it
            }
            server?.mUrl?.let {
                data["url"] = it
            }
            server?.mPadprefix?.let {
                data["prefix"] = it
            }
            server?.mJquery?.let {
                data["jquery"] = it
            }
            server?.mCryptPad?.let {
                data["cryptpad"] = it
            }

            activity.lifecycleScope.launch {
                dialog.setFormData(data)
            }
        }
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(activity)[ServerViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity, id: Long) {
        dialog.setPositiveButtonCallback { data ->
            saveEditServerDialog(activity, id, data)
            dialog.clearForm()
            closeDialog(activity)
        }
    }

    private fun saveEditServerDialog(activity: AppCompatActivity, id: Long, data: Map<String, Any>) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val server = serverViewModel?.getById(id)

            val updateServer = server?.copy(
                mName = data["name"].toString(),
                mPadprefix = data["prefix"].toString(),
                mUrl = data["url"].toString(),
                mJquery = data["jquery"] as Boolean,
                mCryptPad = data["cryptpad"] as Boolean
            )

            if (updateServer != null) {
                serverViewModel!!.updateServer(updateServer)
            }
        }
    }
}
