package com.mikifus.padland;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ArrayAdapter;

import com.mikifus.padland.Adapters.ServerListAdapter;

/**
 * Created by mikifus on 29/05/16.
 */
public class ServerListActivity extends PadLandDataActivity {

    ArrayAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mAdapter = new ServerListAdapter(ServerListActivity.this, android.R.layout.simple_list_item_2);
        setContentView(R.layout.activity_server_list);
//        ((ListView) findViewById(R.id.listView)).setAdapter(mAdapter);
    }
}
