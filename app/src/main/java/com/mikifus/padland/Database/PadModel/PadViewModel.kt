package com.mikifus.padland.Database.PadModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadListDatabase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PadViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<Pad>>

    val pad = MutableLiveData<Pad>()

    private val repository: PadRepository

    init {
        val padDao = PadListDatabase.getInstance(application).padDao()
        repository = PadRepository(padDao)
        getAll = repository.getAll
    }

    suspend fun insertPad(pad: Pad): Long {
        val deferred: Deferred<Long> = viewModelScope.async(Dispatchers.IO) {
            repository.insertPad(pad)
        }

        return deferred.await()
    }

    suspend fun insertPads(pads: List<Pad>): List<Long> {
        val deferred: Deferred<List<Long>> = viewModelScope.async(Dispatchers.IO) {
            repository.insertPads(pads)
        }

        return deferred.await()
    }

    suspend fun getById(id: Long): Pad {
        return repository.getById(id)
    }

    suspend fun getByIds(ids: List<Long>): List<Pad> {
        return repository.getByIds(ids)
    }

    suspend fun getByUrl(url: String): Pad {
        return repository.getByUrl(url)
    }

    suspend fun updatePad(pad: Pad): Int {
        return repository.updatePadGroup(pad)
    }

//    fun updatePadPosition(padId: Long, position: Int) {
//        viewModelScope.launch(Dispatchers.IO) { repository.updatePadPosition(padId, position) }
//    }

    suspend fun deletePad(id: Long): Int {
        val pad = Pad.withOnlyId(id).value!!
        return repository.deletePad(pad)
    }

    suspend fun deletePads(ids: List<Long>): Int {
        val pads = ids.map { Pad.withOnlyId(it).value!! }
        return repository.deletePads(pads)
    }
}