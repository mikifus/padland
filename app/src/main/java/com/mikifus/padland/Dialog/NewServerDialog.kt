package com.mikifus.padland.Dialog

import android.content.ContentValues
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.R
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern

/**
 * Created by mikifus on 7/07/16.
 */
open class NewServerDialog(title: String, callback: FormDialogCallBack) : FormDialog(title, callback) {
    private var fieldName: EditText? = null
    private var fieldUrl: EditText? = null
    private var fieldPadprefix: EditText? = null
    private var checkLite: CheckBox? = null
    private var checkJquery: CheckBox? = null
    private var advancedButton: Button? = null
    private var advancedLayout: LinearLayout? = null

    //    private Dialog currentDialog;
    private var editServerId: Long = 0

    init {
        view = R.layout.dialog_new_server
    }

    override suspend fun saveData() {
        val serverModel = ServerModel(activity)
        val contentValues = contentValues
        val url = contentValues.getAsString(ServerModel.URL)
        val prefix = contentValues.getAsString(ServerModel.PADPREFIX)
        var finalPrefix = url
        if (!finalPrefix.endsWith(prefix)) {
            finalPrefix += prefix
        }
        contentValues.put(ServerModel.PADPREFIX, finalPrefix)
//        serverModel.saveServerData(editServerId, contentValues)
        PadListDatabase.getInstance(requireContext()).serverDao().insertAll(Server.fromFormContentValues(contentValues).value!!)
    }

    override suspend fun validateForm(): Boolean {
        val contentValues = contentValues
        if (!NAME_VALIDATION.matcher(contentValues.getAsString(ServerModel.NAME)).matches()) {
            // TODO: Change toast for something better.
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_name_invalid), Toast.LENGTH_LONG).show()
            return false
        }
        val urlString = contentValues.getAsString(ServerModel.URL)
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
        if (contentValues.getAsString(ServerModel.PADPREFIX).isNotEmpty()
                && !PADPREFIX_VALIDATION.matcher(contentValues.getAsString(ServerModel.PADPREFIX)).matches()) {
            Toast.makeText(context, getString(R.string.serverlist_dialog_new_server_padprefix_invalid), Toast.LENGTH_LONG).show()
            return false
        }
        if (contentValues.getAsInteger(ServerModel.JQUERY) == null) {
            Log.e(TAG, "Something is wrong here")
            return false
        }
        return true
    }// Must start and end with /// Remove trailing slash

    //        Toast.makeText(getContext(), "Server saved", Toast.LENGTH_LONG).show();
    override val contentValues: ContentValues
        get() {
//        Toast.makeText(getContext(), "Server saved", Toast.LENGTH_LONG).show();
            val contentValues = super.contentValues
            val name = fieldName!!.text.toString().trim { it <= ' ' }
            contentValues!!.put(ServerModel.NAME, name)
            var url = fieldUrl!!.text.toString().trim { it <= ' ' }
            try {
                val urlObject = URL(url)
                url = urlObject.toString()
                url = url.replace("/$".toRegex(), "") // Remove trailing slash
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            contentValues.put(ServerModel.URL, url)
            var padprefix = fieldPadprefix!!.text.toString()
            if (padprefix.isNotEmpty()) {
                // Must start and end with /
                if (!padprefix.startsWith("/")) {
                    padprefix = "/$padprefix"
                }
                if (!padprefix.endsWith("/")) {
                    padprefix = "$padprefix/"
                }
            }
            contentValues.put(ServerModel.PADPREFIX, padprefix)
            val jquery = if (checkJquery!!.isChecked.toString() === "true") 1 else 0
            contentValues.put(ServerModel.JQUERY, jquery)
            return contentValues
        }

    fun editServerId(id: Long) {
        editServerId = id
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
        lifecycleScope.launch {
            fieldName = mainView!!.findViewById(R.id.txt_server_name)
            fieldUrl = mainView!!.findViewById(R.id.txt_server_url)
            fieldPadprefix = mainView!!.findViewById(R.id.txt_server_padprefix)
            checkLite = mainView!!.findViewById(R.id.chk_lite)
            checkJquery = mainView!!.findViewById(R.id.chk_jquery)
            advancedButton = mainView!!.findViewById(R.id.advanced_options)
            advancedLayout = mainView!!.findViewById(R.id.advanced_options_layout)
            (fieldPadprefix as EditText).setText(DEFAULT_PADPREFIX_VALUE)
            if (editServerId > 0) {
//            val serverModel = ServerModel(context)
//            val server = serverModel.getServerById(editServerId)
                val server =
                    PadListDatabase.getInstance(requireContext()).serverDao().getById(editServerId)
                (fieldName as EditText).setText(server.mName)
                (fieldUrl as EditText).setText(server.mUrl)
                (fieldPadprefix as EditText).setText(server.mPadprefix)
                (checkJquery as CheckBox).isChecked = server.mJquery
                if (server.mPadprefix == DEFAULT_PADPREFIX_VALUE && server.mEnabled) {
                    (checkLite as CheckBox).isChecked = true
                }
            }
            (advancedButton as Button).setOnClickListener(View.OnClickListener {
                if ((advancedLayout as LinearLayout).getVisibility() == View.VISIBLE) {
                    (advancedLayout as LinearLayout).setVisibility(View.GONE)
                } else {
                    (advancedLayout as LinearLayout).setVisibility(View.VISIBLE)
                }
            })
            (checkLite as CheckBox).setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    (fieldPadprefix as EditText).setText(DEFAULT_PADPREFIX_VALUE)
                } else {
                    (fieldPadprefix as EditText).setText("")
                }
                (checkJquery as CheckBox).isChecked = isChecked
            }
        }
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