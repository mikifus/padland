package com.mikifus.padland.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mikifus.padland.Models.Pad;
import com.mikifus.padland.Models.PadGroup;
import com.mikifus.padland.Models.PadGroupModel;
import com.mikifus.padland.PadLandDataActivity;
import com.mikifus.padland.R;

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

    private PadLandDataActivity context;
    private LayoutInflater layoutInflater;

    private SparseArray<SparseBooleanArray> checkedPositions;
    private HashMap<Long, Bundle> items_positions = new HashMap<>();
    private HashMap<Long, Pad> padDatas;
    private ArrayList<PadGroup> groupDatas;
    private PadGroupModel padGroupModel;

    // The default choice is the multiple one
    private int choiceMode = CHOICE_MODE_MULTIPLE;

    // Initialize constructor for array list
    public PadListAdapter(PadLandDataActivity context) {
        this.context = context;
        layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        checkedPositions = new SparseArray<>();
        padDatas = context._getPads();
        padGroupModel = new PadGroupModel(context);
        groupDatas = padGroupModel.getAllPadgroups();
    }

    @Override
    public int getGroupCount() {
//        int count = context.padlistDb.getPadgroupsCount();
//        return count + 1; // Adds one in the end. The unclassified
        return groupDatas.size() + 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        PadGroup group = getGroup(groupPosition);
        long id = group.getId();
        int count = padGroupModel.getPadgroupChildrenCount(id);
        return count;
    }

    @Override
    public PadGroup getGroup(int groupPosition) {
//        HashMap<String, String> group = context.padlistDb.getPadgroupAt(groupPosition);
        if( groupDatas.size() <= groupPosition || groupDatas.get(groupPosition) == null ) {
            return getUnclassifiedGroup();
        }
        return groupDatas.get(groupPosition);
    }


    @Override
    public Pad getChild(int groupPosition, int childPosition) {
        long id = getChildId(groupPosition, childPosition);
        return padDatas.get(id);
    }

    @Override
    public long getGroupId(int groupPosition) {
        PadGroup group = getGroup(groupPosition);
        if( group.getId() == 0 ) {
            return getUnclassifiedGroupId(groupPosition);
        }
        return group.getId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        PadGroup group = getGroup(groupPosition);
        long id = group.getId();
        ArrayList padlist;
        if( id == 0 ) {
//            padlist = getUnclassifiedGroupChildList(groupPosition);
            padlist = padGroupModel.getPadgroupChildrenIds(0);
        } else {
            padlist = padGroupModel.getPadgroupChildrenIds(id);
        }
        if( padlist.size() == 0 )
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
        PadGroup group = getGroup(groupPosition);
        GroupViewHolder holder;
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.padlist_header, null);

            holder = new GroupViewHolder(convertView);
            holder.name = (TextView) convertView.findViewById(R.id.list_header_title);
            convertView.setTag(holder);
        } else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.name.setText(group.getName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.padlist_item, null);

            holder = new ChildViewHolder(convertView);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.url = (TextView) convertView.findViewById(R.id.url);
            convertView.setTag(holder);
        } else {
            holder = (ChildViewHolder) convertView.getTag();
        }

        Pad child = getChild(groupPosition, childPosition);

        Bundle position_bundle = new Bundle();
        position_bundle.putInt("groupPosition", groupPosition);
        position_bundle.putInt("childPosition", childPosition);
        items_positions.put(child.getId(), position_bundle);

        holder.name.setText(child.getLocalName());
        holder.url.setText( child.getUrl() );

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

    private PadGroup getUnclassifiedGroup() {
        return new PadGroup(context);
//        HashMap<String, String> group_deal = new HashMap<>();
//        group_deal.put(PadContentProvider._ID, "0");
//        group_deal.put(PadContentProvider.NAME, context.getString(R.string.padlist_group_unclassified_name));
//        return group_deal;
    }

    private long getUnclassifiedGroupId(int groupPosition) {
        return 0;
    }

    public Bundle getPosition(long pad_id) {
        return items_positions.get(pad_id);
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView name;

        public GroupViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class ChildViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView url;

        public ChildViewHolder(View itemView) {
            super(itemView);
        }
    }
}
