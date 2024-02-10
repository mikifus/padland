package com.mikifus.padland.Activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ColorPickerListPreference
import com.rarepebble.colorpicker.ColorPreference


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
        var serverViewModel: ServerViewModel? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

            sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)

            initDefaultServerPreference()
        }

        private fun initDefaultServerPreference() {
            if(serverViewModel == null) {
                serverViewModel = ViewModelProvider(requireActivity())[ServerViewModel::class.java]
            }

            serverViewModel!!.getAllEnabled.observe(this) { servers ->
                // DB
                val serverEntries = servers.map{ it.mName }.toTypedArray() +
                        resources.getStringArray(R.array.etherpad_servers_name)

                // Hardcoded
                val serverValues = servers.map{ it.mUrl + it.mPadprefix }.toTypedArray() +
                        resources.getStringArray(R.array.etherpad_servers_url_padprefix)

                val listPreference = findPreference<ListPreference>("padland_default_server")
                listPreference?.entries = serverEntries
                listPreference?.entryValues = serverValues
            }
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (preference is ColorPreference) {
                preference.showDialog(this, 0)
            } else super.onDisplayPreferenceDialog(preference)
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
            (findPreference("padland_default_color") as ColorPickerListPreference?)?.reload()
        }
    }
}