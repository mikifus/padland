package com.mikifus.padland

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import com.mikifus.padland.Models.Pad
import java.util.LinkedList

/**
 * This activity shows pad info like the last time it was used or when
 * it was created. It can be upgraded to show useful info.
 * Its menu as well allows to delete.
 */
class PadInfoActivity : PadLandDataActivity() {
    private var padId: Long = 0

    /**
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pad_info)
        padId = _getPadId()
        if (padId <= 0) {
            Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show()
            return
        }
        val padData = _getPad(padId)

        // Action bar title
        supportActionBar?.title = padData.localName
        val adapter = _doInfoList(padData)
        val list = findViewById<View>(R.id.listView) as ListView
        list.adapter = adapter
    }

    /**
     * Takes the pad data and prepares a list with information,
     * then returns the adapter for the view.
     * @param padData
     * @return
     */
    private fun _doInfoList(padData: Pad?): SeparatedListAdapter {
        val datalist: MutableList<Map<String, *>> = LinkedList()
        datalist.add(_doListItem(padData!!.url, getString(R.string.padinfo_pad_url)))
        datalist.add(_doListItem(padData.getCreateDate(this), getString(R.string.padinfo_createdate)))
        datalist.add(_doListItem(padData.getLastUsedDate(this), getString(R.string.padinfo_lastuseddate)))
        datalist.add(_doListItem(padData.accessCount.toString(), getString(R.string.padinfo_times_accessed)))
        val adapter = SeparatedListAdapter(this)

        // create our list and custom adapter
        adapter.addSection(getString(R.string.padinfo_pad_info),
                SimpleAdapter(this, datalist, R.layout.list_complex, arrayOf("title", "caption"), intArrayOf(R.id.list_complex_title, R.id.list_complex_caption))
        )
        // I leave this here to show an example of how to show another list
        /*adapter.addSection("Options",
                new ArrayAdapter<String>(this, R.layout.list_item, new String[] { "Share", "Delete" })
        );*/return adapter
    }

    /**
     * Makes a list item for the previous method.
     * @param title
     * @param caption
     * @return
     */
    private fun _doListItem(title: String?, caption: String): Map<String, *> {
        val item: MutableMap<String, String?> = HashMap()
        item["title"] = title
        item["caption"] = caption
        return item
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pad_info, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Intent intent;

//        Log.d(TAG, "OptionsItemSelected pad_id: " + pad_id);
        val padList = ArrayList<String?>()
        padList.add(padId.toString())
        when (item.itemId) {
            R.id.menuitem_share -> {
                Log.d("MENU_SHARE", padId.toString())
                menuShare(padList)
            }

            R.id.menuitem_copy -> {
                Log.d("MENU_SHARE", padId.toString())
                menuCopy(padList)
            }

            R.id.menuitem_delete -> {
                Log.d("MENU_DELETE", padId.toString())
                askDelete(padList)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Takes you to the padView activity
     */
    fun onViewButtonClick() {
        val padViewIntent = Intent(this@PadInfoActivity, PadViewActivity::class.java)
        padViewIntent.putExtra("pad_id", padId)
        startActivity(padViewIntent)
    }

    companion object {
        private const val TAG = "PadInfoActivity"
    }
}