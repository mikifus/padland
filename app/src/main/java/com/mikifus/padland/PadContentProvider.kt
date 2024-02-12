package com.mikifus.padland

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupDao
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadDao
import kotlinx.coroutines.runBlocking
import java.util.Arrays
import java.util.Date

/**
 * The content provider allows to store and read database information for the app.
 *
 * TODO: A demo app that connects here
 *
 * @author mikifus
 */
open class PadContentProvider : ContentProvider() {
    /**
     * Database specific constant declarations
     */
//    private var db: SQLiteDatabase? = null

//    private fun getCursorFromLiveData(liveData: LiveData<List<Pad>>): Cursor {
//        val cursor = MatrixCursor(padFieldsList)
//        if(liveData.value == null || liveData.value!!.isEmpty()) {
//            return cursor
//        }
////        val columnNames = liveData.value!![0].javaClass.declaredFields.map { it.name }.toTypedArray()
////        val cursor = MatrixCursor(columnNames)
//
//        liveData.value?.forEach { item -> cursor.addRow(item.javaClass.declaredFields.map{it as Object}) }
//
//        return cursor
//    }
//    private fun getCursorFromLiveDataGroup(liveData: LiveData<List<PadGroup>>): Cursor {
//        val cursor = MatrixCursor(padFieldsList)
//        if(liveData.value == null || liveData.value!!.isEmpty()) {
//            return cursor
//        }
//
//        liveData.value?.forEach { item -> cursor.addRow(item.javaClass.declaredFields.map{it as Object}) }
//
//        return cursor
//    }

