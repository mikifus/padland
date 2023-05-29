package com.mikifus.padland

import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.mikifus.padland.Dialog.BasicAuthDialog
import com.mikifus.padland.Models.Pad
import com.mikifus.padland.Models.PadModel
import com.mikifus.padland.SaferWebView.PadLandSaferWebViewClient
import com.mikifus.padland.Utils.WhiteListMatcher
import com.pnikosis.materialishprogress.ProgressWheel
import java.net.MalformedURLException
import java.net.URL

/**
 * This activity makes a webapp view into a browser and loads the document
 * url.
 * It does as well save the new visited urls in the documents list.
 *
 * @author mikifus
 */
class PadViewActivity : PadLandDataActivity() {
    private var webView: WebView? = null
    private var currentPadUrl: String? = ""

    // It happens that many connections are stablished. We count them so we can track them.
    // They must be handled asyncronously as Android calls the events for each connection,
    // and not
    val webviewHttpConnections = IntArray(1)
    private var handler: Handler? = null

    /*
    Progress wheel is a fancy alternative to the ProgressBar, that wasn't working at all.
     */
    private var pwheel: ProgressWheel? = null

    /**
     * onCreate override
     *
     * @param savedInstanceState
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myIntent = intent
        if (myIntent.extras == null) {
            finish()
            return
        }

        // Checks if the url is a valid pad
        if (!_makePadUrl()) {
            finish()
            return
        }
        handler = Handler()

        // If no network...
        if (!isNetworkAvailable) {
            Toast.makeText(this, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG).show()
            return
        }
        setContentView(R.layout.activity_padview)
        _loadProgressWheel()
        _saveNewPad()
        _updateViewedPad()
        _makeWebView()

        // Cookies will be needed for pads
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        // Load it!
        loadUrl(currentPadUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        onCreate(savedInstanceState)
    }

    /**
     * Loads the fancy ProgressWheel to show it's loading.
     */
    private fun _loadProgressWheel() {
        pwheel = findViewById(R.id.progress_wheel)
        pwheel!!.spin()
    }

    fun _showProgressWheel() {
        pwheel!!.visibility = View.VISIBLE
        Log.d("LOAD_PROGRESS_LOG", "PWheel must be gone by now...")
        handler!!.postDelayed({
            if (webviewHttpConnections[0] > 0 && pwheel!!.visibility == View.VISIBLE) {
                _hideProgressWheel()
            }
        }, 7000)
    }

    fun _hideProgressWheel() {
        pwheel!!.visibility = View.GONE
        Log.d("LOAD_PROGRESS_LOG", "PWheel must be gone by now...")
    }

    /**
     * Gets the pad data from the environment.
     * If the URL is not valid then the activity can't continue.
     */
    private fun _makePadUrl(): Boolean {
        val PadData = _getPadData()
        if (PadData == null || !WhiteListMatcher.checkValidUrl(PadData.url)) {
            Toast.makeText(this, getString(R.string.padview_toast_invalid_url), Toast.LENGTH_SHORT).show()
            return false
        }
        if (!WhiteListMatcher.isValidHost(PadData.url, serverWhiteList)) {
            Toast.makeText(this, getString(R.string.padview_toast_blacklist_url), Toast.LENGTH_SHORT).show()
            // My intention was to allow the user choose another app to open the URL
            // with the Intent.createChooser method. It worked really bad, it's bugged
            // and confusing for the user, so let's just go with a Toast.
            // TODO: Make this behave in a better way. Toasts are ugly.
            val i = Intent(Intent.ACTION_VIEW)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            i.data = Uri.parse(PadData.url)
            startActivity(Intent.createChooser(i, getString(R.string.padview_toast_blacklist_url)))
            return false
        }
        currentPadUrl = PadData.url
        return true
    }

    /**
     * Gets back the padData set.
     *
     * @return padData
     */
    private fun _getPadData(): Pad? {
        val pad_id = _getPadId()
        val PadData: Pad?
        if (pad_id > 0) {
            // TODO: Use a method that returns directly a Pad instance
            val c = padlistDb!!._getPadById(pad_id)
            c!!.moveToFirst()
            PadData = Pad(c)
            c.close()
        } else {
            PadData = _getPadDataFromIntent()
        }
        return PadData
    }

    /**
     * The initial data of the pad is in the intent, it can be an URL, or some parameters in the
     * StringExtra intent.
     *
     * @return
     */
    private fun _getPadDataFromIntent(): Pad? {
        val myIntent = intent
        val action = myIntent.action
        val padData: Pad?
        var padName: String? = null
        var padLocalName: String? = null
        var padServer: String? = null
        val padUrl: String?
        if (Intent.ACTION_VIEW == action) {
            padUrl = myIntent.data.toString()
        } else {
            padName = myIntent.getStringExtra("padName")
            padLocalName = myIntent.getStringExtra("padLocalName")
            padServer = myIntent.getStringExtra("padServer")
            padUrl = myIntent.getStringExtra("padUrl")
        }
        padData = makePadData(padName, padLocalName, padServer, padUrl)
        return padData
    }

