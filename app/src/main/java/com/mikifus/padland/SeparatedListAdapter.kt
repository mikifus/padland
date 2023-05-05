package com.mikifus.padland

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.BaseAdapter

/**
 * @author Jeff Sharkey http://jsharkey.org/blog/2008/08/18/separating-lists-with-headers-in-android-09/
 */
class SeparatedListAdapter(context: Context?) : BaseAdapter() {
    val sections: MutableMap<String, Adapter> = LinkedHashMap()
    val headers: ArrayAdapter<String>

    init {
        headers = ArrayAdapter(context!!, R.layout.list_header)
    }

    fun addSection(section: String, adapter: Adapter) {
        headers.add(section)
        sections[section] = adapter
    }

    override fun getItem(position: Int): Any {
        var position = position
        for (section in sections.keys) {
            val adapter = sections[section]
            val size = adapter!!.count + 1

            // check if position inside this section
            if (position == 0) return section
            if (position < size) return adapter.getItem(position - 1)

            // otherwise jump into next section
            position -= size
        }
        return null
    }

    override fun getCount(): Int {
        // total together all sections, plus one for each section header
        var total = 0
        for (adapter in sections.values) total += adapter.count + 1
        return total
    }

    override fun getViewTypeCount(): Int {
        // assume that headers count as one, then total all sections
        var total = 1
        for (adapter in sections.values) total += adapter.viewTypeCount
        return total
    }

    override fun getItemViewType(position: Int): Int {
        var position = position
        var type = 1
        for (section in sections.keys) {
            val adapter = sections[section]
            val size = adapter!!.count + 1

            // check if position inside this section
            if (position == 0) return TYPE_SECTION_HEADER
            if (position < size) return type + adapter.getItemViewType(position - 1)

            // otherwise jump into next section
            position -= size
            type += adapter.viewTypeCount
        }
        return -1
    }

    fun areAllItemsSelectable(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return getItemViewType(position) != TYPE_SECTION_HEADER
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var position = position
        var sectionnum = 0
        for (section in sections.keys) {
            val adapter = sections[section]
            val size = adapter!!.count + 1

            // check if position inside this section
            if (position == 0) return headers.getView(sectionnum, convertView, parent)
            if (position < size) return adapter.getView(position - 1, convertView, parent)

            // otherwise jump into next section
            position -= size
            sectionnum++
        }
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    companion object {
        const val TYPE_SECTION_HEADER = 0
    }
}