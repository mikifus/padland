package com.mikifus.padland.Dialogs.Managers;

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Activities.PadViewActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Dialogs.NewPadDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.CryptPad.CryptPadUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.HashMap

interface IManagesNewPadDialog {
    var padViewModel: PadViewModel?
    var padGroupViewModel: PadGroupViewModel?
    fun showNewPadDialog(activity: AppCompatActivity,
                         animationOriginView: View? = null,
                         url: String? = null)

    fun initUrl(url: String?) {}
}

public class ManagesNewPadDialog: ManagesDialog(), IManagesNewPadDialog {
    override val DIALOG_TAG: String = "DIALOG_NEW_PAD"

    override val dialog by lazy { NewPadDialog() }
    override var padViewModel: PadViewModel? = null
    override var padGroupViewModel: PadGroupViewModel? = null

    override fun showNewPadDialog(activity: AppCompatActivity,
                                  animationOriginView: View?,
                                  url: String?
    ) {
        showDialog(activity)
        initViewModels(activity)
        initEvents(activity)
        initAnimations(animationOriginView)
        initUrl(url)
    }

    private fun initViewModels(activity: AppCompatActivity) {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(activity)[PadViewModel::class.java]
        }
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(activity)[PadGroupViewModel::class.java]
        }
    }

    private fun initEvents(activity: AppCompatActivity) {
        dialog.setPositiveButtonCallback { data ->
            if (data["save_pad"] as Boolean && !(data["_isCryptPadUrl"] as Boolean)) {
                saveNewPadDialog(activity, data)
            } else {
                val padViewIntent = Intent(activity, PadViewActivity::class.java)
                var url = data["url"].toString()
                padViewIntent.data = Uri.parse(url)
                padViewIntent.putExtra("android.intent.extra.TEXT", url)
                if (data["_isCryptPadUrl"] as Boolean && data["save_pad"] as Boolean) {
                    url = CryptPadUtils.applyNewPadUrl(url)

                    padViewIntent.data = Uri.parse(url)
                    padViewIntent.putExtra("android.intent.extra.TEXT", url)
                    padViewIntent.putExtra("localName", data["local_name"] as String)
                    padViewIntent.putExtra("deferredSave", true)
                } else {
                    padViewIntent.putExtra("padUrlDontSave", true)
                }
                activity.startActivity(padViewIntent)
            }
            dialog.clearForm()
            closeDialog(activity)
        }
    }

    private fun initAnimations(animationOriginView: View?) {
        dialog.animationOriginView = animationOriginView
    }

    private fun saveNewPadDialog(activity: AppCompatActivity, data: Map<String, Any>) {
        val pad: Pad = Pad.fromData(data).value!!
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val padId = padViewModel?.insertPad(pad)

            if(padId != null && data["group_id"] as Long > 0) {
                padGroupViewModel?.insertPadGroupsAndPadList(
                    PadGroupsAndPadList(
                        mGroupId = data["group_id"] as Long,
                        mPadId = padId,
                    )
                )
            }

            if(padId != null) {
                val padViewIntent = Intent(activity, PadViewActivity::class.java)
                padViewIntent.putExtra("padId", padId)
                activity.startActivity(padViewIntent)
            }
        }
    }

    override fun initUrl(url: String?) {
        url?.let { dialog.setFormData(hashMapOf("url" to url)) }
    }
}
