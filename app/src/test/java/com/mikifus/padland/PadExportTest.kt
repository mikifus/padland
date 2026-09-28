package com.mikifus.padland

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Utils.Export.ExclusionStrategies.IgnoreEntityIdStrategy
import com.mikifus.padland.Utils.Export.Maps.DatabaseMap
import com.mikifus.padland.Utils.Export.TypeAdapters.SqlDateTypeAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.sql.Date

/**
 * Exports made with previous versions must still be imported.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class PadExportTest {

    private val gson = GsonBuilder()
        .setVersion(10.0)
        .registerTypeAdapter(Date::class.java, SqlDateTypeAdapter)
        .setExclusionStrategies(IgnoreEntityIdStrategy())
        .create()

    private fun importPads(padJson: String): List<Pad> {
        val json = """
            {
              "app": "Padland",
              "className": "DatabaseMap",
              "version": 9.0,
              "padlist": [ $padJson ]
            }
        """.trimIndent()
        val dataMap: DatabaseMap = gson.fromJson(json, object : TypeToken<DatabaseMap>() {}.type)
        return dataMap.padlist!!
    }

    @Test
    fun import_exportWithoutOfflineAccess_isDisabled() {
        // Exports made before the offline access existed
        val pads = importPads("""
            {"mId": 5, "mName": "test", "mLocalName": "", "mServer": "https://pad.riseup.net",
             "mUrl": "https://pad.riseup.net/p/test", "mLastUsedDate": "2024-01-15",
             "mCreateDate": "2024-01-01", "mAccessCount": 3}
        """)

        assertEquals(1, pads.size)
        assertEquals("test", pads[0].mName)
        assertEquals(0L, pads[0].mId)
        assertFalse(pads[0].mOfflineAccess)
    }

    @Test
    fun import_exportWithOfflineAccess() {
        val pads = importPads("""
            {"mId": 5, "mName": "test", "mLocalName": "", "mServer": "https://pad.riseup.net",
             "mUrl": "https://pad.riseup.net/p/test", "mLastUsedDate": "2024-01-15",
             "mCreateDate": "2024-01-01", "mAccessCount": 3, "mOfflineAccess": true}
        """)

        assertTrue(pads[0].mOfflineAccess)
    }

    @Test
    fun export_containsOfflineAccess() {
        val json = gson.toJson(Pad().copy(mOfflineAccess = true))

        assertTrue(json.contains("\"mOfflineAccess\":true"))
    }
}

