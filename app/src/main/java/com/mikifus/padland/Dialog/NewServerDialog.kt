package com.mikifus.padland.Dialog

import android.content.ContentValues
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.R
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern

/**
 * Created by mikifus on 7/07/16.
 */
class NewServerDialog(title: String, callback: FormDialogCallBack) : FormDialog(title, callback) {
    private var fieldName: EditText? = null
    private var fieldUrl: EditText? = null
    private var fieldPadprefix: EditText? = null
    private var checkLite: CheckBox? = null
    private var checkJquery: CheckBox? = null
    private var advancedButton: Button? = null
    private var advancedLayout: LinearLayout? = null

    //    private Dialog currentDialog;
    private var edit_server_id: Long = 0

    init {
        view = R.layout.dialog_new_server
    }

    override fun saveData() {
        val serverModel = ServerModel(activity)
        val contentValues = contentValues
        val url = contentValues!!.getAsString(ServerModel.Companion.URL)
        val prefix = contentValues.getAsString(ServerModel.Companion.PADPREFIX)
        var final_prefix = url
        if (!final_prefix.endsWith(prefix)) {
            final_prefix = final_prefix + prefix
        }
        contentValues.put(ServerModel.Companion.PADPREFIX, final_prefix)
        serverModel.saveServerData(edit_server_id, contentValues)
    }

    override fun validateForm(): Boolean {
        val contentValues = contentValues
        if (!NAME_VALIDATION.matcher(contentValues!!.getAsString(ServerModel.Companion.NAME)).matches()) {
            // TODO: Change toast for something better.
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_name_invalid), Toast.LENGTH_LONG).show()
            return false
        }
        val urlString = contentValues.getAsString(ServerModel.Companion.URL)
        var host = ""
        val urlParsed: URL
        try {
            urlParsed = URL(urlString)
            host = urlParsed.host
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        if (!URL_VALIDATION.matcher(urlString).matches()
                || host.isEmpty()) {
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_url_invalid), Toast.LENGTH_LONG).show()
            return false
        }
        if (!contentValues.getAsString(ServerModel.Companion.PADPREFIX).isEmpty()
                && !PADPREFIX_VALIDATION.matcher(contentValues.getAsString(ServerModel.Companion.PADPREFIX)).matches()) {
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_padprefix_invalid), Toast.LENGTH_LONG).show()
            return false
        }
        if (contentValues.getAsInteger(ServerModel.Companion.JQUERY) == null) {
            Log.e(TAG, "Something is wrong here")
            return false
        }
        return true
    }// Must start and end with /// Remove trailing slash

    //        Toast.makeText(getContext(), "Server saved", Toast.LENGTH_LONG).show();
    protected override val contentValues: ContentValues?
        protected get() {
//        Toast.makeText(getContext(), "Server saved", Toast.LENGTH_LONG).show();
            val values = super.getContentValues()
            val name = fieldName!!.text.toString().trim { it <= ' ' }
            values!!.put(ServerModel.Companion.NAME, name)
            var url = fieldUrl!!.text.toString().trim { it <= ' ' }
            try {
                val url_object = URL(url)
                url = url_object.toString()
                url = url.replace("/$".toRegex(), "") // Remove trailing slash
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            values!!.put(ServerModel.Companion.URL, url)
            var padprefix = fieldPadprefix!!.text.toString()
            if (!padprefix.isEmpty()) {
                // Must start and end with /
                if (!padprefix.startsWith("/")) {
                    padprefix = "/$padprefix"
                }
                if (!padprefix.endsWith("/")) {
                    padprefix = "$padprefix/"
                }
            }
            values!!.put(ServerModel.Companion.PADPREFIX, padprefix)
            val jquery = if (checkJquery!!.isChecked.toString() === "true") 1 else 0
            values!!.put(ServerModel.Companion.JQUERY, jquery)
            return values
        }

    fun editServerId(id: Long) {
        edit_server_id = id
        //        if( fieldName != null ) {
//            ServerModel serverModel = new ServerModel(getContext());
//            Server server = serverModel.getServerById(id);
//
//            fieldName.setText(server.getName());
//            fieldUrl.setText(server.getUrl());
//            fieldPadprefix.setText(server.getPadPrefix());
//            checkJquery.setChecked(server.jquery);
//
//            if( server.getPadPrefix().equals(DEFAULT_PADPREFIX_VALUE) && server.jquery )
//            {
//                checkLite.setChecked(true);
//            }
//        }
//        if( currentDialog != null ) {
//            currentDialog.setTitle(R.string.serverlist_dialog_edit_server_title);
//        }
    }

    override fun setViewEvents() {
        fieldName = main_view!!.findViewById(R.id.txt_server_name)
        fieldUrl = main_view!!.findViewById(R.id.txt_server_url)
        fieldPadprefix = main_view!!.findViewById(R.id.txt_server_padprefix)
        checkLite = main_view!!.findViewById(R.id.chk_lite)
        checkJquery = main_view!!.findViewById(R.id.chk_jquery)
        advancedButton = main_view!!.findViewById(R.id.advanced_options)
        advancedLayout = main_view!!.findViewById(R.id.advanced_options_layout)
        fieldPadprefix.setText(DEFAULT_PADPREFIX_VALUE)
        if (edit_server_id > 0) {
            val serverModel = ServerModel(context)
            val server = serverModel.getServerById(edit_server_id)
            fieldName.setText(server.getName())
            fieldUrl.setText(server.getUrl())
            fieldPadprefix.setText(server.padPrefix)
            checkJquery.setChecked(server!!.jquery)
            if (server.padPrefix == DEFAULT_PADPREFIX_VALUE && server.jquery) {
                checkLite.setChecked(true)
            }
        }
        advancedButton.setOnClickListener(View.OnClickListener {
            if (advancedLayout.getVisibility() == View.VISIBLE) {
                advancedLayout.setVisibility(View.GONE)
            } else {
                advancedLayout.setVisibility(View.VISIBLE)
            }
        })
        checkLite.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                fieldPadprefix.setText(DEFAULT_PADPREFIX_VALUE)
            } else {
                fieldPadprefix.setText("")
            }
            checkJquery.setChecked(isChecked)
        })
    }

    companion object {
        const val TAG = "NewServerDialog"
        const val DEFAULT_PADPREFIX_VALUE = "/p/"
        val NAME_VALIDATION = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\@\\ ]{2,256}"
        )
        val URL_VALIDATION = Patterns.WEB_URL
        val PADPREFIX_VALIDATION = Pattern.compile(
                "[a-zA-Z0-9\\+\\_\\-\\/\\ \\\\]{1,256}[/]{1}"
        )
    }
}