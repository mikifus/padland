package com.mikifus.padland.Database.PadModel

import android.content.ContentValues
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mikifus.padland.Utils.PadServer
import java.sql.Date

@Entity(tableName = Pad.TABLE_NAME)
data class Pad(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") val mId: Long,
    @ColumnInfo(name = "name") val mName: String,
    @ColumnInfo(name = "local_name") val mLocalName: String,
    @ColumnInfo(name = "server") val mServer: String,
    @ColumnInfo(name = "url") val mUrl: String,
    @ColumnInfo(name = "last_used_date", defaultValue = "(strftime('%s','now'))") val mLastUsedDate: Date,
    @ColumnInfo(name = "create_date", defaultValue = "(strftime('%s','now'))") val mCreateDate: Date,
    @ColumnInfo(name = "access_count", defaultValue = "0") val mAccessCount: Long,
//    @ColumnInfo(name = "position") val mPosition: Int,
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
//        0
    )

    companion object {
        const val TABLE_NAME = "padlist"
        val PKEY = Pad::mId.name

        fun withOnlyId(id: Long): MutableLiveData<Pad> {
            return MutableLiveData(Pad().copy(mId = id))
        }

        fun fromContentValues(contentValues: ContentValues): MutableLiveData<Pad> {
            val item = MutableLiveData(Pad())

//            contentValues.valueSet().forEach { item.value = item.value?.copy(contentValues.getAsString(it.key)) }
//            item.value = item.value?.copy(mId=contentValues.getAsLong(Pad::mId::class.java.canonicalName))
            if(contentValues.containsKey("name")) {
                item.value = item.value!!.copy(mName = contentValues.getAsString("name"))
            }
            if(contentValues.containsKey("url")) {
                item.value = item.value!!.copy(mUrl = contentValues.getAsString("url"))
            }

            return item
        }

        fun fromData(data: Map<String, Any>): MutableLiveData<Pad> {
            val item = MutableLiveData(Pad())
            data["name"]?.let { item.value = item.value!!.copy(mName = it as String) }
            data["local_name"]?.let { item.value = item.value!!.copy(mLocalName = it as String) }
            data["url"]?.let { item.value = item.value!!.copy(mUrl = it as String) }
            data["server"]?.let { item.value = item.value!!.copy(mServer = it as String) }

            return item
        }

        fun fromUrl(padUrl: String): MutableLiveData<Pad> {
            val item = MutableLiveData(Pad())

            val padServer = PadServer.Builder().padUrl(padUrl)

            val name = padServer.padName?: ""
            val server = padServer.server?: ""

            item.value = item.value!!.copy(mUrl = padUrl, mName = name, mServer = server)

            return item
        }
    }

    fun isPartiallyDifferentFrom(pad: Pad): Boolean {
        return (
            mName != pad.mName ||
            mUrl != pad.mUrl ||
            mLocalName != pad.mLocalName
        )
    }
}
