package com.mikifus.padland;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;
import android.widget.Toast;

/**
 * This activity makes a webapp view into a browser and loads the document
 * url.
 *
 * @author mikifus
 */
public class PadViewActivity extends PadLandActivity {
    private WebView webView;
    private String current_padUrl = "";

    /**
     * Local class to handle the Javascript onLoad callback
     */
    final class JavascriptCallbackHandler {
        /**
         * Gets called from Javascript
         */
        @JavascriptInterface
        public void onLoad() {
            onLoadCompleted();
        }
    }

    /**
     * onCreate override
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );

        // Progress bar for fancyness
        // WARNING: it does not work
        this._loadProgressBar();

        // Forces landscape view
        this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

        // If no network...
        if( !isNetworkAvailable() ) {
            Toast.makeText(this, getString( R.string.network_is_unreachable ), Toast.LENGTH_LONG).show();
            return;
        }

        setContentView( R.layout.activity_padview );

        this._makePadUrl();
        this._saveNewPad();
        this._makeWebView();

        this.loadUrl( "file:///android_asset/PadView.html" );
    }

    /**
     * Loads the fancy progressbar in the top of the screen.
     * I don't see it at all, but there is no error.
     */
    private void _loadProgressBar(){
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        this.setProgressBarVisibility(true);
    }

    /**
     * Gets the pad data from the environment
     */
    private void _makePadUrl(){
        String[] padData = this._getPadData();

        current_padUrl = padData[2];
    }

    /**
     * Gets back the padData set.
     * TODO: Refactor padData as a new class
     * @return
     */
    private String[] _getPadData(){
        long pad_id = this._getPadId();
        String[] intentData;

        if( pad_id > 0 ){
            intentData = this._getPadDbData( pad_id );
        }else{
            intentData = this._getIntentData();
        }

        String padName = intentData[0];
        String padServer = intentData[1];
        String padUrl = intentData[2];

        String[] padData = makePadData( padName, padServer, padUrl );
        
        return padData;
    }

    /**
     * The initial data of the pad is in the intent, it can be an URL, or some parameters in the
     * StringExtra intent.
     * @return
     */
    private String[] _getIntentData(){
        Intent myIntent = getIntent();
        String action = myIntent.getAction();
        String[] padData;

        if ( Intent.ACTION_VIEW.equals( action ) ) {
            String padUrl = String.valueOf( myIntent.getData() );
            padData = makePadData( null, null, padUrl );
        } else {
            String padName = myIntent.getStringExtra( "padName" );
            String padServer = myIntent.getStringExtra( "padServer" );
            String padUrl = myIntent.getStringExtra( "padUrl" );

            padData = makePadData( padName, padServer, padUrl );
        }

        return padData;
    }

    /**
     * It gets the pad id by all possible means. This is, reading it from the Intent (in a
     * LongExtra) or using the padUrl to make a database query.
     * @return
     */
    private long _getPadId(){
        Intent myIntent = getIntent();
        long pad_id = myIntent.getLongExtra( "pad_id", 0 );

        if( pad_id > 0 ) {
            return pad_id;
        }

        String[] padData = this._getIntentData();
        Cursor c = this._getPadDataByUrl( padData[2] );

        if(c != null && c.getCount() > 0) {
            c.moveToFirst();
            return c.getLong( 0 );
        }

        return 0;
    }

    /**
     * Saves a new pad from the intent information
     * TODO: Move data management to an independent Pad model
     */
    private void _saveNewPad(){
        Context context = getApplicationContext();
        SharedPreferences userDetails = context.getSharedPreferences(getPackageName() + "_preferences", context.MODE_PRIVATE);

        if ( userDetails.getBoolean("auto_save_new_pads", true) && this._getPadId() == 0 ) {
            // Add a new record
            ContentValues values = new ContentValues();

            String[] intentData = this._getIntentData();

            String padName = intentData[0];
            String padServer = intentData[1];
            String padUrl = intentData[2];

            values.put(PadLandContentProvider.NAME, padName);
            values.put(PadLandContentProvider.SERVER, padServer);
            values.put(PadLandContentProvider.URL, padUrl);

            Log.d("INSERT", "Contents = " + values.toString());
            Uri uri = getContentResolver().insert(
                    PadLandContentProvider.CONTENT_URI, values);
        }
    }

