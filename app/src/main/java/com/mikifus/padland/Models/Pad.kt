package com.mikifus.padland.Models

import android.content.Context
import android.database.Cursor
import android.text.format.DateFormat
import java.util.Date

/**
 * Created by mikifus on 27/02/18.
 */
/**
 * The padData subclass is the summary of information the App needs
 * to deal with the documents. It has the info and returns it
 * in the right format.
 */
class Pad(c: Cursor?) {
    var id: Long = 0
    var name: String? = null
    private var local_name: String? = null
    var server: String? = null
    var url: String? = null
    private var last_used_date: Long = 0
    private var create_date: Long = 0
    var accessCount: Long = 0

    init {
        if (c != null && c.count > 0) {
            id = c.getLong(0)
            name = c.getString(1)
            local_name = c.getString(2)
            server = c.getString(3)
            url = c.getString(4)
            last_used_date = c.getLong(5)
            create_date = c.getLong(6)
            accessCount = c.getLong(7)
        }
    }

    val localName: String?
        get() = if (local_name == null || local_name.isEmpty()) {
            name
        } else local_name
    val rawLocalName: String
        get() = if (local_name == null || local_name.isEmpty()) {
            ""
        } else local_name

    fun getLastUsedDate(context: Context): String {
        return lon_to_date(last_used_date, context)
    }

    fun getCreateDate(context: Context): String {
        return lon_to_date(create_date, context)
    }

    fun lon_to_date(TimeinMilliSeccond: Long, context: Context): String {
        val formatter = DateFormat.getDateFormat(context.applicationContext)
        val dateObj = Date(TimeinMilliSeccond * 1000)
        return formatter.format(dateObj)
    }
}