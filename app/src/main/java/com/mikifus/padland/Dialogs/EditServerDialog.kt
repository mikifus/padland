package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import com.mikifus.padland.R

class EditServerDialog: NewServerDialog() {

    private var data: Map<String, Any>? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.edit)
        return dialog
    }

    override fun setFormData(data: HashMap<String, Any>) {
        this.data = data
        applyFormData()
    }

    private fun applyFormData() {
        data?.get("name")?.let {
            mNameEditText?.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("url")?.let {
            mUrlEditText?.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("prefix")?.let {
            mPadPrefixEditText?.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("jquery")?.let {
            mJqueryCheckBox?.isChecked = it as Boolean
            if(!it) {
                mLiteCheckbox?.isChecked = false
            }
        }
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