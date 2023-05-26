package com.mikifus.padland.Models

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.mikifus.padland.PadContentProvider

/**
 * Created by mikifus on 27/02/18.
 */
class PadModel(context: Context?) : BaseModel(context) {
    private val contentResolver: ContentResolver

    init {
        contentResolver = context!!.contentResolver
    }

    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field
     * @param comparation
     * @return
     */
    private fun _getPadDataFromDatabase(field: String, comparation: String): Cursor? {
        val c: Cursor?
        val comparationSet = arrayOf(comparation)
        c = contentResolver.query(
                PadContentProvider.PADLIST_CONTENT_URI,
                PadContentProvider.padFieldsList,
                "$field = ?",
                comparationSet,  // AKA id
                null
        )
        return c
    }

    /**
     * Self explanatory name.
     * Just get all.
     * @return
     */
    private fun _getPadDataFromDatabase(): Cursor? {
        val c: Cursor? = contentResolver.query(
                PadContentProvider.PADLIST_CONTENT_URI,
                PadContentProvider.padFieldsList,
                null,
                null,  // AKA id
                null
        )
        return c
    }

    /**
     * Queries the database and returns all pads
     * @return
     */
    fun _getAllPadData(): ArrayList<Pad> {
        val cursor = this._getPadDataFromDatabase()
        val padDatas = ArrayList<Pad>()
        if (cursor == null) {
            return padDatas
        }
        if (cursor.count == 0) {
            cursor.close()
            return padDatas
        }
        var padData: Pad
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            // Goes to next by itself
            padData = Pad(cursor)
            padDatas.add(padData)
            cursor.moveToNext()
        }
        cursor.close()
        return padDatas
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    private fun _getPadDataById(pad_id: Long): Cursor? {
        return this._getPadDataFromDatabase(PadContentProvider._ID, pad_id.toString())
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    fun _getPadDataByUrl(padUrl: String): Cursor? {
        return this._getPadDataFromDatabase(URL, padUrl)
    }

    fun getPadById(id: Long): Pad {
        val c = _getPadDataById(id)
        c!!.moveToFirst()
        return Pad(c)
    }

    /**
     * Saves a new pad if pad_id=0 or updates an existing one.
     * @param pad_id
     * @param values
     * @return
     */
    fun savePad(pad_id: Long, values: ContentValues?): Boolean {
        return if (pad_id > 0) {
            val whereValue = arrayOf(pad_id.toString())
            val result = contentResolver.update(PadContentProvider.PADLIST_CONTENT_URI, values, PadContentProvider._ID + " = ?", whereValue)
            result > 0
        } else {
            Log.d("INSERT", "Contents = " + values.toString())
            val result = contentResolver.insert(PadContentProvider.PADLIST_CONTENT_URI, values)
            result != null
        }
    }

    companion object {
        const val TAG = "PadModel"
        const val _ID = "_id"
        const val LOCAL_NAME = "local_name" // Alias of the pad
        const val NAME = "name" // Name of the pad, actually it is the last part of the url
        const val URL = "url" // the full address including server and name
    }
}