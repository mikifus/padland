package com.mikifus.padland.Dialogs.Managers

import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.mikifus.padland.Dialogs.PadViewAuthDialog

interface IManagesPadViewAuthDialog {
    fun showPadViewAuthDialog(
        activity: AppCompatActivity,
        view: WebView,
        handler: HttpAuthHandler
    )
}
class ManagesPadViewAuthDialog: IManagesPadViewAuthDialog {

    private var webView: WebView? = null
    private var lastLoginUrl: String? = null

    override fun showPadViewAuthDialog(activity: AppCompatActivity, view: WebView, handler: HttpAuthHandler) {
        if(dialog.isAdded) {
            return
        }
        this.webView = view

        initEvents(handler)

        val transaction = activity.supportFragmentManager.beginTransaction()

        // For a polished look, specify a transition animation.
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)

        // Add to back stack
        transaction.addToBackStack(DIALOG_TAG)

        dialog.show(transaction, DIALOG_TAG)
    }

    private fun initEvents(handler: HttpAuthHandler) {
        dialog.setPositiveButtonCallback { data ->
            handler.proceed(data["user"].toString(), data["password"].toString())
//            this.webView?.clearCache(true) // No effect?
            dialog.dismiss()
        }
        dialog.setNegativeButtonCallback {
            handler.cancel()
            this.webView?.clearCache(true)
        }
        dialog.setOnResumeCallback {
            if(lastLoginUrl == webView?.url) {
                // Credentials must be invalid, trying again
                dialog.showLoginError()
            }
            lastLoginUrl = webView?.url

            if(URLUtil.isHttpUrl(webView?.url)) {
                // Warn the user that they're not using SSL
                dialog.showSslWarning()
            }
        }
    }

    companion object {
        private const val DIALOG_TAG: String = "DIALOG_BASIC_AUTH"

        private val dialog by lazy { PadViewAuthDialog() }
    }
}