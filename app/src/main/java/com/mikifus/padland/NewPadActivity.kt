package com.mikifus.padland

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.View
import android.webkit.URLUtil
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.Utils.PadUrl
import java.util.Arrays

/**
 * Allows the user to create a new pad, choosing a name and the host.
 *
 * @author mikifus
 */
class NewPadActivity : PadLandActivity() {
    protected var server_list: Array<String?>
    protected var server_url_list: Array<String?>?
    protected var server_url_prefixed_list: Array<String?>?
    protected var serverModel: ServerModel? = null

    /**
     * onCreate override
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_newpad)
        server_list = spinnerValueList
        serverModel = ServerModel(this)
        server_url_list = serverModel!!.getServerUrlList(this@NewPadActivity)
        server_url_prefixed_list = serverModel!!.getServerUrlPrefixList(this)
        _setViewEvents()
        _setSpinnerValues()
        _setSpinnerDefaultValue()
    }

    private fun _setViewEvents() {
        val nameInput = findViewById<EditText>(R.id.editText)
        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val name = s.toString()
                if (URLUtil.isValidUrl(name)) {
                    val uri = Uri.parse(name)
                    val padServer = uri.scheme + "://" + uri.host
                    val padName = uri.lastPathSegment
                    var c = 0
                    for (serverUrl in server_url_list!!) {
                        if (serverUrl == padServer) {
                            nameInput.setText(padName)
                            val spinner = findViewById<Spinner>(R.id.spinner)
                            https@ //etherpad.wikimedia.org/p/anabelle
                            spinner.setSelection(c)
                            Toast.makeText(this@NewPadActivity, getString(R.string.newpad_url_name_success), Toast.LENGTH_LONG).show()
                            return
                        }
                        c++
                    }
                    Toast.makeText(this@NewPadActivity, getString(R.string.newpad_url_name_warning), Toast.LENGTH_LONG).show()
                }
            }
        })
    }// Load the custom servers

    // Server list to provide a fallback value
//        server_list.getResources().getStringArray( R.array.etherpad_servers_name );
    /**
     * Returns a string with the server names.
     * Includes custom servers.
     * @return String[]
     */
    private val spinnerValueList: Array<String?>
        private get() {
            val server_list: Array<String?>
            // Load the custom servers
            val serverModel = ServerModel(this)
            val custom_servers = serverModel.enabledServerList
            val server_names = ArrayList<String?>()
            for (server in custom_servers!!) {
                server_names.add(server.getName())
            }

            // Server list to provide a fallback value
//        server_list.getResources().getStringArray( R.array.etherpad_servers_name );
            val collection: MutableCollection<String?> = ArrayList()
            collection.addAll(server_names)
            collection.addAll(Arrays.asList(*resources.getStringArray(R.array.etherpad_servers_name)))
            server_list = collection.toTypedArray()
            return server_list
        }

    /**
     * Loads the values on the Spinner which is by deafult empty.
     */
    private fun _setSpinnerValues() {
        val spinner = findViewById<View>(R.id.spinner) as Spinner
        if (spinner != null) {
            //selected item will look like a spinner set from XML
            val spinnerArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, server_list)
            spinner.adapter = spinnerArrayAdapter
        }
    }

    /**
     * Gets the default value from user settings and selects it in the
     * spinner. This value can be changed in the settings activity.
     */
    private fun _setSpinnerDefaultValue() {
        val default_server = _getDefaultSpinnerValue()

        // We get position and set it as default
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = spinner.adapter as ArrayAdapter<*>
        spinner.setSelection(adapter.getPosition(default_server))
    }

    /**
     * Returns a string with the default server name. Being a key in the server
     * array
     * @return
     */
    private fun _getDefaultSpinnerValue(): String? {
        // Getting user preferences
        val context = applicationContext
        val userDetails = context.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
        return userDetails.getString("padland_default_server", server_list[0])
    }

    /**
     * Creates the menu
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu, R.menu.new_pad)
    }

    /**
     * Form submit
     * @param w
     */
    fun onCreateButtonClick(w: View?) {
        val padName = getPadNameFromInput(findViewById<View>(R.id.editText) as TextView)
        val padLocalName = getPadNameFromInput(findViewById<View>(R.id.editTextLocalName) as TextView)
        val padPrefix = getPadPrefixFromSpinner(findViewById<View>(R.id.spinner) as Spinner)
        val padServer = getPadServerFromSpinner(findViewById<View>(R.id.spinner) as Spinner)
        Log.d("CREATENEW", padName)
        if (padName.isEmpty()) {
            Toast.makeText(this, getString(R.string.newpad_noname_warning), Toast.LENGTH_LONG).show()
            return
        }
        val padUrl = PadUrl.Builder()
                .padName(padName)
                .padServer(padServer)
                .padPrefix(padPrefix)
                .build()
        if (!URLUtil.isValidUrl(padUrl.string)) {
            Toast.makeText(this, getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show()
            return
        }
        val padViewIntent = Intent(this@NewPadActivity, PadViewActivity::class.java)
        padViewIntent.putExtra("padName", padUrl.padName)
        padViewIntent.putExtra("padLocalName", padLocalName)
        padViewIntent.putExtra("padServer", padUrl.padServer)
        padViewIntent.putExtra("padUrl", padUrl.string)
        startActivity(padViewIntent)
        finish()
    }

    /**
     * Given an input view, gets the text
     * @param input
     * @return
     */
    private fun getPadNameFromInput(input: TextView): String {
        return input.text.toString().trim { it <= ' ' }
    }

    /**
     * Same as previous but with a spinner
     * @param spinner
     * @return
     */
    private fun getPadPrefixFromSpinner(spinner: Spinner): String? {
        return server_url_prefixed_list!![spinner.selectedItemPosition]
    }

    /**
     * Almost the same as the previous one.
     * @param spinner
     * @return
     */
    private fun getPadServerFromSpinner(spinner: Spinner): String? {
        return server_url_list!![spinner.selectedItemPosition]
    }
}