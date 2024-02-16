package com.mikifus.padland.Utils.Export

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mikifus.padland.Database.PadGroupModel.PadGroupRepository
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.PadRepository
import com.mikifus.padland.Database.ServerModel.ServerRepository
import com.mikifus.padland.Utils.Export.ExclusionStrategies.IgnoreEntityIdStrategy
import com.mikifus.padland.Utils.Export.Maps.DatabaseMap
import com.mikifus.padland.Utils.Export.TypeAdapters.SqlDateTypeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Type
import java.sql.Date

interface IImportHelper {
    val activity: FragmentActivity
    val launcher: ActivityResultLauncher<Array<String>>

}
class ImportHelper(
    override val activity: FragmentActivity,
    callback: ((done: Boolean, result: String?) -> Unit) = {_,_->})
    : IImportHelper {

    override val launcher =
        activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) {
            if (it == null) {
                callback(false, null)
                return@registerForActivityResult
            }
            val jsonString = activity.contentResolver
                .openInputStream(it)
                ?.bufferedReader()
                ?.readText()
                ?: "[]"

            val database = PadListDatabase.getInstance(activity)

            val serverRepository = ServerRepository(database.serverDao())
            val padGroupRepository = PadGroupRepository(database.padGroupDao())
            val padRepository = PadRepository(database.padDao())

            val gson = GsonBuilder()
                .setVersion(database.openHelper.readableDatabase.version.toDouble())
                .registerTypeAdapter(Date::class.java, SqlDateTypeAdapter)
                .setExclusionStrategies(
                    IgnoreEntityIdStrategy()
                )
                .setPrettyPrinting()
                .create()

            val listType: Type = object : TypeToken<DatabaseMap>() {}.type
            val dataMap: DatabaseMap = gson.fromJson(jsonString, listType)

            activity.lifecycleScope.launch(Dispatchers.IO) {
                var size = 0
                dataMap.padland_servers?.let { it ->
                    size += serverRepository.insertServers(it).size
                }
                dataMap.padgroups?.let { it ->
                    size += padGroupRepository.insertPadGroups(it).size
                }
                dataMap.padlist?.let { it ->
                    size += padRepository.insertPads(it).size
                }

                val insertedResult = "$size"

                withContext(Dispatchers.Main) {
                    callback(true, insertedResult)
                }
            }

        }
}