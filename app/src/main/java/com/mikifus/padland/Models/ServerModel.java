package com.mikifus.padland.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.mikifus.padland.PadContentProvider;

/**
 * Created by mikifus on 8/07/16.
 */
public class ServerModel extends SQLiteOpenHelper {

    public static final String TAG = "ServerModel";

    public static final String TABLE = "padland_servers";
//    public static final String COLUMN_ID = "_id";
//    public static final String COLUMN_COMMENT = "comment";

    public static final String _ID = "_id";
    public static final String NAME = "name"; // Name of the server
    public static final String URL = "url"; // Domain, with protocol please
    public static final String PADPREFIX = "padprefix"; // the full address including server and name
    public static final String JQUERY = "jquery"; // the full address including server and name
    public static final String POSITION = "position"; // Position inside a sortable data set
    public static final String ENABLED = "enabled"; // Position inside a sortable data set

    private static final String DATABASE_NAME = "commments.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String SERVERS_TABLE_CREATE_QUERY =
            "CREATE TABLE "+ TABLE + "( " +
            " "+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            " "+ NAME + " text not null," +
            " "+ URL+ " TEXT NOT NULL, " +
            " "+ PADPREFIX +" TEXT NOT NULL, " +
            " "+ POSITION+ " INTEGER NOT NULL DEFAULT 0, "+
            " "+ JQUERY+ " INTEGER NOT NULL DEFAULT 0, "+ // Actually boolean
            " "+ ENABLED+ " INTEGER NOT NULL DEFAULT 1 "+ // Actually boolean
            ");";

    SQLiteDatabase db;

    public ServerModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SERVERS_TABLE_CREATE_QUERY);
        this.db = database;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if( newVersion > 1 ) {
            Log.w(TAG,
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }


    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field Field name wich will be compared
     * @param comparation
     * @return
     */
    private Cursor _getServerDataFromDatabase(String field, String comparation ){
        String QUERY;
        Cursor c = null;
        String[] comparation_set = new String[]{ comparation };

        QUERY =
                "SELECT * " +
                        "FROM " + ServerModel.TABLE + " " +
                        "WHERE " + field + "=?" +
                        ") ";

        c = db.rawQuery(QUERY, comparation_set);
        return c;
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    public Cursor _getServerDataById( long pad_id ){
        return this._getServerDataFromDatabase( ServerModel._ID, String.valueOf( pad_id ) );
    }

    /**
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    public Cursor _getServerDataByUrl(String padUrl){
        return this._getServerDataFromDatabase( ServerModel.URL, padUrl );
    }
    /**
     * Saves a new server if server_id=0 or updates an existing one.
     *
     * @param pad_id
     * @param values
     * @return
     */
    public boolean saveServerData(long pad_id, ContentValues values) {
        if (pad_id > 0) {
            String[] where_value = {String.valueOf(pad_id)};
            int result = db.update(ServerModel.TABLE, values, PadContentProvider._ID + "=?", where_value);
            return (result > 0);
        } else {
            Log.d("INSERT", "Contents = " + values.toString());
            long result = db.insert(ServerModel.TABLE, null, values);
            return (result > 0);
        }
    }

    public int getServerCount() {
        String QUERY;
        Cursor c = null;
        String[] comparation_set = new String[]{};

        QUERY =
                "SELECT " + ServerModel._ID + " " +
                        "FROM " + ServerModel.TABLE + " " +
//                            "WHERE " + field + "=?" +
                        ") ";

        Cursor cursor = db.rawQuery(QUERY, comparation_set);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

}