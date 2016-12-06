package com.mikifus.padland;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * It does only one thing: show a fullscreen splash image during the
 * specified time.
 */
public class SplashActivity extends PadLandActivity {
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1500;

    private static final String OPTION_FIRST_START = "first_start";

    /**
     * onCreate override
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                launchNext();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    protected void launchNext() {
        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean(OPTION_FIRST_START, true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(SplashActivity.this, IntroActivity.class);
                    startActivity(i);

                    // Finish current activity to avoid back button taking the user here
                    SplashActivity.this.finish();

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean(OPTION_FIRST_START, false);

                    //  Apply changes
                    e.apply();
                } else {
                    // Launch real main activity
                    Intent mainIntent = new Intent(SplashActivity.this, PadListActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }
        });

        // Start the thread
        t.start();
    }
}