    /**
     * It gets the pad id by all possible means. This is, reading it from the Intent (in a
     * LongExtra) or using the padUrl to make a database query.
     *
     * @return
     */
    override fun _getPadId(): Long {
        val myIntent = intent
        val padId = myIntent.getLongExtra("pad_id", 0)
        if (padId > 0) {
            return padId
        }
        val padData = _getPadDataFromIntent() ?: return 0
        val c = padlistDb!!._getPadByUrl(padData.url)
        if (c != null && c.count > 0) {
            c.moveToFirst()
            val id = c.getLong(0)
            c.close()
            return id
        }
        return 0
    }

    /**
     * Saves a new pad from the intent information
     */
    private fun _saveNewPad(): Boolean {
        val context = applicationContext
        val userDetails = context.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
        var result = false
        if (userDetails.getBoolean("auto_save_new_pads", true) && _getPadId() == 0L) {
            // Add a new record
            val values = ContentValues()
            val intentData = _getPadDataFromIntent()
            values.put(PadModel.Companion.NAME, intentData!!.name)
            values.put(PadContentProvider.Companion.LOCAL_NAME, intentData.localName)
            values.put(PadContentProvider.Companion.SERVER, intentData.server)
            values.put(PadModel.Companion.URL, intentData.url)
            val padModel = PadModel(this)
            result = padModel.savePad(0, values)
        }
        return result
    }

    /**
     * Updates the last used date of a pad.
     * It might update more details in the future.
     *
     *
     * Actually it only calls accessUpdate, where the bussiness is made.
     */
    private fun _updateViewedPad(): Boolean {
        var result = false
        val padId = _getPadId()
        if (padId != 0L) {
            padlistDb!!.accessUpdate(padId)
            result = true
        }
        return result
    }

    /**
     * Makes the webview that will contain a document.
     * It loads asynchronously by calling Javascript.
     *
     * @return
     */
    private fun _makeWebView(): WebView? {
//        final String current_padUrl = this.getCurrentPadUrl();
        val view = findViewById<WebView>(R.id.activity_main_webview)
        webView = view
        view.layoutParams =
            RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // The WebViewClient requires now a whitelist of urls that can interact with the Java side of the code
        val urlWhitelist = serverWhiteList
        view.webViewClient = object : PadLandSaferWebViewClient(urlWhitelist) {
            //            private boolean done_auth;
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                ++webviewHttpConnections[0]
                _showProgressWheel()
                Log.d(TAG, "Added connection " + webviewHttpConnections[0])
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                handler!!.postDelayed(postAfterFinish, 1200)
            }

            val postAfterFinish: Runnable = object : Runnable {
                override fun run() {
                    --webviewHttpConnections[0]
                    Log.d(TAG, "Removed connection " + webviewHttpConnections[0])
                    if (webviewHttpConnections[0] > 0) {
                        // Wait for all of them to end.
                        handler!!.postDelayed(this, 1200)
                        return
                    }
                    _hideProgressWheel()
                    //                    loadJavascriptIfNeeded();
                }
            }

            /**
             * API >= 22
             *
             * @param view
             * @param request
             * @param error
             */
            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                --webviewHttpConnections[0]
                _hideProgressWheel()
                Log.e(TAG, "WebView Error $error, Request: $request")
            }

