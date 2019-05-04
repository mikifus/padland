package com.mikifus.padland;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import com.mikifus.padland.Dialog.EditPadDialog;
import com.mikifus.padland.Dialog.FormDialog;
import com.mikifus.padland.Models.Pad;
import com.mikifus.padland.Models.PadGroupModel;
import com.mikifus.padland.Models.PadModel;
import com.mikifus.padland.Models.Server;
import com.mikifus.padland.Models.ServerModel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * A data activity inherits from the main activity and provides methods
 * to insert, update and delete the documents data. Each activity that
 * deals with data must either inherit from this or make an intent
 * to another activity which does.
 */
public class PadLandDataActivity extends PadLandActivity implements FormDialog.FormDialogCallBack {

    private static final String TAG = "PadLandDataActivity";
    private static final String EDIT_PAD_DIALOG = "EditPadDialog";

    public PadlistDb padlistDb;

    private ArrayList<HashMap<String, String>> meta_groups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        padlistDb = new PadlistDb(getContentResolver());
        Log.d(TAG, "Data activity started running");

        HashMap<String, String> unclassified_group = new HashMap<>();

        unclassified_group.put(PadContentProvider._ID, "0");
        unclassified_group.put(PadModel.NAME, "Unclassified");
        unclassified_group.put(PadGroupModel.POSITION, "999999");

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
     * Gets back a Pad object.
     * @param pad_id
     * @return
     */
    public Pad _getPad(long pad_id ){
        Cursor cursor = padlistDb._getPadById(pad_id);
        cursor.moveToFirst();
        Pad pad_data = new Pad( cursor );
        cursor.close();
        return pad_data;
    }

