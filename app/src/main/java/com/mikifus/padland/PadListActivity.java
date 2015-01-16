package com.mikifus.padland;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

public class PadListActivity extends PadLandActivity
    implements ActionMode.Callback,LoaderManager.LoaderCallbacks<Cursor> {

    protected ActionMode mActionMode;
    public View selectedItem = null;
    public long selectedItem_id = -1;
    public int selectedItem_position = -1;

    private SimpleCursorAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padlist);

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

        getLoaderManager().initLoader(0, null, this);

        final ListView lv = (ListView) findViewById(R.id.listView);
        lv.setTextFilterEnabled(true);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setEmptyView(findViewById(android.R.id.empty));

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mActionMode != null){
                    AdapterView.OnItemLongClickListener listener = lv.getOnItemLongClickListener();
                    listener.onItemLongClick(parent, view, position, id);
                    return;
                }
                Intent padViewIntent =
                        new Intent(PadListActivity.this, PadViewActivity.class);
                padViewIntent.putExtra("pad_id", id);

                startActivity(padViewIntent);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /*if (mActionMode != null) {
                    return false;
                }*/
                selectedItem = view;
                selectedItem_id = id;
                selectedItem_position = position;

                // Start the CAB using the ActionMode.Callback defined above
                PadListActivity.this.startActionMode(PadListActivity.this);
                //view.setSelected(true);
                lv.setItemChecked(position, true);
                return true;
            }
        });

        String PADLIST_REQUEST = "content://com.mikifus.padland.padlandcontentprovider/padlist";
        Uri padlist_uri = Uri.parse(PADLIST_REQUEST);
        Cursor c = getContentResolver().query(padlist_uri,
                new String[] {PadLandContentProvider._ID, PadLandContentProvider.NAME, PadLandContentProvider.URL, PadLandContentProvider.LAST_USED_DATE},
                null,
                null,
                PadLandContentProvider.LAST_USED_DATE + " ASC");

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.padlist_item,
                c,     // Pass in the cursor to bind to.
                new String[] {PadLandContentProvider.NAME, PadLandContentProvider.URL, PadLandContentProvider.LAST_USED_DATE}, // Array of cursor columns to bind to.
                new int[] {R.id.name, R.id.url, R.id.date},  // Parallel array of which template objects to bind to those columns.
                0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder(){
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
        }); // Corrects the data and displays it okay

        // Bind to our new adapter.
        lv.setAdapter(adapter);

    }

    private AlertDialog AskDelete(final long selectedItem_id)
    {
        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.sure_to_delete))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        menu_delete(selectedItem_id);
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

    private void menu_share(long selectedItem_id) {
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

    private void menu_delete(long selectedItem_id) {
        String[] ids = {String.valueOf(selectedItem_id)};
        getContentResolver().delete(PadLandContentProvider.CONTENT_URI,
                PadLandContentProvider._ID + "=?",
                ids);
    }

    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.rowselection, menu);

        mActionMode = mode;

        return true;
    }

    // Called each time the action mode is shown. Always called after
    // onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }

    // Called when the user selects a contextual menu item
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

    // Called when the user exits the action mode
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        ListView lv = (ListView) findViewById(R.id.listView);
        if(selectedItem_id > 0) {
            lv.setItemChecked(selectedItem_position, false);
            //selectedItem.setChecked(false);
        }

        mActionMode = null;
        selectedItem = null;
        selectedItem_id = -1;
        selectedItem_position = -1;
    }
    public void onBackPressed(){
        onDestroyActionMode(mActionMode);
        super.onBackPressed();
    }
/*
    public void onItemCheckedStateChanged(ActionMode mode,
                                          int position, long id, boolean checked) {
        ListView lv = (ListView) findViewById(R.id.listView);
        final int checkedCount = lv.getCheckedItemCount();
        switch (checkedCount) {
            case 0:
                mode.setSubtitle(null);
                break;
            case 1:
                mode.setSubtitle("One item selected");
                break;
            default:
                mode.setSubtitle("" + checkedCount + " items selected");
                break;
        }
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { PadLandContentProvider._ID, PadLandContentProvider.NAME, PadLandContentProvider.URL, PadLandContentProvider.LAST_USED_DATE };
        CursorLoader cursorLoader = new CursorLoader(this,
                PadLandContentProvider.CONTENT_URI, projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        adapter.swapCursor(null);
    }
}