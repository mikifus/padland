package com.mikifus.padland.Database.PadGroupModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mikifus.padland.Database.PadListDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PadGroupViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<PadGroup>>
    val getPadGroupsWithPadList: LiveData<List<PadGroupsWithPadList>>

    val padGroup = MutableLiveData<PadGroup>()

    private val repository: PadGroupRepository

    init {
        val padGroupDao = PadListDatabase.getInstance(application).padGroupDao()
        repository = PadGroupRepository(padGroupDao)
        getAll = repository.getAll
        getPadGroupsWithPadList = repository.getPadGroupsWithPadList
    }

    suspend fun insertPadGroup(padGroup: PadGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPadGroupWithPadlist(padGroup)
        }
    }

    suspend fun getById(id: Long) {
        viewModelScope.launch {
            padGroup.value = repository.getById(id).value
        }
    }

    suspend fun updatePadGroup(padGroup: PadGroup) {
        viewModelScope.launch(Dispatchers.IO) { repository.updatePadGroup(padGroup) }
    }

    suspend fun insertPadGroupsAndPadList(padGroupsAndPadListEntity: PadGroupsAndPadListEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPadGroupWithPadlist(padGroupsAndPadListEntity)
        }
    }

    suspend fun deletePadGroupsAndPadList(padId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePadGroupsAndPadList(padId)
        }
    }
}