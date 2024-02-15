package com.mikifus.padland.Database.PadGroupModel

import android.database.Cursor
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mikifus.padland.Database.PadModel.Pad


@Dao
interface PadGroupDao {
    @Insert
    fun insertAll(vararg padGroups: PadGroup): List<Long>

    @Update
    fun update(vararg padGroups: PadGroup)

    @Delete
    suspend fun delete(padGroup: PadGroup)

    @Delete
    suspend fun delete(padGroups: List<PadGroup>)

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

    @Query("SELECT * FROM padgroups")
    fun getPadGroupsWithPadList(): LiveData<List<PadGroupsWithPadList>>

    @Query("SELECT * FROM padlist_padgroups WHERE _id_pad IN (:padIds)")
    fun getPadGroupsAndPadListByPadIds(padIds: List<Long>): List<PadGroupsAndPadList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPadGroupWithPadlist(padGroupsAndPadList: PadGroupsAndPadList): Long

    @Query("DELETE FROM padlist_padgroups WHERE _id_pad = :padId")
    fun deletePadGroupsAndPadList(padId: Long)

    @Query("DELETE FROM padlist_padgroups WHERE _id_group = :padId")
    fun deletePadGroupsAndPadListByPadGroupId(padId: Long): Int

    @Query("SELECT * FROM padlist WHERE _id NOT IN" +
            "(SELECT _id_pad FROM padlist_padgroups)")
    fun getPadsWithoutGroup(): LiveData<List<Pad>>

    @Query("SELECT * FROM padgroups WHERE _id IN" +
            "(SELECT _id_group FROM padlist_padgroups WHERE _id_pad = :id)")
    suspend fun getByPadId(id: Long): PadGroup
}
