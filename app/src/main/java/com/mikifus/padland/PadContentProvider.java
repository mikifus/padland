package com.mikifus.padland;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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
public class PadContentProvider extends ContentProvider {

    static final String PROVIDER_NAME = "com.mikifus.padland.padlandcontentprovider";
    static final String TAG = "PadLandContentProvider";

    public static final String AUTHORITY = "content://" + PROVIDER_NAME + "/";
    public static final Uri PADLIST_CONTENT_URI = Uri.parse( AUTHORITY + "padlist" );
    public static final Uri PADGROUPS_CONTENT_URI = Uri.parse( AUTHORITY + "padgroups" );

    /**
     * Database specific constant declarations
     */
    protected SQLiteDatabase db;
    static final int DATABASE_VERSION = 7;

    public static final String _ID = "_id";
    public static final String NAME = "name"; // Name of the pad, actually it is the last part of the url
    public static final String SERVER = "server"; // server, might contain the suffix
    public static final String URL = "url"; // the full address including server and name
    public static final String LAST_USED_DATE = "last_used_date"; // Date the pad was accessed last time
    public static final String CREATE_DATE = "create_date"; // Date when the pad was added into the app
    public static final String ACCESS_COUNT = "access_count"; // How many times the document has been accessed in the app

    public static final String POSITION = "position"; // Position inside a sortable data set

    public static final String _ID_GROUP = "_id_group";
    public static final String _ID_PAD = "_id_pad";

    static final int PADLIST = 1;
    static final int PAD_ID = 2;
    static final int PADGROUP_LIST = 3;
    static final int PADGROUP_ID = 4;
    static final int PADLIST_PADGROUP_ID = 5;

