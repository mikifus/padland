package com.mikifus.padland.Database.PadModel

import androidx.lifecycle.LiveData
import com.mikifus.padland.Database.PadGroupModel.PadGroup

class PadRepository(private val padDao: PadDao) {

    val getAll: LiveData<List<Pad>> = padDao.getAll()

    suspend fun insertPad(pad: Pad) {
        padDao.insertAll(pad)
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

}