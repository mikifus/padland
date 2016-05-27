package com.mikifus.padland;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by mikifus on 12/03/16.
 *
 *
 * TODO: Move here all database stuff
 */
public class PadlandDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final String DATABASE_NAME = "padlist";

    public PadlandDbHelper(Context context) {
        super(context, DATABASE_NAME, null, PadContentProvider.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(PadContentProvider.PAD_TABLE_CREATE_QUERY);
        db.execSQL(PadContentProvider.PADGROUP_TABLE_CREATE_QUERY);
        db.execSQL(PadContentProvider.RELATION_TABLE_CREATE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if( oldVersion < 3 ) {
//            Log.w(TAG, "Upgrading database. Existing contents will be deleted. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("DROP TABLE IF EXISTS " + PadContentProvider.PAD_TABLE_NAME);
            onCreate(db);
        }
        if( oldVersion == 3 && newVersion == 4 ) {
//            Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("ALTER TABLE " + PadContentProvider.PAD_TABLE_NAME + " ADD COLUMN " + PadContentProvider.ACCESS_COUNT + " INTEGER NOT NULL DEFAULT 0;");
        }
        if( oldVersion < 6 && newVersion == 6 ) {
//            Log.w(TAG, "Upgrading database. Existing contents will be migrated. [" + oldVersion + "]->[" + newVersion + "]");
            db.execSQL("ALTER TABLE " + PadContentProvider.PADGROUP_TABLE_NAME + " ADD COLUMN " + PadContentProvider.POSITION + " INTEGER NOT NULL DEFAULT 0;");
        }
    }
}