    /**
     * Makes the webview that will contain a document.
     * It loads asynchronously by calling Javascript.
     * @return
     */
    private WebView _makeWebView(){
        final String current_padUrl = this.getCurrentPadUrl();
        webView = (WebView) findViewById(R.id.activity_main_webview);

        // ProgressBar
        final Activity activity = this;
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 100);
            }
        });

        //final String padUrlparameter = padData[2];

        // WebViewClient is neeed in order to load asynchronously
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                //webView.loadUrl("javascript:start('"+ padUrlparameter +"', 'username2', '#555' )");

                Context context = getApplicationContext();
                SharedPreferences userDetails = context.getSharedPreferences( getPackageName() + "_preferences", context.MODE_PRIVATE );
                String default_username = userDetails.getString( "padland_default_username", "PadLand Viewer User" );
                String default_color = userDetails.getString( "padland_default_color", "#555" );

                webView.loadUrl("javascript:start('"+ current_padUrl +"', '"+ default_username +"', '"+ default_color +"' )");
            }
        });

        this._makeWebSettings( webView );
        this._addJavascriptOnLoad( webView );

        return webView;
    }

    /**
     * Enables the required settings and features for the webview
     * @param webView
     */
    private void _makeWebSettings( WebView webView ){
        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
    }

    /**
     * With a JavsascriptInterface I manage to call functions when the page loads
     * @param webView
     */
    private void _addJavascriptOnLoad( WebView webView ){
        webView.addJavascriptInterface(new JavascriptCallbackHandler(), "webviewScriptAPI");
        String fulljs = "javascript:(\n    function() { \n";
        fulljs += "        window.onload = function() {\n";
        fulljs += "            webviewScriptAPI.onLoad();\n";
        fulljs += "        };\n";
        fulljs += "    })()\n";
        webView.loadUrl(fulljs);
    }

    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field
     * @param comparation
     * @return
     */
    private Cursor _getPadDataFromDatabase( String field, String comparation ){
        Cursor c = null;
        String[] comparation_set = new String[]{ comparation };

        c = getContentResolver()
                .query(
                        PadLandContentProvider.CONTENT_URI,
                        new String[] {
                                PadLandContentProvider._ID,
                                PadLandContentProvider.NAME,
                                PadLandContentProvider.SERVER,
                                PadLandContentProvider.URL
                        },
                        field + "=?",
                        comparation_set, // AKA id
                        null
                );
        return c;
    }

    /**
     * Returns a padData object from an id
     * @param pad_id
     * @return
     */
    private String[] _getPadDbData( long pad_id ){
        String[] padData = new String[3];
        Cursor c = this._getPadDataById( pad_id );

        if(c != null && c.getCount() > 0) {
            c.moveToFirst();
            padData = makePadData( c.getString(1), c.getString(2), c.getString(3) );
        }

        return padData;
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    private Cursor _getPadDataById( long pad_id ){
        return this._getPadDataFromDatabase( PadLandContentProvider._ID, String.valueOf( pad_id ) );
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    private Cursor _getPadDataByUrl(String padUrl){
        return this._getPadDataFromDatabase( PadLandContentProvider.URL, padUrl );
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
    public String[] makePadData( String padName, String padServer, String padUrl ){
        if( padUrl == null || padUrl.isEmpty() ) {
            if (padName == null || padName.isEmpty()) {
                return new String[0];
            }
            if (padUrl == null || padUrl.isEmpty()) {
                padUrl = padServer + padName;
            }
            if (padServer == null || padServer.isEmpty()) {
                padServer = padUrl.replace(padName, "");
            }
        }else if ( padName == null && padServer == null ) {
            padName = padUrl.substring( padUrl.lastIndexOf( "/" ) + 1 );
            padServer = padUrl.substring( 0, padUrl.lastIndexOf( "/" ) );
        }else if ( padName.isEmpty() ) {
            padName = padUrl.replace( padServer, "" );
        }else if ( padServer.isEmpty() ) {
            padServer = padUrl.replace( padName, "" );
        }

        String[] padData = new String[3];
        padData[0] = padName;
        padData[1] = padServer;
        padData[2] = padUrl;

        return padData;
    }

    /**
     * Loads the specified url into the webView, it must be previously loaded.
     * @param url
     */
    public void loadUrl( String url ){
        //webView.loadUrl( url );
        webView.loadUrl( url );
    }

    /**
     * Called by the JavascriptCallbackHandler class
     */
    public void onLoadCompleted() {
        Log.d("onLoadCompleted", "On load was triggered. Javascript can run.");
        /*webView.evaluateJavascript("(function() { " +
                " alert(22); " +
                " $(\"#chatbox\").css('top','37px').css('bottom','37px'); " +
                " setTimeout(function(){" +
                "       $(\"#chatbox\").hide();chat.hide();  " +
                "       $(\"#chaticon\").hide(); " +
                "   }, 3000); " +
                "})();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.d("onLoadCompleted", "Feedback test.");
            }
        });*/

        /*String fulljs = "(function() { " +
                " alert(22); " +
                " $(\"#chatbox\").css('top','37px').css('bottom','37px'); " +
                " setTimeout(function(){ " +
                "       $(\"#chatbox\").hide();chat.hide();  " +
                "       $(\"#chaticon\").hide(); " +
                "   }, 3000); " +
                "})();";
        webView.loadUrl(fulljs);*/
    }

    /**
     * Creates the options menu.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu, R.menu.pad_view);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menuitem_share);

        // Fetch and store ShareActionProvider
        ShareActionProvider actionProvider = (ShareActionProvider) item.getActionProvider();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_auto_text) + getCurrentPadUrl());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_document)));

        actionProvider.setShareIntent(sendIntent);
        
        // Return true to display menu
        return true;
    }
    
    private String getCurrentPadUrl() {
        return current_padUrl;
    }
}
