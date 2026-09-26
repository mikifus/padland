package com.mikifus.padland.Dialogs.Managers

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.NewServerDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IManagesNewServerDialog {
    var serverViewModel: ServerViewModel?

    /**
     * @param onSavedCallBack called on the main thread once the new server is stored
     * @param onDismissCallBack called when the dialog is closed (saved or not)
     */
    fun showNewServerDialog(activity: AppCompatActivity,
                            url: String? = null,
                            onSavedCallBack: ((Server) -> Unit)? = null,
                            onDismissCallBack: (() -> Unit)? = null)
}

class ManagesNewServerDialog: ManagesDialog(), IManagesNewServerDialog {
    override val DIALOG_TAG: String = "DIALOG_NEW_SERVER"

    override val dialog by lazy { NewServerDialog() }
    override var serverViewModel: ServerViewModel? = null
    private var onSavedCallBack: ((Server) -> Unit)? = null

    override fun showNewServerDialog(activity: AppCompatActivity,
                                     url: String?,
                                     onSavedCallBack: ((Server) -> Unit)?,
                                     onDismissCallBack: (() -> Unit)?) {
        // Always replaced, the dialog instance is reused between calls
        this.onSavedCallBack = onSavedCallBack
        showDialog(activity)
        initViewModels(activity)
        initEvents(activity, onDismissCallBack)
        initAnimations(activity)

        url?.let { onSetInitialUrl(url) }
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(activity)[ServerViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity,
                           onDismissCallBack: (() -> Unit)?) {
        dialog.setPositiveButtonCallback { data ->
            saveNewServerDialog(activity, data)
            dialog.clearForm()
            closeDialog(activity)
        }
        onDismissCallBack?.let { dialog.dismissCallback = onDismissCallBack }
    }

    private fun initAnimations(activity: AppCompatActivity) {
        dialog.animationOriginView = activity.findViewById(R.id.button_new_server)
    }

    private fun onSetInitialUrl(url: String) {
        val padUrl = PadServer.Builder().padUrl(url)

        dialog.initialName = padUrl.host
        dialog.initialUrl = padUrl.server
        dialog.initialPrefix = padUrl.prefix
    }

    private fun saveNewServerDialog(activity: AppCompatActivity, data: Map<String, Any>) {
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val server = Server()

            val updateServer = server.copy(
                mName = data["name"].toString(),
                mPadprefix = data["prefix"].toString(),
                mUrl = data["url"].toString(),
                mJquery = data["jquery"] as Boolean,
                mCryptPad = data["cryptpad"] as Boolean
            )

            serverViewModel!!.insertServer(updateServer)

            onSavedCallBack?.let { callback ->
                withContext(Dispatchers.Main) { callback(updateServer) }
            }
        }
    }
}
