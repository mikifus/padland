package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikifus.padland.R

class ConfirmDialog: DialogFragment() {
    private var title: String = ""
    private var icon: Int? = null
    private var message: String = ""
    var positiveButtonText: String? = null
    var positiveButtonCallback: OnClickListener? = null
    var negativeButtonText: String? = null
    var negativeButtonCallback: OnClickListener? = OnClickListener { dialog, which -> dialog.dismiss() }
    var neutralButtonText: String? = null
    var neutralButtonCallback: OnClickListener? = null
    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if(positiveButtonText == null) {
            positiveButtonText = getString(R.string.ok)
        }
        if(negativeButtonText == null) {
            negativeButtonText = getString(R.string.cancel)
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, positiveButtonCallback)

        if(neutralButtonText !== null) {
            builder.setNeutralButton(neutralButtonText, neutralButtonCallback)
        }
        if(negativeButtonText !== null) {
            builder.setNegativeButton(negativeButtonText, negativeButtonCallback)
        }

        if(icon != null) {
            builder.setIcon(icon!!)
        }

        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.let { it() }
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun setIcon(icon: Int) {
        this.icon = icon
    }

    fun setMessage(message: String) {
        this.message = message
    }
}