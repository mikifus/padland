package com.mikifus.padland.Adapters

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.mikifus.padland.Database.PadModel.PadViewModel
import com.mikifus.padland.Models.Pad
import com.mikifus.padland.Models.PadGroup
import com.mikifus.padland.Models.PadGroupModel
import com.mikifus.padland.PadLandDataActivity
import com.mikifus.padland.R

/**
 * Created by mikifus on 20/02/16.
 */
class PadListAdapter(private val context: PadLandDataActivity) : BaseExpandableListAdapter() {
    private val layoutInflater: LayoutInflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val checkedPositions: SparseArray<SparseBooleanArray?> = SparseArray()
    private val itemsPositions = HashMap<Long, Bundle>()
    private val padDatas: HashMap<Long, Pad>?
    private var groupDatas: ArrayList<PadGroup> = ArrayList()
    private val padGroupModel: PadGroupModel

    // The default choice is the multiple one
    private var choiceMode = CHOICE_MODE_MULTIPLE

    // Initialize constructor for array list
    init {
        padDatas = context._getPads()
        padGroupModel = PadGroupModel(context)
//        groupDatas = padGroupModel.getAllPadGroups()

        context.padGroupViewModel!!.getAll.observe(this.context, Observer { padGroups ->
            padGroups.forEach { groupDatas.add(PadGroup(it)) }
        })
    }

    override fun getGroupCount(): Int {
//        int count = context.padlistDb.getPadgroupsCount();
//        return count + 1; // Adds one in the end. The unclassified
        return groupDatas!!.size + 1
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        val group = getGroup(groupPosition)
        val id = group.id
        return padGroupModel.getPadgroupChildrenCount(id)
    }

