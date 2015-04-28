package com.mikifus.padland;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class PadInfoActivity extends PadLandDataActivity {
    long pad_id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pad_info);

        pad_id = this._getPadId();

        if( pad_id <= 0 ){
            Toast.makeText(this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            return;
        }

        padData pad_data = this._getPadData( pad_id );

        // Action bar title
        getActionBar().setTitle( pad_data.getName() );

        //TextView padname = (TextView) findViewById( R.id.padInfo );
        //padname.setText( pad_data.getUrl() );

        List<Map<String,?>> datalist = new LinkedList<>();

        datalist.add( this._doListItem( "Pad url", pad_data.getUrl() ) );
        datalist.add( this._doListItem( "Added to list", pad_data.getCreateDate() ) );
        datalist.add( this._doListItem( "Last viewed", pad_data.getLastUsedDate() ) );

        SeparatedListAdapter adapter = new SeparatedListAdapter(this);

        // create our list and custom adapter
        adapter.addSection(getString(R.string.padinfo_pad_info),
                new SimpleAdapter(this, datalist, R.layout.list_complex,
                        new String[] { "title", "caption" },
                        new int[] { R.id.list_complex_title, R.id.list_complex_caption }
                )
        );

        /*adapter.addSection("Options",
                new ArrayAdapter<String>(this, R.layout.list_item, new String[] { "Share", "Delete" })
        );*/

        ListView list = (ListView) findViewById( R.id.listView );
        list.setAdapter(adapter);
    }

    private Map<String,?> _doListItem( String title, String caption )
    {
        Map<String,String> item = new HashMap<String,String>();
        item.put( "title", title );
        item.put( "caption", caption );
        return item;
    }


    /**
     * Form submit
     * @param w
     */
    public void onViewButtonClick( View w ){
        Intent padViewIntent = new Intent( PadInfoActivity.this, PadViewActivity.class );
        padViewIntent.putExtra( "pad_id", pad_id );

        startActivity(padViewIntent);
    }

    /**
     * Creates the menu
     * @param menu
     * @return boolean
     */
    public boolean onCreateOptionsMenu( Menu menu ) {
        return super.onCreateOptionsMenu(menu, R.menu.pad_info);
    }
}
