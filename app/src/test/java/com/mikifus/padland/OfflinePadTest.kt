package com.mikifus.padland

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Utils.Export.ExclusionStrategies.IgnoreEntityIdStrategy
import com.mikifus.padland.Utils.Export.Maps.DatabaseMap
import com.mikifus.padland.Utils.Export.TypeAdapters.SqlDateTypeAdapter
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import com.mikifus.padland.Utils.Offline.OfflinePadStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.sql.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class OfflinePadTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var directory: File
    private lateinit var offlinePadStore: OfflinePadStore

    @Before
    fun setUp() {
        directory = File(temporaryFolder.root, OfflinePadStore.DIRECTORY_NAME)
        offlinePadStore = OfflinePadStore(directory)
    }

    // Store

    @Test
    fun store_saveAndGet_roundTrip() {
        assertTrue(offlinePadStore.save(1, "<html>áéí</html>"))

        assertEquals("<html>áéí</html>", offlinePadStore.get(1))
        assertTrue(offlinePadStore.has(1))
        assertNotNull(offlinePadStore.getSavedTime(1))
    }

    @Test
    fun store_saveTwice_keepsLatest() {
        offlinePadStore.save(1, "old")
        offlinePadStore.save(1, "new")

        assertEquals("new", offlinePadStore.get(1))
        assertEquals(1, directory.listFiles()!!.size)
    }

    @Test
    fun store_missingCopy_isNull() {
        assertNull(offlinePadStore.get(1))
        assertNull(offlinePadStore.getSavedTime(1))
        assertFalse(offlinePadStore.has(1))
    }

    @Test
    fun store_delete() {
        offlinePadStore.save(1, "copy")

        assertTrue(offlinePadStore.delete(1))
        assertFalse(offlinePadStore.has(1))
    }

    @Test
    fun store_keepOnly_removesOtherCopiesAndLeftovers() {
        offlinePadStore.save(1, "one")
        offlinePadStore.save(2, "two")
        offlinePadStore.save(3, "three")
        File(directory, "4.tmp").writeText("half written")
        File(directory, "unknown.html").writeText("?")

        offlinePadStore.keepOnly(listOf(2L))

        assertEquals(listOf("2.html"), directory.list()!!.toList())
    }

    @Test
    fun store_countAndClear() {
        assertEquals(0, offlinePadStore.count())

        offlinePadStore.save(1, "one")
        offlinePadStore.save(2, "two")
        File(directory, "3.tmp").writeText("half written")
        assertEquals(2, offlinePadStore.count())

        offlinePadStore.clear()
        assertEquals(0, offlinePadStore.count())
        assertEquals(0, directory.list()!!.size)
    }

    // Fetcher

    @Test
    fun fetcher_makeExportUrl() {
        assertEquals("https://pad.riseup.net/p/test/export/html",
            OfflinePadFetcher.makeExportUrl("https://pad.riseup.net/p/test"))
        assertEquals("https://pad.riseup.net/p/test/export/html",
            OfflinePadFetcher.makeExportUrl("https://pad.riseup.net/p/test/"))
    }

    @Test
    fun fetcher_makeExportUrl_dropsQueryAndKeepsPrefixAndPort() {
        assertEquals("http://example.org:9001/etherpad/p/my%20pad/export/html",
            OfflinePadFetcher.makeExportUrl(
                "http://example.org:9001/etherpad/p/my%20pad?userName=me&userColor=%23ff0000"))
    }

    @Test
    fun fetcher_isEtherpadUrl() {
        assertTrue(OfflinePadFetcher.isEtherpadUrl("https://pad.riseup.net/p/test"))
        assertFalse(OfflinePadFetcher.isEtherpadUrl("https://cryptpad.fr/pad/#/2/pad/edit/abcdefghijklmnop/"))
        assertFalse(OfflinePadFetcher.isEtherpadUrl("not an url"))
    }

    // Export compatibility

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

