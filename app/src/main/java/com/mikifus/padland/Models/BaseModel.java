package com.mikifus.padland.Models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mikifus.padland.PadContentProvider;
import com.mikifus.padland.PadlandDbHelper;

/**
 * Created by mikifus on 27/02/18.
 */

public class BaseModel extends PadlandDbHelper {

    public static final String TAG = "BaseModel";
//    protected static final String DATABASE_NAME = PadlandDbHelper.DATABASE_NAME;
    protected static final int DATABASE_VERSION = PadContentProvider.DATABASE_VERSION;

    protected SQLiteDatabase db;

    public BaseModel(Context context) {
        super(context);
        this.db = getWritableDatabase();
    }
}
