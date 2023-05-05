package com.mikifus.padland

import android.app.Application
import android.database.sqlite.SQLiteDatabase

/**
 * Parent App class
 * @author mikifus
 */
class PadlandApp : Application() {
    var db: SQLiteDatabase? = null
        private set

    override fun onCreate() {
        super.onCreate()
        val helper = PadlandDbHelper(this)
        db = helper.writableDatabase
    }
}