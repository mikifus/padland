package com.mikifus.padland.Activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikifus.padland.Adapters.ServerAdapter
import com.mikifus.padland.Adapters.ServerSelectionTracker.IMakesServerSelectionTracker
import com.mikifus.padland.Adapters.ServerSelectionTracker.MakesServerSelectionTracker
import com.mikifus.padland.Database.ServerModel.ServerViewModel
import com.mikifus.padland.Dialogs.Managers.IManagesEditServerDialog
import com.mikifus.padland.Dialogs.Managers.IManagesNewServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesEditServerDialog
import com.mikifus.padland.Dialogs.Managers.ManagesNewServerDialog
import com.mikifus.padland.R

class ServerListActivity: AppCompatActivity(),
    IMakesServerSelectionTracker by MakesServerSelectionTracker(),
    IManagesNewServerDialog by ManagesNewServerDialog(),
    IManagesEditServerDialog by ManagesEditServerDialog() {

    override var serverViewModel: ServerViewModel? = null
    private var serverAdapter: ServerAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var mEmptyLayout: LinearLayoutCompat? = null
    private var mNewServerButton: FloatingActionButton? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_list)

        serverViewModel = ViewModelProvider(this)[ServerViewModel::class.java]
        serverAdapter = ServerAdapter(this, getOnItemClickListener())

        mEmptyLayout = findViewById(android.R.id.empty)
        recyclerView = findViewById(R.id.recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = serverAdapter

        mNewServerButton = findViewById(R.id.button_new_server)

        initListView()
        initEvents()
    }

    /*
    This method shall be used to initialize the list view using observer,
    here onChanged shall be triggered realtime as the data changes
     */
    private fun initListView() {
        initSelectionTrackers()

        serverViewModel!!.getAll.observe(this) { serverList ->
            serverAdapter!!.data = serverList
            serverAdapter!!.notifyDataSetChanged()
            onAdapterDataSetChanged()
        }
    }

    private fun initSelectionTrackers() {
        serverAdapter!!.tracker = makeServerSelectionTracker(this, recyclerView!!, serverAdapter!!)
    }

    private fun initEvents() {
        mNewServerButton?.setOnClickListener {
            showNewServerDialog(this)
        }
    }

    private fun getOnItemClickListener(): View.OnClickListener {
        return View.OnClickListener{ view: View ->
            showEditServerDialog(this, view.tag as Long)
        }
    }

    private fun onAdapterDataSetChanged() {
        if(mEmptyLayout != null) {
            mEmptyLayout!!.visibility = if (serverAdapter?.data?.size!! > 0) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}