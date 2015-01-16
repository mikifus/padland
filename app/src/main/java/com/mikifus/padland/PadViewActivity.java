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

public class PadViewActivity extends PadLandActivity {
    private WebView webView;
    private String current_padUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        this.setProgressBarVisibility(true);

        final Intent myIntent = getIntent();
        final String action = myIntent.getAction();

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        long pad_id = myIntent.getLongExtra("pad_id", 0);
        String padName = myIntent.getStringExtra("padName");
        String padServer = myIntent.getStringExtra("padServer");
        String padUrl = myIntent.getStringExtra("padUrl");

        Cursor c = null;

        if (Intent.ACTION_VIEW.equals(action)) {
            padUrl = String.valueOf(myIntent.getData());
        }
        if(pad_id > 0){
            String[] id = {String.valueOf(pad_id)};
            c = getContentResolver().query(PadLandContentProvider.CONTENT_URI,
                    new String[] {PadLandContentProvider._ID, PadLandContentProvider.NAME, PadLandContentProvider.SERVER, PadLandContentProvider.URL},
                    PadLandContentProvider._ID+"=?",
                    id,
                    null);
        }else if(pad_id < 1 && padUrl != null && !padUrl.isEmpty()) {
            c = getContentResolver().query(PadLandContentProvider.CONTENT_URI,
                    new String[]{PadLandContentProvider._ID},
                    PadLandContentProvider.URL + "=?",
                    new String[]{padUrl},
                    null);
        }
        if(c != null && c.getCount() > 0) {
            c.moveToFirst();
            padName = c.getString(1);
            padServer = c.getString(2);
            padUrl = c.getString(3);
        }

        if(padUrl == null || padUrl.isEmpty()) {
            if (padName == null || padName.isEmpty()) {
                return;
            }
            if (padUrl == null || padUrl.isEmpty()) {
                padUrl = padServer + padName;
            }
            if (padServer == null || padServer.isEmpty()) {
                padServer = padUrl.replace(padName, "");
            }
        }else if (padName == null && padServer == null) {
            padName = padUrl.substring(padUrl.lastIndexOf("/")+1);
            padServer = padUrl.substring(0, padUrl.lastIndexOf("/"));
        }else if (padName.isEmpty()) {
            padName = padUrl.replace(padServer, "");
        }else if (padServer.isEmpty()) {
            padServer = padUrl.replace(padName, "");
        }


        if(c != null && c.getCount() < 1 && !padName.isEmpty() && !padServer.isEmpty()) {

            // Add a new record
            ContentValues values = new ContentValues();

            values.put(PadLandContentProvider.NAME, padName);
            values.put(PadLandContentProvider.SERVER, padServer);
            values.put(PadLandContentProvider.URL, padUrl);

            Context context = getApplicationContext();
            SharedPreferences userDetails = context.getSharedPreferences(getPackageName() + "_preferences", context.MODE_PRIVATE);

            if (userDetails.getBoolean("auto_save_new_pads", true)) {
                Log.d("INSERT", "Contents = " + values.toString());
                Uri uri = getContentResolver().insert(
                        PadLandContentProvider.CONTENT_URI, values);
            }
        }

        setContentView(R.layout.activity_padview);

        // If no network...
        if(!isNetworkAvailable()) {
            Toast.makeText(this, getString(R.string.network_is_unreachable), Toast.LENGTH_LONG).show();
            return;
        }

        webView = (WebView) findViewById(R.id.activity_main_webview);

        final Activity activity = this;
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 100);
            }
        });
        final String padUrlparameter = padUrl;
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                //webView.loadUrl("javascript:start('"+ padUrlparameter +"', 'username2', '#555' )");

                Context context = getApplicationContext();
                SharedPreferences userDetails = context.getSharedPreferences(getPackageName() + "_preferences", context.MODE_PRIVATE);
                String default_username = userDetails.getString("padland_default_username", "PadLand Viewer User");
                String default_color = userDetails.getString("padland_default_color", "#555");

                webView.loadUrl("javascript:start('"+ current_padUrl +"', '"+ default_username +"', '"+ default_color +"' )");
            }
        });


        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        webView.addJavascriptInterface(new ObjectExtension(), "webviewScriptAPI");
        String fulljs = "javascript:(\n    function() { \n";
        fulljs += "        window.onload = function() {\n";
        fulljs += "            webviewScriptAPI.onLoad();\n";
        fulljs += "        };\n";
        fulljs += "    })()\n";
        webView.loadUrl(fulljs);

        current_padUrl = padUrl; // to get it in all the methods

        //webView.loadUrl(padUrl);
        webView.loadUrl("file:///android_asset/PadView.html");
    }


    final class ObjectExtension {

        @JavascriptInterface
        public void onLoad() {
            onLoadCompleted();
        }
    }
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
