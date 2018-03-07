package com.mikifus.padland.Models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mikifus.padland.PadContentProvider;

import java.util.ArrayList;

/**
 * Created by mikifus on 27/02/18.
 */

public class PadModel extends BaseModel {
    public static final String TAG = "PadModel";
    public static final String _ID = "_id";
    public static final String LOCAL_NAME = "local_name"; // Alias of the pad
    public static final String NAME = "name"; // Name of the pad, actually it is the last part of the url
    public static final String URL = "url"; // the full address including server and name

    private ContentResolver contentResolver;

    public PadModel(Context context) {
        super(context);
        contentResolver = context.getContentResolver();
    }
    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field
     * @param comparation
     * @return
     */
    private Cursor _getPadDataFromDatabase(String field, String comparation ){
        Cursor c;
        String[] comparation_set = { comparation };

        c = contentResolver.query(
                PadContentProvider.PADLIST_CONTENT_URI,
                PadContentProvider.getPadFieldsList(),
                field + " = ?",
                comparation_set, // AKA id
                null
        );
        return c;
    }
    /**
     * Self explanatory name.
     * Just get all.
     * @return
     */
    private Cursor _getPadDataFromDatabase(){
        Cursor c;
        c = contentResolver.query(
                PadContentProvider.PADLIST_CONTENT_URI,
                PadContentProvider.getPadFieldsList(),
                null,
                null, // AKA id
                null
        );
        return c;
    }

    /**
     * Queries the database and returns all pads
     * @return
     */
    public ArrayList<Pad> _getAllPadData(){
        Cursor cursor = this._getPadDataFromDatabase();
        ArrayList<Pad> PadDatas = new ArrayList<>();

        if (cursor == null ) {
            return PadDatas;
        }
        if( cursor.getCount() == 0 ) {
            cursor.close();
            return PadDatas;
        }

        Pad PadData;
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            // Goes to next by itself
            PadData = new Pad(cursor);
            PadDatas.add(PadData);
            cursor.moveToNext();
        }
        cursor.close();

        return PadDatas;
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    public Cursor _getPadDataById( long pad_id ){
        return this._getPadDataFromDatabase( PadContentProvider._ID, String.valueOf( pad_id ) );
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    public Cursor _getPadDataByUrl(String padUrl){
        return this._getPadDataFromDatabase(URL, padUrl);
    }

    public long getNowDate() {
        return PadContentProvider.getNowDate();
    }

    public Pad getPadById(long id) {
        Cursor c = _getPadDataById(id);
        c.moveToFirst();
        return new Pad(c);
    }

    /**
     * Saves a new pad if pad_id=0 or updates an existing one.
     * @param pad_id
     * @param values
     * @return
     */
    public boolean savePad( long pad_id, ContentValues values ){
        if( pad_id > 0 ) {
            String[] where_value = { String.valueOf(pad_id) };
            int result = contentResolver.update(PadContentProvider.PADLIST_CONTENT_URI, values, PadContentProvider._ID + " = ?", where_value);
            return (result > 0);
        }
        else {
            Log.d("INSERT", "Contents = " + values.toString());
            Uri result = contentResolver.insert(PadContentProvider.PADLIST_CONTENT_URI, values);
            return ( result != null );
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
}
