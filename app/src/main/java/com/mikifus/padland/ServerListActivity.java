package com.mikifus.padland;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mikifus.padland.Adapters.ServerListAdapter;
import com.mikifus.padland.Dialog.FormDialog;
import com.mikifus.padland.Dialog.NewServerDialog;
import com.mikifus.padland.Models.ServerModel;

import java.util.ArrayList;

/**
 * Created by mikifus on 29/05/16.
 */
public class ServerListActivity extends PadLandDataActivity
        implements ActionMode.Callback, FormDialog.FormDialogCallBack {
    /**
     * Multiple choice for all the groups
     */
    private int choiceMode = ListView.CHOICE_MODE_MULTIPLE;

    /**
     * mActionMode defines behaviour of the action-bar
     */
    protected ActionMode mActionMode;

    ArrayAdapter mAdapter;
    ListView listView;

    private static final String NEW_SERVER_DIALOG = "dialog_new_server";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_list);
        mAdapter = new ServerListAdapter(ServerListActivity.this, R.layout.serverlist_item);
        listView = (ListView) findViewById(R.id.listView);
        if (listView != null) {
            listView.setChoiceMode(choiceMode);
            listView.setEmptyView(findViewById(android.R.id.empty));
            listView.setAdapter(mAdapter);
            this._setListViewEvents();
        }
    }

    /**
     * This function adds events listeners for a ListView object to provide usage of the ActionBar
     */
    private void _setListViewEvents() {
        // They look similar but they are different.
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                startActionMode();
                boolean checked = listView.isItemChecked(position);
                listView.setItemChecked(position, !checked);
                view.setSelected(!checked);
                if (listView.getCheckedItemCount() == 0) {
                    mActionMode.finish();
                }
                // Return true as we are handling the event.
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode == null) {
                    editServer( id );
                    listView.setItemChecked(position, false);
                    view.setSelected(false);
                } else {
                    boolean checked = listView.isItemChecked(position);
                    listView.setItemChecked(position, checked);
                    view.setSelected(checked);
                    if (listView.getCheckedItemCount() == 0) {
                        mActionMode.finish();
                    }
                }
                // Return true as we are handling the event.
                return;
            }
        });
    }

    public void onNewServerClick(View view) {
        FragmentManager fm = getSupportFragmentManager();
        NewServerDialog dialog = new NewServerDialog(getString(R.string.serverlist_dialog_new_server_title), this);
        dialog.show(fm, NEW_SERVER_DIALOG);
    }

    public void editServer(long id)  {
        FragmentManager fm = getSupportFragmentManager();
        NewServerDialog dialog = new NewServerDialog(getString(R.string.serverlist_dialog_edit_server_title), this);
        dialog.editServerId(id);
        dialog.show(fm, NEW_SERVER_DIALOG);
    }

    /**
     * Check an item and set is as selected.
     *
     */
    public void startActionMode() {
//        Log.d(TAG, "SELECTION NEW: pos:" + String.valueOf(position) + " id:" + String.valueOf(id));
//
        if (mActionMode == null) {
//            // Start the CAB using the ActionMode.Callback defined above
            this.startActionMode(this);
        }
    }

    /**
     * Called when the action mode is created; startActionMode() was called
     *
     * @param mode
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.server_list, menu);

        mActionMode = mode;

        return true;
    }

    /**
     * Called each time the action mode is shown. Always called after onCreateActionMode, but
     * may be called multiple times if the mode is invalidated.
     *
     * @param mode
     * @param menu
     * @return boolean
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }

    /**
     * Called when the user exits the action mode
     *
     * @param mode
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
        uncheckAllItems();
    }

    private void uncheckAllItems() {
        if( listView == null ) {
            return;
        }
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i)) {
                listView.setItemChecked(position, false);
            }
        }
    }

    /**
     * Called when the user selects a contextual menu item
     *
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_delete:
                AskDelete(getCheckedItemIds());
                // Action picked, so close the CAB
                mode.finish();
                return true;
//            case R.id.menuitem_share:
//                menu_share(getCheckedItemIds());
                // Action picked, so close the CAB
//                mode.finish();
//                return true;
            default:
                return false;
        }
    }

    private ArrayList<String> getCheckedItemIds()
    {
        ArrayList<String> selectedItems = new ArrayList<>();
//        HashMap<Long, ArrayList<String>> padlist_data = _getPadListData();

        SparseBooleanArray positions = listView.getCheckedItemPositions();
//        Log.d(TAG, "selectedItemsPositions: " + positions);
        for (int i = 0; i < positions.size(); ++i)
        {
            int position = positions.keyAt(i);
            if ( positions.valueAt(i) ) {
                selectedItems.add( String.valueOf(mAdapter.getItemId(position)) );
            }
        }
//        Log.d(TAG, "selectedItemsIds: " + selectedItems.toString());

        return selectedItems;
    }

    @Override
    public void onDialogDismiss() {

    }

    @Override
    public void onDialogSuccess() {
        mAdapter.notifyDataSetChanged();
    }


    /**
     * Asks the user to confirm deleting a server.
     * If confirmed the info will be deleted.
     *
     * @param selectedItems
     * @return AlertDialog
     */
    @Override
    public AlertDialog AskDelete(final ArrayList<String> selectedItems)
    {
        AlertDialog DeleteDialogBox = new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(getString(R.string.serverlist_dialog_delete_sure_to_delete))
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ServerModel serverModel = new ServerModel(getBaseContext());
                        for(int i = 0 ; i < selectedItems.size(); i++)
                        {
                            Log.d("DELETE_SERVER", "list_get: " + selectedItems.get(i));
                            boolean result = serverModel.deleteServer(Long.parseLong(selectedItems.get(i)));
                            if (result) {
                                Toast.makeText(getBaseContext(), getString(R.string.serverlist_dialog_delete_server_deleted), Toast.LENGTH_LONG).show();
                                mAdapter.notifyDataSetChanged();
                            }
                        }
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
}
