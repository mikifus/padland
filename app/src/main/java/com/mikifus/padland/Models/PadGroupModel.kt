package com.mikifus.padland.Models

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.mikifus.padland.PadContentProvider
import com.mikifus.padland.PadLandDataActivity
import com.mikifus.padland.PadlandApp
import com.mikifus.padland.R

/**
 * Created by mikifus on 28/02/18.
 */
class PadGroupModel(context: Context) : BaseModel(context) {
    private val contentResolver: ContentResolver
    override var context: Context? = null

    init {
        this.context = context
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
        var c: Cursor?
        val comparationSet = arrayOf(comparation)
        c = contentResolver.query(
                PadContentProvider.PADGROUPS_CONTENT_URI,
                PadContentProvider.padFieldsList,
                "$field=?",
                comparationSet,  // AKA id
                null
        )
        return c
    }

    protected fun _getPadgroupsData(): HashMap<Long, ArrayList<String>> {
        val padlistUri = Uri.parse(context!!.getString(R.string.request_padgroups))
        val cursor = contentResolver.query(padlistUri, arrayOf(PadContentProvider._ID, PadModel.NAME),
                null,
                null,
                PadContentProvider.CREATE_DATE + " DESC")
        val result = HashMap<Long, ArrayList<String>>()
        if (cursor == null || cursor.count == 0) {
            return result
        }
        val padData = HashMap<Long, ArrayList<String>>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val id = cursor.getLong(0)
            val name = cursor.getString(1)
            val padStrings = ArrayList<String>()
            padStrings.add(name)
            padData[id] = padStrings

            // do something
            cursor.moveToNext()
        }
        cursor.close()
        return padData
    }

    val padgroupsCount: Int
        get() {
            val padlistUri = Uri.parse(context!!.getString(R.string.request_padgroups))
            val cursor = contentResolver.query(padlistUri, arrayOf(PadContentProvider._ID, PadModel.NAME),
                    null,
                    null,
                    PadContentProvider.CREATE_DATE + " DESC")
            val count = cursor!!.count
            cursor.close()
            return count
        }

    fun getPadgroupAt(position: Int): HashMap<String, String> {
        val padlistUri = Uri.parse(context!!.getString(R.string.request_padgroups))
        val cursor = contentResolver.query(padlistUri, arrayOf(PadContentProvider._ID, PadModel.NAME, POSITION),
                "",
                null,
                PadContentProvider.CREATE_DATE + " DESC LIMIT " + position + ", 1")
        val group = HashMap<String, String>()
        cursor!!.moveToFirst()
        while (!cursor.isAfterLast) {
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val pos = cursor.getString(2)
            group[PadContentProvider._ID] = id
            group[PadModel.NAME] = name
            group[POSITION] = pos
            break
        }
        cursor.close()
        return group
    }

    private fun getPadGroupById(padGroupId: Long): PadGroup? {
        val padlistUri = Uri.parse(context!!.getString(R.string.request_padgroups))
        val cursor = contentResolver.query(padlistUri, arrayOf(PadContentProvider._ID, PadModel.NAME, POSITION),
                PadContentProvider._ID + "=?", arrayOf(padGroupId.toString()),
                "")

//        HashMap<String, String> group = new HashMap<>();

        val group: PadGroup? = if (cursor != null && cursor.count > 0) {
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
            val padlistUri = Uri.parse(context!!.getString(R.string.request_padgroups))
            val cursor = contentResolver.query(padlistUri, arrayOf(PadContentProvider._ID, PadModel.NAME, POSITION),
                    null,
                    null,
                    PadContentProvider.CREATE_DATE + " DESC")
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
        val query: String
        val values: Array<String>
        if (id == 0L) {
            query = "SELECT " + PadContentProvider._ID + " " +
                    "FROM " + PadContentProvider.PAD_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider._ID + " NOT IN (" +
                    "SELECT DISTINCT " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME + ") "
            values = arrayOf()
        } else {
            query = "SELECT DISTINCT " + PadContentProvider._ID_PAD + " " +
                    "FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider._ID_GROUP + "=? "
            values = arrayOf(id.toString())
        }
        val cursor = db.rawQuery(query, values)
        val padIds = ArrayList<Long>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val idPad = cursor.getLong(0)
            padIds.add(idPad)
            cursor.moveToNext()
        }
        cursor.close()
        return padIds
    }

    fun getPadgroupChildrenCount(id: Long): Int {
        val query: String
        val values: Array<String>
        if (id == 0L) {
            query = "SELECT " + PadContentProvider._ID + " " +
                    "FROM " + PadContentProvider.PAD_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider._ID + " NOT IN (" +
                    "SELECT " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME + ") "
            values = arrayOf()
        } else {
            query = "SELECT * FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                    "WHERE " + PadContentProvider._ID_GROUP + "=? "
            values = arrayOf(id.toString())
        }
        val cursor = db.rawQuery(query, values)
        val count = cursor.count
        cursor.close()
        return count
    }

    private fun getGroupId(padId: Long): Long {
        val query: String = "SELECT " + PadContentProvider.Companion._ID_GROUP + " FROM " + PadContentProvider.Companion.RELATION_TABLE_NAME + " " +
                "WHERE " + PadContentProvider.Companion._ID_PAD + "=? "
        val values: Array<String> = arrayOf(padId.toString())
        val cursor = db.rawQuery(query, values)
        var groupId: Long = 0
        if (cursor.count > 0) {
            cursor.moveToFirst()
            groupId = cursor.getLong(0)
        }
        cursor.close()
        return groupId
    }

    /**
     * Saves a new group if padgroup_id=0 or updates an existing one.
     * @param padgroupId
     * @param pad_id
     * @return
     */
    fun savePadgroupRelation(padgroupId: Long, pad_id: Long): Boolean {
        removePadFromAllGroups(pad_id)
        if (padgroupId == 0L) {
            return false
        }
        val contentValues = ContentValues()
        contentValues.put(PadContentProvider._ID_PAD, pad_id)
        contentValues.put(PadContentProvider._ID_GROUP, padgroupId)
        //            _debug_relations();
        return db.insert(PadContentProvider.RELATION_TABLE_NAME, null, contentValues) > 0
    }

    /**
     * Destroys all possible relation between a pad and any group
     * @param pad_id
     * @return
     */
    private fun removePadFromAllGroups(pad_id: Long): Boolean {
        val deleted = db.delete(PadContentProvider.RELATION_TABLE_NAME, PadContentProvider._ID_PAD + "=? ", arrayOf(pad_id.toString()))
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