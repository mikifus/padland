package com.mikifus.padland.Activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.ErrorReportAdapter
import com.mikifus.padland.Dialogs.Managers.IManagesDeleteErrorReportsDialog
import com.mikifus.padland.Dialogs.Managers.IManagesErrorReportDialog
import com.mikifus.padland.Dialogs.Managers.ManagesDeleteErrorReportsDialog
import com.mikifus.padland.Dialogs.Managers.ManagesErrorReportDialog
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReportStore
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Lists the error reports saved in the app private storage,
 * whether the error popups are enabled or not.
 */
class ErrorReportListActivity: AppCompatActivity(),
    IManagesErrorReportDialog by ManagesErrorReportDialog(),
    IManagesDeleteErrorReportsDialog by ManagesDeleteErrorReportsDialog() {

    private var errorReportStore: ErrorReportStore? = null
    private var errorReportAdapter: ErrorReportAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var mEmptyLayout: LinearLayoutCompat? = null
    private var hasErrorReports: Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_error_report_list)
        setSupportActionBar(findViewById(R.id.activity_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        errorReportStore = ErrorReporter.getStore(this)
        errorReportAdapter = ErrorReportAdapter(this, getOnItemClickListener())

        mEmptyLayout = findViewById(R.id.empty)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = errorReportAdapter
        recyclerView!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        ViewCompat.setNestedScrollingEnabled(recyclerView!!, false)
    }

    override fun onResume() {
        super.onResume()
        // Files are not observable, reload in case new errors were saved meanwhile
        initListView()
    }

    private fun initListView() {
        lifecycleScope.launch {
            val errorReportList = withContext(Dispatchers.IO) {
                errorReportStore!!.list()
            }
            errorReportAdapter!!.data = errorReportList
            hasErrorReports = errorReportList.isNotEmpty()
            showHideEmpty(!hasErrorReports)
            invalidateOptionsMenu()
        }
    }

    private fun getOnItemClickListener(): View.OnClickListener {
        return View.OnClickListener{ view: View ->
            showErrorReportDialog(this, view.tag as String)
        }
    }

    private fun showHideEmpty(visible: Boolean) {
        if(mEmptyLayout != null) {
            mEmptyLayout!!.visibility = if (visible) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.error_report_list, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_delete_error_reports)?.isVisible = hasErrorReports
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_error_reports -> {
                showDeleteErrorReportsDialog(this) {
                    initListView()
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

