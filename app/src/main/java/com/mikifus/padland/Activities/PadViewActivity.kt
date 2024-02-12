package com.mikifus.padland.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.HttpAuthHandler
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.Managers.IManagesNewServerDialog
import com.mikifus.padland.Dialogs.Managers.IManagesPadViewAuthDialog
import com.mikifus.padland.Dialogs.Managers.IManagesSslErrorDialog
import com.mikifus.padland.Dialogs.Managers.IManagesWhitelistServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesPadViewAuthDialog
import com.mikifus.padland.Dialogs.Managers.ManagesSslErrorDialog
import com.mikifus.padland.Dialogs.Managers.ManagesWhitelistServerDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadLandWebViewClient.PadLandWebClientCallbacks
import com.mikifus.padland.Utils.PadLandWebViewClient.PadLandWebViewClient
import com.mikifus.padland.Utils.PadUrl
import com.mikifus.padland.Utils.WhiteListMatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class PadViewActivity :
    AppCompatActivity(),
    IManagesPadViewAuthDialog by ManagesPadViewAuthDialog(),
    IManagesWhitelistServerDialog by ManagesWhitelistServerDialog(),
    IManagesNewServerDialog by ManagesNewServerDialog() ,
    IManagesSslErrorDialog by ManagesSslErrorDialog() {

    private var padViewModel: PadViewModel? = null
    override var serverViewModel: ServerViewModel? = null
    private var webView: WebView? = null
    private var webViewClient: PadLandWebViewClient? = null

    private var currentPadUrl: String? = null
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

        setContentView(R.layout.activity_pad_view)

        loadProgress()
        showProgress()
        initViewModels()
    }

    private fun makeWebView(urlWhitelist: List<String>) {
        // If no network...
        if(!isNetworkAvailable) {
            Toast.makeText(applicationContext, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG)
                .show()

            return
        }

        webViewClient = PadLandWebViewClient(urlWhitelist, object: PadLandWebClientCallbacks {
            override fun onStartLoading() {
                showProgress()
            }

            override fun onStopLoading() {
                hideProgress()
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
                val deferred = CompletableDeferred<Boolean>()
                showWhitelistServerDialog(this@PadViewActivity, url,
                    { dialogUrl ->
                        showNewServerDialog(this@PadViewActivity, dialogUrl) {
                            whitelistUrl(dialogUrl)
                            loadUrl(dialogUrl)
                        }
                        deferred.complete(true)
                    },
                    { dialogUrl ->
                        deferred.complete(true)
                    }
                )

                return deferred.await()
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
            val serverList = servers.map {
                    URL(it.mUrl).host
                } + resources.getStringArray(R.array.etherpad_servers_whitelist)

            makeWebView(serverList)
            loadOrSavePad()
        }
    }

    private fun loadOrSavePad() {
        intent.extras?.containsKey("padId")?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val pad = padViewModel?.getById(intent!!.extras!!.getLong("padId"))

                if(pad != null) {
                    currentPadUrl = pad.mUrl
                    updateViewedPad(pad)
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
            return
        }

        intent.extras?.containsKey("padUrl")?.let {
            if(!it) return

            val padUrl = intent.extras!!.getString("padUrl")!!

            currentPadUrl = padUrl

            val userDetails =
                getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
            if(userDetails.getBoolean("auto_save_new_pads", true)) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val pad = padViewModel?.getByUrl(padUrl)

                    if(pad == null) {
                        val newPad = Pad.fromUrl(padUrl).value!!

                        lifecycleScope.launch(Dispatchers.IO) {
                            padViewModel!!.insertPad(newPad)
                            updateViewedPad(newPad)

                            Toast.makeText(applicationContext, getString(R.string.padview_pad_save_success), Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
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
     * @param webView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private fun makeWebSettings() {
        webView = findViewById(R.id.activity_main_webview)
        webView!!.webViewClient = webViewClient!!
        webView!!.setInitialScale(1)

        val webSettings = webView!!.settings

        // Enable Javascript
        webSettings.javaScriptEnabled = true

        // remove a weird white line on the right size
//        webView!!.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY

        // Other options
        webSettings.useWideViewPort = true
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true // Required for some NodeJS based code
        webSettings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // Feature?: keep cookies

        // Cookies will be needed for pads
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }
    }
}