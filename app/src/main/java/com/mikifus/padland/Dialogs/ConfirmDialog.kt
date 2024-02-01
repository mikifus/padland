package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mikifus.padland.R

class ConfirmDialog: DialogFragment() {
    private var title: String = ""
    private var icon: Int? = null
    private var message: String = ""
    private var positiveButtonText: String? = null
    private var positiveButtonCallback: OnClickListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if(positiveButtonText == null) {
            positiveButtonText = requireActivity().getString(R.string.ok)
        }

        val builder = MaterialAlertDialogBuilder(requireActivity())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, positiveButtonCallback)
            .setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }

        if(icon != null) {
            builder.setIcon(icon!!)
        }

        return builder.create()
    }

    fun setPositiveButtonText(text: String) {
        positiveButtonText = text
    }

    fun setPositiveButtonCallback(callback: OnClickListener) {
        positiveButtonCallback = callback
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