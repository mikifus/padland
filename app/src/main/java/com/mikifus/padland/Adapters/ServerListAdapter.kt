package com.mikifus.padland.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Models.Server
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.R
import com.mikifus.padland.ServerListActivity

/**
 * Created by mikifus on 29/05/16.
 */
class ServerListAdapter(context: ServerListActivity?, //    private ServerListActivity mContext;
                        private val layout_resource: Int) : ArrayAdapter<Any?>(context!!, layout_resource) {
//    private val serverModel: ServerModel
    private var items: List<com.mikifus.padland.Database.ServerModel.Server>

    init {
        //        mContext = context;
//        serverModel = ServerModel(context)
//        items = serverModel.enabledServerList
        items = PadListDatabase.getInstance(context!!).serverDao().getAll().value?.let { return@let it } ?: emptyList()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the data item for this position
        var view = convertView
        val server = getItem(position)

        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(context).inflate(layout_resource, parent, false)
        }
        (view!!.findViewById<View>(R.id.name) as TextView).text = server.mName
        (view.findViewById<View>(R.id.url) as TextView).text = server.mUrl

        // Return the completed view to render on screen
        return view
    }

    override fun getCount(): Int {
//        return serverModel.serverCount
        return items.count()
    }

    override fun getItem(position: Int): com.mikifus.padland.Database.ServerModel.Server {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return items[position].mId
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
//        items = serverModel.enabledServerList
        items = PadListDatabase.getInstance(context!!).serverDao().getAll().value?.let { return@let it } ?: emptyList()
    }
}