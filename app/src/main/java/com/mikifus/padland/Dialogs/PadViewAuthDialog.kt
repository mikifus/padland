package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textview.MaterialTextView
import com.mikifus.padland.R


/**
 * Created by mikifus on 10/03/16.
 */
class PadViewAuthDialog: FormDialog() {

    private var mUserEditText: EditText? = null
    private var mPasswordEditText: EditText? = null
    private var mAuthErrorMessage: MaterialTextView? = null
    private var mAuthSslMessage: MaterialTextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.padview_dialog_basicatuh_title)
        return dialog
    }

    override fun validateForm(): Boolean { return true }

    override fun getFormData(): Map<String, Any> {
        val user = mUserEditText!!.text.toString()
        val password = mPasswordEditText!!.text.toString()

        val data = HashMap<String, Any>()
        data["user"] = user
        data["password"] = password

        return data
    }

    override fun clearForm() {
        mUserEditText!!.text = null
//        mPasswordEditText!!.text = null
        hideLoginError()
        hideSslWarning()
    }

    override fun onStart() {
        super.onStart()

        mUserEditText = requireView().findViewById(R.id.txt_username)
        mPasswordEditText = requireView().findViewById(R.id.txt_password)
        mAuthErrorMessage = requireView().findViewById(R.id.auth_error_message)
        mAuthSslMessage = requireView().findViewById(R.id.auth_warning_message)

        mUserEditText?.requestFocus()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_auth, container, false)
    }

    override fun initEvents() {
        val positiveButton = toolbar?.findViewById<Button>(R.id.dialog_positive_button)
        positiveButton?.setOnClickListener(positiveButtonCallback)
    }
    fun showLoginError() {
        mAuthErrorMessage?.visibility = View.VISIBLE
    }
    fun hideLoginError() {
        mAuthErrorMessage?.visibility = View.GONE
    }
    fun showSslWarning() {
        mAuthSslMessage?.visibility = View.VISIBLE
    }
    fun hideSslWarning() {
        mAuthSslMessage?.visibility = View.GONE
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        clearForm()
    }

    override fun getTheme(): Int = R.style.Theme_MaterialComponents_Dialog_MinWidth
}