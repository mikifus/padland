package com.mikifus.padland.Models

import android.content.Context
import android.database.Cursor
import android.text.format.DateFormat
import com.mikifus.padland.Database.PadModel.Pad
import java.util.Date

/**
 * Created by mikifus on 27/02/18.
 */
/**
 * The padData subclass is the summary of information the App needs
 * to deal with the documents. It has the info and returns it
 * in the right format.
 */
class Pad {
    var id: Long = 0
    var name: String? = null
    private var initLocalName: String? = null
    var server: String? = null
    var url: String? = null
    private var lastUsedDate: Long = 0
    private var createDate: Long = 0
    var accessCount: Long = 0

    constructor(pad: Pad) {
        id = pad.mId!!
        name = pad.mName
        initLocalName = pad.mLocalName
        server = pad.mServer
        url = pad.mUrl
        createDate = pad.mCreateDate.time
        accessCount = pad.mAccessCount
    }

    constructor(c: Cursor?) {
        if (c != null && c.count > 0) {
            id = c.getLong(0)
            name = c.getString(1)
            initLocalName = c.getString(2)
            server = c.getString(3)
            url = c.getString(4)
            lastUsedDate = c.getLong(5)
            createDate = c.getLong(6)
            accessCount = c.getLong(7)
        }
    }

    val localName: String?
        get() = if (initLocalName == null || initLocalName!!.isEmpty()) {
            name
        } else initLocalName
    val rawLocalName: String
        get() = if (initLocalName == null || initLocalName!!.isEmpty()) {
            ""
        } else initLocalName!!

    fun getLastUsedDate(context: Context): String {
        return lonToDate(lastUsedDate, context)
    }

    fun getCreateDate(context: Context): String {
        return lonToDate(createDate, context)
    }

    fun lonToDate(timeInMilliSeconds: Long, context: Context): String {
        val formatter = DateFormat.getDateFormat(context.applicationContext)
        val dateObj = Date(timeInMilliSeconds * 1000)
        return formatter.format(dateObj)
    }
}