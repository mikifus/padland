package com.mikifus.padland;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PadLandDataActivity extends PadLandActivity {


    public static String[] pad_db_fields = new String[] {
            PadLandContentProvider._ID,
            PadLandContentProvider.NAME,
            PadLandContentProvider.SERVER,
            PadLandContentProvider.URL,
            PadLandContentProvider.LAST_USED_DATE,
            PadLandContentProvider.CREATE_DATE
    };
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
     * Queries the database and compares to padUrl
     * @param padUrl
     * @return
     */
    public Cursor _getPadDataByUrl(String padUrl){
        return this._getPadDataFromDatabase( PadLandContentProvider.URL, padUrl );
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
                        pad_db_fields,
                        field + "=?",
                        comparation_set, // AKA id
                        null
                );
        return c;
    }

    /**
     * Saves a new pad if pad_id=0 or updates an existing one.
     *
     * @param pad_id
     * @param values
     * @return
     */
    public boolean savePadData( int pad_id, ContentValues values ){
        if( pad_id > 0 ) {
            String[] where_value = { String.valueOf(pad_id) };
            int result = getContentResolver().update(PadLandContentProvider.CONTENT_URI, values, PadLandContentProvider._ID  + "=?", where_value );
            return (result > 0);
        }
        else {
            Log.d("INSERT", "Contents = " + values.toString());
            Uri result = getContentResolver().insert(PadLandContentProvider.CONTENT_URI, values);
            return ( result != null );
        }
    }

    /**
     * Saves a new pad if pad_id=0 or updates an existing one.
     *
     * @param pad_id
     * @return
     */
    public boolean deletePad(long pad_id){
        if( pad_id > 0 ) {
            int result = getContentResolver().delete( PadLandContentProvider.CONTENT_URI, PadLandContentProvider._ID + "=?", new String[] {String.valueOf(pad_id)} );
            return (result > 0);
        }
        else {
            throw new IllegalArgumentException("Pad id is not valid");
        }
    }

    /**
     * Asks the user to confirm deleting a document
     * @param selectedItem_id
     * @return AlertDialog
     */
    public AlertDialog AskDelete(final long selectedItem_id)
    {
        final PadLandDataActivity context = this;
        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(context, PadListActivity.class);
                        intent.putExtra( "action", "delete" );
                        intent.putExtra( "pad_id", selectedItem_id );
                        context.startActivity(intent);
                        dialog.dismiss();
                    }

                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        DeleteDialogBox.show();
        return DeleteDialogBox;
    }

    /**
     * Menu to share a document url
     * @param selectedItem_id
     */
    public void menu_share(long selectedItem_id) {
        Cursor c = getContentResolver().query(PadLandContentProvider.CONTENT_URI,
                new String[] {PadLandContentProvider._ID, PadLandContentProvider.URL},
                PadLandContentProvider._ID+"=?",
                new String[] {String.valueOf(selectedItem_id)},
                null);
        c.moveToFirst();
        String padUrl = c.getString(1);
        Log.d("SHARING_PAD", selectedItem_id + " - " + padUrl);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_auto_text) + padUrl);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_document)));
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
        private long last_used_date;
        private long create_date;

        public padData(Cursor c) {
            if(c != null && c.getCount() > 0) {
                c.moveToFirst();

                id = c.getLong(0);
                name = c.getString(1);
                server =  c.getString(2);
                url = c.getString(3);
                last_used_date = c.getLong(4);
                create_date = c.getLong(5);
            }
        }
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
        public String getLastUsedDate() {
            return lon_to_date( last_used_date );
        }
        public String getCreateDate() {
            return lon_to_date( create_date );
        }
        public String lon_to_date( long TimeinMilliSeccond ){
            DateFormat formatter = android.text.format.DateFormat.getDateFormat( getApplicationContext() );
            Date dateObj = new Date( TimeinMilliSeccond * 1000 );
            return formatter.format( dateObj );
        }
    }
}
