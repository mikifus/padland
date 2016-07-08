package com.mikifus.padland;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mikifus.padland.Adapters.ServerListAdapter;
import com.mikifus.padland.Dialog.NewServerDialog;

/**
 * Created by mikifus on 29/05/16.
 */
public class ServerListActivity extends PadLandDataActivity {

    ArrayAdapter mAdapter;

    private static final String NEW_SERVER_DIALOG = "dialog_new_server";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
        mAdapter = new ServerListAdapter(ServerListActivity.this, android.R.layout.simple_list_item_2);
        ListView listView = (ListView) findViewById(R.id.listView);
        if (listView != null) {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listView.setEmptyView(findViewById(android.R.id.empty));
            listView.setAdapter(mAdapter);
        }
    }

    public void onNewServerClick(View view) {
        FragmentManager fm = getSupportFragmentManager();
        NewServerDialog dialog = new NewServerDialog();
        dialog.show(fm, NEW_SERVER_DIALOG);
    }
}
