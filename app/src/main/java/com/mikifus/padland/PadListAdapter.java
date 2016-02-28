package com.mikifus.padland;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikifus on 20/02/16.
 */
public class PadListAdapter extends BaseExpandableListAdapter {

    private static final String TAG = PadListAdapter.class.getSimpleName();
    /**
     * Multiple choice for all the groups
     */
    public static final int CHOICE_MODE_MULTIPLE = AbsListView.CHOICE_MODE_MULTIPLE;

    // TODO: Coverage this case
    // Example:
    //https://github.com/commonsguy/cw-omnibus/blob/master/ActionMode/ActionModeMC/src/com/commonsware/android/actionmodemc/ActionModeDemo.java
    public static final int CHOICE_MODE_MULTIPLE_MODAL = AbsListView.CHOICE_MODE_MULTIPLE_MODAL;

    /**
     * No child could be selected
     */
    public static final int CHOICE_MODE_NONE = AbsListView.CHOICE_MODE_NONE;

    /**
     * One single choice per group
     */
    public static final int CHOICE_MODE_SINGLE_PER_GROUP = AbsListView.CHOICE_MODE_SINGLE;

    /**
     * One single choice for all the groups
     */
    public static final int CHOICE_MODE_SINGLE_ABSOLUTE = 10001;

    private Context context;
    private ArrayList<HashMap<String, ArrayList>> group_data;
    private HashMap<Long, ArrayList<String>> pad_data;
    private LayoutInflater layoutInflater;

    private SparseArray<SparseBooleanArray> checkedPositions;

    // The default choice is the multiple one
    private int choiceMode = CHOICE_MODE_MULTIPLE;


    // Initialize constructor for array list
    public PadListAdapter(Context context,
                          ArrayList<HashMap<String, ArrayList>> group_data,
                          HashMap<Long, ArrayList<String>> pad_data, int choiceMode) {
        this(context, group_data, pad_data);
        // For now the choice mode CHOICE_MODE_MULTIPLE_MODAL
        // is not implemented
        if (choiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            throw new RuntimeException("The choice mode CHOICE_MODE_MULTIPLE_MODAL " +
                    "has not implemented yet");
        }
    }

    // Initialize constructor for array list
    public PadListAdapter(Context context,
                                ArrayList<HashMap<String, ArrayList>> group_data,
                                HashMap<Long, ArrayList<String>> pad_data) {
        this.context = context;
        this.group_data = group_data;
        this.pad_data = pad_data;
        layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        checkedPositions = new SparseArray<SparseBooleanArray>();
    }

    @Override
    public int getGroupCount() {
        return group_data.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return pad_data.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group_data.get(groupPosition).keySet().iterator().next();
    }


    @Override
    public ArrayList<String> getChild(int groupPosition, int childPosition) {
        return this.pad_data.get(getChildId(groupPosition, childPosition));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        HashMap group = this.group_data.get(groupPosition);
        ArrayList padlist = (ArrayList) group.get(group.keySet().iterator().next());
        if( padlist.size() < childPosition )
        {
            return 0;
        }
        return (long) padlist.get(childPosition);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_header, null);
        }

        TextView header = (TextView) convertView.findViewById(R.id.list_header_title);
//        lblListHeader.setTypeface(null, Typeface.BOLD);
        header.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.padlist_item, null);
        }
//        Log.d("PadListAdapter", convertView.findViewById(R.id.name).toString());

        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText( getChild(groupPosition, childPosition).get(0) );

        TextView url = (TextView) convertView.findViewById(R.id.url);
        url.setText( getChild(groupPosition, childPosition).get(1) );


        if (checkedPositions.get(groupPosition) != null) {
            Log.v(TAG, "\t \t The child checked position has been saved");
            boolean isChecked = checkedPositions.get(groupPosition).get(childPosition);
            Log.v(TAG, "\t \t \t Is child checked: " + isChecked);
//            ((CheckedTextView)convertView).setChecked(isChecked);
            // If it does not exist, mark the checkBox as false
        } else {
//            ((CheckedTextView)convertView).setChecked(false);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Update the list of the positions checked and update the view
     * @param groupPosition The position of the group which has been checked
     * @param childPosition The position of the child which has been checked
     */
    public void setClicked(int groupPosition, int childPosition) {
        switch (choiceMode) {
            case CHOICE_MODE_MULTIPLE:
                SparseBooleanArray checkedChildPositionsMultiple = checkedPositions.get(groupPosition);
                // if in the group there was not any child checked
                if (checkedChildPositionsMultiple == null) {
                    checkedChildPositionsMultiple = new SparseBooleanArray();
                    // By default, the status of a child is not checked
                    // So a click will enable it
                    checkedChildPositionsMultiple.put(childPosition, true);
                    checkedPositions.put(groupPosition, checkedChildPositionsMultiple);
                } else {
                    boolean oldState = checkedChildPositionsMultiple.get(childPosition);
                    checkedChildPositionsMultiple.put(childPosition, !oldState);
                }
                break;
            // TODO: Implement it
            case CHOICE_MODE_MULTIPLE_MODAL:
                throw new RuntimeException("The choice mode CHOICE_MODE_MULTIPLE_MODAL " +
                        "has not implemented yet");
            case CHOICE_MODE_NONE:
                checkedPositions.clear();
                break;
            case CHOICE_MODE_SINGLE_PER_GROUP:
                SparseBooleanArray checkedChildPositionsSingle = checkedPositions.get(groupPosition);
                // If in the group there was not any child checked
                if (checkedChildPositionsSingle == null) {
                    checkedChildPositionsSingle = new SparseBooleanArray();
                    // By default, the status of a child is not checked
                    checkedChildPositionsSingle.put(childPosition, true);
                    checkedPositions.put(groupPosition, checkedChildPositionsSingle);
                } else {
                    boolean oldState = checkedChildPositionsSingle.get(childPosition);
                    // If the old state was false, set it as the unique one which is true
                    if (!oldState) {
                        checkedChildPositionsSingle.clear();
                        checkedChildPositionsSingle.put(childPosition, !oldState);
                    } // Else does not allow the user to uncheck it
                }
                break;
            // This mode will remove all the checked positions from other groups
            // and enable just one from the selected group
            case CHOICE_MODE_SINGLE_ABSOLUTE:
                checkedPositions.clear();
                SparseBooleanArray checkedChildPositionsSingleAbsolute = new SparseBooleanArray();
                checkedChildPositionsSingleAbsolute.put(childPosition, true);
                checkedPositions.put(groupPosition, checkedChildPositionsSingleAbsolute);
                break;
        }

        // Notify that some data has been changed
        notifyDataSetChanged();
        Log.v(TAG, "List position updated");
//        Log.v(TAG, PrintSparseArrays.sparseArrayToString(checkedPositions));
    }

    public int getChoiceMode() {
        return choiceMode;
    }

    /**
     * Set a new choice mode. This will remove
     * all the checked positions
     * @param choiceMode
     */
    public void setChoiceMode(int choiceMode) {
        this.choiceMode = choiceMode;
        // For now the choice mode CHOICE_MODEL_MULTIPLE_MODAL
        // is not implemented
        if (choiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            throw new RuntimeException("The choice mode CHOICE_MODE_MULTIPLE_MODAL " +
                    "has not implemented yet");
        }
        checkedPositions.clear();
        Log.v(TAG, "The choice mode has been changed. Now it is " + this.choiceMode);
    }

    /**
     * Method used to get the actual state of the checked lists
     * @return The list of the all the positions checked
     */
    public SparseArray<SparseBooleanArray> getCheckedPositions() {
        return checkedPositions;
    }
}
