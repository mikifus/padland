package com.mikifus.padland.Dialogs.Managers

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.color.DynamicColors
import com.google.android.material.shape.MaterialShapeDrawable
import com.mikifus.padland.R

abstract class ManagesDialog {
    abstract val dialog: DialogFragment
    abstract val DIALOG_TAG: String

    fun showDialog(activity: AppCompatActivity) {
        if(dialog.isAdded || activity.supportFragmentManager.isDestroyed) {
            return
        }

        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .add(android.R.id.content, dialog)
            .addToBackStack(null)
            .commit()
//        if (activity.resources.getBoolean(R.bool.large_layout)) {
//            dialog.show(activity.supportFragmentManager,
//                DIALOG_TAG
//            )
//        } else {
//            val transaction = activity.supportFragmentManager.beginTransaction()
//            transaction
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .add(android.R.id.content, dialog)
//                .addToBackStack(null)
//                .commit()
//        }
    }

    fun closeDialog(activity: AppCompatActivity) {
        activity.supportFragmentManager.beginTransaction().remove(dialog).commit()
    }
}