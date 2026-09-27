package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ShareCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.mikifus.padland.Activities.ErrorReportListActivity
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReport
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import com.mikifus.padland.Utils.PadClipboardHelper

/**
 * Shows one or more saved error reports with the full stack trace,
 * and lets the user copy (or share) them to report the problem.
 *
 * Modes:
 * - [MODE_POPUP]: an error just happened.
 * - [MODE_PREVIOUS_CRASH]: the app crashed during the previous run.
 * - [MODE_DETAIL]: opened from the saved errors list.
 *
 * The report ids and mode are kept in the arguments, so they survive recreation.
 */
class ErrorReportDialog: DialogFragment() {

    private var mSummaryTextView: MaterialTextView? = null
    private var mTraceTextView: MaterialTextView? = null

    private var closedByUser: Boolean = false
    private var reportText: String = ""

    private val reportIds: List<String>
        get() = arguments?.getStringArrayList(ARG_REPORT_IDS) ?: listOf()

    private val mode: Int
        get() = arguments?.getInt(ARG_MODE, MODE_DETAIL) ?: MODE_DETAIL

    /** Null for [MODE_DETAIL], which is not managed by the [ErrorReporter] queue */
    private val popupRequest: ErrorReporter.PopupRequest?
        get() = when (mode) {
            MODE_POPUP -> ErrorReporter.PopupRequest(reportIds, previousCrash = false)
            MODE_PREVIOUS_CRASH -> ErrorReporter.PopupRequest(reportIds, previousCrash = true)
            else -> null
        }

    fun setReportIds(ids: List<String>, mode: Int = MODE_DETAIL) {
        arguments = Bundle().apply {
            putStringArrayList(ARG_REPORT_IDS, ArrayList(ids))
            putInt(ARG_MODE, mode)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // The same instance may be shown again, see ManagesErrorReportDialog
        closedByUser = false

        val errorReportStore = ErrorReporter.getStore(requireContext())
        val ids = reportIds
        val reports = ids.take(MAX_SHOWN_REPORTS).mapNotNull { errorReportStore.get(it) }
        reportText = reports.joinToString(REPORT_SEPARATOR) { it.toReportText() }

        val view = layoutInflater.inflate(R.layout.dialog_error_report, null)
        mSummaryTextView = view.findViewById(R.id.txt_error_report_summary)
        mTraceTextView = view.findViewById(R.id.txt_error_report_trace)

        mSummaryTextView!!.text = makeSummary(reports, ids.size)
        mTraceTextView!!.text = reportText

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(makeTitle(reports))
            .setView(view)
            .setNegativeButton(R.string.error_report_dialog_close) { _, _ -> onClosedByUser() }

        if(reports.isNotEmpty()) {
            // Listener set in onStart() so copying does not close the dialog
            builder.setPositiveButton(R.string.copy, null)
        }

        if(mode == MODE_DETAIL) {
            if(reports.isNotEmpty()) {
                builder.setNeutralButton(R.string.share) { _, _ ->
                    onClosedByUser()
                    shareReport()
                }
            }
        } else {
            builder.setNeutralButton(R.string.error_report_dialog_show_all) { _, _ ->
                onClosedByUser()
                startActivity(Intent(requireContext(), ErrorReportListActivity::class.java))
            }
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)
            ?.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setOnClickListener { copyReport() }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onClosedByUser()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(activity?.isChangingConfigurations != true) {
            ErrorReporter.onPopupDestroyed(popupRequest, closedByUser)
        }
    }

    private fun onClosedByUser() {
        if(closedByUser) {
            return
        }
        closedByUser = true
        popupRequest?.let { ErrorReporter.onPopupClosed(it) }
    }

    private fun makeTitle(reports: List<ErrorReport>): String {
        return when {
            reports.isEmpty() -> getString(R.string.error_report_dialog_not_found_title)
            mode == MODE_PREVIOUS_CRASH -> getString(R.string.error_report_dialog_previous_crash_title)
            mode == MODE_POPUP -> getString(R.string.error_report_dialog_popup_title)
            else -> reports.first().shortExceptionClass
        }
    }

    private fun makeSummary(reports: List<ErrorReport>, total: Int): String {
        if(reports.isEmpty()) {
            return getString(R.string.error_report_dialog_not_found)
        }

        val summary = StringBuilder(when (mode) {
            MODE_PREVIOUS_CRASH -> getString(R.string.error_report_dialog_previous_crash_summary)
            MODE_POPUP -> getString(R.string.error_report_dialog_popup_summary)
            else -> {
                val report = reports.first()
                getString(
                    if (report.fatal) R.string.error_report_dialog_detail_crash
                    else R.string.error_report_dialog_detail_error,
                    ErrorReport.formatTimestamp(report.timestamp)
                )
            }
        })

        val notShown = total - reports.size
        if(notShown > 0) {
            summary.append("\n\n").append(resources.getQuantityString(
                R.plurals.error_report_dialog_more_reports, notShown, notShown))
        }

        return summary.toString()
    }

    private fun copyReport() {
        val copied = PadClipboardHelper.copyText(
            requireContext(), getString(R.string.error_report_clip_label), reportText)

        // Android 13+ shows its own confirmation
        if(!copied) {
            Toast.makeText(requireContext(),
                getString(R.string.error_report_dialog_copy_failed), Toast.LENGTH_LONG).show()
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(requireContext(),
                getString(R.string.copy_copied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareReport() {
        try {
            ShareCompat.IntentBuilder(requireActivity())
                .setType("text/plain")
                .setChooserTitle(getString(R.string.share))
                .setSubject(getString(R.string.error_report_clip_label))
                .setText(reportText)
                .startChooser()
        } catch (e: RuntimeException) {
            // No app to share with, or text too large
            Toast.makeText(requireContext(),
                getString(R.string.unexpected_error), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val DIALOG_TAG = "DIALOG_ERROR_REPORT"

        const val MODE_POPUP = 0
        const val MODE_PREVIOUS_CRASH = 1
        const val MODE_DETAIL = 2

        /** Keeps the text (and the clipboard) at a reasonable size */
        private const val MAX_SHOWN_REPORTS = 3
        private const val REPORT_SEPARATOR = "\n\n----------------------------------------\n\n"

        private const val ARG_REPORT_IDS = "report_ids"
        private const val ARG_MODE = "mode"
    }
}