    /**
     * Deletes a document from the db
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val count: Int
        Log.d("DELETE_QUERY", selection + " - " + selectionArgs.toString())
        val id: String
        val query: ArrayList<String?>
        val context = context ?: return 0
        when (uriMatcher!!.match(uri)) {
//            PADLIST -> {
//                Log.d("DELETE_PADLIST", selection + " - " + selectionArgs.toString())
//                db!!.delete(RELATION_TABLE_NAME, "$_ID_PAD =?", selectionArgs)
//                count = db!!.delete(PAD_TABLE_NAME, selection, selectionArgs)
//            }
            PADLIST -> {
                Log.d("DELETE_PADLIST", selection + " - " + selectionArgs.toString())
                count = PadListDatabase.getInstance(context).padDao().deleteBy(selectionArgs)
            }

//            PAD_ID -> {
//                Log.d("DELETE_PAD_ID", selection + " - " + Arrays.toString(selectionArgs))
//                id = uri.pathSegments[1]
//                query = ArrayList(selectionArgs?.let { listOf(*it) }!!)
//                query.add(0, id)
//                db!!.delete(RELATION_TABLE_NAME, _ID_PAD + " = ?", arrayOfNulls(0))
//                count = db!!.delete(PAD_TABLE_NAME, _ID + " = ?" + if (!TextUtils.isEmpty(selection)) " AND (?)" else "", query.toTypedArray())
//            }
            PAD_ID -> {
                Log.d("DELETE_PAD_ID", selection + " - " + Arrays.toString(selectionArgs))
                count = PadListDatabase.getInstance(context).padDao().deleteBy(selectionArgs)
            }

//            PADGROUP_LIST -> {
//                Log.d(TAG, "delete_padgroup_list: " + selection + " - " + Arrays.toString(selectionArgs))
//                db!!.delete(RELATION_TABLE_NAME, _ID_GROUP + " =?", selectionArgs)
//                count = db!!.delete(PADGROUP_TABLE_NAME, selection, selectionArgs)
//            }
            PADGROUP_LIST -> {
                Log.d(TAG, "delete_padgroup_list: " + selection + " - " + Arrays.toString(selectionArgs))
                count = PadListDatabase.getInstance(context).padGroupDao().deleteBy(selectionArgs)
            }

//            PADGROUP_ID -> {
//                Log.d(TAG, "delete_padgroup_id: " + selection + " - " + Arrays.toString(selectionArgs))
//                id = uri.pathSegments[1]
//                query = ArrayList(selectionArgs?.let { listOf(*it) }!!)
//                query.add(0, id)
//                db!!.delete(RELATION_TABLE_NAME, _ID_GROUP + " = ?", arrayOfNulls(0))
//                count = db!!.delete(PADGROUP_TABLE_NAME, _ID + " = ?" + if (!TextUtils.isEmpty(selection)) " AND (?)" else "", query.toTypedArray())
//            }
            PADGROUP_ID -> {
//                Log.d(TAG, "delete_padgroup_id: " + selection + " - " + Arrays.toString(selectionArgs))
                count = PadListDatabase.getInstance(context).padGroupDao().deleteBy(selectionArgs)
            }

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context.contentResolver.notifyChange(uri, null)
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
        val context = context ?: return 0
        val pad: MutableLiveData<Pad> = values?.let { Pad.fromContentValues(it) }!!
        runBlocking {
            when (uriMatcher!!.match(uri)) {
//            PADLIST -> count = db!!.update(PAD_TABLE_NAME, values, selection, selectionArgs)
                PADLIST -> {
                    count = PadListDatabase.getInstance(context).padDao()
                        .update(pad.value!!)
                }
//            PAD_ID -> {
//                id = uri.pathSegments[1]
//                query = ArrayList(selectionArgs?.let { listOf(*it) }!!)
//                query.add(0, id)
//                count = db!!.update(PAD_TABLE_NAME,
//                        values,
//                        _ID + " = ?" + if (!TextUtils.isEmpty(selection)) " AND (?)" else "",
//                        query.toTypedArray())
//            }
                PAD_ID -> {
//                id = uri.pathSegments[1]
//                query = ArrayList(selectionArgs?.let { listOf(*it) }!!)
//                query.add(0, id)

                    count = PadListDatabase.getInstance(context).padDao()
                        .update(pad.value!!)
                }

                else -> throw IllegalArgumentException("Unknown URI $uri")
            }
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

//        return when (uriMatcher!!.match(uri)) {
//            PADLIST -> "vnd.android.cursor.dir/$AUTHORITY.pad"
//            PAD_ID-> "vnd.android.cursor.item/$AUTHORITY.pad"
//            PADGROUP_LIST-> "vnd.android.cursor.item/$AUTHORITY.padgroup"
//            PADGROUP_ID-> "vnd.android.cursor.item/$AUTHORITY.padgroup"
//            PADLIST_PADGROUP_ID-> "vnd.android.cursor.item/$AUTHORITY.padlist_padgroup"
//            else -> throw IllegalArgumentException("Unknown URI: $uri")
//        }
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
        var rowID: Long = 0
        val _uri: Uri? = null
        val context = context ?: throw Exception("No context provided")
        runBlocking {
            when (uriMatcher!!.match(uri)) {
                //            PADLIST -> {
                //                rowID = db!!.insert(PAD_TABLE_NAME, "", values)
                //                ContentUris.withAppendedId(PADLIST_CONTENT_URI, rowID)
                //            }
                PADLIST -> {
                    val pad: MutableLiveData<Pad> = values?.let { Pad.fromContentValues(it) }!!
                    rowID = PadListDatabase.getInstance(context).padDao().insert(pad.value!!)
                    ContentUris.withAppendedId(PADLIST_CONTENT_URI, rowID)
                }

                //            PADGROUP_LIST -> {
                //                rowID = db!!.insert(PADGROUP_TABLE_NAME, "", values)
                //                ContentUris.withAppendedId(PADGROUPS_CONTENT_URI, rowID)
                //            }
                PADGROUP_LIST -> {
                    val padgroup: MutableLiveData<PadGroup> =
                        values?.let { PadGroup.fromContentValues(it) }!!

                    val rowIDs = PadListDatabase.getInstance(context).padGroupDao()
                        .insertAll(padgroup.value!!)
                    ContentUris.withAppendedId(PADGROUPS_CONTENT_URI, rowIDs[0])
                }

                else -> throw IllegalArgumentException("Unknown URI $uri")
            }
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
        var order = sortOrder
        val qb = SQLiteQueryBuilder()
        val context = context ?: return null
        val cursor: Cursor
        when (uriMatcher!!.match(uri)) {
//            PADLIST -> {
//                qb.tables = PAD_TABLE_NAME
//                qb.projectionMap = PROJECTION_MAP
//            }
//            PAD_ID -> {
//                qb.tables = PAD_TABLE_NAME
//                qb.appendWhere(_ID + "=" + DatabaseUtils.sqlEscapeString(uri.pathSegments[1]))
//            }
            PADLIST -> {
                val padDao: PadDao = PadListDatabase.getInstance(context).padDao()
                cursor = if (selectionArgs === null || selectionArgs.isEmpty()) {
                    padDao.getAllCursor()
                } else {
                    padDao.getByUrlCursor(selectionArgs[0])
                }
                cursor.setNotificationUri(context.contentResolver, uri)
            }
            PAD_ID -> {
                val padDao: PadDao = PadListDatabase.getInstance(context).padDao()
                cursor = padDao.getByIdCursor(ContentUris.parseId(uri))
            }

//            PADGROUP_LIST -> {
//                qb.tables = PADGROUP_TABLE_NAME
//                qb.projectionMap = PROJECTION_MAP
//            }
            PADGROUP_LIST -> {
                val padGroupDao: PadGroupDao = PadListDatabase.getInstance(context).padGroupDao()
                cursor = if (selectionArgs === null || selectionArgs.isEmpty()) {
                    padGroupDao.getAllCursor()
                } else {
                    padGroupDao.getByIdCursor(selectionArgs[0].toLong())
                }
                cursor.setNotificationUri(context.contentResolver, uri)
            }

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
//        if (order.isNullOrEmpty()) {
//            /**
//             * By default sort
//             */
//            order = "$LAST_USED_DATE DESC "
//        }
//        val c = qb.query(db, projection, selection, selectionArgs, null, null, order)
        /**
         * register to watch a content URI for changes
         */
//        c.setNotificationUri(context!!.contentResolver, uri)
//        return c