    private static HashMap<String, String> PROJECTION_MAP;

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "padlist", PADLIST);
        uriMatcher.addURI(PROVIDER_NAME, "padlist/#", PAD_ID);
        uriMatcher.addURI(PROVIDER_NAME, "padgroups", PADGROUP_LIST);
        uriMatcher.addURI(PROVIDER_NAME, "padgroups/#", PADGROUP_ID);
        uriMatcher.addURI(PROVIDER_NAME, "padlist_padgroup_id/#", PADLIST_PADGROUP_ID);
    }

    static final String PAD_TABLE_NAME = "padlist";
    static final String PAD_TABLE_CREATE_QUERY =
            " CREATE TABLE " + PAD_TABLE_NAME +
                    " ("+ _ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " "+ NAME+" TEXT NOT NULL, " +
                    " "+ SERVER+" TEXT NOT NULL, " +
                    " "+ URL+" TEXT NOT NULL, " +
                    " "+ LAST_USED_DATE+ " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                    " "+ CREATE_DATE+ " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                    " "+ ACCESS_COUNT+ " INTEGER NOT NULL DEFAULT 0 "+
                    ");";

    static final String PADGROUP_TABLE_NAME = "padgroups";
    static final String PADGROUP_TABLE_CREATE_QUERY =
            " CREATE TABLE " + PADGROUP_TABLE_NAME +
                    " ("+ _ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " "+ NAME+" TEXT NOT NULL, " +
                    " "+ POSITION + " INTEGER DEFAULT 0, " +
                    " "+ LAST_USED_DATE+ " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                    " "+ CREATE_DATE+ " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                    " "+ ACCESS_COUNT+ " INTEGER NOT NULL DEFAULT 0 "+
                    ");";
    static final String RELATION_TABLE_NAME = "padlist_padgroups";
    static final String RELATION_TABLE_CREATE_QUERY =
            " CREATE TABLE " + RELATION_TABLE_NAME +
                    " ("+ _ID_GROUP +" INTEGER NOT NULL, " +
                    " " + _ID_PAD +" INTEGER NOT NULL " +
                    ");";

    /**
     * Gets the current time in the format that the database uses.
     * As it is static, it can be used by other classes.
     * @return
     */
    public static long getNowDate() {
        return ((long) new Date().getTime()) / 1000;
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
        Log.d("DELETE_QUERY", selection + " - " + selectionArgs.toString());

        String id;

        switch (uriMatcher.match(uri)){
            case PADLIST:
                Log.d("DELETE_PADLIST", selection + " - " + selectionArgs.toString());
                db.delete(RELATION_TABLE_NAME, _ID_PAD + " =?", selectionArgs);
                count = db.delete(PAD_TABLE_NAME, selection, selectionArgs);
                break;
            case PAD_ID:
                Log.d("DELETE_PAD_ID", selection + " - " + selectionArgs.toString() );
                id = uri.getPathSegments().get(1);
                db.delete(RELATION_TABLE_NAME, _ID_PAD + " = " + id, new String[0]);
                count = db.delete(PAD_TABLE_NAME, _ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case PADGROUP_LIST:
                Log.d(TAG, "delete_padgroup_list: " + selection + " - " + selectionArgs.toString());
                db.delete(RELATION_TABLE_NAME, _ID_GROUP + " =?", selectionArgs);
                count = db.delete(PADGROUP_TABLE_NAME, selection, selectionArgs);
                break;
            case PADGROUP_ID:
                Log.d(TAG, "delete_padgroup_id: " + selection + " - " + selectionArgs.toString());
                id = uri.getPathSegments().get(1);
                db.delete(RELATION_TABLE_NAME, _ID_GROUP + " = " + id, new String[0]);
                count = db.delete(PADGROUP_TABLE_NAME, _ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * Updates documents' info.
     * Returns an int parameter indicating the amount of rows modified.
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d("PAD_UPDATE", uri.toString() );
        int count = 0;
        String id;
        switch ( uriMatcher.match(uri) ){
            case PADLIST:
                count = db.update(PAD_TABLE_NAME, values, selection, selectionArgs);
                break;
            case PAD_ID:
                id = uri.getPathSegments().get(1);
                count = db.update(PAD_TABLE_NAME,
                        values,
                        _ID +  " = " + id + ( !TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "" ),
                        selectionArgs);
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
        long rowID;
        Uri _uri = null;
        switch (uriMatcher.match(uri)) {
            case PADLIST:
                rowID = db.insert(PAD_TABLE_NAME, "", values);
                ContentUris.withAppendedId(PADLIST_CONTENT_URI, rowID);
                break;
            case PADGROUP_LIST:
                rowID = db.insert(PADGROUP_TABLE_NAME, "", values);
                ContentUris.withAppendedId(PADGROUPS_CONTENT_URI, rowID);
                break;
            default:
                throw new IllegalArgumentException( "Unknown URI " + uri );
        }
        /**
         * If record is added successfully
         */
        if (rowID > 0)
        {
//            getContext().getContentResolver().notifyChange(_uri, null);
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

        switch (uriMatcher.match(uri)) {
            case PADLIST:
                qb.setTables(PAD_TABLE_NAME);
                qb.setProjectionMap( PROJECTION_MAP );
                break;
            case PAD_ID:
                qb.setTables(PAD_TABLE_NAME);
                qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
                break;
            case PADGROUP_LIST:
                qb.setTables(PADGROUP_TABLE_NAME);
                qb.setProjectionMap( PROJECTION_MAP );
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
     * onCreate override
     * @return
     */
    @Override
    public boolean onCreate() {
        Context context = getContext();
        PadlandDbHelper dbHelper = new PadlandDbHelper(context);

        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db != null);
    }

    public static String[] getPadFieldsList() {
        return new String[] {
                _ID,
                NAME,
                SERVER,
                URL,
                LAST_USED_DATE,
                CREATE_DATE,
                ACCESS_COUNT
        };
    }

    public static String[] getPadgroupFieldsList() {
        return new String[] {
                _ID,
                NAME,
                LAST_USED_DATE,
                CREATE_DATE,
                ACCESS_COUNT
        };
    }
}
