package com.mikifus.padland.Utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by mikifus on 28/02/18.
 */
class OldDatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    var db: SQLiteDatabase

    init {
        db = writableDatabase
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        const val DATABASE_NAME = "commments.db"
        const val DATABASE_VERSION = 1
    }
}