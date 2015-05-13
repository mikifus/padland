package com.mikifus.padland;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        this._setSpinnerDefaultValue();
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
        // Server list to provide a fallback value
        String server_list [] = getResources().getStringArray( R.array.etherpad_servers_name );

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
        String padPrefix = this.getPadPrefixFromSpinner((Spinner) findViewById(R.id.spinner));
        String padServer = this.getPadServerFromSpinner( (Spinner) findViewById(R.id.spinner) );

        Log.d("CREATENEW", padName);
        if ( padName.isEmpty() ) {
            Toast.makeText(this, (getString(R.string.newpad_noname_warning)), Toast.LENGTH_LONG).show();
            return;
        }
        Pattern p = Pattern.compile( "[a-zA-Z0-9-_]+" );
        Matcher m = p.matcher( padName );
        if( !m.matches() )
        {
            Toast.makeText(this, "The pad name contains invalid characters.", Toast.LENGTH_LONG).show();
            return;
        }

        String padUrl = padPrefix + padName;

        Intent padViewIntent =
                new Intent( NewPadActivity.this, PadViewActivity.class );
        padViewIntent.putExtra( "padName", padName );
        padViewIntent.putExtra( "padServer", padServer );
        padViewIntent.putExtra( "padUrl", padUrl );

        startActivity(padViewIntent);
        finish();
    }

    /**
     * Given an input view, gets the text
     * @param input
     * @return
     */
    private String getPadNameFromInput( TextView input ){
        String padName = (String) input.getText().toString();
        return padName;
    }

    /**
     * Same as previous but with a spinner
     * @param spinner
     * @return
     */
    private String getPadPrefixFromSpinner( Spinner spinner ){
        String padPrefix = getResources().getStringArray( R.array.etherpad_servers_url_padprefix )[spinner.getSelectedItemPosition()];
        return padPrefix;
    }

    /**
     * Almost the same as the previous one.
     * @param spinner
     * @return
     */
    private String getPadServerFromSpinner( Spinner spinner ){
        String padServer = getResources().getStringArray( R.array.etherpad_servers_url_home )[spinner.getSelectedItemPosition()];
        return padServer;
    }
}
