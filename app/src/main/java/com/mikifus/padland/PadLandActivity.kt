package com.mikifus.padland

import android.app.LoaderManager
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

/**
 * It is just the Activity parent class to inherit
 * @author mikifus
 */
open class PadLandActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * The default menu will be the pad_list one.
     * @param menu
     * @param menu_id
     * @return
     */
    open fun onCreateOptionsMenu(menu: Menu?, menu_id: Int): Boolean {
        var menu_id = menu_id
        if (menu_id == 0) {
            //Default value
            menu_id = R.menu.pad_list
        }
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(menu_id, menu)
        return true
    }

    /**
     * Manage the menu options when selected
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.action_settings -> {
                intent = Intent(this, SettingsActivity::class.java)
                this.startActivity(intent)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Check wheter it is possible to connect to the internet
     * @return
     */
    val isNetworkAvailable: Boolean
        get() {
            val cm = getSystemService(this.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }

    /**
     * Initializes a loader manager
     * The callbacks_container param is an object or class containing callback methods.
     * @param callbacks_container
     * @return
     */
    protected fun initLoader(callbacks_container: LoaderManager.LoaderCallbacks<*>?) {
        loaderManager.initLoader<Any>(0, null, callbacks_container)
    }
}