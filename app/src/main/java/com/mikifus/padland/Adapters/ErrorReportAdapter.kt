package com.mikifus.padland.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReport
import java.text.DateFormat
import java.util.Date

/**
 * Saved error reports list, newest first.
 */
class ErrorReportAdapter(
    private val onClick: (ErrorReport) -> Unit,
) : ListAdapter<ErrorReport, ErrorReportAdapter.ErrorReportViewHolder>(DIFF_CALLBACK) {

    private val dateFormat: DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)

    class ErrorReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.error_report_item_title)
        val dateTextView: TextView = itemView.findViewById(R.id.error_report_item_date)
        val messageTextView: TextView = itemView.findViewById(R.id.error_report_item_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorReportViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.error_report_list_item, parent, false)
        return ErrorReportViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ErrorReportViewHolder, position: Int) {
        val report = getItem(position)
        val context = holder.itemView.context

        val type = context.getString(
            if (report.fatal) R.string.error_report_type_crash else R.string.error_report_type_error)
        holder.titleTextView.text = context.getString(
            R.string.error_report_item_title, type, report.shortExceptionClass)

        val screen = report.screen
        val date = dateFormat.format(Date(report.timestamp))
        holder.dateTextView.text = if (screen.isNullOrBlank()) date else "$date · $screen"

        val message = report.message?.lineSequence()?.firstOrNull()?.trim()
        holder.messageTextView.text = message
        holder.messageTextView.visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE

        holder.itemView.setOnClickListener { onClick(report) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ErrorReport>() {
            override fun areItemsTheSame(oldItem: ErrorReport, newItem: ErrorReport) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ErrorReport, newItem: ErrorReport) =
                oldItem == newItem
        }
    }
}

