package com.mikifus.padland.Database.PadModel

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PadDao {
    @Insert
    fun insertAll(vararg pads: Pad): List<Long>

    @Update
    fun update(vararg pads: Pad): Int

    @Delete
    fun delete(pad: Pad): Int

    @Query("SELECT * FROM padlist")
    fun getAll(): LiveData<List<Pad>>

    @Query("SELECT * FROM padlist")
    fun getAllCursor(): /*List<Pad>*/ Cursor

    @Query("SELECT * FROM padlist WHERE _id == :id")
    fun getById(id: Long): LiveData<Pad>

    @Query("SELECT * FROM padlist WHERE _id == :id")
    fun getByIdCursor(id: Long): /*LiveData<List<Pad>>*/Cursor

    @Query("SELECT * FROM padlist WHERE url == :url")
    fun getByUrl(url: String): LiveData<Pad>

    @Query("SELECT * FROM padlist WHERE url == :url")
    fun getByUrlCursor(url: String): Cursor

    @Query("DELETE FROM padlist WHERE _id IN (:selectionArgs)")
    fun deleteBy(selectionArgs: Array<String>?): Int
}
