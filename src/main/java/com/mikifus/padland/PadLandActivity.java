package com.mikifus.padland;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by mikifus on 12/01/15.
 */
public class PadLandActivity extends Activity {


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pad_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch(item.getItemId()) {
            case R.id.action_newpad:
                intent = new Intent(this, NewPad.class);
                this.startActivity(intent);
                break;
            case R.id.action_about:
                intent = new Intent(this, About.class);
                this.startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
/*                SettingsActivity.GeneralPreferenceFragment fragment = new SettingsActivity.GeneralPreferenceFragment();
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content , fragment)
                        .commit();*/
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}
