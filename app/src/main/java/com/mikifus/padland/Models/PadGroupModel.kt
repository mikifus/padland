package com.mikifus.padland.Models

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.mikifus.padland.PadContentProvider
import com.mikifus.padland.R

/**
 * Created by mikifus on 28/02/18.
 */
class PadGroupModel(context: Context?) : BaseModel(context) {
    private val contentResolver: ContentResolver
    private override val context: Context

    init {
        this.context = context!!
        contentResolver = context.contentResolver
    }

    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field
     * @param comparation
     * @return
     */
    fun _getPadgroupsDataFromDatabase(field: String, comparation: String): Cursor? {
        var c: Cursor? = null
        val comparation_set = arrayOf(comparation)
        c = contentResolver.query(
                PadContentProvider.Companion.PADGROUPS_CONTENT_URI,
                PadContentProvider.Companion.getPadFieldsList(),
                "$field=?",
                comparation_set,  // AKA id
                null
        )
        return c
    }

    protected fun _getPadgroupsData(): HashMap<Long, ArrayList<String>> {
        val padlist_uri = Uri.parse(context.getString(R.string.request_padgroups))
        val cursor = contentResolver.query(padlist_uri, arrayOf<String>(PadContentProvider.Companion._ID, PadModel.Companion.NAME),
                null,
                null,
                PadContentProvider.Companion.CREATE_DATE + " DESC")
        val result = HashMap<Long, ArrayList<String>>()
        if (cursor == null || cursor.count == 0) {
            return result
        }
        val pad_data = HashMap<Long, ArrayList<String>>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val id = cursor.getLong(0)
            val name = cursor.getString(1)
            val pad_strings = ArrayList<String>()
            pad_strings.add(name)
            pad_data[id] = pad_strings

            // do something
            cursor.moveToNext()
        }
        cursor.close()
        return pad_data
    }

    val padgroupsCount: Int
        get() {
            val padlist_uri = Uri.parse(context.getString(R.string.request_padgroups))
            val cursor = contentResolver.query(padlist_uri, arrayOf<String>(PadContentProvider.Companion._ID, PadModel.Companion.NAME),
                    null,
                    null,
                    PadContentProvider.Companion.CREATE_DATE + " DESC")
            val count = cursor!!.count
            cursor.close()
            return count
        }

    fun getPadgroupAt(position: Int): HashMap<String, String> {
        val padlist_uri = Uri.parse(context.getString(R.string.request_padgroups))
        val cursor = contentResolver.query(padlist_uri, arrayOf<String>(PadContentProvider.Companion._ID, PadModel.Companion.NAME, POSITION),
                "",
                null,
                PadContentProvider.Companion.CREATE_DATE + " DESC LIMIT " + position + ", 1")
        val group = HashMap<String, String>()
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val pos = cursor.getString(2)
            group[PadContentProvider.Companion._ID] = id
            group[PadModel.Companion.NAME] = name
            group[POSITION] = pos
            break
        }
        cursor.close()
        return group
    }

    fun getPadGroupById(padGroupId: Long): PadGroup? {
        val padlist_uri = Uri.parse(context.getString(R.string.request_padgroups))
        val cursor = contentResolver.query(padlist_uri, arrayOf<String>(PadContentProvider.Companion._ID, PadModel.Companion.NAME, POSITION),
                PadContentProvider.Companion._ID + "=?", arrayOf<String>(java.lang.Long.toString(padGroupId)),
                "")

//        HashMap<String, String> group = new HashMap<>();
        val group: PadGroup?
        group = if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            PadGroup(cursor)
        } else {
            null
        }
        //        while (!cursor.isAfterLast())
//        {
//            String id = cursor.getString(0);
//            String name = cursor.getString(1);
//            String pos = cursor.getString(2);
//
//            group.put(PadContentProvider._ID, id);
//            group.put(PadContentProvider.NAME, name);
//            group.put(PadGroupModel.POSITION, pos);
//
//            break;
//        }
        cursor!!.close()
        return group
    }
    //            String id = cursor.getString(0);
//            String name = cursor.getString(1);
//            String pos = cursor.getString(2);

    //            group = new HashMap<>();
