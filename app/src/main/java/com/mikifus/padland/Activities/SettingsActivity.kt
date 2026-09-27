package com.mikifus.padland.Activities

import android.content.DialogInterface
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
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import com.mikifus.padland.Utils.Export.ExportHelper
import com.mikifus.padland.Utils.Export.IExportHelper
import com.mikifus.padland.Utils.Export.IImportHelper
import com.mikifus.padland.Utils.Export.ImportHelper
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
        SharedPreferences.OnSharedPreferenceChangeListener {

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
            initErrorReportPreferences()
        }

        override fun onResume() {
            super.onResume()
            updateErrorReportsSummary()
        }

        private fun initErrorReportPreferences() {
            findPreference<Preference>(PREF_ERROR_REPORTS)?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), ErrorReportListActivity::class.java))
                true
            }

            findPreference<Preference>(PREF_ERROR_REPORTS_CLEAR)?.setOnPreferenceClickListener {
                val dialog = ConfirmDialog()
                dialog.setTitle(getString(R.string.error_reports_clear))
                dialog.setMessage(getString(R.string.error_reports_clear_confirm))
                dialog.positiveButtonText = getString(R.string.delete)
                dialog.positiveButtonCallback = DialogInterface.OnClickListener { _, _ ->
                    val store = ErrorReporter.getStore(requireContext())
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) { store.clear() }
                        updateErrorReportsSummary()
                    }
                }
                dialog.show(parentFragmentManager, CONFIRM_CLEAR_TAG)
                true
            }
        }

        private fun updateErrorReportsSummary() {
            val store = ErrorReporter.getStore(requireContext())
            lifecycleScope.launch {
                val count = withContext(Dispatchers.IO) { store.count() }
                if (!isAdded) return@launch

                findPreference<Preference>(PREF_ERROR_REPORTS)?.summary = if (count > 0) {
                    resources.getQuantityString(R.plurals.error_reports_count, count, count)
                } else {
                    getString(R.string.error_reports_none)
                }
                findPreference<Preference>(PREF_ERROR_REPORTS_CLEAR)?.isEnabled = count > 0
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

        companion object {
            private const val PREF_ERROR_REPORTS = "padland_error_reports"
            private const val PREF_ERROR_REPORTS_CLEAR = "padland_error_reports_clear"
            private const val CONFIRM_CLEAR_TAG = "ConfirmClearErrorReports"
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