package com.mikifus.padland.Dialogs.Managers

import android.webkit.HttpAuthHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.mikifus.padland.Dialogs.PadViewAuthDialog

interface IManagesPadViewAuthDialog {
    fun showPadViewAuthDialog(
        activity: AppCompatActivity,
        handler: HttpAuthHandler
    )
}
class ManagesPadViewAuthDialog: IManagesPadViewAuthDialog {

    override fun showPadViewAuthDialog(activity: AppCompatActivity, handler: HttpAuthHandler) {
        if(dialog.isAdded) {
            return
        }

        initEvents(handler)

        val transaction = activity.supportFragmentManager.beginTransaction()

        // For a polished look, specify a transition animation.
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        // Add to back stack
        transaction.addToBackStack(DIALOG_TAG)

        dialog.show(transaction, DIALOG_TAG)

        // TODO: Implement these features
//        if (PadViewActivity.PadViewAuthDialog.done_auth) {
//            // Credentials must be invalid
//            val textView = requireView().findViewById<View>(R.id.auth_error_message) as TextView
//            textView.setText(R.string.basic_auth_error)
//        }
//        try {
//            // Warn the user that is not using SSL
//            val url = URL(currentPadUrl)
//            if (url.protocol != "https") {
//                val textView = requireView().findViewById<View>(R.id.auth_warning_message) as TextView
//                textView.setText(R.string.basic_auth_warning)
//            }
//        } catch (e: MalformedURLException) {
//            e.printStackTrace()
//        }
    }

    private fun initEvents(handler: HttpAuthHandler) {
        dialog.setPositiveButtonCallback { data ->
            handler.proceed(data["user"].toString(), data["password"].toString())

            // TODO: Check if it is useful to not clear (repeated login requests)
//            dialog.clearForm()
        }
        dialog.setNegativeButtonCallback {
            handler.cancel()
        }
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_BASIC_AUTH"

        private val dialog by lazy { PadViewAuthDialog() }
    }
}