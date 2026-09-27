package com.mikifus.padland.Dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
 */
class ErrorReportDialog : DialogFragment() {

    private var closedByUser = false
    private var reportText: String = ""

    private val ids: List<String>
        get() = requireArguments().getStringArrayList(ARG_IDS) ?: listOf()

    private val mode: Int
        get() = requireArguments().getInt(ARG_MODE, MODE_DETAIL)

    /** Null for [MODE_DETAIL], which is not managed by the [ErrorReporter] queue */
    private val popupRequest: ErrorReporter.PopupRequest?
        get() = when (mode) {
            MODE_POPUP -> ErrorReporter.PopupRequest(ids, previousCrash = false)
            MODE_PREVIOUS_CRASH -> ErrorReporter.PopupRequest(ids, previousCrash = true)
            else -> null
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val store = ErrorReporter.getStore(context)
        val allIds = ids
        val reports = allIds.take(MAX_SHOWN_REPORTS).mapNotNull { store.get(it) }
        reportText = reports.joinToString(REPORT_SEPARATOR) { it.toReportText() }

        val view = layoutInflater.inflate(R.layout.dialog_error_report, null)
        view.findViewById<TextView>(R.id.error_report_summary).text =
            makeSummary(reports, allIds.size)
        view.findViewById<TextView>(R.id.error_report_trace).text = reportText

        val builder = MaterialAlertDialogBuilder(context)
            .setTitle(makeTitle(reports))
            .setView(view)
            .setNegativeButton(R.string.error_report_close) { _, _ -> onClosedByUser() }

        if (reports.isNotEmpty()) {
            // Listener set in onStart() so copying does not close the dialog
            builder.setPositiveButton(R.string.copy, null)
        }

        if (mode == MODE_DETAIL) {
            if (reports.isNotEmpty()) {
                builder.setNeutralButton(R.string.share) { _, _ ->
                    onClosedByUser()
                    share()
                }
            }
        } else {
            builder.setNeutralButton(R.string.error_report_show_all) { _, _ ->
                onClosedByUser()
                startActivity(Intent(context, ErrorReportListActivity::class.java))
            }
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)
            ?.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setOnClickListener { copy() }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onClosedByUser()
    }

    override fun onDestroy() {
        super.onDestroy()
        val changingConfigurations = activity?.isChangingConfigurations == true
        if (!changingConfigurations) {
            ErrorReporter.onPopupDestroyed(popupRequest, closedByUser)
        }
    }

    private fun onClosedByUser() {
        if (closedByUser) return
        closedByUser = true
        popupRequest?.let { ErrorReporter.onPopupClosed(it) }
    }

    private fun makeTitle(reports: List<ErrorReport>): String = when {
        reports.isEmpty() -> getString(R.string.error_report_not_found_title)
        mode == MODE_PREVIOUS_CRASH -> getString(R.string.error_report_previous_crash_title)
        mode == MODE_POPUP -> getString(R.string.error_report_popup_title)
        else -> reports.first().shortExceptionClass
    }

    private fun makeSummary(reports: List<ErrorReport>, total: Int): String {
        if (reports.isEmpty()) {
            return getString(R.string.error_report_not_found)
        }

        val summary = StringBuilder(when (mode) {
            MODE_PREVIOUS_CRASH -> getString(R.string.error_report_previous_crash_summary)
            MODE_POPUP -> getString(R.string.error_report_popup_summary)
            else -> {
                val report = reports.first()
                getString(
                    if (report.fatal) R.string.error_report_detail_crash
                    else R.string.error_report_detail_error,
                    ErrorReport.formatTimestamp(report.timestamp)
                )
            }
        })

        val notShown = total - reports.size
        if (notShown > 0) {
            summary.append("\n\n").append(resources.getQuantityString(
                R.plurals.error_report_more_reports, notShown, notShown))
        }

        return summary.toString()
    }

    private fun copy() {
        val context = context ?: return
        val copied = PadClipboardHelper.copyText(
            context, getString(R.string.error_report_clip_label), reportText)

        // Android 13+ shows its own confirmation
        if (!copied) {
            Toast.makeText(context, R.string.error_report_copy_failed, Toast.LENGTH_LONG).show()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, R.string.copy_copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun share() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.error_report_clip_label))
            putExtra(Intent.EXTRA_TEXT, reportText)
        }
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.share)))
        } catch (e: RuntimeException) {
            // No app to share with, or text too large
            Toast.makeText(requireContext(), R.string.unexpected_error, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "ErrorReportDialog"

        const val MODE_POPUP = 0
        const val MODE_PREVIOUS_CRASH = 1
        const val MODE_DETAIL = 2

        /** Keeps the text (and the clipboard) at a reasonable size */
        private const val MAX_SHOWN_REPORTS = 3
        private const val REPORT_SEPARATOR = "\n\n----------------------------------------\n\n"

        private const val ARG_IDS = "ids"
        private const val ARG_MODE = "mode"

        fun newPopup(request: ErrorReporter.PopupRequest): ErrorReportDialog =
            newInstance(
                request.ids,
                if (request.previousCrash) MODE_PREVIOUS_CRASH else MODE_POPUP
            )

        fun newDetail(id: String): ErrorReportDialog = newInstance(listOf(id), MODE_DETAIL)

        private fun newInstance(ids: List<String>, mode: Int): ErrorReportDialog {
            return ErrorReportDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_IDS, ArrayList(ids))
                    putInt(ARG_MODE, mode)
                }
            }
        }
    }
}

