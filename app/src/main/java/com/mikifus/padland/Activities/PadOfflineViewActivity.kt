package com.mikifus.padland.Activities

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import com.mikifus.padland.Utils.PadLandWebViewClient.PadLandOfflineWebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date

/**
 * Shows the offline copy of a pad, read-only: no javascript and no network,
 * links are opened in the user's browser. The copy can be downloaded or shared as an HTML file.
 */
class PadOfflineViewActivity: AppCompatActivity() {

    private var padViewModel: PadViewModel? = null
    private var mWebView: WebView? = null

    private var pad: Pad? = null
    private var html: String? = null

    private val offlinePadStore by lazy { OfflinePadFetcher.getStore(this) }

    // Must be registered before the activity starts
    private val downloadLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(MIME_TYPE)
    ) { uri ->
        onDownloadDestination(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pad_offline_view)
        setSupportActionBar(findViewById(R.id.activity_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mWebView = findViewById(R.id.activity_offline_webview)

        initWebView()
        initViewModels()
        loadOfflineCopy()
    }

    private fun initViewModels() {
        if(padViewModel == null) {
            padViewModel = ViewModelProvider(this)[PadViewModel::class.java]
        }
    }

    private fun initWebView() {
        mWebView!!.webViewClient = PadLandOfflineWebViewClient()

        val webSettings = mWebView!!.settings
        webSettings.javaScriptEnabled = false
        webSettings.allowContentAccess = false
        webSettings.allowFileAccess = false
        webSettings.setSupportZoom(true)
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webSettings.isAlgorithmicDarkeningAllowed = true
        } else if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDark(webSettings,
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    WebSettingsCompat.FORCE_DARK_ON
                } else {
                    WebSettingsCompat.FORCE_DARK_OFF
                })
        }
    }

    private fun loadOfflineCopy() {
        val padId = intent.getLongExtra("padId", 0L)

        lifecycleScope.launch {
            val loadedPad = withContext(Dispatchers.IO) {
                padViewModel!!.getByIds(listOf(padId)).firstOrNull()
            }
            val loadedHtml = withContext(Dispatchers.IO) {
                offlinePadStore.get(padId)
            }

            if (loadedPad == null || loadedHtml == null) {
                Toast.makeText(applicationContext, getString(R.string.unexpected_error), Toast.LENGTH_LONG)
                    .show()
                finish()
                return@launch
            }

            pad = loadedPad
            html = loadedHtml
            showOfflineCopy(loadedPad, loadedHtml)
        }
    }

    private fun showOfflineCopy(pad: Pad, html: String) {
        mWebView!!.loadDataWithBaseURL(null, html, MIME_TYPE, "UTF-8", null)

        // When the copy was saved, and of which pad
        val savedTime = offlinePadStore.getSavedTime(pad.mId) ?: System.currentTimeMillis()
        title = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(savedTime))
        supportActionBar?.subtitle = getPadName(pad)

        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pad_offline_view, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Nothing to save until the copy is loaded
        menu.findItem(R.id.menuitem_download)?.isEnabled = html != null
        menu.findItem(R.id.menuitem_share)?.isEnabled = html != null
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Opened from the pad info or the pad view, back to where it was
                finish()
            }
            R.id.menuitem_download -> {
                downloadLauncher.launch(getFileName())
            }
            R.id.menuitem_share -> {
                share()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun onDownloadDestination(uri: Uri?) {
        val html = html
        if (uri == null || html == null) {
            return // Cancelled
        }

        lifecycleScope.launch {
            val done = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(html.toByteArray(Charsets.UTF_8))
                        stream.flush()
                    } != null
                } catch (e: Exception) {
                    Log.w(TAG, "The offline copy could not be downloaded", e)
                    false
                }
            }

            Toast.makeText(
                applicationContext,
                getString(if (done) R.string.download_file_success else R.string.download_file_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Shares the copy as an HTML file, through a temporary copy in the cache
     * with the name of the pad.
     */
    private fun share() {
        val html = html ?: return
        val fileName = getFileName()

        lifecycleScope.launch {
            val file = withContext(Dispatchers.IO) {
                try {
                    val directory = File(cacheDir, SHARED_DIRECTORY_NAME)
                    // Only the last shared file is kept
                    directory.listFiles()?.forEach { it.delete() }
                    directory.mkdirs()

                    File(directory, fileName).apply {
                        writeText(html, Charsets.UTF_8)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "The offline copy could not be shared", e)
                    null
                }
            }

            if (file == null) {
                Toast.makeText(applicationContext, getString(R.string.unexpected_error), Toast.LENGTH_LONG)
                    .show()
                return@launch
            }

            val uri = FileProvider.getUriForFile(
                this@PadOfflineViewActivity,
                packageName + FILE_PROVIDER_AUTHORITY_SUFFIX,
                file
            )
            val shareIntent = ShareCompat.IntentBuilder(this@PadOfflineViewActivity)
                .setType(MIME_TYPE)
                .setStream(uri)
                .setSubject(pad?.let { getPadName(it) } ?: "")
                .intent
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(Intent.createChooser(shareIntent, getText(R.string.share_document)))
        }
    }

    private fun getPadName(pad: Pad): String {
        return pad.mLocalName.ifBlank { pad.mName }
    }

    /**
     * The pad name, without the characters not allowed in file names.
     */
    private fun getFileName(): String {
        val name = pad?.let { getPadName(it) }
            ?.replace(INVALID_FILE_NAME_CHARACTERS, "_")
            ?.trim()

        return (if (name.isNullOrBlank()) DEFAULT_FILE_NAME else name) + FILE_EXTENSION
    }

    companion object {
        private const val TAG = "PAD_OFFLINE_VIEW"

        private const val MIME_TYPE = "text/html"
        private const val FILE_EXTENSION = ".html"
        private const val DEFAULT_FILE_NAME = "pad"
        private val INVALID_FILE_NAME_CHARACTERS = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")

        /** See file_paths.xml */
        private const val SHARED_DIRECTORY_NAME = "shared"
        private const val FILE_PROVIDER_AUTHORITY_SUFFIX = ".fileprovider"
    }
}

