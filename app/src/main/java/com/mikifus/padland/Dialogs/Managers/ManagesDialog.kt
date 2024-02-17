package com.mikifus.padland.Dialogs.Managers

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction

abstract class ManagesDialog {
    abstract val dialog: DialogFragment
    abstract val DIALOG_TAG: String

    fun showDialog(activity: AppCompatActivity) {
        if(dialog.isAdded || activity.supportFragmentManager.isDestroyed) {
            return
        }

        activity.supportFragmentManager.beginTransaction().apply {
            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            add(android.R.id.content, dialog, DIALOG_TAG)
            addToBackStack(null)
            commit()
        }
    }

    fun closeDialog(activity: AppCompatActivity) {
        activity.supportFragmentManager.beginTransaction().apply {
            remove(dialog)
            commit()
        }
    }
}