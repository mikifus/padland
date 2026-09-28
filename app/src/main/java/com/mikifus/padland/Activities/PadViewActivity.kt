package com.mikifus.padland.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.google.android.material.snackbar.Snackbar
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.Managers.IManagesDetectedPadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesNewPadDialog
import com.mikifus.padland.Dialogs.Managers.IManagesNewServerDialog
import com.mikifus.padland.Dialogs.Managers.IManagesPadViewAuthDialog
import com.mikifus.padland.Dialogs.Managers.IManagesSslErrorDialog
import com.mikifus.padland.Dialogs.Managers.IManagesWhitelistServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDetectedPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewPadDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesPadViewAuthDialog
import com.mikifus.padland.Dialogs.Managers.ManagesSslErrorDialog
import com.mikifus.padland.Dialogs.Managers.ManagesWhitelistServerDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.CryptPad.CryptPadUtils
import com.mikifus.padland.Utils.Download.DownloadHelper
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import com.mikifus.padland.Utils.Offline.OfflinePadStore
import com.mikifus.padland.Utils.PadLandWebViewClient.PadLandOfflineWebViewClient
import com.mikifus.padland.Utils.PadLandWebViewClient.PadLandWebClientCallbacks
import com.mikifus.padland.Utils.PadLandWebViewClient.PadLandWebViewClient
import com.mikifus.padland.Utils.PadServer
import com.mikifus.padland.Utils.PadUrl
import com.mikifus.padland.Utils.WhiteListMatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.DateFormat
import java.util.Date

