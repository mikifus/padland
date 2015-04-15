package com.mikifus.padland;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class PadLandDataActivity extends PadLandActivity {

    /**
     * It gets the pad id by all possible means. This is, reading it from the Intent (in a
     * LongExtra) or using the padUrl to make a database query.
     * @return
     */
    public long _getPadId(){
        Intent myIntent = getIntent();
        long pad_id = myIntent.getLongExtra("pad_id", 0);
        return pad_id;
    }

    /**
     * Gets back a padData object.
     * @param pad_id
     * @return
     */
    public padData _getPadData( long pad_id ){
        Cursor cursor = (Cursor) this._getPadDbData(pad_id);

        padData pad_data = new padData( cursor );

        return pad_data;
    }

    /**
     * Returns a padData object from an id
     * @param pad_id
     * @return
     */
    public Cursor _getPadDbData( long pad_id ){
        Cursor c = this._getPadDataById( pad_id );

        return c;
    }

    /**
     * Queries the database and compares to pad_id
     * @param pad_id
     * @return
     */
    public Cursor _getPadDataById( long pad_id ){
        return this._getPadDataFromDatabase( PadLandContentProvider._ID, String.valueOf( pad_id ) );
    }

    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field
     * @param comparation
     * @return
     */
    public Cursor _getPadDataFromDatabase( String field, String comparation ){
        Cursor c = null;
        String[] comparation_set = new String[]{ comparation };

        c = getContentResolver()
                .query(
                        PadLandContentProvider.CONTENT_URI,
                        new String[] {
                                PadLandContentProvider._ID,
                                PadLandContentProvider.NAME,
                                PadLandContentProvider.SERVER,
                                PadLandContentProvider.URL
                        },
                        field + "=?",
                        comparation_set, // AKA id
                        null
                );
        return c;
    }

    /**
     * Creates the menu
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu( Menu menu, int id_menu ) {
        return super.onCreateOptionsMenu( menu, id_menu );
    }

    public class padData
    {
        private long id;
        private String name;
        private String server;
        private String url;

        public padData(Cursor c) {
            if(c != null && c.getCount() > 0) {
                c.moveToFirst();

                id = c.getLong(0);
                name = c.getString(1);
                server =  c.getString(2);
                url = c.getString(3);
            }
        }

        private String[] fields = new String[] {
                PadLandContentProvider._ID,
                PadLandContentProvider.NAME,
                PadLandContentProvider.SERVER,
                PadLandContentProvider.URL
        };


        public long getId() {
            return id;
        }
        public String getName() {
            return name;
        }

        public String getServer() {
            return server;
        }

        public String getUrl() {
            return url;
        }
    }
}
