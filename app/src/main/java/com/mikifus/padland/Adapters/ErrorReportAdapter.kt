package com.mikifus.padland.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Adapters.DiffUtilCallbacks.ErrorReportAdapterDiffUtilCallback
import com.mikifus.padland.R
import com.mikifus.padland.Utils.ErrorReporting.ErrorReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

/**
 * Saved error reports list. The item layout tag holds the report id.
 */
class ErrorReportAdapter(
    private val activity: AppCompatActivity,
    private val onClickListener: View.OnClickListener? = null):
    RecyclerView.Adapter<ErrorReportAdapter.ErrorReportViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private val dateFormat: DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)

    /**
     * Incremented on every data update, so that only the latest diff is applied.
     */
    private var dataGeneration: Int = 0

    var data: List<ErrorReport> = listOf()
        set(value) {
            val generation = ++dataGeneration
            val oldValue = field
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val diffResult = computeDataSetChanged(oldValue, value)
                withContext(Dispatchers.Main) {
                    if (generation != dataGeneration) return@withContext
                    field = value
                    diffResult.dispatchUpdatesTo(this@ErrorReportAdapter)
                }
            }
        }

    override fun getItemCount(): Int {
        return data.size
    }

    class ErrorReportViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView
        val dateTextView: TextView
        val messageTextView: TextView
        val itemLayout: ConstraintLayout

        init {
            nameTextView = itemView.findViewById(R.id.text_recyclerview_item_name)
            dateTextView = itemView.findViewById(R.id.text_recyclerview_item_date)
            messageTextView = itemView.findViewById(R.id.text_recyclerview_item_message)
            itemLayout = itemView.findViewById(R.id.error_report_list_recyclerview_item_error_report)
        }

        fun bindName(name: String) {
            nameTextView.text = name
        }

        fun bindDate(date: String) {
            dateTextView.text = date
        }

        fun bindMessage(message: String?) {
            messageTextView.text = message
            messageTextView.visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorReportViewHolder {
        val itemView: View = mInflater.inflate(
            R.layout.error_report_list_recyclerview_item_error_report, parent, false)

        onClickListener?.let { itemView.setOnClickListener(onClickListener) }

        return ErrorReportViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ErrorReportViewHolder, position: Int) {
        val current: ErrorReport = data[position]

        val type = activity.getString(
            if (current.fatal) R.string.error_report_type_crash else R.string.error_report_type_error)
        holder.bindName(activity.getString(
            R.string.error_report_list_item_title, type, current.shortExceptionClass))

        val date = dateFormat.format(Date(current.timestamp))
        holder.bindDate(if (current.screen.isNullOrBlank()) date else "$date · ${current.screen}")

        holder.bindMessage(current.message?.lineSequence()?.firstOrNull()?.trim())

        holder.itemLayout.tag = current.id
    }

    private fun computeDataSetChanged(
        oldValue: List<ErrorReport>,
        newValue: List<ErrorReport>): DiffUtil.DiffResult {

        return DiffUtil.calculateDiff(ErrorReportAdapterDiffUtilCallback(oldValue, newValue), true)
    }
}

