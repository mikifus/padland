package com.mikifus.padland.Database.ServerModel

import android.content.ContentValues
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadModel.Pad
import java.sql.Date

@Entity(tableName = "padland_servers")
data class Server(
    @PrimaryKey
    @ColumnInfo(name = "_id") val mId: Long,
    @ColumnInfo(name = "name") val mName: String,
    @ColumnInfo(name = "url") val mUrl: String,
    @ColumnInfo(name = "padprefix") val mPadprefix: String,
    @ColumnInfo(name = "position") val mPosition: Long,
    @ColumnInfo(name = "jquery") val mJquery: Boolean,
    @ColumnInfo(name = "enabled") val mEnabled: Boolean,
    @ColumnInfo(name = "create_date") val mCreateDate: Date
)
{
    constructor() : this(
        0,
        "",
        "",
        "",
        0,
        true,
        true,
        Date(System.currentTimeMillis())
    )

    companion object {

        fun fromFormContentValues(contentValues: ContentValues): MutableLiveData<Server> {
            val item = MutableLiveData<Server>(Server())

//            contentValues.valueSet().forEach { item.value = item.value?.copy(contentValues.getAsString(it.key)) }
//            item.value = item.value?.copy(mId=contentValues.getAsLong(Pad::mId::class.java.canonicalName))
            if(contentValues.containsKey("name")) item.value = item.value!!.copy(mName = contentValues.getAsString("name"))
            if(contentValues.containsKey("prefix")) item.value = item.value!!.copy(mPadprefix = contentValues.getAsString("uprefix"))
            if(contentValues.containsKey("url")) item.value = item.value!!.copy(mUrl = contentValues.getAsString("url"))
            if(contentValues.containsKey("jquery")) item.value = item.value!!.copy(mJquery = contentValues.getAsBoolean("jquery"))
            if(contentValues.containsKey("enabled")) item.value = item.value!!.copy(mEnabled = contentValues.getAsBoolean("enabled"))

            return item
        }

        fun withOnlyId(id: Long): MutableLiveData<Server> {
            val item = MutableLiveData(Server())

            item.value = item.value!!.copy(mId = id)

            return item
        }
    }
}

