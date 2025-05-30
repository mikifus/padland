package com.mikifus.padland.Dialogs

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.Views.Helpers.SpinnerHelper


/**
 * Created by mikifus on 10/03/16.
 */
class EditPadDialog: NewPadDialog() {

    private var data: Map<String, Any>? = null

    override fun setFormData(data: HashMap<String, Any>) {
        this.data = data
        applyFormData()
    }

    private fun applyFormData() {
        data?.get("name")?.let {
            mNameEditText?.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("local_name")?.let {
            mAliasEditText?.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("server")?.let {
            val index = serverSpinnerData?.indexOfFirst { it ->
                it.second == data!!["server"]
            }!!
            if(index > -1) {
                mServerSpinner?.selectedItemPosition = index
            }
        }
        data?.get("group_id")?.let {
            val index = padGroupsSpinnerData?.indexOfFirst { it.mId == data!!["group_id"] }!!
            if(index > -1) {
                mPadGroupSpinner?.selectedItemPosition = index
            }
        }
        data?.get("url")?.let {
            for (prefix in documentTypeSpinnerData!!) {
                if(it.toString().contains(prefix.second)) {
                    mDocumentTypeSpinner?.selectedItemPosition = documentTypeSpinnerData?.indexOfFirst { it -> it == prefix } ?: 0
                    break
                }
            }
        }
    }

    private fun initViewModels() {
        if(padGroupViewModel == null) {
            padGroupViewModel = ViewModelProvider(this)[PadGroupViewModel::class.java]
        }
        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(this)[ServerViewModel::class.java]
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

        serverViewModel!!.getAll.observe(this) { servers ->
            // Get DB servers
            serverSpinnerData = servers.map { Triple(it.mName, it.mUrl + it.mPadprefix, it.mCryptPad) }

            // Get hardcoded servers
            val cryptPadInfo = resources.obtainTypedArray(R.array.etherpad_servers_cryptpad)
            serverSpinnerData = serverSpinnerData!! + resources.getStringArray(R.array.etherpad_servers_name)
                .zip(resources.getStringArray(R.array.etherpad_servers_url_padprefix))
                .mapIndexed{ i, t -> Triple<String, String, Boolean>(t.first, t.second, cryptPadInfo.getBoolean(i, false)) }

            cryptPadInfo.recycle()

            // Set adapter
            mServerSpinner?.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.recyclerview_item,
                    serverSpinnerData!!.map { it.first }
                )
            )

            mServerSpinner?.selectedItemPosition = 0
            applyFormData()
        }

        // CryptPad Document Types
        documentTypeSpinnerData = resources.getStringArray(R.array.prefixes_name_cryptpad)
            .zip(resources.getStringArray(R.array.prefixes_cryptpad))
            .mapIndexed{ _, t -> Pair<String, String>(t.first, t.second) }

        // Set adapter
        mDocumentTypeSpinner?.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.recyclerview_item,
                documentTypeSpinnerData!!.map { it.first }
            )
        )
    }

    override fun clearForm() {
        mNameEditText!!.text = null
        mAliasEditText!!.text = null
        mPadGroupSpinner!!.setSelection(0)
        mServerSpinner!!.setSelection(0)
        mDocumentTypeSpinner!!.setSelection(0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.dialog_edit_pad, container, false)

        mNameEditText = v.findViewById(R.id.txt_pad_name)
        mAliasEditText = v.findViewById(R.id.txt_pad_local_name)
        mPadGroupSpinner = v.findViewById(R.id.spinner_pad_pad_group)
        mServerSpinner = v.findViewById(R.id.spinner_pad_server)
        mDocumentTypeSpinner = v.findViewById(R.id.spinner_cryptpad_prefix)
        mDocumentTypeSpinnerContainer = v.findViewById(R.id.spinner_cryptpad_prefix_container)

        return v
    }

    override fun initToolBar() {
        super.initToolBar()

        toolbar!!.title = getString(R.string.padlist_dialog_edit_pad_title)
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
        applyFormData()
    }
}