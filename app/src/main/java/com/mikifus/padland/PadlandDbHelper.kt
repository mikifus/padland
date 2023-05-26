package com.mikifus.padland

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.mikifus.padland.Models.PadGroupModel
import com.mikifus.padland.Models.ServerModel
import com.mikifus.padland.Utils.OldDatabaseHelper

/**
 * Created by mikifus on 12/03/16.
 *
 *
 * TODO: Move here all database stuff
 */
open class PadlandDbHelper    //    public boolean requires_db_migration = false;
(protected open var context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, PadContentProvider.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(PadContentProvider.PAD_TABLE_CREATE_QUERY)
        db.execSQL(PadContentProvider.PADGROUP_TABLE_CREATE_QUERY)
        db.execSQL(PadContentProvider.RELATION_TABLE_CREATE_QUERY)
        db.execSQL(ServerModel.SERVERS_TABLE_CREATE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
//            Log.w(TAG, "Upgrading database. Existing contents will be deleted. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("DROP TABLE IF EXISTS " + PadContentProvider.PAD_TABLE_NAME)
            onCreate(db)
        }
        if (oldVersion == 3 && newVersion == 4) {
//            Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("ALTER TABLE " + PadContentProvider.PAD_TABLE_NAME + " ADD COLUMN " + PadContentProvider.ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0;")
        }
        if (oldVersion < 6 && newVersion == 6) {
//            Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("ALTER TABLE " + PadContentProvider.PADGROUP_TABLE_NAME + " ADD COLUMN " + PadGroupModel.POSITION + " INTEGER NOT NULL DEFAULT 0;")
        }
        if (oldVersion < 8 && newVersion == 8) {
            db.execSQL("ALTER TABLE " + PadContentProvider.PAD_TABLE_NAME + " ADD COLUMN " + PadContentProvider.LOCAL_NAME + " TEXT;")
            db.execSQL(ServerModel.SERVERS_TABLE_CREATE_QUERY)

            // DO NOT THIS AT HOME
            val exdbHelper = OldDatabaseHelper(context)
            val cursor = exdbHelper.db.query(ServerModel.TABLE, null, null, null, null, null, null)
            var contentValues: ContentValues
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                contentValues = ContentValues()
                contentValues.put(ServerModel._ID, cursor.getLong(0))
                contentValues.put(ServerModel.NAME, cursor.getString(1))
                contentValues.put(ServerModel.URL, cursor.getString(2))
                contentValues.put(ServerModel.PADPREFIX, cursor.getString(3))
                contentValues.put(ServerModel.POSITION, cursor.getInt(4))
                contentValues.put(ServerModel.JQUERY, cursor.getInt(5))
                contentValues.put(ServerModel.ENABLED, cursor.getInt(6))
                db.insert(ServerModel.TABLE, null, contentValues)

                // do something
                cursor.moveToNext()
            }
            cursor.close()
        }
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_NAME = "padlist"
    }
}