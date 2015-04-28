package com.mikifus.padland;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

public class padView extends PadLandActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padview);

        Intent myIntent = getIntent(); // gets the previously created intent
        String padName = myIntent.getStringExtra("padName");
        String padUrl = myIntent.getStringExtra("padUrl");
        String padServer = myIntent.getStringExtra("padUrl");

        if(padName == null || padName.isEmpty()){
            return;
        }
        if(padUrl == null || padUrl.isEmpty()){
            padUrl = padServer + padName;
        }
        if(padServer == null || padServer.isEmpty()){
            padServer = padUrl.replace(padName,"");
        }

        PadlandApp context = (PadlandApp) getApplicationContext();
        Log.v("inPadList?", "Is it?");
        if(!context.isInPadList(padUrl)){
            Log.v("inPadList?", "It is not!");
            try {
                context.addToPadList(padName,padServer,padUrl);
                Log.v("inPadList?", "Now saved!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.v("inPadList?", "Go check it");

        webView = (WebView) findViewById(R.id.activity_main_webview);
        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient()); // tells page not to open links in android browser and instead open them in this webview

        webView.loadUrl(padUrl);
    }

}
