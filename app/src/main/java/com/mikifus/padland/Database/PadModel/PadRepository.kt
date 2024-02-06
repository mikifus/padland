package com.mikifus.padland.Database.PadModel

import androidx.lifecycle.LiveData
import com.mikifus.padland.Database.PadGroupModel.PadGroup

class PadRepository(private val padDao: PadDao) {

    val getAll: LiveData<List<Pad>> = padDao.getAll()

    suspend fun insertPad(pad: Pad): Long {
        return padDao.insert(pad)
    }

    suspend fun insertPads(pads: List<Pad>): List<Long> {
        return padDao.insertAll(pads)
    }

    fun getById(id: Long): LiveData<Pad> {
        return padDao.getById(id)
    }

    fun getByUrl(url: String): LiveData<Pad> {
        return padDao.getByUrl(url)
    }

    suspend fun updatePadGroup(padGroup: Pad) {
        padDao.update(padGroup)
    }

//    fun updatePadPosition(padId: Long, position: Int) {
//        return padDao.updatePadPosition(padId, position)
//    }

}