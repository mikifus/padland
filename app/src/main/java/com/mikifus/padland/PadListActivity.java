/*
 * Copyleft PadLand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mikifus.padland;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import static android.support.v4.view.MotionEventCompat.getActionIndex;

/**
 * This activity displays a list of previously checked documents.
 * Here documents can be deleted via Intent.
 * It handles as well the sharing intent to the app.
 *
 * @author mikifus
 * @since 0.1
 */
public class PadListActivity extends PadLandDataActivity
    implements ActionMode.Callback,LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * mActionMode defines behaviour of the action-bar
     */
    protected ActionMode mActionMode;
    /**
     * Currently selected item (View) in the list
     */
    public View selectedItem = null;
    /**
     * The id of the selected pad
     * Default: -1 = none
     */
    public long selectedItem_id = -1;
    /**
     * The position of the selected item in the list
     * Default: -1 = none
     */
    public int selectedItem_position = -1;

    /**
     * Adapter to play with the listView
     */
    private SimpleCursorAdapter adapter = null;

    /**
     * Override onCreate
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padlist);

        // Intent
        this._textFromIntent();

        // Intent
        this._actionFromIntent();
        
        // Loader
        this.initLoader( (LoaderManager.LoaderCallbacks) this );
        
        // Init list view
        ListView lv = this._initListView();
        this._setListViewEvents( lv );

        // Get the data
        SimpleCursorAdapter data_adapter = this._getDataAdapter();

        // Bind to adapter.
        lv.setAdapter(data_adapter);
    }

    /**
     * If there is a share intent this function gets the extra text
     * and copies it into clipboard
     */
    private void _textFromIntent() {
        String extra_text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if(extra_text != null) {
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(extra_text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", extra_text);
                clipboard.setPrimaryClip(clip);
            }

            Toast.makeText(this, getString(R.string.activity_padlist_implicitintent_text_copied), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * If there is an intent with "action", here it is processed
     * For now there's only the delete action.
     */
    private void _actionFromIntent() {
        String action = getIntent().getStringExtra("action");
        long pad_id = getIntent().getLongExtra("pad_id", 0);
        if( action != null && pad_id > 0 ) {
            switch( action )
            {
                case "delete":
                    Log.d("DELETE_PAD_INTENT", action + String.valueOf(pad_id)  );
                    boolean result = deletePad(pad_id);
                    if( result ) {
                        Toast.makeText(this, getString(R.string.padlist_document_deleted), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    /**
     * When the list is empty a message with a button is shown.
     * This handles the button onClick.
     * @param view
     */
    public void onEmptyCreateNewClick( View view ) {
        Intent newPadIntent = new Intent(this, NewPadActivity.class);
        startActivity(newPadIntent);
    }

    /**
     * Makes an empty ListView and returns it.
     * @return ListView
     */
    private ListView _initListView(){
        final ListView lv = (ListView) findViewById(R.id.listView);
        lv.setTextFilterEnabled(true);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setEmptyView(findViewById(android.R.id.empty));
        return lv;
    }

    /**
     * This function adds events listeners for a ListView object to provide usage of the ActionBar
     * @param lv
     */
    private void _setListViewEvents(final ListView lv){
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mActionMode != null){
                    listItemSelect(view, position, id);
                    return;
                }
                // Clean selection
                lv.setItemChecked(position, false);

                Intent padViewIntent = new Intent(PadListActivity.this, PadInfoActivity.class);
                padViewIntent.putExtra("pad_id", id);

                startActivity(padViewIntent);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                listItemSelect(view, position, id);
                return true;
            }
        });
    }

    /**
     * Check an item and set is as selected.
     *
     * @param view
     * @param position
     * @param id
     */
    public void listItemSelect( View view, int position, long id )
    {
        Log.d("SELECTION", "NEW: pos:" + String.valueOf(position) + " id:" + String.valueOf(id) );

        if( selectedItem == null )
        {
            // Start the CAB using the ActionMode.Callback defined above
            PadListActivity.this.startActionMode(PadListActivity.this);
        }

        selectedItem = view;
        selectedItem_id = id;
        selectedItem_position = position;

        // Set selected
        ListView lv = (ListView) view.getParent();
        lv.setItemChecked(position, true);
    }

    /**
     * Gets an adapter for the listView with the contents from the database
     *
     * @return
     */
    private SimpleCursorAdapter _getDataAdapter(){
        Uri padlist_uri = Uri.parse( getString( R.string.request_padlist ) );
        Cursor c = getContentResolver().query(padlist_uri,
                new String[] { PadLandContentProvider._ID, PadLandContentProvider.NAME, PadLandContentProvider.URL },
                null,
                null,
                PadLandContentProvider.LAST_USED_DATE + " ASC");

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.padlist_item,
                c,     // Pass in the cursor to bind to.
                new String[] { PadLandContentProvider.NAME, PadLandContentProvider.URL }, // Array of cursor columns to bind to.
                new int[] { R.id.name, R.id.url },  // Parallel array of which template objects to bind to those columns.
                0);
        // Corrects the data and displays it okay if necessary
        /*adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Cursor cursor, int index) {
                if (index == cursor.getColumnIndex(PadLandContentProvider.LAST_USED_DATE)) {
                    // get a locale based string for the date
                    DateFormat formatter = android.text.format.DateFormat
                            .getDateFormat(getApplicationContext());
                    long date = cursor.getLong(index);
                    Date dateObj = new Date(date * 1000);
                    ((TextView) view).setText(formatter.format(dateObj));
                    return true;
                } else {
                    return false;
                }
            }
        });*/

        return adapter;
    }

    /**
     * Data loader initial event
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { PadLandContentProvider._ID, PadLandContentProvider.NAME, PadLandContentProvider.URL };
        CursorLoader cursorLoader = new CursorLoader(this, PadLandContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    /**
     * Data loader finish event
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    /**
     * Data loader event
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }

    /**
     * Called when the action mode is created; startActionMode() was called
     * @param mode
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.rowselection, menu);

        mActionMode = mode;

        return true;
    }

    /**
     * Called each time the action mode is shown. Always called after onCreateActionMode, but
     * may be called multiple times if the mode is invalidated.
     * @param mode
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }

    /**
     * Called when the user selects a contextual menu item
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_delete:
                AskDelete(selectedItem_id);
                // Action picked, so close the CAB
                mode.finish();
                return true;
            case R.id.menuitem_share:
                menu_share(selectedItem_id);
                // Action picked, so close the CAB
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    /**
     * Called when the user exits the action mode
     * @param mode
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d( "SELECTION", "unset_checked " + String.valueOf(selectedItem_position) );
        if( selectedItem != null )
        {
            ListView lv = (ListView) selectedItem.getParent();
            lv.setItemChecked( selectedItem_position, false );
        }

        mActionMode = null;
        selectedItem = null;
        selectedItem_id = -1;
        selectedItem_position = -1;
    }

    /**
     * backbutton event
     */
    public void onBackPressed(){
        onDestroyActionMode(mActionMode);
        super.onBackPressed();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        return super.onCreateOptionsMenu( menu, R.menu.pad_list );
    }
}