package com.mikifus.padland;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mikifus.padland.SaferWebView.PadLandSaferWebViewClient;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;

/**
 * This activity makes a webapp view into a browser and loads the document
 * url.
 * It does as well save the new visited urls in the documents list.
 *
 * @author mikifus
 */
public class PadViewActivity extends PadLandDataActivity {
    public static final String TAG = "PadViewActivity";
    private WebView webView;
    private String current_padUrl = "";

    // It happens that many connections are stablished. We count them so we can track them.
    // They must be handled asyncronously as Android calls the events for each connection,
    // and not
    final int[] webview_http_connections = new int[1];
    private Handler handler;

    /*
    Progress wheel is a fancy alternative to the ProgressBar, that wasn't working at all.
     */
    private ProgressWheel pwheel;

    /*
    Let me know when it is ready, please, I need it.
     */
    private boolean javascriptIsReady = false;

    /*
    Maximum size for the webView, it is quite complicated
     */
    private int max_viewport_size = 400;

    /**
     * Local class to handle the Javascript onLoad callback
     */
    final class JavascriptCallbackHandler {
        /**
         * Gets called from Javascript
         */
        @JavascriptInterface
        public void onLoad() {
        }

        @JavascriptInterface
        public void onIframeLoaded() {
            // Must run on ui thread (async)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onIframeLoaded");
//                    _hideProgressWheel();
                }
            });
        }
    }

    /**
     * onCreate override
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forces landscape view
//        this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        handler = new Handler();

        // If no network...
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(R.layout.activity_padview);
        _loadProgressWheel();

        this._makePadUrl();
        this._saveNewPad();
        this._updateViewedPad();
        this._makeWebView();

        // Cookies will be needed for pads
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true);
        }

        // Load it!
        loadUrl(current_padUrl);
    }
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        onCreate(savedInstanceState);
    }

    /**
     * Loads the fancy ProgressWheel to show it's loading.
     */
    private void _loadProgressWheel() {
        pwheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        pwheel.spin();
    }

    private void _setProgressWheelProgress(int progress) {
        float p = progress / 100;

        if( webview_http_connections[0] > 0 ) {
            p = p / webview_http_connections[0];
        }

        Log.d("LOAD_PROGRESS_LOG", String.valueOf(progress));
        pwheel.setProgress(p);

//        if (progress > 99) {
//            _hideProgressWheel();
//        }
    }

    public void _showProgressWheel() {
        pwheel.setVisibility(View.VISIBLE);
        Log.d("LOAD_PROGRESS_LOG", "PWheel must be gone by now...");
        handler.postDelayed(new Runnable() {
            public void run() {
                if( webview_http_connections[0] > 0 && pwheel.getVisibility() == View.VISIBLE ){
                    _hideProgressWheel();
                }
            }
        }, 7000);
    }

    public void _hideProgressWheel() {
        pwheel.setVisibility(View.GONE);
        Log.d("LOAD_PROGRESS_LOG", "PWheel must be gone by now...");
    }

    /**
     * Gets the pad data from the environment
     */
    private void _makePadUrl() {
        padData padData = this._getPadData();

        current_padUrl = padData.getUrl();
    }

    /**
     * Gets back the padData set.
     *
     * @return padData
     */
    private padData _getPadData() {
        long pad_id = this._getPadId();
        padData padData;

        if (pad_id > 0) {
            Cursor c = padlandDb._getPadDataById(pad_id);
            padData = new padData(c);
            c.close();
        } else {
            padData = this._getPadDataFromIntent();
        }

        return padData;
    }

    /**
     * The initial data of the pad is in the intent, it can be an URL, or some parameters in the
     * StringExtra intent.
     *
     * @return
     */
    private padData _getPadDataFromIntent() {
        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        padData padData;

        if (Intent.ACTION_VIEW.equals(action)) {
            String padUrl = String.valueOf(myIntent.getData());
            padData = makePadData(null, null, padUrl);
        } else {
            String padName = myIntent.getStringExtra("padName");
            String padServer = myIntent.getStringExtra("padServer");
            String padUrl = myIntent.getStringExtra("padUrl");

            padData = makePadData(padName, padServer, padUrl);
        }

        return padData;
    }

    /**
     * It gets the pad id by all possible means. This is, reading it from the Intent (in a
     * LongExtra) or using the padUrl to make a database query.
     *
     * @return
     */
    public long _getPadId() {
        Intent myIntent = getIntent();
        long pad_id = myIntent.getLongExtra("pad_id", 0);

        if (pad_id > 0) {
            return pad_id;
        }

        padData padData = this._getPadDataFromIntent();
        Cursor c = padlandDb._getPadDataByUrl(padData.getUrl());

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            long id = c.getLong(0);
            c.close();
            return id;
        }

        return 0;
    }

    /**
     * Saves a new pad from the intent information
     */
    private boolean _saveNewPad() {
        Context context = getApplicationContext();
        SharedPreferences userDetails = context.getSharedPreferences(getPackageName() + "_preferences", context.MODE_PRIVATE);

        boolean result = false;
        if (userDetails.getBoolean("auto_save_new_pads", true) && this._getPadId() == 0) {
            // Add a new record
            ContentValues values = new ContentValues();
            padData intentData = this._getPadDataFromIntent();

            values.put(PadContentProvider.NAME, intentData.getName());
            values.put(PadContentProvider.SERVER, intentData.getServer());
            values.put(PadContentProvider.URL, intentData.getUrl());

            result = padlandDb.savePadData(0, values);
        }
        return result;
    }

    /**
     * Updates the last used date of a pad.
     * It might update more details in the future.
     * <p/>
     * Actually it only calls accessUpdate, where the bussiness is made.
     */
    private boolean _updateViewedPad() {
        boolean result = false;
        long pad_id = this._getPadId();
        if (pad_id != 0) {
            padlandDb.accessUpdate(pad_id);
            result = true;
        }
        return result;
    }

    /**
     * Makes the webview that will contain a document.
     * It loads asynchronously by calling Javascript.
     *
     * @return
     */
    private WebView _makeWebView() {
        final String current_padUrl = this.getCurrentPadUrl();
        webView = (WebView) findViewById(R.id.activity_main_webview);
//        webView = new WebView(PadViewActivity.this);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // The WebViewClient requires now a whitelist of urls that can interact with the Java side of the code
        String[] url_whitelist = getResources().getStringArray(R.array.etherpad_servers_whitelist);
        webView.setWebViewClient(new PadLandSaferWebViewClient(url_whitelist) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                ++webview_http_connections[0];
                _showProgressWheel();
                Log.d(TAG, "Added connection " + webview_http_connections[0]);
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                handler.postDelayed(new Runnable() {
                    public void run() {
                        --webview_http_connections[0];
                        Log.d(TAG, "Removed connection " + webview_http_connections[0]);

                        if( webview_http_connections[0] > 0 ) {
                            // Wait for all of them to end.
                            return;
                        }
                        _hideProgressWheel();
                        loadJavascriptIfNeeded();
                    }
                }, 1200);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                --webview_http_connections[0];
                _hideProgressWheel();
//                Log.d(TAG, "Removed connection " + webview_http_connections[0]);
                Log.e(TAG, "WebView Error " + error.toString() +", Request: "+ request.toString());
            }
        });
        this._makeWebSettings(webView);
        this._addListenersToView();
        this._addJavascriptOnLoad(webView);
        return webView;
    }

    private void loadJavascriptIfNeeded() {
        if( ! supportsJquery(current_padUrl) ){
            return;
        }
        javascriptIsReady = true;

        Context context = getApplicationContext();
        SharedPreferences userDetails = context.getSharedPreferences(getPackageName() + "_preferences", context.MODE_PRIVATE);
        String default_username = userDetails.getString("padland_default_username", "PadLand Viewer User");
        String default_color = userDetails.getString("padland_default_color", "#555");

        // If using jQuery, here is called the pad load
        runJavascriptOnView(webView, "start('" + current_padUrl + "', '" + default_username + "', '" + default_color + "' );");
        javascript_padViewResize();
    }

    /**
     * Enables the required settings and features for the webview
     *
     * @param webView
     */
    private void _makeWebSettings(WebView webView) {
        webView.setInitialScale(1);
        WebSettings webSettings = webView.getSettings();
        // Enable Javascript
        webSettings.setJavaScriptEnabled(true);
        // remove a weird white line on the right size
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
    }

    private void runJavascriptOnView(WebView view, String js_string) {
        runJavascriptOnView(view, js_string, false);
    }

    private void runJavascriptOnView(WebView view, String js_string, Boolean force) {
        if (!force && !javascriptIsReady) {
            // If javascript is not ready better not to break anything
//            Log.d("LOAD_PROGRESS_LOG", "JS call has been interrupted: " + js_string);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // In KitKat+ you should use the evaluateJavascript method
            view.evaluateJavascript(js_string, new ValueCallback<String>() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onReceiveValue(String s) {
                    //Log.d("onLoadCompleted", "Feedback test.");
                }
            });
        } else {
            view.loadUrl("javascript:" + js_string);
        }
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
    public padData makePadData(String padName, String padServer, String padUrl) {
        if (padUrl == null || padUrl.isEmpty()) {
            if (padName == null || padName.isEmpty()) {
                return null;
            }
            if (padUrl == null || padUrl.isEmpty()) {
                padUrl = padServer + padName;
            }
            if (padServer == null || padServer.isEmpty()) {
                padServer = padUrl.replace(padName, "");
            }
        } else if (padName == null && padServer == null) {
            padName = padUrl.substring(padUrl.lastIndexOf("/") + 1);
            padServer = padUrl.substring(0, padUrl.lastIndexOf("/"));
        } else if (padName.isEmpty()) {
            padName = padUrl.replace(padServer, "");
        } else if (padServer.isEmpty()) {
            padServer = padUrl.replace(padName, "");
        }

        String[] columns = PadContentProvider.getPadFieldsList();

        // This creates a fake cursor
        MatrixCursor matrixCursor = new MatrixCursor(columns);
        startManagingCursor(matrixCursor);
        matrixCursor.addRow(new Object[]{0, padName, padServer, padUrl, 0, 0, 0});

        padData padData = new padData(matrixCursor);
        matrixCursor.close();

        return padData;
    }

    /**
     * Loads the specified url into the webView, it must be previously loaded.
     *
     * @param url
     */
    public void loadUrl(String url) {
        if( supportsJquery(url) ) {
            current_padUrl = url;
            webView.loadUrl("file:///android_asset/PadView.html");
        }
        webView.loadUrl(url);
    }

    /**
     * Etherpad Lite can be loaded wit jQuery.
     *
     * @param url
     * @return
     */
    private boolean supportsJquery(String url) {
        String[] support_jquery = getResources().getStringArray(R.array.etherpad_servers_supports_jquery);
        final String host = Uri.parse(url).getHost();
        for (String supported_host : support_jquery){
            if ( supported_host.equalsIgnoreCase(host) ){
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the options menu.
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu, R.menu.pad_view);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final ArrayList<String> pad_list = new ArrayList<>();
        pad_list.add(String.valueOf( _getPadId() ));

        switch( item.getItemId() ) {
            case R.id.menuitem_share:
                menu_share( pad_list );
                break;
            case R.id.menuitem_padlist:
                startPadListActivityWithPadId();
                break;
//            case R.id.menuitem_download:
//                downloadPadWithJavascriptIfPossible();
//                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private String getCurrentPadUrl() {
        return current_padUrl;
    }

    private void _addListenersToView() {
        webView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                // its possible that the layout is not complete in which case
                // we will get all zero values for the positions, so ignore the event
                if (left == 0 && top == 0 && right == 0 && bottom == 0) {
                    return;
                }

                if(supportsJquery(current_padUrl)) {
                    javascript_padViewResize();
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                _setProgressWheelProgress(progress);
            }
        });
    }

    /**
     * With a JavsascriptInterface I manage to call functions when the page loads
     *
     * @param webView
     */
    private void _addJavascriptOnLoad(WebView webView) {
        webView.addJavascriptInterface(new JavascriptCallbackHandler(), "webviewScriptAPI");
        /*
        String fulljs = "(\n    function() { \n";
        fulljs += "        window.onload = function() {\n";
        fulljs += "            webviewScriptAPI.onLoad();\n";
        fulljs += "        };\n";
        fulljs += "    })()\n";
        runJavascriptOnView(webView, fulljs, true); // Forced
        */
    }

    private void javascript_padViewResize() {


        //runJavascriptOnView(webView, "PadViewResize(" + getResources().getDisplayMetrics().widthPixels + ", " + getResources().getDisplayMetrics().heightPixels + ")");
        int local_max_viewport_size = max_viewport_size;
        int w;
        int h;
        //int current_w = getResources().getDisplayMetrics().widthPixels;
        //int current_h = getResources().getDisplayMetrics().heightPixels;
        int current_w = webView.getMeasuredWidth();
        int current_h = webView.getMeasuredHeight();

        if( getResources().getDisplayMetrics().widthPixels < max_viewport_size )
        {
            local_max_viewport_size = 300; // Fallback for small screens
        }
        double ratio = current_h / (double) current_w;

        if( current_h > current_w ) // vertical
        {
            w = local_max_viewport_size;
            h = (int) Math.floor(local_max_viewport_size * ratio);
        }
        else // horizontal
        {
            h = local_max_viewport_size;
            w = (int) Math.floor(local_max_viewport_size / ratio);
        }

        Log.d("RESIZE", "old w: " + current_w + ", h: " + current_h + " ratio: " + ratio);
        Log.d("RESIZE", "new w: " + w + ", h: " + h);


        runJavascriptOnView(webView, "PadViewResize("+w+","+h+")");
    }

    private void downloadPadWithJavascriptIfPossible() {
        if( ! supportsJquery(current_padUrl) ){
            return;
        }
        // TODO: Implement this:
        // runJavascriptOnView(webView, "$('#examplePadBasic').pad({'getContents':'exampleGetContents'});");
    }

    public void startPadListActivityWithPadId() {
        Intent intent = new Intent(PadViewActivity.this, PadListActivity.class);
        intent.putExtra(PadListActivity.INTENT_FOCUS_PAD, _getPadId());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        startPadListActivityWithPadId();
        finish();
    }
}