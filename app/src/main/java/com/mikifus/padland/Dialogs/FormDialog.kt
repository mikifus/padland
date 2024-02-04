package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.mikifus.padland.R
import java.util.regex.Pattern

interface IFormDialog {
    var positiveButtonCallback: View.OnClickListener?
    var toolbar: Toolbar?
    fun setPositiveButtonCallback(callback: (Map<String, Any>) -> Unit)
    fun validateForm(): Boolean
    fun getFormData(): Map<String, Any>
    fun clearForm()
    fun initEvents()
    fun initToolBar()
}

open class FormDialog: DialogFragment(), IFormDialog {
    override var positiveButtonCallback: View.OnClickListener? = null
    override var toolbar: Toolbar? = null

    override fun getTheme(): Int {
        return if (activity?.resources!!.getBoolean(R.bool.large_layout))
            R.style.Theme_MaterialComponents_Dialog_MinWidth
        else R.style.Theme_MaterialComponents_DialogWhenLarge
    }

    override fun setPositiveButtonCallback(callback: (Map<String, Any>) -> Unit) {
        positiveButtonCallback = View.OnClickListener() { view ->
            if (validateForm()) {
                callback(getFormData())
            }
        }
    }

    override fun validateForm(): Boolean {
        return false
    }

    override fun getFormData(): Map<String, Any> {
        return mapOf()
    }

    override fun clearForm() {
    }

    override fun initEvents() {
        val positiveButton = toolbar?.findViewById<Button>(R.id.dialog_positive_button)
        positiveButton?.setOnClickListener(positiveButtonCallback)
    }

    override fun initToolBar() {
        toolbar!!.title = getString(R.string.padlist_dialog_new_padgroup_title)
        toolbar!!.setNavigationIcon(R.drawable.ic_arrow_back_white)
        toolbar!!.setNavigationOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dialog_toolbar)
        initToolBar()
        initEvents()
    }

    companion object {
        val NAME_VALIDATION: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+\\@\\ ]{2,256}"
        )
    }
}