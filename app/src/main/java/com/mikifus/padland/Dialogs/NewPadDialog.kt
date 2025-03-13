package com.mikifus.padland.Dialogs

import android.content.Intent
import android.net.Uri
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
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.mikifus.padland.Activities.PadViewActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupViewModel
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.CryptPad.CryptPadUtils
import com.mikifus.padland.Utils.PadClipboardHelper
import com.mikifus.padland.Utils.PadServer
import com.mikifus.padland.Utils.PadUrl
import com.mikifus.padland.Utils.Views.Helpers.SpinnerHelper
import java.net.MalformedURLException
import java.net.URL


/**
 * Created by mikifus on 10/03/16.
 */
open class NewPadDialog: FormDialog() {

    var padGroupViewModel: PadGroupViewModel? = null
    var serverViewModel: ServerViewModel? = null

    var mButtonPaste: MaterialButton? = null
    var mNameEditText: EditText? = null
    var mPadNameContainer: TextInputLayout? = null
    var mAliasEditText: EditText? = null
    var mServerSpinner: SpinnerHelper? = null
    var mButtonCryptpadDrive: MaterialButton? = null
    var mPadGroupSpinner: SpinnerHelper? = null
    var mSaveCheckBox: CheckBox? = null
    var mDocumentTypeSpinner: SpinnerHelper? = null
    var mDocumentTypeSpinnerContainer: TextInputLayout? = null

