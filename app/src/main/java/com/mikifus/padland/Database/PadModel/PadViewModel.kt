package com.mikifus.padland.Database.PadModel

import android.app.Application
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

    suspend fun getById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            pad.value = repository.getById(id).value
        }
    }

    suspend fun getByUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            pad.value = repository.getByUrl(url).value
        }
    }

    suspend fun updatePad(pad: Pad) {
        viewModelScope.launch(Dispatchers.IO) { repository.updatePadGroup(pad) }
    }

//    fun updatePadPosition(padId: Long, position: Int) {
//        viewModelScope.launch(Dispatchers.IO) { repository.updatePadPosition(padId, position) }
//    }
}