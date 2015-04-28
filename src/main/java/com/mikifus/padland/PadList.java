package com.mikifus.padland;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class PadList extends PadLandActivity
        implements ActionMode.Callback {

        protected Object mActionMode;
        public int selectedItem = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_padlist);

        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setTextFilterEnabled(true);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Toast.makeText(getApplicationContext(),
                        "" + position, Toast.LENGTH_SHORT).show();


                String padName = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                String padUrl = ((TextView) view.findViewById(android.R.id.text2)).getText().toString();

                if (padName == null || padName.isEmpty()) {
                    Log.w("Warning", "The info for this pad is corrupted.");
                    return;
                }

                Intent padViewIntent =
                        new Intent(PadList.this, padView.class);
                padViewIntent.putExtra("padName", padName);
                padViewIntent.putExtra("padUrl", padUrl);

                startActivity(padViewIntent);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Start the CAB using the ActionMode.Callback defined above
                //PadList.this.startActionMode(PadList.this);
                view.setSelected(true);
                return true;
            }
        });

        // Take our saved pad list from XML and populate the View
        PadlandApp context = ((PadlandApp)getApplication());
        Map<Integer, Map<String, String>> padList = context.getPadList();

        // This iteration cleans corrupted pad data in order to generate a clean menu
        for(Map.Entry<Integer, Map<String, String>> element_entry : padList.entrySet()){
            Map<String, String> element = element_entry.getValue();
            if(element.containsKey(null) || (element.get("name") == null)){
                padList.remove(element_entry);
            }
        }

        // Populate
        final ArrayList<Map.Entry<Integer,Map<String, String>>> cursor = new ArrayList<Map.Entry<Integer, Map<String, String>>>();
        cursor.addAll(padList.entrySet());

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter arrayAdapter = new ArrayAdapter (context, android.R.layout.simple_list_item_2, android.R.id.text1, cursor)
        {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Map.Entry element = (Map.Entry) cursor.get(position);
                Map<String, String> element_value = (Map<String, String>) element.getValue();

                /*if(element_value.get("url")== null || element_value.get("url").isEmpty()){
                    Log.w("Warning", "The info for this pad is corrupted. Jumping to next one.");
                    return null;
                }*/

                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                text1.setText(element_value.get("name"));
                text2.setText(element_value.get("url"));

                return view;
            }
        };

        lv.setAdapter(arrayAdapter);

    }

    private void show() {
        Toast.makeText(PadList.this, String.valueOf(selectedItem), Toast.LENGTH_LONG).show();
    }

    // Called when the action mode is created; startActionMode() was called
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        // Assumes that you have "contexual.xml" menu resources
        inflater.inflate(R.menu.rowselection, menu);
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
            case R.id.menuitem1_show:
                show();
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
        mActionMode = null;
        selectedItem = -1;
    }
}