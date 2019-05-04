package com.mikifus.padland;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.mikifus.padland.Models.Pad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This activity shows pad info like the last time it was used or when
 * it was created. It can be upgraded to show useful info.
 * Its menu as well allows to delete.
 */
public class PadInfoActivity extends PadLandDataActivity {

    private static final String TAG = "PadInfoActivity";

    long pad_id = 0;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad_info);

        pad_id = this._getPadId();

        if( pad_id <= 0 ){
            Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            return;
        }

        Pad pad_data = this._getPad( pad_id );

        // Action bar title
        getSupportActionBar().setTitle( pad_data.getLocalName() );

        SeparatedListAdapter adapter = this._doInfoList( pad_data );

        ListView list = (ListView) findViewById( R.id.listView );
        list.setAdapter(adapter);
    }

    /**
     * Takes the pad data and prepares a list with information,
     * then returns the adapter for the view.
     * @param pad_data
     * @return
     */
    private SeparatedListAdapter _doInfoList( Pad pad_data )
    {
        List<Map<String,?>> datalist = new LinkedList<>();

        datalist.add( this._doListItem( pad_data.getUrl(), getString(R.string.padinfo_pad_url) ) );
        datalist.add( this._doListItem( pad_data.getCreateDate(this), getString(R.string.padinfo_createdate) ) );
        datalist.add( this._doListItem( pad_data.getLastUsedDate(this), getString(R.string.padinfo_lastuseddate) ) );
        datalist.add( this._doListItem( String.valueOf(pad_data.getAccessCount() ), getString(R.string.padinfo_times_accessed) ) );

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        // create our list and custom adapter
        adapter.addSection(getString(R.string.padinfo_pad_info),
                new SimpleAdapter(this, datalist, R.layout.list_complex,
                        new String[] { "title", "caption" },
                        new int[] { R.id.list_complex_title, R.id.list_complex_caption }
                )
        );
        // I leave this here to show an example of how to show another list
        /*adapter.addSection("Options",
                new ArrayAdapter<String>(this, R.layout.list_item, new String[] { "Share", "Delete" })
        );*/

        return adapter;
    }

    /**
     * Makes a list item for the previous method.
     * @param title
     * @param caption
     * @return
     */
    private Map<String,?> _doListItem( String title, String caption )
    {
        Map<String,String> item = new HashMap<String,String>();
        item.put( "title", title );
        item.put( "caption", caption );
        return item;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pad_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Intent intent;

//        Log.d(TAG, "OptionsItemSelected pad_id: " + pad_id);
        final ArrayList<String> pad_list = new ArrayList<>();
        pad_list.add(String.valueOf(pad_id));

        switch( item.getItemId() ) {
            case R.id.menuitem_share:
                Log.d("MENU_SHARE", String.valueOf(pad_id) );
                menu_share( pad_list );
                break;
            case R.id.menuitem_copy:
                Log.d("MENU_SHARE", String.valueOf(pad_id) );
                menu_copy( pad_list );
                break;
            case R.id.menuitem_delete:
                Log.d("MENU_DELETE", String.valueOf(pad_id) );
                AskDelete( pad_list );
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Takes you to the padView activity
     * @param w
     */
    public void onViewButtonClick( View w ){
        Intent padViewIntent = new Intent( PadInfoActivity.this, PadViewActivity.class );
        padViewIntent.putExtra( "pad_id", pad_id );

        startActivity(padViewIntent);
    }
}
