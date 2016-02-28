package com.mikifus.padland;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A data activity inherits from the main activity and provides methods
 * to insert, update and delete the documents data. Each activity that
 * deals with data must either inherit from this or make an intent
 * to another activity which does.
 */
public class PadLandDataActivity extends PadLandActivity {

    private static final String TAG = "PadLandDataActivity";

    /**
     * The db fields in a single string array to use
     * the variable directly.
     */
    public static String[] pad_db_fields = new String[] {
            PadLandContentProvider._ID,
            PadLandContentProvider.NAME,
            PadLandContentProvider.SERVER,
            PadLandContentProvider.URL,
            PadLandContentProvider.LAST_USED_DATE,
            PadLandContentProvider.CREATE_DATE,
            PadLandContentProvider.ACCESS_COUNT
    };
    /**
     * It gets the pad id from an intent if there is such info, else 0
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
        Cursor cursor = this._getPadDataById(pad_id);
        padData pad_data = new padData( cursor );
        return pad_data;
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
     * @param pad_id
     * @param values
     * @return
     */
    public boolean savePadData( long pad_id, ContentValues values ){
        if( pad_id > 0 ) {
            String[] where_value = { String.valueOf(pad_id) };
            int result = getContentResolver().update(PadLandContentProvider.CONTENT_URI, values, PadLandContentProvider._ID + "=?", where_value);
            return (result > 0);
        }
        else {
            Log.d("INSERT", "Contents = " + values.toString());
            Uri result = getContentResolver().insert( PadLandContentProvider.CONTENT_URI, values );
            return ( result != null );
        }
    }

    /**
     * Deletes a pad by its id, no confirmation, won't be recoverable
     * @param pad_id
     * @return
     */
    public boolean deletePad(long pad_id){
        if( pad_id > 0 ) {
            int result = getContentResolver().delete(PadLandContentProvider.CONTENT_URI, PadLandContentProvider._ID + "=?", new String[]{String.valueOf(pad_id)});
            return (result > 0);
        }
        else {
            throw new IllegalArgumentException("Pad id is not valid");
        }
    }

    /**
     * Gets current pad data and saves the modified values (LAST_USED_DATE and ACCESS_COUNT).
     * I tried to optimize it in such way that there's no need to use _getPadData, but it didn't work.
     * @param pad_id
     * @return
     */
    public void accessUpdate( long pad_id ){
        if( pad_id > 0 ) {
            padData data = _getPadData( pad_id );
            ContentValues values = new ContentValues();
            values.put( PadLandContentProvider.LAST_USED_DATE, getNowDate() );
            values.put( PadLandContentProvider.ACCESS_COUNT, (data.getAccessCount() + 1));
            String[] where_value = { String.valueOf(pad_id) };
            getContentResolver().update(PadLandContentProvider.CONTENT_URI, values, PadLandContentProvider._ID  + "=?", where_value );
        }
    }

    /**
     * Asks the user to confirm deleting a document.
     * If confirmed, will make an intent to PadListActivity, where the info will be
     * deleted.
     * @param selectedItems
     * @return AlertDialog
     */
    public AlertDialog AskDelete(final ArrayList<String> selectedItems)
    {
        final PadLandDataActivity context = this;

        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Bundle extra = new Bundle();
                        extra.putString("action", "delete");
                        extra.putStringArrayList("pad_id", selectedItems);
                        Intent intent = new Intent(context, PadListActivity.class);
                        intent.putExtras(extra);
                        context.startActivity(intent);
                        dialog.dismiss();
                        finish();
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
     * TODO: Share multiple pads.
     * @param selectedItems
     */
    public void menu_share(final ArrayList<String> selectedItems) {
        Log.d("PadLandDataActivity", selectedItems.get(0).toString());
        // Only the first one
        final String selectedItem_id = selectedItems.get(0);
        padData padData = _getPadData( Long.parseLong(selectedItem_id) );
        String padUrl = padData.getUrl();
        Log.d("SHARING_PAD", selectedItem_id + " - " + padUrl);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_auto_text) + padUrl);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_document)));
    }

    public long getNowDate() {
        return PadLandContentProvider.getNowDate();
    }

    public boolean onCreateOptionsMenu( Menu menu, int id_menu ) {
        return super.onCreateOptionsMenu(menu, id_menu);
    }

    /**
     * The padData subclass is the summary of information the App needs
     * to deal with the documents. It has the info and returns it
     * in the right format.
     */
    public class padData
    {
        private long id;
        private String name;
        private String server;
        private String url;
        private long last_used_date;
        private long create_date;
        private long access_count;

        public padData(Cursor c) {
            if(c != null && c.getCount() > 0) {
                c.moveToFirst();
//                Log.d("padData LOADED", DatabaseUtils.dumpCursorToString(c));

                id = c.getLong(0);
                name = c.getString(1);
                server =  c.getString(2);
                url = c.getString(3);
                last_used_date = c.getLong(4);
                create_date = c.getLong(5);
                access_count = c.getLong(6);
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
        public long getAccessCount() {
            return access_count;
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
