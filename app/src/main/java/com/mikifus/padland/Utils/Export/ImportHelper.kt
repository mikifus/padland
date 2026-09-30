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
                var size = 0L
                // Records identical to existing ones are skipped
                dataMap.padland_servers?.let { it ->
                    val servers = ImportMatcher.filterNew(it, serverRepository.getAllList())
                    size += serverRepository.insertServers(servers).size
                }
                dataMap.padgroups?.let { it ->
                    val padGroups = ImportMatcher.filterNew(it, padGroupRepository.getAllList())
                    size += padGroupRepository.insertPadGroups(padGroups).size
                }
                var skippedPadUrls = emptySet<String>()
                dataMap.padlist?.let { it ->
                    val pads = ImportMatcher.filterNew(it, padRepository.getAllList())
                    skippedPadUrls = it.map { pad -> pad.mUrl }.toSet() - pads.map { pad -> pad.mUrl }.toSet()
                    size += padRepository.insertPads(pads).size
                }
                dataMap.padlist_padgroups?.let { it ->
                    // Skipped pads keep their current groups, we skip their relations.
                    val relations = it.filter { rel -> rel.mPadRelString !in skippedPadUrls }
                    size += padGroupRepository.insertPadGroupWithPadlistByRelString(relations).size
                }

                val insertedResult = "$size"

                withContext(Dispatchers.Main) {
                    callback(true, insertedResult)
                }
            }

        }
}