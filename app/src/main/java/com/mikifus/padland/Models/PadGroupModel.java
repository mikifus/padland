package com.mikifus.padland.Models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.mikifus.padland.PadContentProvider;
import com.mikifus.padland.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikifus on 28/02/18.
 */

public class PadGroupModel extends BaseModel {
    public static final String TAG = "PadGroupModel";

    public static final String POSITION = "position"; // Position inside a sortable data set

    private ContentResolver contentResolver;
    private Context context;

    public PadGroupModel(Context context) {
        super(context);
        this.context = context;
        contentResolver = context.getContentResolver();
    }


    /**
     * Self explanatory name.
     * Field to compare must be specified by its identifier. Accepts only one comparation value.
     * @param field
     * @param comparation
     * @return
     */
    public Cursor _getPadgroupsDataFromDatabase( String field, String comparation ){
        Cursor c = null;
        String[] comparation_set = new String[]{ comparation };

        c = contentResolver.query(
                PadContentProvider.PADGROUPS_CONTENT_URI,
                PadContentProvider.getPadFieldsList(),
                field + "=?",
                comparation_set, // AKA id
                null
        );
        return c;
    }

    protected HashMap<Long, ArrayList<String>> _getPadgroupsData()
    {
        Uri padlist_uri = Uri.parse(context.getString(R.string.request_padgroups));
        Cursor cursor = contentResolver.query(padlist_uri,
                new String[]{PadContentProvider._ID, PadModel.NAME},
                null,
                null,
                PadContentProvider.CREATE_DATE + " DESC");

        HashMap<Long, ArrayList<String>> result = new HashMap<>();

        if (cursor == null || cursor.getCount() == 0) {
            return result;
        }

        HashMap<Long, ArrayList<String>> pad_data = new HashMap<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            long id = cursor.getLong(0);
            String name = cursor.getString(1);

            ArrayList<String> pad_strings = new ArrayList<String>();
            pad_strings.add(name);

            pad_data.put(id, pad_strings);

            // do something
            cursor.moveToNext();
        }
        cursor.close();

