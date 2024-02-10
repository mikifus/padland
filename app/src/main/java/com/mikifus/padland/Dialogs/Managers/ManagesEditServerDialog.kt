package com.mikifus.padland.Dialogs.Managers;

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.EditServerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesEditServerDialog {
    var serverViewModel: ServerViewModel?
    fun showEditServerDialog(activity: AppCompatActivity, id: Long)
}

public class ManagesEditServerDialog: IManagesEditServerDialog {

    override var serverViewModel: ServerViewModel? = null

    override fun showEditServerDialog(activity: AppCompatActivity, id: Long) {
        if(dialog.isAdded) {
            return
        }

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
        }
    }

    private fun saveEditServerDialog(activity: AppCompatActivity, id: Long, data: Map<String, Any>) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val server = serverViewModel?.getById(id)

            val updateServer = server?.copy(
                mName = data["name"].toString(),
                mPadprefix = data["prefix"].toString(),
                mUrl = data["url"].toString(),
                mJquery = data["jquery"] as Boolean
            )

            if (updateServer != null) {
                serverViewModel!!.updateServer(updateServer)
            }
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_EDIT_SERVER"

        private val dialog by lazy { EditServerDialog() }
    }
}
