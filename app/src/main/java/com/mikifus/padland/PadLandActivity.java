package com.mikifus.padland;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Menu;
import android.view.MenuItem;

/**
 * It is just the Activity parent class to inherit
 * @author mikifus
 */
public class PadLandActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pad_list, menu);

        // Share button
        /*MenuItem share_menu_item = menu.getItem(R.id.menuitem_share);
        if(share_menu_item != null){

        }*/

        return true;
    }
    public boolean onCreateOptionsMenu( Menu menu, int menu_id ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( menu_id, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.action_newpad:
                intent = new Intent(this, NewPadActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_about:
                intent = new Intent(this, About.class);
                this.startActivity(intent);
                break;
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
