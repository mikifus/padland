package com.mikifus.padland;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mikifus.padland.Adapters.ServerListAdapter;

/**
 * Created by mikifus on 29/05/16.
 */
public class ServerListActivity extends PadLandDataActivity {

    ArrayAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
        mAdapter = new ServerListAdapter(ServerListActivity.this, android.R.layout.simple_list_item_2);
        if (findViewById(R.id.listView) != null) {
            ((ListView) findViewById(R.id.listView)).setAdapter(mAdapter);
        }
    }
}
