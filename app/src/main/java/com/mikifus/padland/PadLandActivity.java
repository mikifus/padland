package com.mikifus.padland;

import android.app.LoaderManager;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * It is just the Activity parent class to inherit
 * @author mikifus
 */
public class PadLandActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The default menu will be the pad_list one.
     * @param menu
     * @param menu_id
     * @return
     */
    public boolean onCreateOptionsMenu( Menu menu, int menu_id ) {
        if( menu_id == 0 )
        {
            //Default value
            menu_id = R.menu.pad_list;
        }
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( menu_id, menu );
        return true;
    }

    /**
     * Manage the menu options when selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Check wheter it is possible to connect to the internet
     * @return
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Initializes a loader manager
     * The callbacks_container param is an object or class containing callback methods.
     * @param callbacks_container
     * @return
     */
    protected void initLoader(LoaderManager.LoaderCallbacks callbacks_container){
        getLoaderManager().initLoader(0, null, callbacks_container);
    }
}
