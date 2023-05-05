package com.mikifus.padland

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.CheckBoxPreference
import android.preference.ListPreference
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.Utils.ColorPickerListPreference
import java.util.Arrays

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatActivity() {
    var settingsFragment: SettingsFragment? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                // TODO: If Settings has multiple levels, Up should navigate up
                // that hierarchy.
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Display the fragment as the main content.
        val arguments = Bundle()
        arguments.putStringArray("server_name_list", serverNameList)
        settingsFragment = SettingsFragment()
        settingsFragment!!.arguments = arguments
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit()
    }

    override fun onResume() {
        super.onResume()
        // If a new pad is added, the list to choose default must be refreshed
        if (settingsFragment != null) {
            settingsFragment!!.arguments.putStringArray("server_name_list", serverNameList)
            settingsFragment!!.setDefaultServerPreferenceValues()
        }
    }

    /**
     * This fragment shows general pref_general only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)

            // Bind the summaries of EditText/List/Dialog/Ringtone pref_general
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("auto_save_new_pads"))

//            setDefaultServerPreferenceValues();
        }
    }

    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(paramBundle: Bundle?) {
            super.onCreate(paramBundle)
            addPreferencesFromResource(R.xml.pref_general)
            setDefaultServerPreferenceValues()
        }

        fun setDefaultServerPreferenceValues() {
            val entries = arguments["server_name_list"] as Array<String>?
            val defaultServer = findPreference("padland_default_server") as ListPreference
            defaultServer.entries = entries
            defaultServer.entryValues = entries
        }
    }// Load the custom servers

    // Server list to provide a fallback value
    /**
     * Returns a string with the server urls and the prefix to see a pad.
     * Includes custom servers.
     * @return String[]
     */
    val serverNameList: Array<String?>
        get() {
            val server_list: Array<String?>
            // Load the custom servers
            val serverModel = ServerModel(this)
            val custom_servers = serverModel.enabledServerList
            val server_names = ArrayList<String?>()
            for (server in custom_servers!!) {
                server_names.add(server.getName())
            }

            // Server list to provide a fallback value
            val collection: MutableCollection<String?> = ArrayList()
            collection.addAll(server_names)
            collection.addAll(Arrays.asList(*resources.getStringArray(R.array.etherpad_servers_name)))
            server_list = collection.toTypedArray()
            return server_list
        }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        (settingsFragment!!.findPreference("padland_default_color") as ColorPickerListPreference).reload()
    }

    companion object {
        /**
         * Determines whether to always show the simplified settings UI, where
         * settings are presented in a single list. When false, settings are shown
         * as a master/detail two-pane view on tablets. When true, a single pane is
         * shown on tablets.
         */
        private const val ALWAYS_SIMPLE_PREFS = false

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return (context.resources.configuration.screenLayout
                    and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Determines whether the simplified settings UI should be shown. This is
         * true if this is forced via [.ALWAYS_SIMPLE_PREFS], or the device
         * doesn't have newer APIs like [PreferenceFragment], or the device
         * doesn't have an extra-large screen. In these cases, a single-pane
         * "simplified" settings UI should be shown.
         */
        private fun isSimplePreferences(context: Context): Boolean {
            return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context)
        }

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()
            if (preference is ListPreference) {
                // For list pref_general, look up the correct display value in
                // the preference's 'entries' list.
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0) listPreference.entries[index] else null)
            } else {
                // For all other pref_general, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.
         *
         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            /*// Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));*/

            // This method crashed by default, because of yeah. So Let's workaround:
            // Set the listener to watch for value changes.
            if (preference is ListPreference) {
                // Trigger the listener immediately with the preference's
                // current value.
                sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(
                                preference.getContext()).getInt(preference.getKey(), 0))
            } else if (preference is CheckBoxPreference) {
                // Trigger the listener immediately with the preference's
                // current value.
                sBindPreferenceSummaryToValueListener.onPreferenceChange(
                        preference,
                        PreferenceManager.getDefaultSharedPreferences(
                                preference.getContext()).getBoolean(preference.getKey(), true))
            } else {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.context)
                                .getString(preference.key, ""))
            }
        }
    }
}