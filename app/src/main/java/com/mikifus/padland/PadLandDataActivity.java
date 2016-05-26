package com.mikifus.padland;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * A data activity inherits from the main activity and provides methods
 * to insert, update and delete the documents data. Each activity that
 * deals with data must either inherit from this or make an intent
 * to another activity which does.
 */
public class PadLandDataActivity extends PadLandActivity {

    private static final String TAG = "PadLandDataActivity";

    public PadlandDb padlandDb;

    private ArrayList<HashMap<String, String>> meta_groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        padlandDb = new PadlandDb(getContentResolver());
        Log.d(TAG, "Data activity started running");

        HashMap<String, String> unclassified_group = new HashMap<>();

        unclassified_group.put(PadContentProvider._ID, "0");
        unclassified_group.put(PadContentProvider.NAME, "Unclassified");
        unclassified_group.put(PadContentProvider.POSITION, "999999");

        meta_groups.add(unclassified_group);
    }

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
        Cursor cursor = padlandDb._getPadDataById(pad_id);
        padData pad_data = new padData( cursor );
        cursor.close();
        return pad_data;
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
                .setMessage(getString(R.string.sure_to_delete_pad))
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
     * Asks the user to confirm deleting a document.
     * If confirmed, will make an intent to PadListActivity, where the info will be
     * deleted.
     * @return AlertDialog
     */

    protected Long getGroupIdFromAdapterData(int groupPosition) {
        HashMap<String, String> padgroups_data = padlandDb.getPadgroupAt(groupPosition);
        long id = 0L;
        if( padgroups_data.size() > 0 ) {
            id = Long.parseLong(padgroups_data.get(PadContentProvider._ID));
        }
        return id;
    }

    protected String getGroupNameFromAdapterData(int groupPosition) {
        HashMap<String, String> padgroups_data = padlandDb.getPadgroupAt(groupPosition);
        String name = "";
        if( padgroups_data.size() > 0 ) {
            name = padgroups_data.get(PadContentProvider.NAME);
        }
        return name;
    }

    public AlertDialog menu_group(final ArrayList<String> selectedItems)
    {
        final PadLandDataActivity context = this;
        final ArrayList<Long> selectedGroups = new ArrayList<>();
        int group_count = padlandDb.getPadgroupsCount();
        String[] group_names = new String[ group_count + 1 ];
        for( int i = 0; i < group_count; ++i ) {
            group_names[ i ] = getGroupNameFromAdapterData(i);
        }

        // Add unclassified group as choice
        group_names[ group_count ] = getString(R.string.padlist_group_unclassified_name);

        final boolean[] checkboxStatusArray = new boolean[ group_count + 1 ];

        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.padlist_group_select_dialog)
                .setIcon(R.drawable.ic_group_add)
                .setMultiChoiceItems(group_names, checkboxStatusArray,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            if (isChecked) {
                                
                                // Noew clean and set the view
                                ListView listView = ((AlertDialog) dialog).getListView();
                                SparseBooleanArray checked = listView.getCheckedItemPositions();
                                for( int i = 0; i < checkboxStatusArray.length; ++i ) {
                                    if ( checkboxStatusArray[i] && i != which ) {
                                        checkboxStatusArray[ i ] = false;
                                        listView.setItemChecked(i, false);
                                    }
                                }

                                // clean to just select one
                                selectedGroups.clear();

                                // If the user checked the item, add it to the selected items
                                Long group_id = context.getGroupIdFromAdapterData(which);
                                selectedGroups.add(group_id);
                            } else if (selectedGroups.contains(which)) {
                                // Else, if the item is already in the array, remove it
//                                selectedGroups.remove(Integer.valueOf(which));
                                ((AlertDialog) dialog).getListView().setItemChecked(which, true);
                            }
                        }
                    })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        long save_pad_id;
                        for(String pad_id_string : selectedItems) {
                            save_pad_id = Long.parseLong(pad_id_string);
                            for(Long save_padgroup_id : selectedGroups) {
                                boolean saved = context.padlandDb.savePadgroupRelation(save_padgroup_id,  save_pad_id);
                                Log.d(TAG, "Added to group? " + saved);
                            }
                        }
                        ((PadListActivity) context).notifyDataSetChanged();
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

    public AlertDialog menu_delete_group(final long group_id)
    {
        final PadLandDataActivity context = this;

        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete_group))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        if( context.padlandDb.deleteGroup(group_id) ) {
                            Toast.makeText(PadLandDataActivity.this, getString(R.string.padlist_group_deleted), Toast.LENGTH_LONG).show();
                        }
                        ((PadListActivity) context).notifyDataSetChanged();
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

    public class PadlandDb {

        ContentResolver contentResolver;
        SQLiteDatabase db;

        public PadlandDb (ContentResolver contentResolver) {
            this.contentResolver = contentResolver;

            PadlandDbHelper helper = new PadlandDbHelper(getBaseContext());
            this.db = helper.getWritableDatabase();
        }
        /**
         * Self explanatory name.
         * Field to compare must be specified by its identifier. Accepts only one comparation value.
         * @param field
         * @param comparation
         * @return
         */
        private Cursor _getPadDataFromDatabase( String field, String comparation ){
            Cursor c = null;
            String[] comparation_set = new String[]{ comparation };

            c = contentResolver.query(
                    PadContentProvider.PADLIST_CONTENT_URI,
                    PadContentProvider.getPadFieldsList(),
                    field + "=?",
                    comparation_set, // AKA id
                    null
            );
            return c;
        }

        /**
         * Queries the database and compares to pad_id
         * @param pad_id
         * @return
         */
        public Cursor _getPadDataById( long pad_id ){
            return this._getPadDataFromDatabase( PadContentProvider._ID, String.valueOf( pad_id ) );
        }

        /**
         * Queries the database and compares to padUrl
         * @param padUrl
         * @return
         */
        public Cursor _getPadDataByUrl(String padUrl){
            return this._getPadDataFromDatabase(PadContentProvider.URL, padUrl);
        }

        public long getNowDate() {
            return PadContentProvider.getNowDate();
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
                values.put( PadContentProvider.LAST_USED_DATE, getNowDate() );
                values.put( PadContentProvider.ACCESS_COUNT, (data.getAccessCount() + 1));
                String[] where_value = { String.valueOf(pad_id) };
                contentResolver.update(PadContentProvider.PADLIST_CONTENT_URI, values, PadContentProvider._ID + "=?", where_value);
            }
        }

        /**
         * Self explanatory name.
         * Field to compare must be specified by its identifier. Accepts only one comparation value.
         * @param field
         * @param comparation
         * @return
         */
        public Cursor _getPadgroupsDataFromDatabase( String field, String comparation ){
            Cursor c = null;
            String[] comparation_set = new String[]{ comparation };

            c = contentResolver.query(
                    PadContentProvider.PADGROUPS_CONTENT_URI,
                    PadContentProvider.getPadFieldsList(),
                    field + "=?",
                    comparation_set, // AKA id
                    null
            );
            return c;
        }

        protected HashMap<Long, ArrayList<String>> _getPadgroupsData()
        {
            Uri padlist_uri = Uri.parse(getString(R.string.request_padgroups));
            Cursor cursor = contentResolver.query(padlist_uri,
                    new String[]{PadContentProvider._ID, PadContentProvider.NAME},
                    null,
                    null,
                    PadContentProvider.CREATE_DATE + " ASC");

            HashMap<Long, ArrayList<String>> result = new HashMap<>();

            if (cursor == null || cursor.getCount() == 0) {
                return result;
            }

            HashMap<Long, ArrayList<String>> pad_data = new HashMap<>();

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);

                ArrayList<String> pad_strings = new ArrayList<String>();
                pad_strings.add(name);

                pad_data.put(id, pad_strings);

                // do something
                cursor.moveToNext();
            }
            cursor.close();

            return pad_data;
        }

        public int getPadgroupsCount() {
            Uri padlist_uri = Uri.parse(getString(R.string.request_padgroups));
            Cursor cursor = contentResolver.query(padlist_uri,
                    new String[]{PadContentProvider._ID, PadContentProvider.NAME},
                    null,
                    null,
                    PadContentProvider.CREATE_DATE + " ASC");

            int count = cursor.getCount();
            cursor.close();
            return count;
        }

        public HashMap<String, String> getPadgroupAt(int position) {
            Uri padlist_uri = Uri.parse(getString(R.string.request_padgroups));
            Cursor cursor = contentResolver.query(padlist_uri,
                    new String[]{PadContentProvider._ID, PadContentProvider.NAME, PadContentProvider.POSITION},
                    "",
                    null,
                    PadContentProvider.CREATE_DATE + " ASC LIMIT " + position + ", 1");

            HashMap<String, String> group = new HashMap<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String pos = cursor.getString(2);

                group.put(PadContentProvider._ID, id);
                group.put(PadContentProvider.NAME, name);
                group.put(PadContentProvider.POSITION, pos);

                break;
            }
            cursor.close();

            return group;
        }

        public HashMap<String, String> getPadgroupAtPosition(int position) {
//            final String QUERY =
//                    "SELECT * FROM " + PadContentProvider.PADGROUP_TABLE_NAME +
//                            "WHERE " + PadContentProvider.POSITION + "=? ";
//            Cursor cursor = db.rawQuery(QUERY, new String[]{String.valueOf(position)});
            Uri padlist_uri = Uri.parse(getString(R.string.request_padgroups));
            Cursor cursor = contentResolver.query(padlist_uri,
                    new String[]{PadContentProvider._ID, PadContentProvider.NAME, PadContentProvider.POSITION},
                    PadContentProvider.POSITION + " = ?",
                    new String[]{String.valueOf(position)},
                    PadContentProvider.CREATE_DATE + " ASC");


            HashMap<String, String> group = new HashMap<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String pos = cursor.getString(2);

                group.put(PadContentProvider._ID, id);
                group.put(PadContentProvider.NAME, name);
                group.put(PadContentProvider.POSITION, pos);

                break;
//                cursor.moveToNext();
            }
            cursor.close();

            return group;
        }

        public ArrayList<Long> getPadgroupChildrenIds(long id) {
            String QUERY;
            String[] values;
            if( id == 0 ) {
                QUERY =
                        "SELECT " + PadContentProvider._ID + " " +
                            "FROM " + PadContentProvider.PAD_TABLE_NAME + " " +
                            "WHERE " + PadContentProvider._ID + " NOT IN (" +
                                "SELECT DISTINCT " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME +
                                ") ";
                values = new String[]{};
            } else {
                QUERY =
                        "SELECT DISTINCT " + PadContentProvider._ID_PAD + " " +
                                "FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                                "WHERE " + PadContentProvider._ID_GROUP + "=? ";
                values = new String[]{String.valueOf(id)};
            }
            Cursor cursor = db.rawQuery(QUERY, values);


            ArrayList<Long> pad_ids = new ArrayList<>();

            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                long id_pad = cursor.getLong(0);
                pad_ids.add(id_pad);
                cursor.moveToNext();
            }
            cursor.close();

            return pad_ids;
        }

        public int getPadgroupChildrenCount(long id) {
            String QUERY;
            String[] values;
            if( id == 0 ) {
                QUERY =
                        "SELECT " + PadContentProvider._ID + " " +
                                "FROM " + PadContentProvider.PAD_TABLE_NAME + " " +
                                "WHERE " + PadContentProvider._ID + " NOT IN (" +
                                "SELECT " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME +
                                ") ";
                values = new String[]{};
            } else {
                QUERY =
                    "SELECT * FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                            "WHERE " + PadContentProvider._ID_GROUP + "=? ";
                values = new String[]{String.valueOf(id)};
            }

            Cursor cursor = db.rawQuery(QUERY, values);
            int count = cursor.getCount();
            cursor.close();
            return count;
        }

        public void _debug_relations() {
            String QUERY = "SELECT " + PadContentProvider._ID_GROUP + ", " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME;
            String[] values = new String[]{};
            Cursor cursor = db.rawQuery(QUERY, values);

            HashMap<Long, Long> hashMap = new HashMap<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                hashMap.put(cursor.getLong(0), cursor.getLong(1));
                cursor.moveToNext();
            }
            cursor.close();
            Log.d(TAG, hashMap.toString());
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
                int result = contentResolver.update(PadContentProvider.PADLIST_CONTENT_URI, values, PadContentProvider._ID + "=?", where_value);
                return (result > 0);
            }
            else {
                Log.d("INSERT", "Contents = " + values.toString());
                Uri result = contentResolver.insert(PadContentProvider.PADLIST_CONTENT_URI, values);
                return ( result != null );
            }
        }

        /**
         * Saves a new group if padgroup_id=0 or updates an existing one.
         * @param padgroup_id
         * @param values
         * @return
         */
        public boolean savePadgroupData( long padgroup_id, ContentValues values ){
            if( padgroup_id > 0 ) {
                String[] where_value = { String.valueOf(padgroup_id) };
                int result = contentResolver.update(PadContentProvider.PADGROUPS_CONTENT_URI, values, PadContentProvider._ID + "=?", where_value);
                return (result > 0);
            }
            else {
                Log.d("INSERT", "Contents = " + values.toString());
                Uri result = contentResolver.insert(PadContentProvider.PADGROUPS_CONTENT_URI, values);
                return ( result != null );
            }
        }

        /**
         * Saves a new group if padgroup_id=0 or updates an existing one.
         * @param padgroup_id
         * @param pad_id
         * @return
         */
        public boolean savePadgroupRelation( long padgroup_id, long pad_id ){
            removePadFromAllGroups(pad_id);

            if( padgroup_id == 0 ) {
                return false;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(PadContentProvider._ID_PAD, pad_id);
            contentValues.put(PadContentProvider._ID_GROUP, padgroup_id);

            boolean result = db.insert(PadContentProvider.RELATION_TABLE_NAME, null, contentValues) > 0;
//            _debug_relations();
            return result;
        }

        /**
         * Destroys all possible relation between a pad and any group
         * @param pad_id
         * @return
         */
        public boolean removePadFromAllGroups(long pad_id) {
            int deleted = db.delete(PadContentProvider.RELATION_TABLE_NAME, PadContentProvider._ID_PAD + "=? ", new String[]{String.valueOf(pad_id)});
            return deleted > 0;
        }

        /**
         * Deletes a pad by its id, no confirmation, won't be recoverable
         * @param pad_id
         * @return
         */
        public boolean deletePad(long pad_id){
            if( pad_id > 0 ) {
                int result = contentResolver.delete(PadContentProvider.PADLIST_CONTENT_URI, PadContentProvider._ID + "=?", new String[]{String.valueOf(pad_id)});
                return (result > 0);
            }
            else {
                throw new IllegalArgumentException("Pad id is not valid");
            }
        }

        /**
         * Deletes a group by its id, no confirmation, won't be recoverable.
         * The group pads will be moved to the zero-group (Unclassified)
         * @param group_id
         * @return
         */
        public boolean deleteGroup(long group_id){
            if( group_id > 0 ) {
                emptyGroup(group_id);

                int result = db.delete(PadContentProvider.PADGROUP_TABLE_NAME, PadContentProvider._ID + "=?", new String[]{String.valueOf(group_id)});
                return (result > 0);
            }
            else {
                throw new IllegalArgumentException("Group id is not valid");
            }
        }

        /**
         * Erases all relations with stablished with this group
         * @param group_id
         * @return
         */
        public boolean emptyGroup(long group_id) {
            int result = db.delete(PadContentProvider.RELATION_TABLE_NAME, PadContentProvider._ID_GROUP + "=?", new String[]{String.valueOf(group_id)});
            return result > 0;
        }
    }
}
