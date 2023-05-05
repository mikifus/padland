package com.mikifus.padland

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.mikifus.padland.Models.PadGroupModel
import com.mikifus.padland.Models.PadModel
import java.util.Arrays
import java.util.Date

/**
 * The content provider allows to store and read database information for the app.
 *
 * TODO: A demo app that connects here
 *
 * @author mikifus
 */
class PadContentProvider : ContentProvider() {
    /**
     * Database specific constant declarations
     */
    protected var db: SQLiteDatabase? = null

    /**
     * Deletes a document from the db
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        Log.d("DELETE_QUERY", selection + " - " + selectionArgs.toString())
        val id: String
        val query: ArrayList<String?>
        when (uriMatcher!!.match(uri)) {
            PADLIST -> {
                Log.d("DELETE_PADLIST", selection + " - " + selectionArgs.toString())
                db!!.delete(RELATION_TABLE_NAME, _ID_PAD + " =?", selectionArgs)
                count = db!!.delete(PAD_TABLE_NAME, selection, selectionArgs)
            }

            PAD_ID -> {
                Log.d("DELETE_PAD_ID", selection + " - " + Arrays.toString(selectionArgs))
                id = uri.pathSegments[1]
                query = ArrayList(Arrays.asList(*selectionArgs))
                query.add(0, id)
                db!!.delete(RELATION_TABLE_NAME, _ID_PAD + " = ?", arrayOfNulls(0))
                count = db!!.delete(PAD_TABLE_NAME, _ID + " = ?" + if (!TextUtils.isEmpty(selection)) " AND (?)" else "", query.toTypedArray())
            }

            PADGROUP_LIST -> {
                Log.d(TAG, "delete_padgroup_list: " + selection + " - " + Arrays.toString(selectionArgs))
                db!!.delete(RELATION_TABLE_NAME, _ID_GROUP + " =?", selectionArgs)
                count = db!!.delete(PADGROUP_TABLE_NAME, selection, selectionArgs)
            }

            PADGROUP_ID -> {
                Log.d(TAG, "delete_padgroup_id: " + selection + " - " + Arrays.toString(selectionArgs))
                id = uri.pathSegments[1]
                query = ArrayList(Arrays.asList(*selectionArgs))
                query.add(0, id)
                db!!.delete(RELATION_TABLE_NAME, _ID_GROUP + " = ?", arrayOfNulls(0))
                count = db!!.delete(PADGROUP_TABLE_NAME, _ID + " = ?" + if (!TextUtils.isEmpty(selection)) " AND (?)" else "", query.toTypedArray())
            }

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
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
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        Log.d("PAD_UPDATE", uri.toString())
        val count: Int
        val id: String
        val query: ArrayList<String?>
        when (uriMatcher!!.match(uri)) {
            PADLIST -> count = db!!.update(PAD_TABLE_NAME, values, selection, selectionArgs)
            PAD_ID -> {
                id = uri.pathSegments[1]
                query = ArrayList(Arrays.asList(*selectionArgs))
                query.add(0, id)
                count = db!!.update(PAD_TABLE_NAME,
                        values,
                        _ID + " = ?" + if (!TextUtils.isEmpty(selection)) " AND (?)" else "",
                        query.toTypedArray())
            }

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    /**
     * It does nothing
     * @param uri
     * @return
     */
    override fun getType(uri: Uri): String? {
        /*        switch (uriMatcher.match(uri)){
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }*/
        return null
    }

    /**
     * Insert into db
     * @param uri
     * @param values
     * @return
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        /**
         * Add a new record
         */
        val rowID: Long
        val _uri: Uri? = null
        when (uriMatcher!!.match(uri)) {
            PADLIST -> {
                rowID = db!!.insert(PAD_TABLE_NAME, "", values)
                ContentUris.withAppendedId(PADLIST_CONTENT_URI, rowID)
            }

            PADGROUP_LIST -> {
                rowID = db!!.insert(PADGROUP_TABLE_NAME, "", values)
                ContentUris.withAppendedId(PADGROUPS_CONTENT_URI, rowID)
            }

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        /**
         * If record is added successfully
         */
        if (rowID > 0) {
//            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri
        }
        throw SQLException("Failed to add a record into $uri")
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
    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        when (uriMatcher!!.match(uri)) {
            PADLIST -> {
                qb.tables = PAD_TABLE_NAME
                qb.projectionMap = PROJECTION_MAP
            }

            PAD_ID -> {
                qb.tables = PAD_TABLE_NAME
                qb.appendWhere(_ID + "=" + DatabaseUtils.sqlEscapeString(uri.pathSegments[1]))
            }

            PADGROUP_LIST -> {
                qb.tables = PADGROUP_TABLE_NAME
                qb.projectionMap = PROJECTION_MAP
            }

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            /**
             * By default sort
             */
            sortOrder = LAST_USED_DATE + " DESC "
        }
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        /**
         * register to watch a content URI for changes
         */
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    /**
     * onCreate override
     * @return
     */
    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = PadlandDbHelper(context)
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.writableDatabase
        return db != null
    }

