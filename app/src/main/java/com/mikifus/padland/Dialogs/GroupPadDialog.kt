package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.Views.Helpers.SpinnerHelper


/**
 * Created by mikifus on 10/03/16.
 */
class GroupPadDialog: FormDialog() {

    private var data: Map<String, Any>? = null

    private var padGroupViewModel: PadGroupViewModel? = null

    private var mPadGroupSpinner: SpinnerHelper? = null

    private var padGroupsSpinnerData: List<PadGroup>? = listOf()

    override fun setFormData(data: HashMap<String, Any>) {
        this.data = data
        applyFormData()
    }

    private fun applyFormData() {
        data?.get("group_id")?.let {
            val index = padGroupsSpinnerData?.indexOfFirst { it.mId == data!!["group_id"] }!!
            mPadGroupSpinner?.selectedItemPosition = if(index > -1) index else 0
        }
    }

    private fun initViewModels() {
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(this)[PadGroupViewModel::class.java]
        }

        padGroupViewModel!!.getAll.observe(this) { padGroups ->
            padGroupsSpinnerData = listOf(
                    PadGroup.fromName(getString(R.string.padlist_group_unclassified_name)).value!!
                ) + padGroups

            mPadGroupSpinner?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.recyclerview_item,
                    padGroupsSpinnerData!!.map { it.mName }
                )
            )

            mPadGroupSpinner?.selectedItemPosition = 0
            applyFormData()
        }
    }

    override fun validateForm(): Boolean {
        return true
    }

    override fun getFormData(): Map<String, Any> {
        val data = HashMap<String, Any>()
        data["group_id"] = padGroupsSpinnerData!![mPadGroupSpinner!!.selectedItemPosition].mId

        return data
    }

    override fun clearForm() {
        mPadGroupSpinner!!.setSelection(0)
    }

    override fun onStart() {
        super.onStart()
        mPadGroupSpinner = requireView().findViewById(R.id.spinner_pad_pad_group)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.dialog_group_pad, container, false)
    }

    override fun initToolBar() {
        super.initToolBar()

        toolbar!!.title = getString(R.string.padlist_group_select_dialog)
    }

    /**
     * initViewModels() must be called here. The spinners will
     * only set the adapter when called from onResume().
     *
     *
     * @see SpinnerHelper
     * @see https://github.com/material-components/material-components-android/issues/1464
     * @see https://github.com/material-components/material-components-android/issues/2012
     */
    override fun onResume() {
        super.onResume()
        initViewModels()
    }

    override fun getTheme(): Int = R.style.DialogStyleMinWidth
}