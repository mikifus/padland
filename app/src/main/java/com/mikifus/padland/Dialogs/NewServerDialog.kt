package com.mikifus.padland.Dialogs

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.mikifus.padland.R
import java.lang.Exception
import java.net.URL


/**
 * Created by mikifus on 10/03/16.
 */
open class NewServerDialog: FormDialog() {

    protected var mNameEditText: EditText? = null
    protected var mUrlEditText: EditText? = null
    protected var mPadPrefixEditText: EditText? = null
    protected var mLiteCheckbox: CheckBox? = null
    protected var mCryptPadCheckbox: CheckBox? = null
    protected var mJqueryCheckBox: CheckBox? = null
    private var mAdvancedButton: Button? = null
    private var mAdvancedLayout: LinearLayout? = null

    var initialName: String? = null
    var initialUrl: String? = null
    var initialPrefix: String? = null
    var initialCryptPad: Boolean? = false

    var isNew: Boolean = true

    override fun validateForm(): Boolean {
        val name = mNameEditText!!.text.toString()
        val url = mUrlEditText!!.text.toString()
        val pathprefix = mPadPrefixEditText!!.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_name_invalid), Toast.LENGTH_LONG).show()
            return false
        }
//        if(!NAME_VALIDATION.matcher(name).matches()) {
//            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_name_invalid), Toast.LENGTH_LONG).show()
//            return false
//        }

        // Build URL, it will throw an exception if not correct
        try {
            URL(url)
        } catch (exception: Exception) {
            Toast.makeText(context, getString(R.string.validation_url_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        // Now with pad prefix
        try {
            URL(url + pathprefix)
        } catch (exception: Exception) {
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_padprefix_invalid), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    override fun getFormData(): Map<String, Any> {
        val name = mNameEditText!!.text.toString()
        val url = mUrlEditText!!.text.toString()
        val jquery = mJqueryCheckBox!!.isChecked
        var padprefix = mPadPrefixEditText!!.text.toString()
        val cryptpad = mCryptPadCheckbox!!.isChecked

        val saveUrl = URL(url)
            .toString()
            .replace("/$".toRegex(), "")

        if (padprefix.isNotEmpty()) {
            // Must start and end with /
            if (padprefix.startsWith(saveUrl)) {
                // Autodetect from full URL
                padprefix = padprefix.substring(saveUrl.length)
            }
            if (!padprefix.startsWith("/")) {
                padprefix = "/$padprefix"
            }
            if (!padprefix.endsWith("/")) {
                padprefix = "$padprefix/"
            }
        }

        val data = HashMap<String, Any>()
        data["name"] = name
        data["url"] = saveUrl
        data["prefix"] = padprefix
        data["jquery"] = jquery
        data["cryptpad"] = cryptpad

        return data
    }

    override fun clearForm() {
        mNameEditText!!.text = null
        mUrlEditText!!.text = null
        mLiteCheckbox?.isChecked = true
        mPadPrefixEditText!!.text = Editable.Factory.getInstance().newEditable(getString(R.string.default_pad_prefix_lite))
        mJqueryCheckBox?.isChecked = true
        mCryptPadCheckbox?.isChecked = false

        initialName = null
        initialUrl = null
        initialPrefix = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mNameEditText = requireView().findViewById(R.id.txt_server_name)
        mUrlEditText = requireView().findViewById(R.id.txt_server_url)
        mPadPrefixEditText = requireView().findViewById(R.id.txt_server_padprefix)
        mLiteCheckbox = requireView().findViewById(R.id.checkbox_lite)
        mCryptPadCheckbox = requireView().findViewById(R.id.checkbox_cryptpad)
        mJqueryCheckBox = requireView().findViewById(R.id.checkbox_jquery)
        mAdvancedButton = requireView().findViewById(R.id.button_advanced)
        mAdvancedLayout = requireView().findViewById(R.id.layout_advanced)

        mNameEditText?.requestFocus()
        mPadPrefixEditText?.text = Editable.Factory.getInstance().newEditable(getString(R.string.default_pad_prefix_lite))

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_edit_server, container, false)
    }

    override fun initToolBar() {
        super.initToolBar()
        toolbar!!.title = getString(R.string.serverlist_dialog_new_server_title)
    }

    override fun initEvents() {
        super.initEvents()

        mAdvancedButton?.setOnClickListener {
            mAdvancedLayout?.visibility =  if (mAdvancedLayout?.visibility == View.GONE){
                View.VISIBLE
            } else{
                View.GONE
            }
            if(mAdvancedLayout?.visibility == View.VISIBLE) {
                // Will scroll a bit down to reveal the new content.
                // Will incidentally focus the input on that content which is ok.
                mAdvancedLayout!!.requestFocus()
            }
        }
        mUrlEditText?.addTextChangedListener{ url ->
            if (isNew && url?.contains("cryptpad") == true) {
                mCryptPadCheckbox?.isChecked = true
                mLiteCheckbox?.isChecked = false
            }
        }
        mPadPrefixEditText?.addTextChangedListener { padprefix ->
            mLiteCheckbox?.isChecked = (padprefix.toString() ==
                    activity?.getString(R.string.default_pad_prefix_lite)
                    && mJqueryCheckBox?.isChecked == true)
        }
        mLiteCheckbox?.setOnCheckedChangeListener { compoundButton, b ->
            if (b && mCryptPadCheckbox?.isChecked == true) {
                mCryptPadCheckbox?.isChecked = false
            }
            if (b && mPadPrefixEditText?.text.toString() !=
                activity?.getString(R.string.default_pad_prefix_lite))
            {
                mPadPrefixEditText?.text =
                    Editable.Factory.getInstance().newEditable(
                        activity?.getString(R.string.default_pad_prefix_lite))
            }
            if (b && mJqueryCheckBox?.isChecked == false) {
                mJqueryCheckBox?.isChecked = true
            } else if(!b) {
                mJqueryCheckBox?.isChecked = false
            }
        }
        mJqueryCheckBox?.setOnCheckedChangeListener { _, b ->
            if (!b && activity?.getString(R.string.default_pad_prefix_cryptpad) == mPadPrefixEditText?.text.toString()) {
                mCryptPadCheckbox?.isChecked = true
            } else if(b && mPadPrefixEditText?.text.toString() ==
                activity?.getString(R.string.default_pad_prefix_lite)) {
                mLiteCheckbox?.isChecked = true
            }
        }
        mCryptPadCheckbox?.setOnCheckedChangeListener { _, b ->
            if (b && mLiteCheckbox?.isChecked == true) {
                mLiteCheckbox?.isChecked = false
            }
            if (b && activity?.getString(R.string.default_pad_prefix_cryptpad)  != mPadPrefixEditText?.text.toString())
            {
                mPadPrefixEditText?.text =
                    Editable.Factory.getInstance().newEditable(
                        activity?.getString(R.string.default_pad_prefix_cryptpad))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initialName?.let {
            mNameEditText?.text = Editable.Factory.getInstance().newEditable(initialName)
        }
        initialUrl?.let {
            mUrlEditText?.text = Editable.Factory.getInstance().newEditable(initialUrl)
        }
        initialPrefix?.let {
            mPadPrefixEditText?.text = Editable.Factory.getInstance().newEditable(initialPrefix)
        }
    }
}