    var padGroupsSpinnerData: List<PadGroup>? = listOf()
    var serverSpinnerData: List<Triple<String, String, Boolean>>? = listOf()
    var documentTypeSpinnerData: List<Pair<String, String>>? = listOf()

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
        }

        serverViewModel!!.getAll.observe(this) { servers ->
            // Get DB servers
            serverSpinnerData = servers.map {
                Triple(it.mName, PadServer.Builder().padUrl(it.mUrl + it.mPadprefix).build().baseUrl!!, it.mCryptPad)
            }

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

            // Get default server from user preferences
            val userDetails = context?.getSharedPreferences(context?.packageName + "_preferences",
                AppCompatActivity.MODE_PRIVATE
            )

            // Fallback to first in list
            val defaultServer = userDetails?.getString("padland_default_server", serverSpinnerData!![0].second)
            val position = serverSpinnerData?.indexOfFirst { it.second == defaultServer } ?: 0

            // Set default selection
            mServerSpinner?.selectedItemPosition = position
            checkCryptPadServer()
        }

        // CryptPad Document Types
        documentTypeSpinnerData = resources.getStringArray(R.array.prefixes_name_cryptpad)
            .zip(resources.getStringArray(R.array.prefixes_cryptpad))
            .mapIndexed{ i, t -> Pair<String, String>(t.first, t.second) }

        // Set adapter
        mDocumentTypeSpinner?.setAdapter(
            ArrayAdapter(
                requireContext(),
                R.layout.recyclerview_item,
                documentTypeSpinnerData!!.map { it.first }
            )
        )

        mDocumentTypeSpinner?.selectedItemPosition = 0
    }

    override fun initEvents() {
        super.initEvents()

        mButtonPaste?.setOnClickListener {
            mNameEditText?.text = Editable.Factory.getInstance().newEditable(PadClipboardHelper.getFromClipboard(requireContext() as AppCompatActivity))
        }

        mNameEditText?.addTextChangedListener { name ->
            // Check if just pasted URL
            try {
                URL(name.toString())
                // If it reaches this line, the name is an URL, remove the server baseurl
                var newName = name.toString()
                for (server in serverSpinnerData!!) {
                    if(newName.contains(server.second)) {
                        // Use the server defined by the URL
                        mServerSpinner?.selectedItemPosition = serverSpinnerData?.indexOfFirst { it == server } ?: 0
                        newName = name.toString().replace(server.second,"")
                        break
                    }
                }
                for (prefix in documentTypeSpinnerData!!) {
                    if("/".plus(newName).contains(prefix.second)) {
                        // Use the document type defined by the URL
                        mDocumentTypeSpinner?.selectedItemPosition = documentTypeSpinnerData?.indexOfFirst { it == prefix } ?: 0
                        newName = "/".plus(newName).replace(prefix.second,"")
                        break
                    }
                }
                mNameEditText?.text = Editable.Factory.getInstance().newEditable(newName)

                // Check for document type
                for (prefix in documentTypeSpinnerData!!) {
                    if("/".plus(newName).contains(prefix.second)) {
                        // Use the prefix defined by the URL
                        mDocumentTypeSpinner?.selectedItemPosition = documentTypeSpinnerData?.indexOfFirst { it == prefix } ?: 0
                        //newName = name.toString().replace(prefix.second,"")
                        break
                    }
                }
            } catch (e: MalformedURLException) {
                // Not an URL, perfect, do nothing
            }
        }

        mServerSpinner?.addTextChangedListener {
            mServerSpinner!!.post {
                checkCryptPadServer()
            }
        }

        mButtonCryptpadDrive?.setOnClickListener {
            val cryptPadDriveIntent = Intent(activity, PadViewActivity::class.java)
            val url = CryptPadUtils.applyDriveUrl(serverSpinnerData!![mServerSpinner!!.selectedItemPosition].second)
            cryptPadDriveIntent.data = Uri.parse(url)
            cryptPadDriveIntent.putExtra("android.intent.extra.TEXT", url)
            cryptPadDriveIntent.putExtra("padUrlDontSave", true)
            activity?.startActivity(cryptPadDriveIntent)
        }

        mDocumentTypeSpinner?.addTextChangedListener {
            mDocumentTypeSpinner!!.post {
                val docTypePrefix = documentTypeSpinnerData!![mDocumentTypeSpinner!!.selectedItemPosition].second
                val newName = CryptPadUtils.replaceCryptPadType(mNameEditText?.text.toString(), docTypePrefix)
                mNameEditText?.text = Editable.Factory.getInstance().newEditable(newName)
            }
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
        val server: Triple<String, String, Boolean> = serverSpinnerData!![mServerSpinner!!.selectedItemPosition]
        val documentType: Pair<String, String> = documentTypeSpinnerData!![mDocumentTypeSpinner!!.selectedItemPosition]
        val isCryptPadUrl = server.third

        var name = mNameEditText!!.text.toString()

        if(isCryptPadUrl && documentType.second.isEmpty()) {
            Toast.makeText(context, getString(R.string.newpad_nodoctype_warning), Toast.LENGTH_LONG).show()
            return false
        }

        if (!isCryptPadUrl && name.isEmpty()) {
            Toast.makeText(context, getString(R.string.newpad_noname_warning), Toast.LENGTH_LONG).show()
            return false
        }
        if(!isCryptPadUrl && !NAME_VALIDATION.matcher(name).matches()) {
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_name_title), Toast.LENGTH_LONG).show()
            return false
        }

        // Check if just pasted URL
        try {
            URL(name)
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_name_title), Toast.LENGTH_LONG).show()
            return false
        } catch (e: MalformedURLException) {
            // Not an URL, perfect, do nothing
        }

        var padPrefix = server.second
        if(isCryptPadUrl) {
            padPrefix = CryptPadUtils.makePadPrefixFromServerAndType(server.second, documentType.second)
            name = name.replace(documentType.second, "")
        }

        // Build URL, it will throw an exception if not correct
        val padUrl: PadUrl?
        try {
            padUrl = PadUrl.Builder()
                .padName(name)
                .padPrefix(padPrefix)
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
        var name = mNameEditText!!.text.toString()
        val localName = mAliasEditText!!.text.toString()
        val server: Triple<String, String, Boolean> = serverSpinnerData!![mServerSpinner!!.selectedItemPosition]
        val documentType: Pair<String, String> = documentTypeSpinnerData!![mDocumentTypeSpinner!!.selectedItemPosition]
        val isCryptPadUrl = server.third

        val groupId = padGroupsSpinnerData!![mPadGroupSpinner!!.selectedItemPosition].mId

        var padPrefix = server.second
        if(isCryptPadUrl) {
            padPrefix = CryptPadUtils.makePadPrefixFromServerAndType(server.second, documentType.second)
            name = name.replace(documentType.second, "")
        }

        // Build URL
        val url = PadUrl.Builder()
            .padName(name)
            .padPrefix(padPrefix)
            .build()

        val data = HashMap<String, Any>()
        data["name"] = name
        data["local_name"] = localName
        data["url"] = url.string
        data["server"] = server.second
        data["group_id"] = groupId
        data["_isCryptPadUrl"] = isCryptPadUrl

        data["save_pad"] = mSaveCheckBox?.isChecked!!

        return data
    }

    override fun clearForm() {
        mNameEditText?.text = null
        mAliasEditText?.text = null
        mPadGroupSpinner?.setSelection(0)
        mServerSpinner?.setSelection(0)
        mDocumentTypeSpinner?.setSelection(0)
    }

    override fun onStart() {
        super.onStart()
        mNameEditText?.requestFocus()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val v = inflater.inflate(R.layout.dialog_new_pad, container, false)

        mNameEditText = v.findViewById(R.id.txt_pad_name)
        mPadNameContainer = v.findViewById(R.id.pad_name_container)
        mButtonPaste = v.findViewById(R.id.button_paste)
        mAliasEditText = v.findViewById(R.id.txt_pad_local_name)
        mPadGroupSpinner = v.findViewById(R.id.spinner_pad_pad_group)
        mServerSpinner = v.findViewById(R.id.spinner_pad_server)
        mButtonCryptpadDrive = v.findViewById(R.id.button_cryptpad_drive)
        mDocumentTypeSpinner = v.findViewById(R.id.spinner_cryptpad_prefix)
        mDocumentTypeSpinnerContainer = v.findViewById(R.id.spinner_cryptpad_prefix_container)
        mSaveCheckBox = v.findViewById(R.id.checkbox_pad_save)

        return v
    }

    override fun initToolBar() {
        super.initToolBar()

        toolbar!!.title = getString(R.string.title_activity_new_pad)
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
    }

    private fun setCryptPadServer() {
        mDocumentTypeSpinnerContainer?.visibility = View.VISIBLE
        mButtonCryptpadDrive?.visibility = View.VISIBLE
        if (mNameEditText?.text.toString().isEmpty()) {
            mPadNameContainer?.visibility = View.GONE
        }
        mNameEditText?.isEnabled = false
    }

    private fun unsetCryptPadServer() {
        mDocumentTypeSpinnerContainer?.visibility = View.GONE
        mButtonCryptpadDrive?.visibility = View.GONE
        mPadNameContainer?.visibility = View.VISIBLE
        mNameEditText?.isEnabled = true
    }

    private fun checkCryptPadServer() {
        if (serverSpinnerData!![mServerSpinner!!.selectedItemPosition].third) {
            setCryptPadServer()
        } else {
            unsetCryptPadServer()
        }
    }

    override fun setFormData(data: HashMap<String, Any>) {
        data["url"]?.let {
            mNameEditText?.text = Editable.Factory.getInstance().newEditable(it.toString())
        }
    }
}