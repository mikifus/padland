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
(protected var context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, PadContentProvider.Companion.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(PadContentProvider.Companion.PAD_TABLE_CREATE_QUERY)
        db.execSQL(PadContentProvider.Companion.PADGROUP_TABLE_CREATE_QUERY)
        db.execSQL(PadContentProvider.Companion.RELATION_TABLE_CREATE_QUERY)
        db.execSQL(ServerModel.Companion.SERVERS_TABLE_CREATE_QUERY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
//            Log.w(TAG, "Upgrading database. Existing contents will be deleted. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("DROP TABLE IF EXISTS " + PadContentProvider.Companion.PAD_TABLE_NAME)
            onCreate(db)
        }
        if (oldVersion == 3 && newVersion == 4) {
//            Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("ALTER TABLE " + PadContentProvider.Companion.PAD_TABLE_NAME + " ADD COLUMN " + PadContentProvider.Companion.ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0;")
        }
        if (oldVersion < 6 && newVersion == 6) {
//            Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("ALTER TABLE " + PadContentProvider.Companion.PADGROUP_TABLE_NAME + " ADD COLUMN " + PadGroupModel.Companion.POSITION + " INTEGER NOT NULL DEFAULT 0;")
        }
        if (oldVersion < 8 && newVersion == 8) {
            db.execSQL("ALTER TABLE " + PadContentProvider.Companion.PAD_TABLE_NAME + " ADD COLUMN " + PadContentProvider.Companion.LOCAL_NAME + " TEXT;")
            db.execSQL(ServerModel.Companion.SERVERS_TABLE_CREATE_QUERY)

            // DO NOT THIS AT HOME
            val exdbHelper = OldDatabaseHelper(context)
            val cursor = exdbHelper.db.query(ServerModel.Companion.TABLE, null, null, null, null, null, null)
            var contentValues: ContentValues
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                contentValues = ContentValues()
                contentValues.put(ServerModel.Companion._ID, cursor.getLong(0))
                contentValues.put(ServerModel.Companion.NAME, cursor.getString(1))
                contentValues.put(ServerModel.Companion.URL, cursor.getString(2))
                contentValues.put(ServerModel.Companion.PADPREFIX, cursor.getString(3))
                contentValues.put(ServerModel.Companion.POSITION, cursor.getInt(4))
                contentValues.put(ServerModel.Companion.JQUERY, cursor.getInt(5))
                contentValues.put(ServerModel.Companion.ENABLED, cursor.getInt(6))
                db.insert(ServerModel.Companion.TABLE, null, contentValues)

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