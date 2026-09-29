package com.mikifus.padland

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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class OfflinePadStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var directory: File
    private lateinit var offlinePadStore: OfflinePadStore

    @Before
    fun setUp() {
        directory = File(temporaryFolder.root, OfflinePadStore.DIRECTORY_NAME)
        offlinePadStore = OfflinePadStore(directory)
    }

    @Test
    fun saveAndGet_roundTrip() {
        assertTrue(offlinePadStore.save(1, "<html>áéí</html>"))

        assertEquals("<html>áéí</html>", offlinePadStore.get(1))
        assertTrue(offlinePadStore.has(1))
        assertNotNull(offlinePadStore.getSavedTime(1))
    }

    @Test
    fun saveTwice_keepsLatest() {
        offlinePadStore.save(1, "old")
        offlinePadStore.save(1, "new")

        assertEquals("new", offlinePadStore.get(1))
        assertEquals(1, directory.listFiles()!!.size)
    }

    @Test
    fun missingCopy_isNull() {
        assertNull(offlinePadStore.get(1))
        assertNull(offlinePadStore.getSavedTime(1))
        assertFalse(offlinePadStore.has(1))
    }

    @Test
    fun delete() {
        offlinePadStore.save(1, "copy")

        assertTrue(offlinePadStore.delete(1))
        assertFalse(offlinePadStore.has(1))
    }

    @Test
    fun keepOnly_removesOtherCopiesAndLeftovers() {
        offlinePadStore.save(1, "one")
        offlinePadStore.save(2, "two")
        offlinePadStore.save(3, "three")
        File(directory, "4.tmp").writeText("half written")
        File(directory, "unknown.html").writeText("?")

        offlinePadStore.keepOnly(listOf(2L))

        assertEquals(listOf("2.html"), directory.list()!!.toList())
    }

    @Test
    fun countAndClear() {
        assertEquals(0, offlinePadStore.count())

        offlinePadStore.save(1, "one")
        offlinePadStore.save(2, "two")
        File(directory, "3.tmp").writeText("half written")
        assertEquals(2, offlinePadStore.count())

        offlinePadStore.clear()
        assertEquals(0, offlinePadStore.count())
        assertEquals(0, directory.list()!!.size)
    }
}

