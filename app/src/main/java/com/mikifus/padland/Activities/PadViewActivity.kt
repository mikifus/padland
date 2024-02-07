package com.mikifus.padland.Activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadLandSafeWebViewClientInstance
import com.pnikosis.materialishprogress.ProgressWheel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PadViewActivity : AppCompatActivity() {
    private var padViewModel: PadViewModel? = null
    private var webView: WebView? = null
    private var currentPadUrl: String? = ""

    // It happens that many connections are stablished. We count them so we can track them.
    // They must be handled asyncronously as Android calls the events for each connection.
    val webviewHttpConnections = IntArray(1)
//    private var handler: Handler? = null

    /*
    Progress wheel is a fancy alternative to the ProgressBar, that wasn't working at all.
     */
    private var pwheel: ProgressWheel? = null

    /**
     * Check whether it is possible to connect to the internet
     * @return
     */
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

        // If no network...
        if(!isNetworkAvailable) {
            Toast.makeText(applicationContext, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG)
                .show()
        } else {
            setContentView(R.layout.activity_padview)
            initViewModels()
            loadProgressWheel()
            loadOrSavePad()
//            updateViewedPad()
            makeWebView()
        }
    }

    private fun makeWebView() {
        webView = findViewById(R.id.activity_main_webview)
//        webView.layoutParams =
//            RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//        val urlWhitelist = serverWhiteList
        webView!!.webViewClient = PadLandSafeWebViewClientInstance(arrayOf())

        makeWebSettings(webView)
    }

    private fun initViewModels() {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(this)[PadViewModel::class.java]
        }
    }

    private fun loadOrSavePad() {
        intent.extras?.containsKey("padId")?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val pad = padViewModel?.getById(intent!!.extras!!.getLong("padId"))

                if(pad != null) {
                    currentPadUrl = pad.mUrl

                    loadUrl(currentPadUrl)
                } else {
                    Toast.makeText(applicationContext, getString(R.string.unexpected_error), Toast.LENGTH_LONG)
                        .show()
                }
            }
            return
        }

        intent.extras?.containsKey("padUrl")?.let {
            if(!it) return

            val userDetails =
                getSharedPreferences(packageName + "_preferences", MODE_PRIVATE)
            if(userDetails.getBoolean("auto_save_new_pads", true)) {
                val padUrl = intent.extras!!.getString("padUrl")!!

                currentPadUrl = padUrl

                loadUrl(currentPadUrl)

                lifecycleScope.launch(Dispatchers.IO) {
                    val pad = padViewModel?.getByUrl(padUrl)

                    if(pad == null) {
                        val newPad = Pad.fromUrl(padUrl).value!!

                        lifecycleScope.launch(Dispatchers.IO) {
                            padViewModel!!.insertPad(newPad)

//                                Toast.makeText(applicationContext, "SAVE SUCCESS", Toast.LENGTH_LONG)
//                                    .show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Loads the fancy ProgressWheel to show it's loading.
     */
    private fun loadProgressWheel() {
        pwheel = findViewById(R.id.progress_wheel)
        pwheel!!.spin()
    }
    fun showProgressWheel() {
        pwheel!!.visibility = View.VISIBLE
//        Log.d("LOAD_PROGRESS_LOG", "PWheel must be gone by now...")
//        handler!!.postDelayed({
//            if (webviewHttpConnections[0] > 0 && pwheel!!.visibility == View.VISIBLE) {
//                _hideProgressWheel()
//            }
//        }, 7000)
    }

    fun hideProgressWheel() {
        pwheel!!.visibility = View.GONE
//        Log.d("LOAD_PROGRESS_LOG", "PWheel must be gone by now...")
    }

    /**
     * Loads the specified url into the webView, it must be previously set up.
     *
     * @param url
     */
    private fun loadUrl(url: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            webView!!.loadUrl(url!!)
        }
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
    private fun makeWebSettings(webView: WebView?) {
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


        // Cookies will be needed for pads
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }
    }
}