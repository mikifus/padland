package com.mikifus.padland.Activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.Managers.IManagesDeleteErrorReportsDialog
import com.mikifus.padland.Dialogs.Managers.IManagesDeleteOfflineCopiesDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDeleteErrorReportsDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDeleteOfflineCopiesDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import com.mikifus.padland.Utils.Export.ExportHelper
import com.mikifus.padland.Utils.Export.IExportHelper
import com.mikifus.padland.Utils.Export.IImportHelper
import com.mikifus.padland.Utils.Export.ImportHelper
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import com.rarepebble.colorpicker.ColorPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import androidx.activity.enableEdgeToEdge


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.activity_toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_content, SettingsFragment())
            .commit()
    }


    class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        IManagesDeleteErrorReportsDialog by ManagesDeleteErrorReportsDialog(),
        IManagesDeleteOfflineCopiesDialog by ManagesDeleteOfflineCopiesDialog() {

        private var sharedPreferences: SharedPreferences? = null
        var serverViewModel: ServerViewModel? = null
        private var exportHelper: IExportHelper? = null
        private var importHelper: IImportHelper? = null


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.preferences)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

            sharedPreferences!!.registerOnSharedPreferenceChangeListener(this)

            initDefaultServerPreference()
            initExportPreference()
            initImportPreference()
            initOfflineCopiesPreference()
            initErrorReportPreferences()
        }

        override fun onResume() {
            super.onResume()
            updateErrorReportsSummary()
            updateOfflineCopiesPreference()
        }

        private fun initOfflineCopiesPreference() {
            val preference = findPreference<Preference>("padland_offline_copies_delete")
            preference?.setOnPreferenceClickListener {
                showDeleteOfflineCopiesDialog(requireActivity() as AppCompatActivity) {
                    updateOfflineCopiesPreference()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.offline_copies_dialog_delete_copies_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
        }

        /**
         * Nothing to delete, nothing to click.
         */
        private fun updateOfflineCopiesPreference() {
            if(!isAdded) {
                return
            }

            val offlinePadStore = OfflinePadFetcher.getStore(requireContext())
            lifecycleScope.launch {
                val count = withContext(Dispatchers.IO) {
                    offlinePadStore.count()
                }
                if(!isAdded) {
                    return@launch
                }

                findPreference<Preference>("padland_offline_copies_delete")?.isEnabled = count > 0
            }
        }

        private fun initErrorReportPreferences() {
            val listPreference = findPreference<Preference>("padland_error_report_list")
            listPreference?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), ErrorReportListActivity::class.java))
                true
            }

            val deletePreference = findPreference<Preference>("padland_error_report_list_delete")
            deletePreference?.setOnPreferenceClickListener {
                showDeleteErrorReportsDialog(requireActivity() as AppCompatActivity) {
                    updateErrorReportsSummary()
                }
                true
            }
        }

        private fun updateErrorReportsSummary() {
            if(!isAdded) {
                return
            }

            val errorReportStore = ErrorReporter.getStore(requireContext())
            lifecycleScope.launch {
                val count = withContext(Dispatchers.IO) {
                    errorReportStore.count()
                }
                if(!isAdded) {
                    return@launch
                }

                val listPreference = findPreference<Preference>("padland_error_report_list")
                listPreference?.summary = if(count > 0) {
                    resources.getQuantityString(R.plurals.pref_error_report_list_summary, count, count)
                } else {
                    getString(R.string.pref_error_report_list_summary_none)
                }

                val deletePreference = findPreference<Preference>("padland_error_report_list_delete")
                deletePreference?.isEnabled = count > 0
            }
        }

        private fun initDefaultServerPreference() {
            if(serverViewModel == null) {
                serverViewModel = ViewModelProvider(this)[ServerViewModel::class.java]
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

        private fun initExportPreference() {
            val preference = findPreference<Preference>("padland_export")
            preference?.setOnPreferenceClickListener {
                val formatter: DateFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        requireContext().resources.configuration.locales.get(0))
                } else {
                    @Suppress("DEPRECATION")
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                        requireContext().resources.configuration.locale)
                }

                val now: Date = Calendar.getInstance().time
                exportHelper?.launcher?.launch("Padland-export-${formatter.format(now)}.json")

                Toast.makeText(
                    requireContext(),
                    getString(R.string.export_exporting_file),
                    Toast.LENGTH_LONG
                ).show()

                true
            }

            initExportHelper()
        }

        private fun initImportPreference() {
            val preference = findPreference<Preference>("padland_import")
            preference?.setOnPreferenceClickListener {
                val now: Date = Calendar.getInstance().time
                importHelper?.launcher?.launch(arrayOf("text/*"))

                Toast.makeText(
                    requireContext(),
                    getString(R.string.import_importing_file),
                    Toast.LENGTH_LONG
                ).show()

                true
            }

            initImportHelper()
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (preference is ColorPreference) {
                preference.showDialog(this, 0)
            } else super.onDisplayPreferenceDialog(preference)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {}

        private fun initExportHelper() {
            exportHelper = ExportHelper(requireActivity()) { result ->
                if(result) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.export_exporting_success),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.export_exporting_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        private fun initImportHelper() {
            importHelper = ImportHelper(requireActivity()) { done, result ->
                if(done) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.import_importing_success, result.toString()),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.import_importing_failed),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
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
}