package com.mikifus.padland;

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad


public class PadLandContentProvider: ContentProvider() {

    // Defines a handle to the Room database
    private lateinit var database: PadListDatabase

    companion object {
        /** The authority of this content provider.  */
        const val AUTHORITY = "com.mikifus.padland.padlandcontentprovider"

        /** The URI for the tables.  */
        val URI_PAD_LIST = Uri.parse(
            "content://$AUTHORITY/padlist"
        )
        val URI_PAD_GROUP_LIST = Uri.parse(
            "content://$AUTHORITY/padgroups"
        )
        val URI_PAD_LIST_PAD_GROUP = Uri.parse(
            "content://$AUTHORITY/pad_list_pad_group_id"
        )

        private const val PAD_LIST = 1
        private const val PAD_ID = 2
        private const val PAD_GROUP_LIST = 3
        private const val PAD_GROUP_ID = 4
        private const val PAD_LIST_PAD_GROUP_ID = 5

        /** The URI matcher.  */
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        init {
            MATCHER.addURI(AUTHORITY, "padlist", PAD_LIST)
            MATCHER.addURI(AUTHORITY, "padlist/#", PAD_ID)
            MATCHER.addURI(AUTHORITY, "padgroups", PAD_GROUP_LIST)
            MATCHER.addURI(AUTHORITY, "padgroups/#", PAD_GROUP_ID)
            MATCHER.addURI(AUTHORITY, "pad_list_pad_group_id/#", PAD_LIST_PAD_GROUP_ID)
        }
    }

    override fun onCreate(): Boolean {
        if(context == null) {
            return false
        }
        database = PadListDatabase.getInstance(context!!)
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (MATCHER.match(uri)) {
            PAD_LIST -> "vnd.android.cursor.dir/$AUTHORITY" + "." + Pad.TABLE_NAME
            PAD_ID -> "vnd.android.cursor.item/$AUTHORITY" + "." + Pad.TABLE_NAME
            PAD_GROUP_LIST -> "vnd.android.cursor.dir/$AUTHORITY" + "." + Pad.TABLE_NAME
            PAD_GROUP_ID -> "vnd.android.cursor.item/$AUTHORITY" + "." + PadGroup.TABLE_NAME
            PAD_LIST_PAD_GROUP_ID-> "vnd.android.cursor.item/$AUTHORITY" + "." + PadGroupsAndPadList.TABLE_NAME
            else -> throw java.lang.IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        return when (MATCHER.match(uri)) {
            PAD_ID, PAD_LIST-> {
                val cursor = database.openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder
                        .builder(Pad.TABLE_NAME)
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
                cursor.setNotificationUri(context!!.contentResolver, uri);

                cursor
            }
            PAD_GROUP_ID, PAD_GROUP_LIST-> {
                val cursor = database.openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder
                        .builder(PadGroup.TABLE_NAME)
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
                cursor.setNotificationUri(context!!.contentResolver, uri);

                cursor
            }
            PAD_LIST_PAD_GROUP_ID-> {
                val cursor = database.openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder
                        .builder(PadGroupsAndPadList.TABLE_NAME)
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
                cursor.setNotificationUri(context!!.contentResolver, uri);

                cursor
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        if(values == null) {
            throw IllegalArgumentException("No values were provided")
        }
        return when (MATCHER.match(uri)) {
            PAD_LIST-> {
                val context = context ?: throw IllegalStateException("Context was not initialized")
                val db = database.openHelper.writableDatabase
                val id = db.insert(Pad.TABLE_NAME, SQLiteDatabase.CONFLICT_ROLLBACK, values)

                context.contentResolver.notifyChange(uri, null)

                ContentUris.withAppendedId(uri, id)
            }
//            PAD_GROUP_LIST-> {
//                val context = context ?: throw IllegalStateException("Context was not initialized")
//                val db = database.openHelper.writableDatabase
//                val id = db.insert(PadGroup.TABLE_NAME, SQLiteDatabase.CONFLICT_ROLLBACK, values)
//
//                context.contentResolver.notifyChange(uri, null)
//
//                ContentUris.withAppendedId(uri, id)
//            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun update(uri: Uri,
                        values: ContentValues?,
                        selection: String?,
                        selectionArgs: Array<String?>?): Int {
        if(values == null) {
            return 0
        }

        return when (MATCHER.match(uri)) {
            PAD_LIST-> throw IllegalArgumentException("Invalid URI, cannot update without ID [$uri]")
            PAD_ID-> {
                val context = context ?: return 0
                val db = database.openHelper.writableDatabase
                val count: Int = db.update(
                    Pad.TABLE_NAME,
                    SQLiteDatabase.CONFLICT_ROLLBACK,
                    values,
                    selection,
                    selectionArgs
                )

                context.contentResolver.notifyChange(uri, null)

                count
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if(selectionArgs == null){
            return 0
        }

        return when (MATCHER.match(uri)) {
            PAD_LIST-> throw IllegalArgumentException("Invalid URI, cannot update without ID [$uri]")
            PAD_ID-> {
                val context = context ?: return 0
                val db = database.openHelper.writableDatabase
                val count: Int = db.delete(Pad.TABLE_NAME, selection, selectionArgs)

                context.contentResolver.notifyChange(uri, null)

                count
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }
}
