package com.mikifus.padland.Database.PadGroupModel

import android.content.ContentValues
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "padgroups")
data class PadGroup(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val mId: Long,
    @ColumnInfo(name = "name") val mName: String,
    @ColumnInfo(name = "position") val mPosition: Int,
    @ColumnInfo(name = "last_used_date") val mLastUsedDate: Date?,
    @ColumnInfo(name = "create_date") val mCreateDate: Date,
    @ColumnInfo(name = "access_count") val mAccessCount: Long,
)
{
    constructor() : this(
        0,
        "",
        0,
        Date(System.currentTimeMillis()),
        Date(System.currentTimeMillis()),
        0
    )

    companion object {

        fun fromContentValues(contentValues: ContentValues): MutableLiveData<PadGroup> {
            val item = MutableLiveData<PadGroup>(PadGroup())

//            contentValues.valueSet().forEach { item.value = item.value?.copy(contentValues.getAsString(it.key)) }
//            item.value = item.value?.copy(mId=contentValues.getAsLong(Pad::mId::class.java.canonicalName))
            if(contentValues.containsKey("name")) item.value = item.value!!.copy(mName = contentValues.getAsString("name"))


            return item
        }
    }
}