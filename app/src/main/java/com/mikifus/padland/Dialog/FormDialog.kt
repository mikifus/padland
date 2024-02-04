package com.mikifus.padland.Dialog

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.R
import kotlinx.coroutines.launch

/**
 * Created by mikifus on 27/02/18.
 */
open class FormDialog(protected var title: String, private var callbackObject: FormDialogCallBack) : DialogFragment() {
    protected var view: Int
    private var currentDialog: Dialog? = null
    protected var mainView: View? = null

    init {
        view = R.layout.dialog_pad_edit
    }

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

    protected open suspend fun validateForm(): Boolean {
        return true
    }

    protected open suspend fun saveData() {}
    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                lifecycleScope.launch{
                    if (validateForm()) {
                        saveData()
                        callbackObject.onDialogSuccess()
                        callbackObject.onDialogDismiss()
                        d.dismiss()
                    }
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