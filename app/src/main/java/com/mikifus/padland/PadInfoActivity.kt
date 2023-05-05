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
    var pad_id: Long = 0

    /**
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pad_info)
        pad_id = _getPadId()
        if (pad_id <= 0) {
            Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show()
            return
        }
        val pad_data = _getPad(pad_id)

        // Action bar title
        supportActionBar.setTitle(pad_data.localName)
        val adapter = _doInfoList(pad_data)
        val list = findViewById<View>(R.id.listView) as ListView
        list.adapter = adapter
    }

    /**
     * Takes the pad data and prepares a list with information,
     * then returns the adapter for the view.
     * @param pad_data
     * @return
     */
    private fun _doInfoList(pad_data: Pad?): SeparatedListAdapter {
        val datalist: MutableList<Map<String, *>> = LinkedList()
        datalist.add(_doListItem(pad_data.getUrl(), getString(R.string.padinfo_pad_url)))
        datalist.add(_doListItem(pad_data!!.getCreateDate(this), getString(R.string.padinfo_createdate)))
        datalist.add(_doListItem(pad_data.getLastUsedDate(this), getString(R.string.padinfo_lastuseddate)))
        datalist.add(_doListItem(pad_data.accessCount.toString(), getString(R.string.padinfo_times_accessed)))
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
        val pad_list = ArrayList<String?>()
        pad_list.add(pad_id.toString())
        when (item.itemId) {
            R.id.menuitem_share -> {
                Log.d("MENU_SHARE", pad_id.toString())
                menu_share(pad_list)
            }

            R.id.menuitem_copy -> {
                Log.d("MENU_SHARE", pad_id.toString())
                menu_copy(pad_list)
            }

            R.id.menuitem_delete -> {
                Log.d("MENU_DELETE", pad_id.toString())
                AskDelete(pad_list)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * Takes you to the padView activity
     * @param w
     */
    fun onViewButtonClick(w: View?) {
        val padViewIntent = Intent(this@PadInfoActivity, PadViewActivity::class.java)
        padViewIntent.putExtra("pad_id", pad_id)
        startActivity(padViewIntent)
    }

    companion object {
        private const val TAG = "PadInfoActivity"
    }
}