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
        val comparation_set = arrayOf(comparation)
        c = contentResolver.query(
                PadContentProvider.Companion.PADLIST_CONTENT_URI,
                PadContentProvider.Companion.getPadFieldsList(),
                "$field = ?",
                comparation_set,  // AKA id
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
        val c: Cursor?
        c = contentResolver.query(
                PadContentProvider.Companion.PADLIST_CONTENT_URI,
                PadContentProvider.Companion.getPadFieldsList(),
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
        val PadDatas = ArrayList<Pad>()
        if (cursor == null) {
            return PadDatas
        }
        if (cursor.count == 0) {
            cursor.close()
            return PadDatas
        }
        var PadData: Pad
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            // Goes to next by itself
            PadData = Pad(cursor)
            PadDatas.add(PadData)
            cursor.moveToNext()
        }
        cursor.close()
        return PadDatas
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    fun _getPadDataById(pad_id: Long): Cursor? {
        return this._getPadDataFromDatabase(PadContentProvider.Companion._ID, pad_id.toString())
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    fun _getPadDataByUrl(padUrl: String): Cursor? {
        return this._getPadDataFromDatabase(URL, padUrl)
    }

    val nowDate: Long
        get() = PadContentProvider.Companion.getNowDate()

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
            val where_value = arrayOf(pad_id.toString())
            val result = contentResolver.update(PadContentProvider.Companion.PADLIST_CONTENT_URI, values, PadContentProvider.Companion._ID + " = ?", where_value)
            result > 0
        } else {
            Log.d("INSERT", "Contents = " + values.toString())
            val result = contentResolver.insert(PadContentProvider.Companion.PADLIST_CONTENT_URI, values)
            result != null
        }
    }

    /**
     * Gets current pad data and saves the modified values (LAST_USED_DATE and ACCESS_COUNT).
     * I tried to optimize it in such way that there's no need to use _getPadData, but it didn't work.
     * @param pad_id
     * @return
     */
    //    public void accessUpdate( long pad_id ){
    //        if( pad_id > 0 ) {
    //            Pad data = _getPadData( pad_id );
    //            ContentValues values = new ContentValues();
    //            values.put( PadContentProvider.LAST_USED_DATE, getNowDate() );
    //            values.put( PadContentProvider.ACCESS_COUNT, (data.getAccessCount() + 1));
    //            String[] where_value = { String.valueOf(pad_id) };
    //            contentResolver.update(PadContentProvider.PADLIST_CONTENT_URI, values, PadContentProvider._ID + "=?", where_value);
    //        }
    //    }
    companion object {
        const val TAG = "PadModel"
        const val _ID = "_id"
        const val LOCAL_NAME = "local_name" // Alias of the pad
        const val NAME = "name" // Name of the pad, actually it is the last part of the url
        const val URL = "url" // the full address including server and name
    }
}