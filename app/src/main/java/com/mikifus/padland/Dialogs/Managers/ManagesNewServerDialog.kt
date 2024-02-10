package com.mikifus.padland.Dialogs.Managers

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.NewServerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface IManagesNewServerDialog {
    var serverViewModel: ServerViewModel?
    fun showNewServerDialog(activity: AppCompatActivity)
}

class ManagesNewServerDialog: IManagesNewServerDialog {

    override var serverViewModel: ServerViewModel? = null

    override fun showNewServerDialog(activity: AppCompatActivity) {
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
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(activity)[ServerViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.setPositiveButtonCallback { data ->
            saveNewServerDialog(activity, data)
            dialog.clearForm()
        }
    }

    private fun saveNewServerDialog(activity: AppCompatActivity, data: Map<String, Any>) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val server = Server()

            val updateServer = server.copy(
                mName = data["name"].toString(),
                mPadprefix = data["prefix"].toString(),
                mUrl = data["url"].toString(),
                mJquery = data["jquery"] as Boolean
            )

            serverViewModel!!.insertServer(updateServer)
        }
        dialog.dismiss()
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_EDIT_SERVER"

        private val dialog by lazy { NewServerDialog() }
    }
}
