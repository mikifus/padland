package com.mikifus.padland.Dialog

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mikifus.padland.R

/**
 * Created by mikifus on 27/02/18.
 */
open class FormDialog    /*R.layout.dialog_new_server*/(protected var title: String, private var callbackObject: FormDialogCallBack) : DialogFragment() {
    protected var view = 0
    private var currentDialog: Dialog? = null
    protected var mainView: View? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mainView = requireActivity().layoutInflater.inflate(view, null)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(mainView)
        builder.setTitle(title)
        builder.setPositiveButton(getString(R.string.ok)
        ) { dialog, whichButton ->
            //Do nothing here because we override this button later to change the close behaviour.
            //However, we still need this because on older versions of Android unless we
            //pass a handler the button doesn't get instantiated
        }
                .setNegativeButton(R.string.cancel
                ) { dialog, whichButton ->
                    callbackObject.onDialogDismiss()
                    dialog.dismiss()
                }
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.setCancelable(false)
        currentDialog = alertDialog
        setViewEvents()
        return alertDialog
    }

    protected open fun validateForm(): Boolean {
        return true
    }

    protected open fun saveData() {}
    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                if (validateForm()) {
                    saveData()
                    callbackObject.onDialogSuccess()
                    callbackObject.onDialogDismiss()
                    d.dismiss()
                }
            }
        }
    }

    protected open val contentValues: ContentValues?
        get() = ContentValues()

    protected open fun setViewEvents() {}
    interface FormDialogCallBack {
        fun onDialogDismiss()
        fun onDialogSuccess()
    }

    companion object {
        const val TAG = "FormDialog"
    }
}