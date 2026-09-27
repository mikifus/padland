package com.mikifus.padland.Dialogs.Managers

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.Dialogs.GroupPadDialog

interface IManagesChooseGroupDialog {
    /**
     * Shows the group selector and returns the chosen group id through [onGroupChosen]
     * (0 means unclassified). Nothing is saved by the dialog itself.
     * Nothing is called if the user cancels.
     */
    fun showChooseGroupDialog(activity: AppCompatActivity,
                              title: String? = null,
                              preselectedGroupId: Long = 0,
                              animationOriginView: View? = null,
                              onGroupChosen: (groupId: Long) -> Unit)
}

class ManagesChooseGroupDialog: ManagesDialog(), IManagesChooseGroupDialog {
    override val DIALOG_TAG: String = "DIALOG_CHOOSE_GROUP"

    override val dialog by lazy { GroupPadDialog() }

    override fun showChooseGroupDialog(activity: AppCompatActivity,
                                       title: String?,
                                       preselectedGroupId: Long,
                                       animationOriginView: View?,
                                       onGroupChosen: (groupId: Long) -> Unit) {
        // Set before the view is created, the dialog instance is reused between calls
        dialog.title = title
        dialog.animationOriginView = animationOriginView

        showDialog(activity)

        dialog.setPositiveButtonCallback { data ->
            val groupId = data["group_id"] as? Long ?: 0L
            dialog.clearForm()
            closeDialog(activity)
            onGroupChosen(groupId)
        }
        dialog.setFormData(hashMapOf("group_id" to preselectedGroupId))
    }
}