    companion object {
        const val PROVIDER_NAME = "com.mikifus.padland.padlandcontentprovider"
        const val TAG = "PadLandContentProvider"
        const val AUTHORITY = "content://" + PROVIDER_NAME + "/"
        val PADLIST_CONTENT_URI = Uri.parse(AUTHORITY + "padlist")
        val PADGROUPS_CONTENT_URI = Uri.parse(AUTHORITY + "padgroups")
        const val DATABASE_VERSION = 8
        const val _ID = "_id"
        const val LOCAL_NAME = "local_name" // Alias of the pad
        const val SERVER = "server" // server, might contain the suffix
        const val LAST_USED_DATE = "last_used_date" // Date the pad was accessed last time
        const val CREATE_DATE = "create_date" // Date when the pad was added into the app
        const val ACCESS_COUNT = "access_count" // How many times the document has been accessed in the app
        const val _ID_GROUP = "_id_group"
        const val _ID_PAD = "_id_pad"
        const val PADLIST = 1
        const val PAD_ID = 2
        const val PADGROUP_LIST = 3
        const val PADGROUP_ID = 4
        const val PADLIST_PADGROUP_ID = 5
        private val PROJECTION_MAP: HashMap<String, String>? = null
        val uriMatcher: UriMatcher? = null

        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher.addURI(PROVIDER_NAME, "padlist", PADLIST)
            uriMatcher.addURI(PROVIDER_NAME, "padlist/#", PAD_ID)
            uriMatcher.addURI(PROVIDER_NAME, "padgroups", PADGROUP_LIST)
            uriMatcher.addURI(PROVIDER_NAME, "padgroups/#", PADGROUP_ID)
            uriMatcher.addURI(PROVIDER_NAME, "padlist_padgroup_id/#", PADLIST_PADGROUP_ID)
        }

        const val PAD_TABLE_NAME = "padlist"
        val PAD_TABLE_CREATE_QUERY = " CREATE TABLE " + PAD_TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " " + PadModel.Companion.NAME + " TEXT NOT NULL, " +
                " " + LOCAL_NAME + " TEXT, " +
                " " + SERVER + " TEXT NOT NULL, " +
                " " + PadModel.Companion.URL + " TEXT NOT NULL, " +
                " " + LAST_USED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                " " + CREATE_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                " " + ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0 " +
                ");"
        const val PADGROUP_TABLE_NAME = "padgroups"
        val PADGROUP_TABLE_CREATE_QUERY = " CREATE TABLE " + PADGROUP_TABLE_NAME +
                " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " " + PadModel.Companion.NAME + " TEXT NOT NULL, " +
                " " + PadGroupModel.Companion.POSITION + " INTEGER DEFAULT 0, " +
                " " + LAST_USED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
                " " + CREATE_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
                " " + ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0 " +
                ");"
        const val RELATION_TABLE_NAME = "padlist_padgroups"
        const val RELATION_TABLE_CREATE_QUERY = " CREATE TABLE " + RELATION_TABLE_NAME +
                " (" + _ID_GROUP + " INTEGER NOT NULL, " +
                " " + _ID_PAD + " INTEGER NOT NULL " +
                ");"

        /**
         * Gets the current time in the format that the database uses.
         * As it is static, it can be used by other classes.
         * @return
         */
        val nowDate: Long
            get() = Date().time / 1000
        val padFieldsList: Array<String>
            get() = arrayOf<String>(
                    _ID,
                    PadModel.Companion.NAME,
                    LOCAL_NAME,
                    SERVER,
                    PadModel.Companion.URL,
                    LAST_USED_DATE,
                    CREATE_DATE,
                    ACCESS_COUNT
            )
        val padgroupFieldsList: Array<String>
            get() = arrayOf<String>(
                    _ID,
                    PadModel.Companion.NAME,
                    LAST_USED_DATE,
                    CREATE_DATE,
                    ACCESS_COUNT
            )
    }
}