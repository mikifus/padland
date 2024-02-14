package com.mikifus.padland.Dialogs

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.mikifus.padland.R
import java.util.regex.Pattern

interface IFormDialog {
    var dismissed: Boolean
    var positiveButtonCallback: OnClickListener?
    var negativeButtonCallback: OnClickListener?
    var onDismissCallback: (() -> Unit)?
    var toolbar: Toolbar?
    fun setPositiveButtonCallback(callback: (Map<String, Any>) -> Unit)
    fun setNegativeButtonCallback(callback: () -> Unit)
    fun validateForm(): Boolean { return false }
    fun setFormData(data: HashMap<String, Any>) {}
    fun getFormData(): Map<String, Any> { return mapOf() }
    fun clearForm() {}
    fun initEvents()
    fun initToolBar()
}

open class FormDialog: DialogFragment(), IFormDialog {
    override var dismissed = false

    override var toolbar: Toolbar? = null
    override var positiveButtonCallback: OnClickListener? = null
    override var negativeButtonCallback: OnClickListener? = null
    override var onDismissCallback: (() -> Unit)? = null

    private var onResumeCallback: (() -> Unit)? = null


    override fun getTheme(): Int {
        return R.style.DialogStyleWhenLarge
    }

    override fun setPositiveButtonCallback(callback: (Map<String, Any>) -> Unit) {
        positiveButtonCallback = OnClickListener { view ->
            if (validateForm()) {
                callback(getFormData())
            }
        }
    }

    override fun setNegativeButtonCallback(callback: () -> Unit) {
        negativeButtonCallback = OnClickListener { view ->
            callback()
            dismissed = true
            dismiss()
        }
    }

    fun setOnResumeCallback(callback: () -> Unit) {
        onResumeCallback = callback
    }

    override fun initEvents() {
        val positiveButton = toolbar?.findViewById<Button>(R.id.dialog_positive_button)
        positiveButton?.setOnClickListener(positiveButtonCallback)
    }

    override fun initToolBar() {
        toolbar!!.title = getString(R.string.edit)
        toolbar!!.setNavigationOnClickListener(
            if(negativeButtonCallback != null) {
                negativeButtonCallback
            } else OnClickListener {
                dismiss()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dialog_toolbar)
        initToolBar()
        initEvents()
    }

    override fun onResume() {
        super.onResume()
        onResumeCallback?.let { it() }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!dismissed) {
            onDismissCallback?.let { it() }
        }
        dismissed = false
    }

    companion object {
        val NAME_VALIDATION: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\@\\ \\:\\/\\?\\!\\¿\\¡\\#\\|\\&\\=\\·\\$]{2,256}"
        )
    }
}