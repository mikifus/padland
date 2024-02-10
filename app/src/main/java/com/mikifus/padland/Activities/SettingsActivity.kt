package com.mikifus.padland.Activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mikifus.padland.R


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_content, SettingsFragment())
            .commit()
    }


    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        private var sharedPreferences: SharedPreferences? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

            sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
//            val preference: Preference? = key?.let { findPreference(it) }
//            if (preference is ListPreference) {
//                val listPreference: ListPreference = preference as ListPreference
//                val prefIndex: Int =
//                    listPreference.findIndexOfValue(sharedPreferences.getString(key, ""))
//                if (prefIndex >= 0) {
//                    preference.setSummary(listPreference.entries[prefIndex])
//                }
//            } else if (preference is SwitchPreferenceCompat) {
//                val switchPreference: SwitchPreferenceCompat = preference
////                val prefIndex: Int =
////                    switchPreference.findIndexOfValue(sharedPreferences.getString(key, ""))
////                if (prefIndex >= 0) {
////                    preference.setSummary(switchPreference.entries[prefIndex])
////                }
//            } else {
//                preference?.summary = sharedPreferences.getString(key, "")
//            }
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
//            (findPreference("padland_default_color") as ColorPickerListPreference?)?.reload()
        }

//        fun setDefaultServerPreferenceValues() {
//            val entries = arguments["server_name_list"] as Array<String>?
//            val defaultServer = findPreference("padland_default_server") as ListPreference
//            defaultServer.entries = entries
//            defaultServer.entryValues = entries
//        }
    }
}