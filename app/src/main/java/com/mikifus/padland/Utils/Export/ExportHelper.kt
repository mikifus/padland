package com.mikifus.padland.Utils.Export

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.google.gson.GsonBuilder
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.PadRepository
import com.mikifus.padland.Utils.Export.TypeAdapters.SqlDateTypeAdapter
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
            val repository = PadRepository(database.padDao())

            val gson = GsonBuilder()
                .setVersion(database.openHelper.readableDatabase.version.toDouble())
                .registerTypeAdapter(Date::class.java, SqlDateTypeAdapter)
                .setPrettyPrinting()
                .create()

            repository
                .getAll
                .observe(activity) { pads ->
                    val json = gson.toJson(pads)

                    activity.contentResolver.openOutputStream(it)?.bufferedWriter()
                        ?.apply {
                            write(json)
                            flush()
                        }
                    callback(true)
                }
        }
}