package com.mikifus.padland.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mikifus on 28/02/18.
 */

public class OldDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "commments.db";
    public static final int DATABASE_VERSION = 1;

    public SQLiteDatabase db;

    public OldDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
