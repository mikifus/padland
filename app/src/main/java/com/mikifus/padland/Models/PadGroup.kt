package com.mikifus.padland.Models

import android.content.Context
import android.database.Cursor
import com.mikifus.padland.R

/**
 * Created by mikifus on 28/02/18.
 */
class PadGroup {
    var id: Long = 0
        private set
    var name: String? = null
        private set
    var position = 0
        private set

    constructor(context: Context?) {
        id = 0
        name = context!!.getString(R.string.padlist_group_unclassified_name)
        position = 0
    }

    constructor(c: Cursor?) {
        if (c != null && c.count > 0) {
            id = c.getLong(0)
            name = c.getString(1)
            position = c.getInt(2)
        }
    }

    override fun equals(obj: Any?): Boolean {
        return (obj as PadGroup?)!!.id == id
    }
}