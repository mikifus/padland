package com.mikifus.padland.Database.PadGroupModel

import androidx.lifecycle.LiveData
import com.mikifus.padland.Database.PadModel.Pad

class PadGroupRepository(private val padGroupDao: PadGroupDao) {

    val getAll: LiveData<List<PadGroup>> = padGroupDao.getAll()
    val getPadGroupsWithPadList: LiveData<List<PadGroupsWithPadList>> = padGroupDao.getPadGroupsWithPadList()
    val getPadsWithoutGroup: LiveData<List<Pad>> = padGroupDao.getPadsWithoutGroup()

    suspend fun insertPadGroup(padGroup: PadGroup) {
        padGroupDao.insertAll(padGroup)
    }

    suspend fun getById(id: Long): PadGroup {
        return padGroupDao.getById(id)
    }

    suspend fun getByPadId(id: Long): PadGroup {
        return padGroupDao.getByPadId(id)
    }

    suspend fun getPadGroupsAndPadListByPadIds(padIds: List<Long>): List<PadGroupsAndPadList> {
        return padGroupDao.getPadGroupsAndPadListByPadIds(padIds)
    }

    fun updatePadGroup(padGroup: PadGroup) {
        padGroupDao.update(padGroup)
    }

    suspend fun deletePadGroup(padGroup: PadGroup) {
        return padGroupDao.delete(padGroup)
    }

    suspend fun deletePadGroups(padGroups: List<PadGroup>) {
        return padGroupDao.delete(padGroups)
    }

    suspend fun insertPadGroupWithPadlist(padGroupsAndPadList: PadGroupsAndPadList) {
        padGroupDao.insertPadGroupWithPadlist(padGroupsAndPadList)
    }

    suspend fun deletePadGroupsAndPadList(padId: Long) {
        padGroupDao.deletePadGroupsAndPadList(padId)
    }

    suspend fun deletePadGroupsAndPadListByPadGroupId(padId: Long): Int {
        return padGroupDao.deletePadGroupsAndPadListByPadGroupId(padId)
    }
}