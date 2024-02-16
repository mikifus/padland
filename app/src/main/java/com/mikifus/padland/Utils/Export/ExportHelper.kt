package com.mikifus.padland.Utils.Export

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupRepository
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.PadModel.PadRepository
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Database.ServerModel.ServerRepository
import com.mikifus.padland.Utils.Export.Maps.DatabaseMap
import com.mikifus.padland.Utils.Export.TypeAdapters.SqlDateTypeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.sql.Date

interface IExportHelper {
    val activity: FragmentActivity
    val launcher: ActivityResultLauncher<String>
}

class ExportHelper(
    override val activity: FragmentActivity,
    callback: ((done: Boolean) -> Unit) = {})
    : IExportHelper {

    override val launcher =
        activity.registerForActivityResult(
            ActivityResultContracts.CreateDocument("text/plain")
        ) {
            if (it == null) {
                callback(false)
                return@registerForActivityResult
            }

            val database = PadListDatabase.getInstance(activity)

            val serverRepository = ServerRepository(database.serverDao())
            val padGroupRepository = PadGroupRepository(database.padGroupDao())
            val padRepository = PadRepository(database.padDao())

            val gson = GsonBuilder()
                .setVersion(database.openHelper.readableDatabase.version.toDouble())
                .registerTypeAdapter(Date::class.java, SqlDateTypeAdapter)
                .setPrettyPrinting()
                .create()

            val dataMap = MediatorLiveData<Map<String, Any>>(
                mapOf(
                    "app" to activity.applicationContext.applicationInfo.name.toString(),
                    "className" to DatabaseMap::class.java.toString(),
                    "version" to database.openHelper.readableDatabase.version.toDouble()
                )
            )
            dataMap.addSource(serverRepository.getAll) { value1 ->
                val currentValue = dataMap.value ?: emptyMap()
                val updatedValue = currentValue.toMutableMap().apply {
                    put(Server.TABLE_NAME, value1)
                }
                dataMap.value = updatedValue
            }
            dataMap.addSource(padGroupRepository.getAll) { value1 ->
                val currentValue = dataMap.value ?: emptyMap()
                val updatedValue = currentValue.toMutableMap().apply {
                    put(PadGroup.TABLE_NAME, value1)
                }
                dataMap.value = updatedValue
            }
            dataMap.addSource(padRepository.getAll) { value1 ->
                val currentValue = dataMap.value ?: emptyMap()
                val updatedValue = currentValue.toMutableMap().apply {
                    put(Pad.TABLE_NAME, value1)
                }
                dataMap.value = updatedValue
            }

            dataMap.observe(activity) { map ->
                if(map.size < 6) {
                    return@observe
                }
                val json = gson.toJson(map)

                activity.contentResolver.openOutputStream(it)?.bufferedWriter()
                    ?.apply {
                        write(json)
                        flush()
                    }
                dataMap.removeObservers(activity)
                callback(true)
            }
        }
}