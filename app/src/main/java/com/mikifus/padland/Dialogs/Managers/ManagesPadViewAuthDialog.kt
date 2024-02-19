package com.mikifus.padland.Dialogs.Managers

import android.os.Build
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.URLUtil
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.Dialogs.PadViewAuthDialog

interface IManagesPadViewAuthDialog {
    fun showPadViewAuthDialog(
        activity: AppCompatActivity,
        view: WebView,
        handler: HttpAuthHandler
    )
}
class ManagesPadViewAuthDialog: ManagesDialog(), IManagesPadViewAuthDialog {
    override val DIALOG_TAG: String = "DIALOG_BASIC_AUTH"

    override val dialog by lazy { PadViewAuthDialog() }
    private var webView: WebView? = null
    private var lastLoginUrl: String? = null

    override fun showPadViewAuthDialog(activity: AppCompatActivity, view: WebView, handler: HttpAuthHandler) {
        this.webView = view
        showDialog(activity)
        initEvents(activity, handler)
    }

    private fun initEvents(activity: AppCompatActivity, handler: HttpAuthHandler) {
        dialog.setPositiveButtonCallback { data ->
            handler.proceed(data["user"].toString(), data["password"].toString())
            webView?.clearCache(true) // No effect?
            dialog.clearForm()
            closeDialog(activity)
        }
        dialog.setNegativeButtonCallback {
            handler.cancel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null)
            } else {
                @Suppress("DEPRECATION")
                CookieManager.getInstance().removeAllCookie()
            }
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
        dialog.dismissCallback = {
            handler.cancel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null)
            } else {
                @Suppress("DEPRECATION")
                CookieManager.getInstance().removeAllCookie()
            }
        }
    }
}