        cursor.setNotificationUri(context.contentResolver, uri)
        return cursor
    }

    /**
     * onCreate override
     * @return
     */
    override fun onCreate(): Boolean {
//        val context = context
//        val dbHelper = PadlandDbHelper(context)
        /**
         * Create a writeable database which will trigger its
         * creation if it doesn't already exist.
         */
//        db = dbHelper.writableDatabase
//        return db != null
        return context != null
    }

    companion object {
        private const val PROVIDER_NAME = "com.mikifus.padland.padlandcontentprovider"
        const val TAG = "PadLandContentProvider"
        private const val AUTHORITY = "content://$PROVIDER_NAME/"
        val PADLIST_CONTENT_URI: Uri = Uri.parse(AUTHORITY + "padlist")
        val PADGROUPS_CONTENT_URI: Uri = Uri.parse(AUTHORITY + "padgroups")
        const val DATABASE_VERSION = 9
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
        var uriMatcher: UriMatcher? = null

        init {
            uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            uriMatcher!!.addURI(PROVIDER_NAME, "padlist", PADLIST)
            uriMatcher!!.addURI(PROVIDER_NAME, "padlist/#", PAD_ID)
            uriMatcher!!.addURI(PROVIDER_NAME, "padgroups", PADGROUP_LIST)
            uriMatcher!!.addURI(PROVIDER_NAME, "padgroups/#", PADGROUP_ID)
            uriMatcher!!.addURI(PROVIDER_NAME, "padlist_padgroup_id/#", PADLIST_PADGROUP_ID)
        }

        const val PAD_TABLE_NAME = "padlist"
//        val PAD_TABLE_CREATE_QUERY = " CREATE TABLE " + PAD_TABLE_NAME +
//                " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                " " + PadModel.Companion.NAME + " TEXT NOT NULL, " +
//                " " + LOCAL_NAME + " TEXT, " +
//                " " + SERVER + " TEXT NOT NULL, " +
//                " " + PadModel.Companion.URL + " TEXT NOT NULL, " +
//                " " + LAST_USED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
//                " " + CREATE_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
//                " " + ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0 " +
//                ");"
        const val PADGROUP_TABLE_NAME = "padgroups"
//        val PADGROUP_TABLE_CREATE_QUERY = " CREATE TABLE " + PADGROUP_TABLE_NAME +
//                " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                " " + PadModel.Companion.NAME + " TEXT NOT NULL, " +
//                " " + PadGroupModel.Companion.POSITION + " INTEGER DEFAULT 0, " +
//                " " + LAST_USED_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now')), " +
//                " " + CREATE_DATE + " INTEGER NOT NULL DEFAULT (strftime('%s','now'))," +
//                " " + ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0 " +
//                ");"
        const val RELATION_TABLE_NAME = "padlist_padgroups"
//        const val RELATION_TABLE_CREATE_QUERY = " CREATE TABLE " + RELATION_TABLE_NAME +
//                " (" + _ID_GROUP + " INTEGER NOT NULL, " +
//                " " + _ID_PAD + " INTEGER NOT NULL " +
//                ");"

        /**
         * Gets the current time in the format that the database uses.
         * As it is static, it can be used by other classes.
         * @return
         */
        val nowDate: Long
            get() = Date().time / 1000
//        val padFieldsList: Array<String>
//            get() = arrayOf<String>(
//                    _ID,
//                    PadModel.Companion.NAME,
//                    LOCAL_NAME,
//                    SERVER,
//                    PadModel.Companion.URL,
//                    LAST_USED_DATE,
//                    CREATE_DATE,
//                    ACCESS_COUNT
//            )
//        val padgroupFieldsList: Array<String>
//            get() = arrayOf<String>(
//                    _ID,
//                    PadModel.Companion.NAME,
//                    LAST_USED_DATE,
//                    CREATE_DATE,
//                    ACCESS_COUNT
//            )
    }
}