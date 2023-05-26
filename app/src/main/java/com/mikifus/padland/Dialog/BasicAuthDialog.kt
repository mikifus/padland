package com.mikifus.padland.Dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mikifus.padland.R

/**
 * Created by mikifus on 21/09/16.
 */
abstract class BasicAuthDialog : DialogFragment() {
    private var mUsername: EditText? = null
    private var mPassword: EditText? = null
    private var mView: View? = null
    private var dialog: Dialog? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_auth, null)
        mView = view
        mUsername = view.findViewById<View>(R.id.txt_username) as EditText
        mPassword = view.findViewById<View>(R.id.txt_password) as EditText
        mUsername!!.requestFocus()
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle(R.string.padview_dialog_basicatuh_title)
        builder.setPositiveButton(getString(R.string.ok)
        ) { dialog, whichButton ->
            val username = mUsername!!.text.toString()
            val password = mPassword!!.text.toString()
            onPositiveButtonClick(username, password)
        }
        builder.setNegativeButton(R.string.cancel
        ) { dialog, whichButton ->
            dialog.dismiss()
            onNegativeButtonClick()
        }
        dialog = builder.create()
        onDialogCreated(dialog, view)
        return dialog as AlertDialog
    }

    protected open fun onDialogCreated(dialog: Dialog?, view: View?) {}
    protected abstract fun onPositiveButtonClick(username: String?, password: String?)
    protected abstract fun onNegativeButtonClick()
}