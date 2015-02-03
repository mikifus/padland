package com.mikifus.padland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class NewPadActivity extends PadLandActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpad);

        // Set default position choosen by user
        // This can be changed on the preferences
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();

        // Server list to provide a default value
        String server_list [] = getResources().getStringArray(R.array.etherpad_servers_name);

        // Getting user preferences
        Context context = getApplicationContext();
        SharedPreferences userDetails = context.getSharedPreferences(getPackageName() + "_preferences", context.MODE_PRIVATE);
        String default_server = userDetails.getString("padland_default_server", server_list[0]);

        // We get position and set it as default
        spinner.setSelection(adapter.getPosition(default_server));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu, R.menu.new_pad);
    }

    public void onCreateButtonClick(View w){

        String padName = getPadNameFromInput((TextView) findViewById(R.id.editText));
        String padPrefix = getPadPrefixFromSpinner((Spinner) findViewById(R.id.spinner));
        String padServer = getPadServerFromSpinner((Spinner) findViewById(R.id.spinner));

        String padUrl = padPrefix + padName;

        if (padName == "") {
            return;
        }

        Intent padViewIntent =
                new Intent(NewPadActivity.this, PadViewActivity.class);
        padViewIntent.putExtra("padName", padName);
        padViewIntent.putExtra("padServer", padServer);
        padViewIntent.putExtra("padUrl", padUrl);

        startActivity(padViewIntent);
    }

    private String getPadNameFromInput(TextView input){
        String padName = (String) input.getText().toString();

        return padName;
    }

    private String getPadPrefixFromSpinner(Spinner spinner){
        String padPrefix = getResources().getStringArray(R.array.etherpad_servers_url_padprefix)[spinner.getSelectedItemPosition()];

        return padPrefix;
    }

    private String getPadServerFromSpinner(Spinner spinner){
        String padServer = getResources().getStringArray(R.array.etherpad_servers_url_home)[spinner.getSelectedItemPosition()];

        return padServer;
    }
}
