package com.mikifus.padland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

    protected String[] server_list;
    protected String[] server_url_list;
    protected String[] server_url_prefixed_list;
    protected ServerModel serverModel;

    /**
     * onCreate override
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newpad);

        server_list = getSpinnerValueList();
        serverModel = new ServerModel(this);
        server_url_list = serverModel.getServerUrlList(NewPadActivity.this);
        server_url_prefixed_list = serverModel.getServerUrlPrefixList(this);

        _setViewEvents();
        _setSpinnerValues();
        _setSpinnerDefaultValue();
    }

    private void _setViewEvents() {
        final EditText nameInput = findViewById( R.id.editText );
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString();
                if(URLUtil.isValidUrl(name)) {
                    Uri uri = Uri.parse(name);
                    String padServer = uri.getScheme() + "://" + uri.getHost();
                    String padName = uri.getLastPathSegment();

                    int c = 0;
                    for (String serverUrl : server_url_list) {
                        if(serverUrl.equals(padServer)) {
                            nameInput.setText(padName);

                            Spinner spinner = findViewById( R.id.spinner );https://etherpad.wikimedia.org/p/anabelle
                            spinner.setSelection(c);

                            Toast.makeText(NewPadActivity.this, getString(R.string.newpad_url_name_success), Toast.LENGTH_LONG).show();
                            return;
                        }
                        c++;
                    }
                    Toast.makeText(NewPadActivity.this, getString(R.string.newpad_url_name_warning), Toast.LENGTH_LONG).show();
                }
            }
        });
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
     * Loads the values on the Spinner which is by deafult empty.
     */
    private void _setSpinnerValues() {
        Spinner spinner = (Spinner) findViewById( R.id.spinner );
        if (spinner != null) {
            //selected item will look like a spinner set from XML
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, server_list);
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
        Spinner spinner = findViewById( R.id.spinner );
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        spinner.setSelection( adapter.getPosition( default_server ) );
    }

    /**
     * Returns a string with the default server name. Being a key in the server
     * array
     * @return
     */
    private String _getDefaultSpinnerValue(){
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
        return server_url_prefixed_list[ spinner.getSelectedItemPosition() ];
    }

    /**
     * Almost the same as the previous one.
     * @param spinner
     * @return
     */
    private String getPadServerFromSpinner( Spinner spinner ){
        return server_url_list[ spinner.getSelectedItemPosition() ];
    }
}
