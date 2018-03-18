package com.mikifus.padland;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikifus.padland.Dialog.BasicAuthDialog;
import com.mikifus.padland.Models.Pad;
import com.mikifus.padland.Models.PadModel;
import com.mikifus.padland.SaferWebView.PadLandSaferWebViewClient;
import com.mikifus.padland.Utils.WhiteListMatcher;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.net.MalformedURLException;
import java.net.URL;
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

        // Checks if the url is a valid pad
        this._makePadUrl();

        handler = new Handler();

        titanPadCheck();

        // If no network...
        if (!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(R.layout.activity_padview);
        _loadProgressWheel();

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

    /**
     * This class must be removed in the future (2018) along with
     * titanpad.com support.
     */
    private void titanPadCheck() {
        Pad PadData = this._getPadData();
        if( ! PadData.getServer().equals("https://titanpad.com") ) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(PadViewActivity.this);
        builder.setTitle(R.string.titanpad_deprecated_title);
        builder.setMessage(R.string.titanpad_deprecated_text);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        Dialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

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
        pwheel = findViewById(R.id.progress_wheel);
        pwheel.spin();
    }

    private void _setProgressWheelProgress(int progress) {
        float p = progress / 100;

        if( webview_http_connections[0] > 0 ) {
            p = p / webview_http_connections[0];
        }

        Log.d("LOAD_PROGRESS_LOG", String.valueOf(progress));
        pwheel.setProgress(p);
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
     * Gets the pad data from the environment.
     * If the URL is not valid then the activity can't continue.
     */
    private void _makePadUrl() {
        Pad PadData = this._getPadData();

        if( ! WhiteListMatcher.checkValidUrl(PadData.getUrl()) ) {
            Toast.makeText(this, getString(R.string.padview_toast_invalid_url), Toast.LENGTH_SHORT).show();
            finish();
        }
        if( ! WhiteListMatcher.isValidHost(PadData.getUrl(), getServerWhiteList()) ) {
            Toast.makeText(this, getString(R.string.padview_toast_blacklist_url), Toast.LENGTH_SHORT).show();
            // My intention was to allow the user choose another app to open the URL
            // with the Intent.createChooser method. It worked really bad, it's bugged
            // and confusing for the user, so let's just go with a Toast.
            // TODO: Make this behave in a better way. Toasts are ugly.
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setData(Uri.parse(PadData.getUrl()));
            finish();
            startActivity(Intent.createChooser(i, getString(R.string.padview_toast_blacklist_url)));
            return;
        }


        current_padUrl = PadData.getUrl();
    }

    /**
     * Gets back the padData set.
     *
     * @return padData
     */
    private Pad _getPadData() {
        long pad_id = this._getPadId();
        Pad PadData;

        if (pad_id > 0) {
            // TODO: Use a method that returns directly a Pad instance
            Cursor c = padlistDb._getPadById(pad_id);
            c.moveToFirst();
            PadData = new Pad(c);
            c.close();
        } else {
            PadData = this._getPadDataFromIntent();
        }

        return PadData;
    }

    /**
     * The initial data of the pad is in the intent, it can be an URL, or some parameters in the
     * StringExtra intent.
     *
     * @return
     */
    private Pad _getPadDataFromIntent() {
        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        Pad padData;
        String padName = null;
        String padLocalName = null;
        String padServer = null;
        String padUrl;

        if (Intent.ACTION_VIEW.equals(action)) {
            padUrl = String.valueOf(myIntent.getData());
        } else {
            padName = myIntent.getStringExtra("padName");
            padLocalName = myIntent.getStringExtra("padLocalName");
            padServer = myIntent.getStringExtra("padServer");
            padUrl = myIntent.getStringExtra("padUrl");
        }

        padData = makePadData(padName, padLocalName, padServer, padUrl);

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

        Pad PadData = this._getPadDataFromIntent();
        Cursor c = padlistDb._getPadByUrl(PadData.getUrl());

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
            Pad intentData = this._getPadDataFromIntent();

            values.put(PadModel.NAME, intentData.getName());
            values.put(PadContentProvider.LOCAL_NAME, intentData.getLocalName());
            values.put(PadContentProvider.SERVER, intentData.getServer());
            values.put(PadModel.URL, intentData.getUrl());

            PadModel padModel = new PadModel(this);
            result = padModel.savePad(0, values);
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
            padlistDb.accessUpdate(pad_id);
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
//        final String current_padUrl = this.getCurrentPadUrl();
        webView = findViewById(R.id.activity_main_webview);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // The WebViewClient requires now a whitelist of urls that can interact with the Java side of the code
        String[] url_whitelist = getServerWhiteList();
        webView.setWebViewClient(new PadLandSaferWebViewClient(url_whitelist) {
//            private boolean done_auth;

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
                handler.postDelayed(postAfterFinish, 1200);
            }

            final Runnable postAfterFinish = new Runnable() {
                public void run() {
                    --webview_http_connections[0];
                    Log.d(TAG, "Removed connection " + webview_http_connections[0]);

                    if( webview_http_connections[0] > 0 ) {
                        // Wait for all of them to end.
                        handler.postDelayed(postAfterFinish, 1200);
                        return;
                    }
                    _hideProgressWheel();
                    loadJavascriptIfNeeded();
                }
            };

            /**
             * API >= 22
             *
             * @param view
             * @param request
             * @param error
             */
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                --webview_http_connections[0];
                _hideProgressWheel();
                Log.e(TAG, "WebView Error " + error.toString() +", Request: "+ request.toString());
            }

            /**
             * API < 22
             *
             * @param view
             * @param errorCode
             * @param description
             * @param failingUrl
             */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                --webview_http_connections[0];
                _hideProgressWheel();
                Log.e(TAG, "WebView Error ("+errorCode+") " + description +", Request: "+ failingUrl);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, final HttpAuthHandler handler, String host, String realm) {
                FragmentManager fm = getSupportFragmentManager();
                BasicAuthDialog dialog = new PadViewAuthDialog(getCurrentPadUrl(), handler);
                dialog.show(fm, "dialog_auth");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))
                    && !WhiteListMatcher.isValidHost(url, hostsWhitelist)) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);

                String message = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    int errorCode = error.getPrimaryError();
                    switch (errorCode) {
                        case SslError.SSL_EXPIRED:
                            message = getString(R.string.error_ssl_expired);
                            break;
                        case SslError.SSL_IDMISMATCH:
                            message = getString(R.string.error_ssl_id_mismatch);
                            break;
                        case SslError.SSL_NOTYETVALID:
                            message = getString(R.string.error_ssl_not_yet_valid);
                            break;
                        case SslError.SSL_UNTRUSTED:
                            message = getString(R.string.error_ssl_untrusted);
                            break;
                    }

                    if (message == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                            && errorCode == SslError.SSL_DATE_INVALID) {
                        message = getString(R.string.error_ssl_date_invalid);
                    }
                }
                Log.e(TAG, "SSL Error received: "+ error.getPrimaryError() + " - " + message);

                AlertDialog.Builder builder = new AlertDialog.Builder(PadViewActivity.this);
                builder.setTitle(R.string.ssl_error);
                builder.setMessage( message );
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                builder.setNegativeButton(R.string.ssl_learn_more, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse( getString(R.string.ssl_learn_more_link) ));
                        startActivity(browserIntent);
                    }
                });
                Dialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.show();
            }
        });
        this._makeWebSettings(webView);
        this._addListenersToView();
        this._addJavascriptOnLoad(webView);
        return webView;
    }

    public static class PadViewAuthDialog extends BasicAuthDialog {
        public static boolean done_auth = false;
        public String current_pad_url;
        public HttpAuthHandler handler;

        public PadViewAuthDialog(String current_pad_url, HttpAuthHandler handler) {
            this.current_pad_url = current_pad_url;
            this.handler = handler;
        }

        @Override
        protected void onDialogCreated(Dialog dialog, View view) {
            if( done_auth )
            {
                // Credentials must be invalid
                TextView textView = (TextView) view.findViewById(R.id.auth_error_message);
                textView.setText(R.string.basic_auth_error);
            }
            try {
                // Warn the user that is not using SSL
                URL url = new URL(this.current_pad_url);
                if( !url.getProtocol().equals("https") ) {
                    TextView textView = (TextView) view.findViewById(R.id.auth_warning_message);
                    textView.setText(R.string.basic_auth_warning);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPositiveButtonClick(String username, String password) {
            done_auth = true;
            handler.proceed(username, password);
        }
        @Override
        protected void onNegativeButtonClick() {
            done_auth = false;
            handler.cancel();
        }
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
        webSettings.setDomStorageEnabled(true); // Required for some NodeJS based code
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
    public Pad makePadData(String padName, String padLocalName, String padServer, String padUrl) {
        if (padUrl == null || padUrl.isEmpty()) {
            if (padName == null || padName.isEmpty()) {
                return null;
            }
            if (padLocalName == null || padLocalName.isEmpty()) {
                padLocalName = padName;
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
        matrixCursor.addRow(new Object[]{0, padName, padLocalName, padServer, padUrl, 0, 0, 0});

        matrixCursor.moveToFirst();
        Pad PadData = new Pad(matrixCursor);
        matrixCursor.close();

        return PadData;
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

    /**
     * Goes back to the pad list.
     */
    public void startPadListActivityWithPadId() {
        long padId = _getPadId();
        // It can happen that the pad ID is not set
        // if back is pressed before having it.
        if( padId == 0 ) {
            return;
        }
        Intent intent = new Intent(PadViewActivity.this, PadListActivity.class);
        intent.putExtra(PadListActivity.INTENT_FOCUS_PAD, padId);
        startActivity(intent);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        startPadListActivityWithPadId();
//        finish();
//    }
}