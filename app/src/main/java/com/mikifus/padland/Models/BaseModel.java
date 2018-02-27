package com.mikifus.padland.Models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mikifus on 27/02/18.
 */

public class BaseModel extends SQLiteOpenHelper {

    public static final String TAG = "BaseModel";
    protected static final String DATABASE_NAME = "commments.db";
    protected static final int DATABASE_VERSION = 1;

    protected SQLiteDatabase db;

    public BaseModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