class PadViewActivity :
    AppCompatActivity(),
    IManagesPadViewAuthDialog by ManagesPadViewAuthDialog(),
    IManagesWhitelistServerDialog by ManagesWhitelistServerDialog(),
    IManagesNewServerDialog by ManagesNewServerDialog() ,
    IManagesSslErrorDialog by ManagesSslErrorDialog(),
    IManagesNewPadDialog by ManagesNewPadDialog(),
    IManagesDetectedPadDialog by ManagesDetectedPadDialog() {

//    private var padViewModel: PadViewModel? = null
    override var serverViewModel: ServerViewModel? = null
    private var webView: WebView? = null
    private var webViewClient: PadLandWebViewClient? = null
    private var downloadHelper: DownloadHelper? = null
    private var deferredSave: Boolean = false

    /** The saved pad being viewed, null if it is not in the list */
    private var viewedPad: Pad? = null
    private var isOfflineMode: Boolean = false
    private val offlinePadStore by lazy { OfflinePadStore(this) }

    private var currentUrl: String? = null
        get() {
            return webView?.url
        }
        set(value) {
            lifecycleScope.launch(Dispatchers.IO) {
                value?.let { loadUrl(value) }
            }
            field = value
        }

    // ProgressBar
    private var mProgressBar: ProgressBar? = null

    /**
     * Check whether it is possible to connect to the internet
     * @return
     */
    @Suppress("DEPRECATION")
    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
            return false
        }

    /**
     * onCreate override
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.extras == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_pad_view)

        val root = findViewById<View>(R.id.activity_pad_view_root)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        loadProgress()
        showProgress()
        initDownloadHelper()
        initViewModels()
    }

    /**
     * Must be done before the activity starts, it registers an activity result.
     */
    private fun initDownloadHelper() {
        downloadHelper = DownloadHelper(this) { done ->
            Toast.makeText(
                applicationContext,
                getString(if (done) R.string.download_file_success else R.string.download_file_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun makeWebView(urlWhitelist: List<String>) {
        // If no network...
        if(!isNetworkAvailable) {
            Toast.makeText(applicationContext, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG)
                .show()

            return
        }

        val activity = this
        webViewClient = PadLandWebViewClient(urlWhitelist, object: PadLandWebClientCallbacks {
            override fun onStartLoading() {
                showProgress()
            }

            override fun onStopLoading() {
                hideProgress()
            }

            override fun onReceivedMainFrameErrorCallback(view: WebView, errorCode: Int) {
                // The server can not be reached, the offline copy is shown if there is one
                if (errorCode == WebViewClient.ERROR_HOST_LOOKUP ||
                    errorCode == WebViewClient.ERROR_CONNECT ||
                    errorCode == WebViewClient.ERROR_TIMEOUT) {
                    loadOfflineCopy(finishIfMissing = false)
                }
            }

            override suspend fun onUnsafeUrlProtocol(url: String): Boolean {
                val deferred = CompletableDeferred<Boolean>()
                showSslErrorDialog(this@PadViewActivity,
                    url,
                    getString(R.string.ssl_error_dialog_unsafe),
                    {
                        webView?.destroy()
                        finish()
                        deferred.complete(false)
                    },
                    {
                        deferred.complete(true)
                    }
                )

                return deferred.await()
            }

            override suspend fun onExternalHostUrlLoad(url: String): Boolean {
                showWhitelistServerDialog(this@PadViewActivity, url,
                    { dialogUrl ->
                        showNewServerDialog(this@PadViewActivity, dialogUrl) {
                            whitelistUrl(dialogUrl)
                            loadUrl(dialogUrl)
                        }
                    },{
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                        if(webView?.canGoBack() == true) {
                            webView!!.goBack()
                        } else {
                            finish()
                        }
                    },
                    { dialogUrl ->
                        whitelistUrl(dialogUrl)
                        loadUrl(dialogUrl)
                    }
                )
                return false
            }

            override fun onReceivedSslError(handler: SslErrorHandler, url: String, message: String) {
                showSslErrorDialog(this@PadViewActivity, url, message,
                    {
                        handler.cancel()
                    },
                    {
                        handler.proceed()
                    }
                )
            }

            override fun onReceivedHttpAuthRequestCallback(
                view: WebView,
                handler: HttpAuthHandler,
                host: String,
                realm: String
            ) {
                showPadViewAuthDialog(this@PadViewActivity, view, handler)
            }

            override fun onPageFinishedCallback(view: WebView?, url: String?) {
                if(url.isNullOrBlank()) {
                    return
                }
                Pad.fromUrl(url, activity).value!!

                // If it seems a version 2 cryptpad URL, check if it's a new pad created from drive
                if (!deferredSave && CryptPadUtils.seemsCrpytPadUrl(url)) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        val pad = withContext(Dispatchers.IO) {
                            padViewModel?.getByUrl(url)
                        }
                        if(pad == null &&
                            webViewClient!!.hostsWhitelist.contains(
                                PadServer.Builder().padUrl(url, activity).host
                            )) {
                            showDetectedPadDialog(activity, url,
                                {
                                    // Do nothing
                                },
                                {
                                    savePadFromUrl(url)
                                }
                            )
                        }
                    }
                } else if (deferredSave && WhiteListMatcher.isValidHost(url, webViewClient!!.hostsWhitelist) && CryptPadUtils.seemsCrpytPadUrl(url)) {
                    deferredSave = false
                    savePadFromUrl(url)
                }
            }
        })

        makeWebSettings()
    }

    private fun initViewModels() {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(this)[PadViewModel::class.java]
        }

        if(serverViewModel == null) {
            serverViewModel = ViewModelProvider(this)[ServerViewModel::class.java]
        }

        serverViewModel?.getAllEnabled!!.observe(this) { servers ->
            if (isOfflineMode) {
                return@observe
            }
            // Requested from the pad info, whatever the connection
            if (!isNetworkAvailable || intent.getBooleanExtra(EXTRA_OFFLINE_COPY, false)) {
                loadOfflineCopy(finishIfMissing = true)
                return@observe
            }

            val serverList = servers.map {
                    URL(it.mUrl).host
                } + resources.getStringArray(R.array.etherpad_servers_whitelist)

            makeWebView(serverList)
            loadOrSavePad()
        }
    }

    private fun loadOrSavePad() {
        if(!isNetworkAvailable) {
            finish()
            return
        }

        if(intent.extras?.containsKey("padId") == true) {
            loadPadById(intent!!.extras!!.getLong("padId"))
            return
        }

        val padUrl = intent.extras!!.getString("android.intent.extra.TEXT")

        if(padUrl.isNullOrBlank()) {
            Toast.makeText(
                applicationContext,
                getString(R.string.unexpected_error),
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val userDetails =
            getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
        var save = userDetails.getBoolean("auto_save_new_pads", true)
        if(intent.extras?.containsKey("padUrlDontSave") == true
            && intent.extras!!.getBoolean("padUrlDontSave")) {
            save = false
        } else if(intent.extras?.containsKey("deferredSave") == true
            && intent.extras!!.getBoolean("deferredSave")) {
            this.deferredSave = true
            save = false
        }
        currentUrl = padUrl

        if(save) {
            savePadFromUrl(padUrl)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            padViewModel?.getByUrl(padUrl)?.let { pad ->
                viewedPad = pad
                OfflinePadFetcher.update(applicationContext, pad)
            }
        }
    }

    private fun loadPadById(id: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pad = padViewModel?.getById(id)

            if (pad != null) {
                viewedPad = pad
                currentUrl = pad.mUrl
                updateViewedPad(pad)
                OfflinePadFetcher.update(applicationContext, pad)
            } else {
                lifecycleScope.launch {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.unexpected_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun savePadFromUrl(padUrl: String) {
        val activity = this
        val localName = intent.extras?.getString("localName", "") ?: ""
        lifecycleScope.launch(Dispatchers.IO) {
            val pad = withContext(Dispatchers.IO){
                padViewModel?.getByUrl(padUrl)
            }

            if(pad == null &&
                webViewClient!!.hostsWhitelist.contains(
                    PadServer.Builder().padUrl(padUrl, activity).host
                )) {

                val newPad = withContext(Dispatchers.Main){
                    Pad.fromUrl(padUrl, activity).value!!.copy(mLocalName = localName)
                }

                withContext(Dispatchers.IO){
                    padViewModel!!.insertPad(newPad)
                    updateViewedPad(newPad)
                }

                withContext(Dispatchers.Main){
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.padview_pad_save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun updateViewedPad(pad: Pad) {
        val updatedPad = pad.copy(
            mAccessCount = pad.mAccessCount + 1,
            mLastUsedDate = java.sql.Date(System.currentTimeMillis())
        )

        padViewModel?.updatePad(updatedPad)
    }

    /**
     * Leaving the pad, the offline copy gets the latest changes.
     */
    override fun onStop() {
        super.onStop()
        if (!isOfflineMode) {
            viewedPad?.let { OfflinePadFetcher.update(applicationContext, it) }
        }
    }

    /**
     * Shows the offline copy of the pad when it can not be loaded.
     *
     * @param finishIfMissing close if there is no offline copy, otherwise keep
     * the current page (i.e. the WebView error page)
     */
    private fun loadOfflineCopy(finishIfMissing: Boolean) {
        lifecycleScope.launch {
            val pad = viewedPad ?: withContext(Dispatchers.IO) { findViewedPad() }
            val html = pad?.takeIf { it.mOfflineAccess }?.let {
                withContext(Dispatchers.IO) { offlinePadStore.get(it.mId) }
            }

            if (pad == null || html == null) {
                if (finishIfMissing) {
                    Toast.makeText(applicationContext, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
                return@launch
            }

            showOfflineCopy(pad, html)
        }
    }

    private suspend fun findViewedPad(): Pad? {
        val extras = intent.extras ?: return null
        if (extras.containsKey("padId")) {
            return padViewModel?.getById(extras.getLong("padId"))
        }
        val padUrl = extras.getString("android.intent.extra.TEXT")
        if (padUrl.isNullOrBlank()) {
            return null
        }
        return padViewModel?.getByUrl(padUrl)
    }

    /**
     * Read only: no javascript, no network and links are not followed.
     */
    private fun showOfflineCopy(pad: Pad, html: String) {
        if (isOfflineMode) {
            return
        }
        isOfflineMode = true
        viewedPad = pad

        webView = findViewById(R.id.activity_main_webview)
        webView!!.stopLoading()
        webView!!.setOnKeyListener(null)
        webView!!.setDownloadListener(null)
        webView!!.webViewClient = PadLandOfflineWebViewClient()
        webView!!.settings.javaScriptEnabled = false
        webView!!.settings.allowContentAccess = false
        webView!!.settings.allowFileAccess = false

        // The export has no viewport, undo the settings made for the pad (if any)
        webView!!.setInitialScale(0)
        webView!!.settings.useWideViewPort = false
        webView!!.settings.loadWithOverviewMode = false
        webView!!.settings.setSupportZoom(true)
        webView!!.settings.builtInZoomControls = true
        webView!!.settings.displayZoomControls = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webView!!.settings.isAlgorithmicDarkeningAllowed = true
        }

        webView!!.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        hideProgress()

        val savedTime = offlinePadStore.getSavedTime(pad.mId) ?: System.currentTimeMillis()
        val savedDate = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(savedTime))

        Snackbar.make(webView!!, getString(R.string.padview_offline_copy_shown, savedDate),
            Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.ok) {}
            .show()
    }

    /**
     * Loads the fancy ProgressWheel to show it's loading.
     */
    private fun loadProgress() {
        mProgressBar = findViewById(R.id.progress_indicator)
    }

    fun showProgress() {
        mProgressBar!!.visibility = View.VISIBLE
    }

    fun hideProgress() {
        mProgressBar!!.visibility = View.GONE
    }

    /**
     * Loads the specified url into the webView, it must be previously set up.
     * Reads user config on username and color and adds them to the URL.
     *
     * @param url
     */
    private fun loadUrl(url: String) {
        if (!WhiteListMatcher.isValidHost(url, webViewClient!!.hostsWhitelist)) {
            showWhitelistServerDialog(this, url,
                { dialogUrl ->
                    showNewServerDialog(this, dialogUrl){
                        whitelistUrl(dialogUrl)
                        loadUrl(dialogUrl)
                    }
                },
                { dialogUrl ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dialogUrl))
                    startActivity(intent)
                    if(webView?.canGoBack() == true) {
                        webView!!.goBack()
                    } else {
                        finish()
                    }
                },
                { dialogUrl ->
                    whitelistUrl(dialogUrl)
                    loadUrl(dialogUrl)
                }
            )
            return
        }

        val userDetails =
            getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)

        val username = userDetails.getString("padland_default_username", "")
        val color = userDetails.getInt("padland_default_color", 0)

        val newUrl = PadUrl.etherpadAddUsernameAndColor(url, username, color)

        lifecycleScope.launch(Dispatchers.Main) {
            webView!!.loadUrl(newUrl)
        }
    }

    fun whitelistUrl(url: String) {
        val urlObject = URL(url)
        webViewClient!!.hostsWhitelist += urlObject.host
    }

    /**
     * Creates the options menu.
     *
     * @param menu
     * @return
     */
//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        super.onCreateOptionsMenu(menu, R.menu.pad_view)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val padList = ArrayList<String?>()
//        padList.add(_getPadId().toString())
//        when (item.itemId) {
//            R.id.menuitem_share -> menuShare(padList)
//            R.id.menuitem_padlist -> startPadListActivityWithPadId()
//            else -> return super.onOptionsItemSelected(item)
//        }
//        return true
//    }

    /**
     * Enables the required settings and features for the webview
     *
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun makeWebSettings() {
        webView = findViewById(R.id.activity_main_webview)
        webView!!.webViewClient = webViewClient!!
        webView!!.setInitialScale(1)
        downloadHelper?.attach(webView!!)

        val webSettings = webView!!.settings

        // Enable Javascript
        webSettings.javaScriptEnabled = true

        // Other options
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true // Required for some NodeJS based code
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Feature?: keep cookies

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webSettings.isAlgorithmicDarkeningAllowed = true
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            @Suppress("DEPRECATION")
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_ON)
                }
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setForceDark(webSettings, WebSettingsCompat.FORCE_DARK_OFF)
                }
            }
        }

        // Cookies will be needed for pads
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        webView!!.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View, keyCode: Int, keyEvent: KeyEvent): Boolean {
                if (keyEvent.action == KeyEvent.ACTION_DOWN) {
                    val webView = view as WebView
                    when (keyCode) {
                        KeyEvent.KEYCODE_BACK -> if (webView.canGoBack()) {
                            webView.goBack()
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    companion object {
        /** Boolean extra, shows the offline copy even if there is connection */
        const val EXTRA_OFFLINE_COPY = "offlineCopy"
    }
}