package com.mikifus.padland;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

/**
 * Parent App class
 * @author mikifus
 */
public class PadlandApp extends Application {
    private SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        PadlandDbHelper helper = new PadlandDbHelper(this);
        this.db = helper.getWritableDatabase();
    }

    public SQLiteDatabase getDb() {
        return db;
    }
}
