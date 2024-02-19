package com.mikifus.padland.Database.ServerModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mikifus.padland.Database.PadListDatabase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServerViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<Server>>
    val getAllEnabled: LiveData<List<Server>>

    val server = MutableLiveData<Server>()

    private val repository: ServerRepository

    init {
        val serverDao = PadListDatabase.getInstance(application).serverDao()
        repository = ServerRepository(serverDao)
        getAll = repository.getAll
        getAllEnabled = repository.getAllEnabled
    }

    suspend fun insertServer(server: Server) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertServer(server)
        }
    }

    suspend fun getById(id: Long): Server {
        return repository.getById(id)
    }

    suspend fun updateServer(server: Server) {
        viewModelScope.launch(Dispatchers.IO) { repository.updateServer(server) }
    }

    fun deleteServer(id: Long) {
        viewModelScope.launch {
            repository.deleteServer(Server.withOnlyId(id).value!!)
        }
    }

    fun deleteServer(ids: List<Long>)=viewModelScope.launch {
        viewModelScope.launch {
            ids.forEach {
                val server = Server.withOnlyId(it).value!!

                withContext(Dispatchers.IO) {
                    repository.deleteServer(server)
                }
            }
        }
    }

}