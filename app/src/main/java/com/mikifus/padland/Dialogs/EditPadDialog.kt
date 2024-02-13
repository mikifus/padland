package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadUrl
import com.mikifus.padland.Utils.Views.Helpers.SpinnerHelper
import java.lang.Exception


/**
 * Created by mikifus on 10/03/16.
 */
class EditPadDialog: FormDialog() {

    private var data: Map<String, Any>? = null

    private var padGroupViewModel: PadGroupViewModel? = null
    private var serverViewModel: ServerViewModel? = null

    private var mNameEditText: EditText? = null
    private var mAliasEditText: EditText? = null
    private var mServerSpinner: SpinnerHelper? = null
    private var mPadGroupSpinner: SpinnerHelper? = null
    private var mSaveCheckBox: CheckBox? = null

    private var padGroupsSpinnerData: List<PadGroup>? = listOf()
    private var serverSpinnerData: List<Pair<String, String>>? = listOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(R.string.padlist_dialog_edit_pad_title)
        return dialog
    }

    override fun setFormData(data: HashMap<String, Any>) {
        this.data = data
        applyFormData()
    }

    private fun applyFormData() {
        data?.get("name")?.let {
            mNameEditText!!.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("local_name")?.let {
            mAliasEditText!!.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
        data?.get("server")?.let {
            val index = serverSpinnerData?.indexOfFirst { it.second == data!!["server"] }!!
            mServerSpinner?.selectedItemPosition = if(index > -1) index else 0
        }
        data?.get("group_id")?.let {
            val index = padGroupsSpinnerData?.indexOfFirst { it.mId == data!!["group_id"] }!!
            mPadGroupSpinner?.selectedItemPosition = if(index > -1) index else 0
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
            serverSpinnerData = servers.map { Pair(it.mName, it.mUrl + it.mPadprefix) }

            // Get hardcoded servers
            serverSpinnerData = serverSpinnerData!! + resources.getStringArray(R.array.etherpad_servers_name)
                .zip(
                    resources.getStringArray(R.array.etherpad_servers_url_padprefix)
                ) { a,b -> Pair<String, String>(a, b) }

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
    }

    private fun initCheckBox() {
        // Get new pad save from user preferences
        val userDetails = context?.getSharedPreferences(context?.packageName + "_preferences",
            AppCompatActivity.MODE_PRIVATE
        )

        val savePadOption = userDetails?.getBoolean("auto_save_new_pads", true)
        mSaveCheckBox?.isChecked = savePadOption!!
    }

    override fun validateForm(): Boolean {
        val name = mNameEditText!!.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(context, getString(R.string.newpad_noname_warning), Toast.LENGTH_LONG).show()
            return false
        }
        if(!NAME_VALIDATION.matcher(name).matches()) {
            Toast.makeText(context, getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        val server: Pair<String, String> = serverSpinnerData!![mServerSpinner!!.selectedItemPosition]

        // Build URL, it will throw an exception if not correct
        var padUrl: PadUrl? = null
        try {
            padUrl = PadUrl.Builder()
                .padName(name)
                .padPrefix(server.second)
                .build()
        } catch (exception: Exception) {
            Toast.makeText(context, getString(R.string.validation_url_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        if (!URLUtil.isValidUrl(padUrl.string)) {
            Toast.makeText(context, getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    override fun getFormData(): Map<String, Any> {
        val name = mNameEditText!!.text.toString()
        val localName = mAliasEditText!!.text.toString()
        val server: Pair<String, String> = serverSpinnerData!![mServerSpinner!!.selectedItemPosition]
        val groupId = padGroupsSpinnerData!![mPadGroupSpinner!!.selectedItemPosition].mId

        // Build URL
        val url = PadUrl.Builder()
            .padName(name)
            .padPrefix(server.second)
            .build()

        val data = HashMap<String, Any>()
        data["name"] = name
        data["local_name"] = localName
        data["url"] = url.string
        data["server"] = server.second
        data["group_id"] = groupId

        return data
    }

    override fun clearForm() {
        mNameEditText!!.text = null
        mAliasEditText!!.text = null
        mPadGroupSpinner!!.setSelection(0)
        mServerSpinner!!.setSelection(0)
    }

    override fun onStart() {
        super.onStart()

        mNameEditText = requireView().findViewById(R.id.txt_pad_name)
        mNameEditText?.requestFocus()
        mAliasEditText = requireView().findViewById(R.id.txt_pad_local_name)
        mPadGroupSpinner = requireView().findViewById(R.id.spinner_pad_pad_group)
        mServerSpinner = requireView().findViewById(R.id.spinner_pad_server)
        mSaveCheckBox = requireView().findViewById(R.id.checkbox_pad_save)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.dialog_edit_pad, container, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        if(activity?.supportFragmentManager?.isDestroyed == true) {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }
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
        initCheckBox()
        applyFormData()
    }
}