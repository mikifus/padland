package com.mikifus.padland.Database.ServerModel

import androidx.lifecycle.LiveData

class ServerRepository(private val serverDao: ServerDao) {

    val getAll: LiveData<List<Server>> = serverDao.getAll()

    suspend fun insertServer(server: Server) {
        serverDao.insertAll(server)
    }

    suspend fun getById(id: Long): Server {
        return serverDao.getById(id)
    }

    fun updateServer(server: Server) {
        serverDao.update(server)
    }

    suspend fun deleteServer(server: Server) {
        return serverDao.delete(server)
    }
}