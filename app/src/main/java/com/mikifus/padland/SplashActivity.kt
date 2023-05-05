package com.mikifus.padland

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager

/**
 * It does only one thing: show a fullscreen splash image during the
 * specified time.
 */
class SplashActivity : PadLandActivity() {
    /** Duration of wait  */
    private val SPLASH_DISPLAY_LENGTH = 1500

    /**
     * onCreate override
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/Handler().postDelayed({ launchNext() }, SPLASH_DISPLAY_LENGTH.toLong())
    }

    protected fun launchNext() {
        //  Declare a new thread to do a preference check
        val t = Thread {
            //  Initialize SharedPreferences
            val getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(baseContext)

            //  Create a new boolean and preference and set it to true
            val isFirstStart = getPrefs.getBoolean(OPTION_FIRST_START, true)

            //  If the activity has never started before...
            if (isFirstStart) {

                //  Launch app intro
                val i = Intent(this@SplashActivity, IntroActivity::class.java)
                startActivity(i)

                // Finish current activity to avoid back button taking the user here
                finish()

                //  Make a new preferences editor
                val e = getPrefs.edit()

                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean(OPTION_FIRST_START, false)

                //  Apply changes
                e.apply()
            } else {
                // Launch real main activity
                val mainIntent = Intent(this@SplashActivity, PadListActivity::class.java)
                this@SplashActivity.startActivity(mainIntent)
                finish()
            }
        }

        // Start the thread
        t.start()
    }

    companion object {
        private const val OPTION_FIRST_START = "first_start"
    }
}