//            group.put(PadContentProvider._ID, id);
//            group.put(PadContentProvider.NAME, name);
//            group.put(PadGroupModel.POSITION, pos);
//            groups.add(group);
    val allPadgroups: ArrayList<PadGroup>
        get() {
            val padlist_uri = Uri.parse(context.getString(R.string.request_padgroups))
            val cursor = contentResolver.query(padlist_uri, arrayOf<String>(PadContentProvider.Companion._ID, PadModel.Companion.NAME, POSITION),
                    null,
                    null,
                    PadContentProvider.Companion.CREATE_DATE + " DESC")
            val groups = ArrayList<PadGroup>()
            var group: PadGroup
            if (cursor == null) {
                return groups
            }
            if (cursor.count == 0) {
                cursor.close()
                return groups
            }
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                group = PadGroup(cursor)
                groups.add(group)

//            String id = cursor.getString(0);
//            String name = cursor.getString(1);
//            String pos = cursor.getString(2);

//            group = new HashMap<>();
//            group.put(PadContentProvider._ID, id);
//            group.put(PadContentProvider.NAME, name);
//            group.put(PadGroupModel.POSITION, pos);
//            groups.add(group);
                cursor.moveToNext()
            }
            cursor.close()
            return groups
        }

    fun getPadgroupChildrenIds(id: Long): ArrayList<Long> {
        val QUERY: String
        val values: Array<String>
        if (id == 0L) {
            QUERY = "SELECT " + PadContentProvider.Companion._ID + " " +
                    "FROM " + PadContentProvider.Companion.PAD_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider.Companion._ID + " NOT IN (" +
                    "SELECT DISTINCT " + PadContentProvider.Companion._ID_PAD + " FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME + ") "
            values = arrayOf()
        } else {
            QUERY = "SELECT DISTINCT " + PadContentProvider.Companion._ID_PAD + " " +
                    "FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider.Companion._ID_GROUP + "=? "
            values = arrayOf(id.toString())
        }
        val cursor = db.rawQuery(QUERY, values)
        val pad_ids = ArrayList<Long>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val id_pad = cursor.getLong(0)
            pad_ids.add(id_pad)
            cursor.moveToNext()
        }
        cursor.close()
        return pad_ids
    }

    fun getPadgroupChildrenCount(id: Long): Int {
        val QUERY: String
        val values: Array<String>
        if (id == 0L) {
            QUERY = "SELECT " + PadContentProvider.Companion._ID + " " +
                    "FROM " + PadContentProvider.Companion.PAD_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider.Companion._ID + " NOT IN (" +
                    "SELECT " + PadContentProvider.Companion._ID_PAD + " FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME + ") "
            values = arrayOf()
        } else {
            QUERY = "SELECT * FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider.Companion._ID_GROUP + "=? "
            values = arrayOf(id.toString())
        }
        val cursor = db.rawQuery(QUERY, values)
        val count = cursor.count
        cursor.close()
        return count
    }

    fun getGroupId(padId: Long): Long {
        val QUERY: String
        val values: Array<String>
        QUERY = "SELECT " + PadContentProvider.Companion._ID_GROUP + " FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME + " " +
                "WHERE " + PadContentProvider.Companion._ID_PAD + "=? "
        values = arrayOf(padId.toString())
        val cursor = db.rawQuery(QUERY, values)
        var groupid: Long = 0
        if (cursor.count > 0) {
            cursor.moveToFirst()
            groupid = cursor.getLong(0)
        }
        cursor.close()
        return groupid
    }

    /**
     * Saves a new group if padgroup_id=0 or updates an existing one.
     * @param padgroup_id
     * @param pad_id
     * @return
     */
    fun savePadgroupRelation(padgroup_id: Long, pad_id: Long): Boolean {
        removePadFromAllGroups(pad_id)
        if (padgroup_id == 0L) {
            return false
        }
        val contentValues = ContentValues()
        contentValues.put(PadContentProvider.Companion._ID_PAD, pad_id)
        contentValues.put(PadContentProvider.Companion._ID_GROUP, padgroup_id)
        //            _debug_relations();
        return db.insert(PadContentProvider.Companion.RELATION_TABLE_NAME, null, contentValues) > 0
    }

    /**
     * Destroys all possible relation between a pad and any group
     * @param pad_id
     * @return
     */
    fun removePadFromAllGroups(pad_id: Long): Boolean {
        val deleted = db.delete(PadContentProvider.Companion.RELATION_TABLE_NAME, PadContentProvider.Companion._ID_PAD + "=? ", arrayOf<String>(pad_id.toString()))
        return deleted > 0
    }

    fun getPadGroup(padId: Long): PadGroup? {
        return getPadGroupById(getGroupId(padId))
    }

    val unclassifiedPadGroup: PadGroup
        get() = PadGroup(context)

    companion object {
        const val TAG = "PadGroupModel"
        const val POSITION = "position" // Position inside a sortable data set
    }
}