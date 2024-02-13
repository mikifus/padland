package com.mikifus.padland.Database.PadGroupModel

import android.content.ContentValues
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = PadGroup.TABLE_NAME )
data class PadGroup(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val mId: Long,
    @ColumnInfo(name = "name") var mName: String,
    @ColumnInfo(name = "position", defaultValue = "0") val mPosition: Int,
    @ColumnInfo(name = "last_used_date", defaultValue = "(strftime('%s','now'))") val mLastUsedDate: Date,
    @ColumnInfo(name = "create_date", defaultValue = "(strftime('%s','now'))") val mCreateDate: Date,
    @ColumnInfo(name = "access_count", defaultValue = "0") val mAccessCount: Long,
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
        const val TABLE_NAME = "padgroups"

        fun withOnlyId(id: Long): MutableLiveData<PadGroup> {
            return MutableLiveData(PadGroup().copy(mId = id))
        }

        fun fromName(name: String): MutableLiveData<PadGroup> {
            return MutableLiveData(PadGroup().copy(mName = name))
        }

        fun fromContentValues(contentValues: ContentValues): MutableLiveData<PadGroup> {
            val item = MutableLiveData(PadGroup())

//            contentValues.valueSet().forEach { item.value = item.value?.copy(contentValues.getAsString(it.key)) }
//            item.value = item.value?.copy(mId=contentValues.getAsLong(Pad::mId::class.java.canonicalName))
            if(contentValues.containsKey("name")) item.value = item.value!!.copy(mName = contentValues.getAsString("name"))


            return item
        }
    }
}
