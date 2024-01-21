package com.mikifus.padland.Database.PadGroupModel

import androidx.lifecycle.LiveData

class PadGroupRepository(private val padGroupDao: PadGroupDao) {

    val getAll: LiveData<List<PadGroup>> = padGroupDao.getAll()
    val getPadGroupsWithPadList: LiveData<List<PadGroupsWithPadList>> = padGroupDao.getPadGroupsWithPadList()

    suspend fun insertPadGroupWithPadlist(padGroup: PadGroup) {
        padGroupDao.insertAll(padGroup)
    }

    fun getById(id: Long): LiveData<PadGroup> {
        return padGroupDao.getById(id)
    }

    suspend fun updatePadGroup(padGroup: PadGroup) {
        padGroupDao.update(padGroup)
    }

    suspend fun insertPadGroupWithPadlist(padGroupsAndPadListEntity: PadGroupsAndPadListEntity) {
        padGroupDao.insertPadGroupWithPadlist(padGroupsAndPadListEntity)
    }

    suspend fun deletePadGroupsAndPadList(padId: Long) {
        padGroupDao.deletePadGroupsAndPadList(padId)
    }
}