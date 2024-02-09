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
import com.mikifus.padland.R


/**
 * Created by mikifus on 10/03/16.
 */
class PadViewAuthDialog: FormDialog() {

    private var mUserEditText: EditText? = null
    private var mPasswordEditText: EditText? = null

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
        mPasswordEditText!!.text = null
    }

    override fun onStart() {
        super.onStart()

        mUserEditText = requireView().findViewById<View>(R.id.txt_username) as EditText
        mPasswordEditText = requireView().findViewById<View>(R.id.txt_password) as EditText

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

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }
    override fun getTheme(): Int {
        return R.style.Theme_MaterialComponents_Dialog_MinWidth
    }
}