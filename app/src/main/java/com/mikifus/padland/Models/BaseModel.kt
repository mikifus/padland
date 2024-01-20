package com.mikifus.padland.Models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.mikifus.padland.PadContentProvider
import com.mikifus.padland.PadlandDbHelper

/**
 * Created by mikifus on 27/02/18.
 */
open class BaseModel(context: Context?) : PadlandDbHelper(context) {
    protected var db: SQLiteDatabase = writableDatabase

    companion object {
        const val TAG = "BaseModel"

        //    protected static final String DATABASE_NAME = PadlandDbHelper.DATABASE_NAME;
        @JvmStatic
        protected val DATABASE_VERSION: Int = PadContentProvider.DATABASE_VERSION
    }
}