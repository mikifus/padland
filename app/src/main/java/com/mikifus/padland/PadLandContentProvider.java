package com.mikifus.padland;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;

/**
 * The content provider allows to store and read database information for the app.
 *
 * @author mikifus
 */
public class PadLandContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.mikifus.padland.padlandcontentprovider";
    static final String TAG = "PadLandContentProvider";
    static final String AUTHORITY = "content://" + PROVIDER_NAME + "/padlist";
    public static final Uri CONTENT_URI = Uri.parse( AUTHORITY );

    static final String _ID = "_id";
    static final String NAME = "name";
    static final String SERVER = "server";
    static final String URL = "url";
    static final String LAST_USED_DATE = "last_used_date";
    static final String CREATE_DATE = "create_date";

    static final int PADLIST = 1;
    static final int PAD_ID = 2;

    private static HashMap<String, String> PROJECTION_MAP;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "padlist", PADLIST);
        uriMatcher.addURI(PROVIDER_NAME, "padlist/#", PAD_ID);
    }

    /**
     * Database specific constant declarations
     */
    private SQLiteDatabase db;
    static final String DATABASE_NAME = "padland";
    static final String TABLE_NAME = "padlist";
    static final int DATABASE_VERSION = 3;
    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + TABLE_NAME +
                    " ("+_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " "+NAME+" TEXT NOT NULL, " +
                    " "+SERVER+" TEXT NOT NULL, " +
                    " "+URL+" TEXT NOT NULL, " +
                    " "+LAST_USED_DATE+ " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                    " "+CREATE_DATE+ " INTEGER NOT NULL DEFAULT (strftime('%s','now')));";

    /**
     * Helper class that actually creates and manages
     * the provider's underlying data repository.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database. Existing contents will be deleted. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
            onCreate(db);
            //Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            //db.execSQL("ALTER TABLE " +  TABLE_NAME + " ADD COLUMN " + CREATE_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now'));");
        }
    }

    /**
     * onCreate override
     * @return
     */
    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    /**
     * Deletes a document from the db
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        Log.d("DELETE_QUERY", selection + " - " + selectionArgs.toString() );

        switch (uriMatcher.match(uri)){
            case PADLIST:
                Log.d("DELETE_PADLIST", selection + " - " + selectionArgs.toString() );
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            case PAD_ID:
                Log.d("DELETE_PAD_ID", selection + " - " + selectionArgs.toString() );
                String id = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME, _ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Updates documents' info
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        if (values.get(LAST_USED_DATE) == null) {
            long now = getNowDate();
            values.put(LAST_USED_DATE, now);
        }
        switch (uriMatcher.match(uri)){
            case PADLIST:
                count = db.update(TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case PAD_ID:
                count = db.update(TABLE_NAME, values, _ID +
                        " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" +
                                selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * It does nothing
     * @param uri
     * @return
     */
    @Override
    public String getType(Uri uri) {
/*        switch (uriMatcher.match(uri)){
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }*/
        return null;
    }

    /**
     * Insert into db
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        /**
         * Add a new record
         */
        long rowID = db.insert(	TABLE_NAME, "", values);
        /**
         * If record is added successfully
         */
        if (rowID > 0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to add a record into " + uri);
    }

    /**
     * Query to the the db
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case PADLIST:
                qb.setProjectionMap( PROJECTION_MAP );
                break;
            case PAD_ID:
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1) );
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }
        if (sortOrder == null || sortOrder.isEmpty()){
            /**
             * By default sort
             */
            sortOrder = LAST_USED_DATE + " DESC ";
        }
        Cursor c = qb.query( db, projection, selection, selectionArgs, null, null, sortOrder );
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri( getContext().getContentResolver(), uri );

        return c;
    }

    /**
     * Gets the current time in the format that the database uses.
     * As it is static, it can be used by other classes.
     * @return
     */
    public static long getNowDate() {
        return ((long) new Date().getTime()) / 1000;
    }
}