            /**
             * API < 22
             *
             * @param view
             * @param errorCode
             * @param description
             * @param failingUrl
             */
            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                --webviewHttpConnections[0]
                _hideProgressWheel()
                Log.e(TAG, "WebView Error ($errorCode) $description, Request: $failingUrl")
            }

            override fun onReceivedHttpAuthRequest(view: WebView, handler: HttpAuthHandler, host: String, realm: String) {
                val fm = supportFragmentManager
                val dialog: BasicAuthDialog = PadViewAuthDialog(currentPadUrl, handler)
                dialog.show(fm, "dialog_auth")
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return if ((url.startsWith("http://") || url.startsWith("https://")) && !WhiteListMatcher.isValidHost(url, hostsWhitelist)) {
                    view.context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                } else {
                    super.shouldOverrideUrlLoading(view, url)
                }
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                super.onReceivedSslError(view, handler, error)
                var message: String? = null
                val errorCode = error.primaryError
                when (errorCode) {
                    SslError.SSL_EXPIRED -> message = getString(R.string.error_ssl_expired)
                    SslError.SSL_IDMISMATCH -> message = getString(R.string.error_ssl_id_mismatch)
                    SslError.SSL_NOTYETVALID -> message = getString(R.string.error_ssl_not_yet_valid)
                    SslError.SSL_UNTRUSTED -> message = getString(R.string.error_ssl_untrusted)
                }
                if (message == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && errorCode == SslError.SSL_DATE_INVALID) {
                    message = getString(R.string.error_ssl_date_invalid)
                }
                Log.e(TAG, "SSL Error received: " + error.primaryError + " - " + message)
                val builder = AlertDialog.Builder(this@PadViewActivity)
                builder.setTitle(R.string.ssl_error)
                builder.setMessage(message)
                builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                }
                builder.setNegativeButton(R.string.ssl_learn_more) { dialogInterface, i ->
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.ssl_learn_more_link)))
                    startActivity(browserIntent)
                }
                val alertDialog: Dialog = builder.create()
                alertDialog.setCanceledOnTouchOutside(true)
                alertDialog.show()
            }
        }
        _makeWebSettings(webView)
        return webView
    }

    class PadViewAuthDialog(private var currentPadUrl: String?, private var handler: HttpAuthHandler) : BasicAuthDialog() {
        override fun onDialogCreated(dialog: Dialog?, view: View?) {
            if (done_auth) {
                // Credentials must be invalid
                val textView = requireView().findViewById<View>(R.id.auth_error_message) as TextView
                textView.setText(R.string.basic_auth_error)
            }
            try {
                // Warn the user that is not using SSL
                val url = URL(currentPadUrl)
                if (url.protocol != "https") {
                    val textView = requireView().findViewById<View>(R.id.auth_warning_message) as TextView
                    textView.setText(R.string.basic_auth_warning)
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }

        override fun onPositiveButtonClick(username: String?, password: String?) {
            done_auth = true
            handler.proceed(username, password)
        }

        override fun onNegativeButtonClick() {
            done_auth = false
            handler.cancel()
        }

        companion object {
            var done_auth = false
        }
    }

    /**
     * Enables the required settings and features for the webview
     *
     * @param webView
     */
    private fun _makeWebSettings(webView: WebView?) {
        webView!!.setInitialScale(1)
        val webSettings = webView.settings
        // Enable Javascript
        webSettings.javaScriptEnabled = true
        // remove a weird white line on the right size
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true // Required for some NodeJS based code
    }

    /**
     * Creates a padData object trying to complete the empty fields the others.
     * Possible combinations is like this: padName + padServer = padUrl
     *
     * @param padName
     * @param padServer
     * @param padUrl
     * @return
     */
    fun makePadData(padName: String?, padLocalName: String?, padServer: String?, padUrl: String?): Pad? {
        // Since the format of parameter is uncontrollable, use try-catch block to avoid exception
        var padName = padName
        var padLocalName = padLocalName
        var padServer = padServer
        var padUrl = padUrl
        try {
            if (padUrl.isNullOrEmpty()) {
                if (padName.isNullOrEmpty()) {
                    return null
                }
                if (padLocalName.isNullOrEmpty()) {
                    padLocalName = padName
                }
                // Condition "padUrl == null || padUrl.isEmpty()" is always true
                // if (padUrl == null || padUrl.isEmpty()) {
                padUrl = padServer + padName
                // }
                if (padServer.isNullOrEmpty()) {
                    padServer = padUrl.replace(padName, "")
                }
            } else if (padName == null && padServer == null) {
                // Since the format of padUrl is uncontrollable, the following code
                // may produce StringIndexOutOfBoundsException
                padName = padUrl.substring(padUrl.lastIndexOf("/") + 1)
                padServer = padUrl.substring(0, padUrl.lastIndexOf("/"))
            } else if (padName!!.isEmpty()) {
                padName = padUrl.replace(padServer!!, "")
            } else if (padServer!!.isEmpty()) {
                padServer = padUrl.replace(padName, "")
            }
        } catch (e: Exception) {
            // Any exception is related to the format error of parameters
            e.printStackTrace()
            return null
        }
        val columns: Array<String> = PadContentProvider.padFieldsList

        // This creates a fake cursor
        val matrixCursor = MatrixCursor(columns)
        startManagingCursor(matrixCursor)
        matrixCursor.addRow(arrayOf<Any?>(0, padName, padLocalName, padServer, padUrl, 0, 0, 0))
        matrixCursor.moveToFirst()
        val padData = Pad(matrixCursor)
        matrixCursor.close()
        return padData
    }

    /**
     * Loads the specified url into the webView, it must be previously loaded.
     *
     * @param url
     */
    private fun loadUrl(url: String?) {
        webView!!.loadUrl(url!!)
    }

    /**
     * Creates the options menu.
     *
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu, R.menu.pad_view)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val padList = ArrayList<String?>()
        padList.add(_getPadId().toString())
        when (item.itemId) {
            R.id.menuitem_share -> menuShare(padList)
            R.id.menuitem_padlist -> startPadListActivityWithPadId()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Goes back to the pad list.
     */
    private fun startPadListActivityWithPadId() {
        val padId = _getPadId()
        // It can happen that the pad ID is not set
        // if back is pressed before having it.
        if (padId == 0L) {
            return
        }
        val intent = Intent(this@PadViewActivity, PadListActivity::class.java)
        intent.putExtra(PadListActivity.Companion.INTENT_FOCUS_PAD, padId)
        startActivity(intent)
    } //    @Override

    //    public void onBackPressed() {
    //        super.onBackPressed();
    //        startPadListActivityWithPadId();
    //        finish();
    //    }
    companion object {
        const val TAG = "PadViewActivity"
    }
}