    /**
     * Gets back a Pad array object.
     * @return
     */
    public HashMap<Long, Pad> _getPads(){
        HashMap<Long, Pad> PadHashMap = new HashMap<>();
        ArrayList<Pad> Pads = padlistDb._getAllPad();
        for(Pad Pad : Pads) {
            PadHashMap.put(Pad.getId(), Pad);
        }
        return PadHashMap;
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
        final Context activity = this;
        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete_pad))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        // TODO: Delete without an intent and call notofydatasetchanged or specify a callback
                        Bundle extra = new Bundle();
                        extra.putString("action", "delete");
                        extra.putStringArrayList("pad_id", selectedItems);
                        Intent intent = new Intent(getBaseContext(), PadListActivity.class);
                        intent.putExtras(extra);
                        activity.startActivity(intent);
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
        PadGroupModel model = new PadGroupModel(this);
        HashMap<String, String> padgroups_data = model.getPadgroupAt(groupPosition);
        long id = 0L;
        if( padgroups_data.size() > 0 ) {
            id = Long.parseLong(padgroups_data.get(PadContentProvider._ID));
        }
        return id;
    }

    protected String getGroupNameFromAdapterData(int groupPosition) {
        PadGroupModel model = new PadGroupModel(this);
        HashMap<String, String> padgroups_data = model.getPadgroupAt(groupPosition);
        String name = "";
        if( padgroups_data.size() > 0 ) {
            name = padgroups_data.get(PadModel.NAME);
        }
        return name;
    }

    public void menu_copy(final ArrayList<String> selectedItems) {
        StringBuilder copy_string = new StringBuilder();
        for(String pad_id_string : selectedItems) {
            Pad Pad = _getPad( Long.parseLong(pad_id_string) );
            if(copy_string.length() > 0){
                copy_string.append("\n");
            }
            copy_string.append(Pad.getUrl());
        }
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getBaseContext().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Pad urls", copy_string);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(PadLandDataActivity.this, getString(R.string.copy_copied), Toast.LENGTH_LONG).show();
    }

    public FormDialog menu_edit(final ArrayList<String> selectedItems) {
        FragmentManager fm = getSupportFragmentManager();
        EditPadDialog dialog = new EditPadDialog(getString(R.string.padlist_dialog_edit_pad_title), this);
        dialog.editPadId(Long.parseLong(selectedItems.get(0)));
        dialog.show(fm, EDIT_PAD_DIALOG);
        return dialog;
    }

    public AlertDialog menu_group(final ArrayList<String> selectedItems)
    {
        final PadLandDataActivity context = this;
        final ArrayList<Long> selectedGroups = new ArrayList<>();
        PadGroupModel padGroupModel = new PadGroupModel(this);
        int group_count = padGroupModel.getPadgroupsCount();
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
//                                SparseBooleanArray checked = listView.getCheckedItemPositions();
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
                        PadGroupModel padGroupModel = new PadGroupModel(context);
                        for(String pad_id_string : selectedItems) {
                            save_pad_id = Long.parseLong(pad_id_string);
                            for(Long save_padgroup_id : selectedGroups) {
                                boolean saved = context.padlistDb.savePadgroupRelation(save_padgroup_id,  save_pad_id);
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
                        if( context.padlistDb.deleteGroup(group_id) ) {
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
        Pad Pad = _getPad( Long.parseLong(selectedItem_id) );
        String padUrl = Pad.getUrl();
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

    @Override
    public void onDialogDismiss() {

    }

    @Override
    public void onDialogSuccess() {

    }

    public class PadlistDb {
        ContentResolver contentResolver;
        SQLiteDatabase db;

        public PadlistDb(ContentResolver contentResolver) {
            this.contentResolver = contentResolver;
            PadlandDbHelper padlandDbHelper = new PadlandDbHelper(PadLandDataActivity.this);
            this.db = padlandDbHelper.getWritableDatabase();
        }
        /**
         * Self explanatory name.
         * Field to compare must be specified by its identifier. Accepts only one comparation value.
         * @param field
         * @param comparation
         * @return
         */
        private Cursor _getPadFromDatabase( String field, String comparation ){
            Cursor c;
            String[] comparation_set = { comparation };

            // I have to use LIKE in order to query by ID. A mistery.
            c = contentResolver.query(
                    PadContentProvider.PADLIST_CONTENT_URI,
                    PadContentProvider.getPadFieldsList(),
                    field + " LIKE ?",
                    comparation_set, // AKA id
                    null
            );
            return c;
        }
        /**
         * Self explanatory name.
         * Just get all.
         * @return
         */
        private Cursor _getPadFromDatabase(){
            Cursor c;
            c = contentResolver.query(
                    PadContentProvider.PADLIST_CONTENT_URI,
                    PadContentProvider.getPadFieldsList(),
                    null,
                    null, // AKA id
                    null
            );
            return c;
        }

        /**
         * Queries the database and returns all pads
         * @return
         */
        public ArrayList<Pad> _getAllPad(){
            Cursor cursor = this._getPadFromDatabase();
            ArrayList<Pad> Pads = new ArrayList<>();

            if (cursor == null ) {
                return Pads;
            }
            if( cursor.getCount() == 0 ) {
                cursor.close();
                return Pads;
            }

            Pad Pad;
            cursor.moveToFirst();
            while (!cursor.isAfterLast())
            {
                // Goes to next by itself
                Pad = new Pad(cursor);
                Pads.add(Pad);
                cursor.moveToNext();
            }
            cursor.close();

            return Pads;
        }

        /**
         * Queries the database and compares to pad_id
         * @param pad_id
         * @return
         */
        public Cursor _getPadById( long pad_id ){
            return this._getPadFromDatabase( PadModel._ID, String.valueOf( pad_id ) );
        }

        /**
         * Queries the database and compares to padUrl
         * @param padUrl
         * @return
         */
        public Cursor _getPadByUrl(String padUrl){
            return this._getPadFromDatabase(PadModel.URL, padUrl);
        }

        public long getNowDate() {
            return PadContentProvider.getNowDate();
        }

        /**
         * Gets current pad data and saves the modified values (LAST_USED_DATE and ACCESS_COUNT).
         * I tried to optimize it in such way that there's no need to use _getPad, but it didn't work.
         * @param pad_id
         * @return
         */
        public void accessUpdate( long pad_id ){
            if( pad_id > 0 ) {
                Pad data = _getPad( pad_id );
                ContentValues values = new ContentValues();
                values.put( PadContentProvider.LAST_USED_DATE, getNowDate() );
                values.put( PadContentProvider.ACCESS_COUNT, (data.getAccessCount() + 1));
                String[] where_value = { String.valueOf(pad_id) };
                contentResolver.update(PadContentProvider.PADLIST_CONTENT_URI, values, PadContentProvider._ID + "=?", where_value);
            }
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

        public void close() {
            if( db.isOpen() ) {
                db.close();
            }
        }
    }

    /**
     * Retrieves a list of all hosts both from the XML default list
     * and the database.
     *
     * @return
     */
    protected String[] getServerWhiteList() {
        String[] server_list;
        // Load the custom servers
        ServerModel serverModel = new ServerModel(this);
        ArrayList<Server> custom_servers = serverModel.getEnabledServerList();
        ArrayList<String> server_names = new ArrayList<>();
        for(Server server : custom_servers) {
            try {
                URL url = new URL(server.getUrl());
                server_names.add(url.getHost());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        Collection<String> collection = new ArrayList<>();
        collection.addAll(server_names);
        collection.addAll(Arrays.asList(getResources().getStringArray( R.array.etherpad_servers_whitelist )));
        server_list = collection.toArray(new String[collection.size()]);

        return server_list;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (padlistDb != null) {
            padlistDb.close();
        }
    }
}
