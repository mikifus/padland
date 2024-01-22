package com.mikifus.padland.Database.PadModel

import android.content.ContentValues
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import java.sql.Date

@Entity(tableName = "padlist")
data class Pad(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val mId: Long,
    @ColumnInfo(name = "name") val mName: String,
    @ColumnInfo(name = "local_name") val mLocalName: String,
    @ColumnInfo(name = "server") val mServer: String,
    @ColumnInfo(name = "url") val mUrl: String,
    @ColumnInfo(name = "last_used_date") val mLastUsedDate: Date?,
    @ColumnInfo(name = "create_date") val mCreateDate: Date,
    @ColumnInfo(name = "access_count") val mAccessCount: Long,
    @ColumnInfo(name = "position") val mPosition: Int,
)
{
    constructor() : this(
        0,
        "",
        "",
        "",
        "",
        Date(System.currentTimeMillis()),
        Date(System.currentTimeMillis()),
        0,
        0
    )

    companion object {

        fun fromContentValues(contentValues: ContentValues): MutableLiveData<Pad> {
            val item = MutableLiveData<Pad>(Pad())

//            contentValues.valueSet().forEach { item.value = item.value?.copy(contentValues.getAsString(it.key)) }
//            item.value = item.value?.copy(mId=contentValues.getAsLong(Pad::mId::class.java.canonicalName))
            if(contentValues.containsKey("name")) item.value = item.value!!.copy(mName = contentValues.getAsString("name"))
            if(contentValues.containsKey("url")) item.value = item.value!!.copy(mUrl = contentValues.getAsString("url"))

            return item
        }
    }
}
