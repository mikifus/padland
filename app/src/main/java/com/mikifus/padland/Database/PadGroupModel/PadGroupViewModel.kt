package com.mikifus.padland.Database.PadGroupModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PadGroupViewModel(application: Application): AndroidViewModel(application) {

    val getAll: LiveData<List<PadGroup>>
    val getPadGroupsWithPadList: LiveData<List<PadGroupsWithPadList>>
    val getPadsWithoutGroup: LiveData<List<Pad>>

    val padGroup = MutableLiveData<PadGroup>()

    private val repository: PadGroupRepository

    init {
        val padGroupDao = PadListDatabase.getInstance(application).padGroupDao()
        repository = PadGroupRepository(padGroupDao)
        getAll = repository.getAll
        getPadGroupsWithPadList = repository.getPadGroupsWithPadList
        getPadsWithoutGroup = repository.getPadsWithoutGroup
    }

    suspend fun insertPadGroup(padGroup: PadGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPadGroup(padGroup)
        }
    }

    suspend fun getById(id: Long): PadGroup {
        return repository.getById(id)
    }

    suspend fun updatePadGroup(padGroup: PadGroup) {
        viewModelScope.launch(Dispatchers.IO) { repository.updatePadGroup(padGroup) }
    }

    fun deletePadGroup(id: Long) {
        val padGroup = PadGroup.withOnlyId(id).value!!
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePadGroup(padGroup)
        }
    }

    fun deletePadGroup(ids: List<Long>)=viewModelScope.launch {
        val padGroups = ids.map { PadGroup.withOnlyId(it).value!! }
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePadGroups(padGroups)
        }
    }

    suspend fun getByPadId(id: Long): PadGroup {
        return repository.getByPadId(id)
    }

    suspend fun getPadGroupsAndPadListByPadIds(padIds: List<Long>): List<PadGroupsAndPadList> {
        return repository.getPadGroupsAndPadListByPadIds(padIds)
    }

    suspend fun insertPadGroupsAndPadList(padGroupsAndPadList: PadGroupsAndPadList) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPadGroupWithPadlist(padGroupsAndPadList)
        }
    }

    suspend fun deletePadGroupsAndPadList(padId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePadGroupsAndPadList(padId)
        }
    }

}