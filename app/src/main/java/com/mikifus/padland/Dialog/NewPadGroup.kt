package com.mikifus.padland.Dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.mikifus.padland.Dialogs.FormDialog
import com.mikifus.padland.R


/**
 * Created by mikifus on 10/03/16.
 */
class NewPadGroup: FormDialog() {

    private var mEditText: EditText? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.padlist_dialog_new_padgroup_title)
        return dialog
    }

    override fun validateForm(): Boolean {
        val text = mEditText!!.text.toString()

        if(!NAME_VALIDATION.matcher(text).matches()) {
            Toast.makeText(context, getString(R.string.padlist_dialog_new_padgroup_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    override fun getFormData(): Map<String, Any> {
        val text = mEditText!!.text.toString()

        val data = HashMap<String, Any>()
        data["name"] = text

        return mapOf("name" to text)
    }

    override fun onStart() {
        super.onStart()

        mEditText = requireView().findViewById<View>(R.id.txt_padgroup_name) as EditText
        mEditText?.requestFocus()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.dialog_new_padgroup, container, false)
    }
}