package com.mikifus.padland.Models;

import android.content.Context;
import android.database.Cursor;

import com.mikifus.padland.R;

/**
 * Created by mikifus on 28/02/18.
 */

public class PadGroup {
    private long id;
    private String name;
    private int position;

    public PadGroup(Context context) {
        id = 0;
        name = context.getString(R.string.padlist_group_unclassified_name);
        position = 0;
    }
    public PadGroup(Cursor c) {
        if(c != null && c.getCount() > 0) {
            id = c.getLong(0);
            name = c.getString(1);
            position = c.getInt(2);
        }
    }
    public long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        return ((PadGroup) obj).id == id;
    }
}
