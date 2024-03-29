package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.mikifus.padland.R


/**
 * Created by mikifus on 10/03/16.
 */
class EditPadGroupDialog: FormDialog() {

    private var mEditText: EditText? = null
    private var data: Map<String, Any?> = mapOf()

    override fun validateForm(): Boolean {
        val text = mEditText!!.text.toString()

        if(!NAME_VALIDATION.matcher(text).matches()) {
            Toast.makeText(context, getString(R.string.padlist_dialog_new_padgroup_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    override fun setFormData(data: HashMap<String, Any>) {
        this.data = data
        applyFormData()
    }

    private fun applyFormData() {
        data["name"]?.let {
            mEditText!!.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
    }

    override fun getFormData(): Map<String, Any> {
        val text = mEditText!!.text.toString()

        val data = HashMap<String, Any>()
        data["name"] = text

        return data
    }

    override fun clearForm() {
        mEditText!!.text = null
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

    override fun initToolBar() {
        super.initToolBar()

        toolbar!!.title = getString(R.string.edit)
    }

    override fun onResume() {
        super.onResume()
        applyFormData()
    }
}