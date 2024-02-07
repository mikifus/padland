package com.mikifus.padland.Database.PadGroupModel

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mikifus.padland.Database.PadModel.Pad


@Dao
interface PadGroupDao {
    @Insert
    fun insertAll(vararg pads: PadGroup): List<Long>

    @Update
    fun update(vararg pads: PadGroup)

    @Delete
    fun delete(pad: PadGroup)
    @Delete
    fun delete(pad: List<PadGroup>)

    @Query("SELECT * FROM padgroups")
    fun getAll(): LiveData<List<PadGroup>>

    @Query("SELECT * FROM padgroups")
    fun getAllCursor(): Cursor

    @Query("SELECT * FROM padgroups WHERE _id == :id")
    suspend fun getById(id: Long): PadGroup

    @Query("SELECT * FROM padgroups WHERE _id == :id")
    fun getByIdCursor(id: Long): Cursor

    @Query("DELETE FROM padgroups WHERE _id IN (:selectionArgs)")
    fun deleteBy(selectionArgs: Array<String>?): Int

    @Transaction
    @Query("SELECT * FROM padgroups")
    fun getPadGroupsWithPadList(): LiveData<List<PadGroupsWithPadList>>

    @Transaction
    @Query("SELECT * FROM padgroups")
    fun getPadGroupsWithPadListWithEmpty(): LiveData<List<PadGroupsWithPadList>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPadGroupWithPadlist(padGroupsAndPadListEntity: PadGroupsAndPadListEntity): Long

    @Query("DELETE FROM padlist_padgroups WHERE _id_pad = :padId")
    fun deletePadGroupsAndPadList(padId: Long)

    @Query("SELECT * FROM padlist WHERE _id NOT IN" +
            "(SELECT _id_pad FROM padlist_padgroups)")
    fun getPadsWithoutGroup(): LiveData<List<Pad>>

    @Query("SELECT * FROM padgroups WHERE _id IN" +
            "(SELECT _id_group FROM padlist_padgroups WHERE _id_pad = :id)")
    suspend fun getByPadId(id: Long): PadGroup
}
