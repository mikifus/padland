package com.mikifus.padland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mikifus.padland.Models.Server;
import com.mikifus.padland.Models.ServerModel;
import com.mikifus.padland.Utils.PadUrl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Allows the user to create a new pad, choosing a name and the host.
 *
 * @author mikifus
 */
public class NewPadActivity extends PadLandActivity {

    /**
     * onCreate override
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpad);

        this._setSpinnerValues();
        this._setSpinnerDefaultValue();
    }

    /**
     * Returns a string with the server names.
     * Includes custom servers.
     * @return String[]
     */
    private String[] getSpinnerValueList() {
        String[] server_list;
        // Load the custom servers
        ServerModel serverModel = new ServerModel(this);
        ArrayList<Server> custom_servers = serverModel.getEnabledServerList();
        ArrayList<String> server_names = new ArrayList<>();
        for(Server server : custom_servers) {
            server_names.add(server.getName());
        }

        // Server list to provide a fallback value
//        server_list.getResources().getStringArray( R.array.etherpad_servers_name );
        Collection<String> collection = new ArrayList<>();
        collection.addAll(server_names);
        collection.addAll(Arrays.asList(getResources().getStringArray( R.array.etherpad_servers_name )));

        server_list = collection.toArray(new String[collection.size()]);

        return server_list;
    }

    /**
     * Returns a string with the server urls.
     * Includes custom servers.
     * @return String[]
     */
    private String[] getServerUrlList() {
        String[] server_list;
        // Load the custom servers
        ServerModel serverModel = new ServerModel(this);
        ArrayList<Server> custom_servers = serverModel.getEnabledServerList();
        ArrayList<String> server_names = new ArrayList<>();
        for(Server server : custom_servers) {
            server_names.add(server.getUrl());
        }

        // Server list to provide a fallback value
//        server_list.getResources().getStringArray( R.array.etherpad_servers_name );
        Collection<String> collection = new ArrayList<>();
        collection.addAll(server_names);
        collection.addAll(Arrays.asList(getResources().getStringArray( R.array.etherpad_servers_url_home )));

        server_list = collection.toArray(new String[collection.size()]);

        return server_list;
    }

    /**
     * Returns a string with the server urls and the prefix to see a pad.
     * Includes custom servers.
     * @return String[]
     */
    private String[] getServerUrlPrefixList() {
        String[] server_list;
        // Load the custom servers
        ServerModel serverModel = new ServerModel(this);
        ArrayList<Server> custom_servers = serverModel.getEnabledServerList();
        ArrayList<String> server_names = new ArrayList<>();
        for(Server server : custom_servers) {
            server_names.add(server.getPadPrefix());
        }

        // Server list to provide a fallback value
//        server_list.getResources().getStringArray( R.array.etherpad_servers_name );
        Collection<String> collection = new ArrayList<>();
        collection.addAll(server_names);
        collection.addAll(Arrays.asList(getResources().getStringArray( R.array.etherpad_servers_url_padprefix )));

        server_list = collection.toArray(new String[collection.size()]);

        return server_list;
    }

    /**
     * Loads the values on the Spinner which is by deafult empty.
     */
    private void _setSpinnerValues() {
        String[] server_list = getSpinnerValueList();

        Spinner spinner = (Spinner) findViewById( R.id.spinner );
        if (spinner != null) {
            //selected item will look like a spinner set from XML
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, server_list);
            spinner.setAdapter(spinnerArrayAdapter);
        }
    }

    /**
     * Gets the default value from user settings and selects it in the
     * spinner. This value can be changed in the settings activity.
     */
    private void _setSpinnerDefaultValue(){
        String default_server = this._getDefaultSpinnerValue();

        // We get position and set it as default
        Spinner spinner = (Spinner) findViewById( R.id.spinner );
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        spinner.setSelection( adapter.getPosition( default_server ) );
    }

    /**
     * Returns a string with the default server name. Being a key in the server
     * array
     * @return
     */
    private String _getDefaultSpinnerValue(){
        String[] server_list = getSpinnerValueList();

        // Getting user preferences
        Context context = getApplicationContext();
        SharedPreferences userDetails = context.getSharedPreferences( getPackageName() + "_preferences", context.MODE_PRIVATE );
        String default_server = userDetails.getString( "padland_default_server", server_list[0] );
        return default_server;
    }

    /**
     * Creates the menu
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu( Menu menu ) {
        return super.onCreateOptionsMenu(menu, R.menu.new_pad);
    }

    /**
     * Form submit
     * @param w
     */
    public void onCreateButtonClick( View w ){
        String padName = this.getPadNameFromInput((TextView) findViewById(R.id.editText));
        String padLocalName = this.getPadNameFromInput((TextView) findViewById(R.id.editTextLocalName));
        String padPrefix = this.getPadPrefixFromSpinner((Spinner) findViewById(R.id.spinner));
        String padServer = this.getPadServerFromSpinner( (Spinner) findViewById(R.id.spinner) );

        Log.d("CREATENEW", padName);
        if ( padName.isEmpty() ) {
            Toast.makeText(this, (getString(R.string.newpad_noname_warning)), Toast.LENGTH_LONG).show();
            return;
        }

        PadUrl padUrl = new PadUrl.Builder()
                .padName(padName)
                .padServer(padServer)
                .padPrefix(padPrefix)
                .build();

        if( !URLUtil.isValidUrl(padUrl.getString()) )
        {
            Toast.makeText(this, getString(R.string.new_pad_name_invalid), Toast.LENGTH_LONG).show();
            return;
        }

        Intent padViewIntent =
                new Intent( NewPadActivity.this, PadViewActivity.class );
        padViewIntent.putExtra( "padName", padUrl.getPadName() );
        padViewIntent.putExtra( "padLocalName", padLocalName );
        padViewIntent.putExtra( "padServer", padUrl.getPadServer() );
        padViewIntent.putExtra( "padUrl", padUrl.getString() );

        startActivity(padViewIntent);
        finish();
    }

    /**
     * Given an input view, gets the text
     * @param input
     * @return
     */
    private String getPadNameFromInput( TextView input ){
        String padName = (String) input.getText().toString().trim();
        return padName;
    }

    /**
     * Same as previous but with a spinner
     * @param spinner
     * @return
     */
    private String getPadPrefixFromSpinner( Spinner spinner ){
        String[] padUrls = getServerUrlPrefixList();
//        String padPrefix = getResources().getStringArray( R.array.etherpad_servers_url_padprefix )[spinner.getSelectedItemPosition()];
        String padPrefix = padUrls[ spinner.getSelectedItemPosition() ];
        return padPrefix;
    }

    /**
     * Almost the same as the previous one.
     * @param spinner
     * @return
     */
    private String getPadServerFromSpinner( Spinner spinner ){
        String[] padUrls = getServerUrlList();
//        String padServer = getResources().getStringArray( R.array.etherpad_servers_url_home )[spinner.getSelectedItemPosition()];
        String padServer = padUrls[ spinner.getSelectedItemPosition() ];
        return padServer;
    }
}