    override fun getGroup(groupPosition: Int): PadGroup {
//        HashMap<String, String> group = context.padlistDb.getPadgroupAt(groupPosition);
        return if (groupDatas!!.size <= groupPosition) {
            unclassifiedGroup
        } else groupDatas[groupPosition]
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Pad {
        val id = getChildId(groupPosition, childPosition)
        return padDatas!![id]!!
    }

    override fun getGroupId(groupPosition: Int): Long {
        val group = getGroup(groupPosition)
        return if (group.id == 0L) {
            getUnclassifiedGroupId(groupPosition)
        } else group.id
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        val group = getGroup(groupPosition)
        val id = group.id
        val padlist: ArrayList<*>? = if (id == 0L) {
//            padlist = getUnclassifiedGroupChildList(groupPosition);
            padGroupModel.getPadgroupChildrenIds(0)
        } else {
            padGroupModel.getPadgroupChildrenIds(id)
        }
        return if (padlist?.isEmpty() == true) {
            0
        } else padlist!![childPosition] as Long
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        val holder: GroupViewHolder
        val group = getGroup(groupPosition)
        if(view == null) {
            view = layoutInflater.inflate(R.layout.padlist_header, null)
            holder = GroupViewHolder(view);
            holder.name = view.findViewById(R.id.list_header_title) as TextView
            view.tag = holder
        } else {
            holder = view.tag as GroupViewHolder
        }
        holder.name?.text = group.name

        return view as View
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        val holder: ChildViewHolder
//        val group = getGroup(groupPosition)
        if(view == null) {
            view = layoutInflater.inflate(R.layout.padlist_item, null)
            holder = ChildViewHolder(view);
            holder.name = view.findViewById(R.id.name) as TextView
            holder.url = view.findViewById(R.id.url) as TextView
            view.tag = holder
        } else {
            holder = view.tag as ChildViewHolder
        }

        val child = getChild(groupPosition, childPosition)
        val positionBundle = Bundle()
        positionBundle.putInt("groupPosition", groupPosition)
        positionBundle.putInt("childPosition", childPosition)
        itemsPositions[child.id] = positionBundle
        holder.name?.text = child.localName
        holder.url?.text = child.url
        if (checkedPositions[groupPosition] != null) {
            Log.v(TAG, "\t \t The child checked position has been saved")
            val isChecked = checkedPositions[groupPosition]!![childPosition]
            Log.v(TAG, "\t \t \t Is child checked: $isChecked")
            //            ((CheckedTextView)convertView).setChecked(isChecked);
            // If it does not exist, mark the checkBox as false
        } else {
//            ((CheckedTextView)convertView).setChecked(false);
        }
        return view as View
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    /**
     * Update the list of the positions checked and update the view
     * @param groupPosition The position of the group which has been checked
     * @param childPosition The position of the child which has been checked
     */
    fun setClicked(groupPosition: Int, childPosition: Int) {
        when (choiceMode) {
            CHOICE_MODE_MULTIPLE -> {
                var checkedChildPositionsMultiple = checkedPositions[groupPosition]
                // if in the group there was not any child checked
                if (checkedChildPositionsMultiple == null) {
                    checkedChildPositionsMultiple = SparseBooleanArray()
                    // By default, the status of a child is not checked
                    // So a click will enable it
                    checkedChildPositionsMultiple.put(childPosition, true)
                    checkedPositions.put(groupPosition, checkedChildPositionsMultiple)
                } else {
                    val oldState = checkedChildPositionsMultiple[childPosition]
                    checkedChildPositionsMultiple.put(childPosition, !oldState)
                }
            }

            CHOICE_MODE_MULTIPLE_MODAL -> throw RuntimeException("The choice mode CHOICE_MODE_MULTIPLE_MODAL " +
                    "has not implemented yet")

            CHOICE_MODE_NONE -> checkedPositions.clear()
            CHOICE_MODE_SINGLE_PER_GROUP -> {
                var checkedChildPositionsSingle = checkedPositions[groupPosition]
                // If in the group there was not any child checked
                if (checkedChildPositionsSingle == null) {
                    checkedChildPositionsSingle = SparseBooleanArray()
                    // By default, the status of a child is not checked
                    checkedChildPositionsSingle.put(childPosition, true)
                    checkedPositions.put(groupPosition, checkedChildPositionsSingle)
                } else {
                    val oldState = checkedChildPositionsSingle[childPosition]
                    // If the old state was false, set it as the unique one which is true
                    if (!oldState) {
                        checkedChildPositionsSingle.clear()
                        checkedChildPositionsSingle.put(childPosition, !oldState)
                    } // Else does not allow the user to uncheck it
                }
            }

            CHOICE_MODE_SINGLE_ABSOLUTE -> {
                checkedPositions.clear()
                val checkedChildPositionsSingleAbsolute = SparseBooleanArray()
                checkedChildPositionsSingleAbsolute.put(childPosition, true)
                checkedPositions.put(groupPosition, checkedChildPositionsSingleAbsolute)
            }
        }

        // Notify that some data has been changed
        notifyDataSetChanged()
        Log.v(TAG, "List position updated")
        //        Log.v(TAG, PrintSparseArrays.sparseArrayToString(checkedPositions));
    }

    fun getChoiceMode(): Int {
        return choiceMode
    }

    /**
     * Set a new choice mode. This will remove
     * all the checked positions
     * @param choiceMode
     */
    fun setChoiceMode(choiceMode: Int) {
        this.choiceMode = choiceMode
        // For now the choice mode CHOICE_MODEL_MULTIPLE_MODAL
        // is not implemented
        if (choiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            throw RuntimeException("The choice mode CHOICE_MODE_MULTIPLE_MODAL " +
                    "has not implemented yet")
        }
        checkedPositions.clear()
        Log.v(TAG, "The choice mode has been changed. Now it is " + this.choiceMode)
    }

    //        HashMap<String, String> group_deal = new HashMap<>();
//        group_deal.put(PadContentProvider._ID, "0");
//        group_deal.put(PadContentProvider.NAME, context.getString(R.string.padlist_group_unclassified_name));
//        return group_deal;
    private val unclassifiedGroup: PadGroup
        get() = PadGroup(context)

    //        HashMap<String, String> group_deal = new HashMap<>();
//        group_deal.put(PadContentProvider._ID, "0");
//        group_deal.put(PadContentProvider.NAME, context.getString(R.string.padlist_group_unclassified_name));
//        return group_deal;
    private fun getUnclassifiedGroupId(groupPosition: Int): Long {
        return 0
    }

    fun getPosition(pad_id: Long): Bundle? {
        return itemsPositions[pad_id]
    }

    private inner class GroupViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var name: TextView? = null
    }

    private inner class ChildViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var name: TextView? = null
        var url: TextView? = null
    }

    companion object {
        private val TAG = PadListAdapter::class.java.simpleName

        /**
         * Multiple choice for all the groups
         */
        const val CHOICE_MODE_MULTIPLE = AbsListView.CHOICE_MODE_MULTIPLE

        // TODO: Coverage this case
        // Example:
        //https://github.com/commonsguy/cw-omnibus/blob/master/ActionMode/ActionModeMC/src/com/commonsware/android/actionmodemc/ActionModeDemo.java
        const val CHOICE_MODE_MULTIPLE_MODAL = AbsListView.CHOICE_MODE_MULTIPLE_MODAL

        /**
         * No child could be selected
         */
        const val CHOICE_MODE_NONE = AbsListView.CHOICE_MODE_NONE

        /**
         * One single choice per group
         */
        const val CHOICE_MODE_SINGLE_PER_GROUP = AbsListView.CHOICE_MODE_SINGLE

        /**
         * One single choice for all the groups
         */
        const val CHOICE_MODE_SINGLE_ABSOLUTE = 10001
    }
}