package com.mikifus.padland;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mikifus.padland.Models.PadGroupModel;
import com.mikifus.padland.Models.ServerModel;
import com.mikifus.padland.Utils.OldDatabaseHelper;

/**
 * Created by mikifus on 12/03/16.
 *
 *
 * TODO: Move here all database stuff
 */
public class PadlandDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final String DATABASE_NAME = "padlist";
    protected Context context;
//    public boolean requires_db_migration = false;

    public PadlandDbHelper(Context context) {
        super(context, DATABASE_NAME, null, PadContentProvider.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(PadContentProvider.PAD_TABLE_CREATE_QUERY);
        db.execSQL(PadContentProvider.PADGROUP_TABLE_CREATE_QUERY);
        db.execSQL(PadContentProvider.RELATION_TABLE_CREATE_QUERY);
        db.execSQL(ServerModel.SERVERS_TABLE_CREATE_QUERY);
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
            db.execSQL("ALTER TABLE " + PadContentProvider.PADGROUP_TABLE_NAME + " ADD COLUMN " + PadGroupModel.POSITION + " INTEGER NOT NULL DEFAULT 0;");
        }
        if( oldVersion < 8 && newVersion == 8 ) {
            db.execSQL("ALTER TABLE " + PadContentProvider.PAD_TABLE_NAME + " ADD COLUMN " + PadContentProvider.LOCAL_NAME + " TEXT;");
            db.execSQL(ServerModel.SERVERS_TABLE_CREATE_QUERY);

            // DO NOT THIS AT HOME
            OldDatabaseHelper exdbHelper = new OldDatabaseHelper(context);
            Cursor cursor = exdbHelper.db.query(ServerModel.TABLE, null, null, null, null, null, null);

            ContentValues contentValues;

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                contentValues = new ContentValues();
                contentValues.put(ServerModel._ID, cursor.getLong(0));
                contentValues.put(ServerModel.NAME, cursor.getString(1));
                contentValues.put(ServerModel.URL, cursor.getString(2));
                contentValues.put(ServerModel.PADPREFIX, cursor.getString(3));
                contentValues.put(ServerModel.POSITION, cursor.getInt(4));
                contentValues.put(ServerModel.JQUERY, cursor.getInt(5));
                contentValues.put(ServerModel.ENABLED, cursor.getInt(6));

                db.insert(ServerModel.TABLE,null,contentValues);

                // do something
                cursor.moveToNext();
            }
            cursor.close();
        }
    }
}