        return pad_data;
    }

    public int getPadgroupsCount() {
        Uri padlist_uri = Uri.parse(context.getString(R.string.request_padgroups));
        Cursor cursor = contentResolver.query(padlist_uri,
                new String[]{PadContentProvider._ID, PadModel.NAME},
                null,
                null,
                PadContentProvider.CREATE_DATE + " DESC");

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public HashMap<String, String> getPadgroupAt(int position) {
        Uri padlist_uri = Uri.parse(context.getString(R.string.request_padgroups));
        Cursor cursor = contentResolver.query(padlist_uri,
                new String[]{PadContentProvider._ID, PadModel.NAME, PadGroupModel.POSITION},
                "",
                null,
                PadContentProvider.CREATE_DATE + " DESC LIMIT " + position + ", 1");

        HashMap<String, String> group = new HashMap<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            String pos = cursor.getString(2);

            group.put(PadContentProvider._ID, id);
            group.put(PadModel.NAME, name);
            group.put(PadGroupModel.POSITION, pos);

            break;
        }
        cursor.close();

        return group;
    }

    public PadGroup getPadGroupById(long padGroupId) {
        Uri padlist_uri = Uri.parse(context.getString(R.string.request_padgroups));
        Cursor cursor = contentResolver.query(padlist_uri,
                new String[]{PadContentProvider._ID, PadModel.NAME, PadGroupModel.POSITION},
                PadContentProvider._ID + "=?",
                new String[]{Long.toString(padGroupId)},
                "");

//        HashMap<String, String> group = new HashMap<>();
        PadGroup group;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            group = new PadGroup(cursor);
        } else {
            group = null;
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
        cursor.close();

        return group;
    }

    public ArrayList<PadGroup> getAllPadgroups() {
        Uri padlist_uri = Uri.parse(context.getString(R.string.request_padgroups));
        Cursor cursor = contentResolver.query(padlist_uri,
                new String[]{PadContentProvider._ID, PadModel.NAME, PadGroupModel.POSITION},
                null,
                null,
                PadContentProvider.CREATE_DATE + " DESC");

        ArrayList<PadGroup> groups = new ArrayList<>();
        PadGroup group;
        if( cursor == null ) {
            return groups;
        }
        if( cursor.getCount() == 0 ) {
            cursor.close();
            return groups;
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            group = new PadGroup(cursor);
            groups.add(group);

//            String id = cursor.getString(0);
//            String name = cursor.getString(1);
//            String pos = cursor.getString(2);

//            group = new HashMap<>();
//            group.put(PadContentProvider._ID, id);
//            group.put(PadContentProvider.NAME, name);
//            group.put(PadGroupModel.POSITION, pos);
//            groups.add(group);

            cursor.moveToNext();
        }
        cursor.close();

        return groups;
    }

    public ArrayList<Long> getPadgroupChildrenIds(long id) {
        String QUERY;
        String[] values;
        if( id == 0 ) {
            QUERY =
                    "SELECT " + PadContentProvider._ID + " " +
                            "FROM " + PadContentProvider.PAD_TABLE_NAME + " " +
                            "WHERE " + PadContentProvider._ID + " NOT IN (" +
                            "SELECT DISTINCT " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME +
                            ") ";
            values = new String[]{};
        } else {
            QUERY =
                    "SELECT DISTINCT " + PadContentProvider._ID_PAD + " " +
                            "FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                            "WHERE " + PadContentProvider._ID_GROUP + "=? ";
            values = new String[]{String.valueOf(id)};
        }
        Cursor cursor = db.rawQuery(QUERY, values);


        ArrayList<Long> pad_ids = new ArrayList<>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            long id_pad = cursor.getLong(0);
            pad_ids.add(id_pad);
            cursor.moveToNext();
        }
        cursor.close();

        return pad_ids;
    }

    public int getPadgroupChildrenCount(long id) {
        String QUERY;
        String[] values;
        if( id == 0 ) {
            QUERY =
                    "SELECT " + PadContentProvider._ID + " " +
                            "FROM " + PadContentProvider.PAD_TABLE_NAME + " " +
                            "WHERE " + PadContentProvider._ID + " NOT IN (" +
                            "SELECT " + PadContentProvider._ID_PAD + " FROM " + PadContentProvider.RELATION_TABLE_NAME +
                            ") ";
            values = new String[]{};
        } else {
            QUERY =
                    "SELECT * FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                            "WHERE " + PadContentProvider._ID_GROUP + "=? ";
            values = new String[]{String.valueOf(id)};
        }

        Cursor cursor = db.rawQuery(QUERY, values);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public long getGroupId(long padId) {
        String QUERY;
        String[] values;
        QUERY =
                "SELECT " + PadContentProvider._ID_GROUP + " FROM " + PadContentProvider.RELATION_TABLE_NAME + " " +
                        "WHERE " + PadContentProvider._ID_PAD + "=? ";
        values = new String[]{String.valueOf(padId)};

        Cursor cursor = db.rawQuery(QUERY, values);
        long groupid = 0;
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            groupid = cursor.getLong(0);
        }
        cursor.close();

        return groupid;
    }


    /**
     * Saves a new group if padgroup_id=0 or updates an existing one.
     * @param padgroup_id
     * @param pad_id
     * @return
     */
    public boolean savePadgroupRelation( long padgroup_id, long pad_id ){
        removePadFromAllGroups(pad_id);

        if( padgroup_id == 0 ) {
            return false;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(PadContentProvider._ID_PAD, pad_id);
        contentValues.put(PadContentProvider._ID_GROUP, padgroup_id);

        boolean result = db.insert(PadContentProvider.RELATION_TABLE_NAME, null, contentValues) > 0;
//            _debug_relations();
        return result;
    }

    /**
     * Destroys all possible relation between a pad and any group
     * @param pad_id
     * @return
     */
    public boolean removePadFromAllGroups(long pad_id) {
        int deleted = db.delete(PadContentProvider.RELATION_TABLE_NAME, PadContentProvider._ID_PAD + "=? ", new String[]{String.valueOf(pad_id)});
        return deleted > 0;
    }

    public PadGroup getPadGroup(long padId) {
        return getPadGroupById(getGroupId(padId));
    }

    public PadGroup getUnclassifiedPadGroup() {
        return new PadGroup(context);
    }
}
