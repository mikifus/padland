package com.mikifus.padland.Database.ServerModel

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ServerDao {
    @Insert
    fun insertAll(vararg pads: Server)

    @Update
    fun update(vararg pads: Server)

    @Delete
    fun delete(pad: Server)

    @Query("SELECT * FROM padland_servers")
    fun getAll(): LiveData<List<Server>>

    @Query("SELECT * FROM padland_servers WHERE enabled = 1")
    fun getAllEnabled(): LiveData<List<Server>>

//    @Query("SELECT * FROM padland_servers WHERE _id == :id")
//    fun getById(id: Long): LiveData<Server>

    @Query("SELECT * FROM padland_servers WHERE _id == :id")
    fun getById(id: Long): Server

    @Query("SELECT padprefix FROM padland_servers WHERE url == :url")
    fun getServerPrefixFromUrl(url: String?): String

//    fun getServerPrefixFromUrl(context: Context?, server: String?): String? {
//        var c = 0
//        val serverUrlList = getServerUrlList(context)
//        val serverUrlPrefixList = getServerUrlPrefixList(context)
//        for (s in serverUrlList) {
//            if (s == server) {
//                break
//            }
//            c++
//        }
//        return if (c < serverUrlPrefixList.size && serverUrlPrefixList[c] != null) {
//            serverUrlPrefixList[c]
//        } else null
//    }
}
