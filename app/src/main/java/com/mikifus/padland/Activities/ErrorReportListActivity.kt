package com.mikifus.padland.Activities

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.ErrorReportAdapter
import com.mikifus.padland.Dialogs.ConfirmDialog
import com.mikifus.padland.Dialogs.ErrorReportDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReportStore
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Lists the saved error reports (stored in the app private storage),
 * whether the error popups are enabled or not.
 */
class ErrorReportListActivity : AppCompatActivity() {

    private lateinit var store: ErrorReportStore
    private lateinit var adapter: ErrorReportAdapter
    private var emptyView: View? = null
    private var hasReports = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_error_report_list)
        setSupportActionBar(findViewById(R.id.activity_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.error_reports_title)

        store = ErrorReporter.getStore(this)
        adapter = ErrorReportAdapter { report -> showReport(report.id) }

        emptyView = findViewById(R.id.empty)
        findViewById<RecyclerView>(R.id.recyclerview).apply {
            layoutManager = LinearLayoutManager(this@ErrorReportListActivity)
            adapter = this@ErrorReportListActivity.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    override fun onResume() {
        super.onResume()
        loadReports()
    }

    private fun loadReports() {
        lifecycleScope.launch {
            val reports = withContext(Dispatchers.IO) { store.list() }
            adapter.submitList(reports)
            hasReports = reports.isNotEmpty()
            emptyView?.visibility = if (hasReports) View.GONE else View.VISIBLE
            invalidateOptionsMenu()
        }
    }

    private fun showReport(id: String) {
        if (supportFragmentManager.findFragmentByTag(ErrorReportDialog.TAG) != null) return
        ErrorReportDialog.newDetail(id).show(supportFragmentManager, ErrorReportDialog.TAG)
    }

    private fun confirmClear() {
        val dialog = ConfirmDialog()
        dialog.setTitle(getString(R.string.error_reports_clear))
        dialog.setMessage(getString(R.string.error_reports_clear_confirm))
        dialog.positiveButtonText = getString(R.string.delete)
        dialog.positiveButtonCallback = DialogInterface.OnClickListener { _, _ ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) { store.clear() }
                loadReports()
            }
        }
        dialog.show(supportFragmentManager, CONFIRM_CLEAR_TAG)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.error_report_list, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_clear_error_reports)?.isVisible = hasReports
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            R.id.action_clear_error_reports -> {
                confirmClear()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val CONFIRM_CLEAR_TAG = "ConfirmClearErrorReports"
    }
}

