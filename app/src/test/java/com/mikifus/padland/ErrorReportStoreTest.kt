package com.mikifus.padland

import com.mikifus.padland.Utils.ErrorReporting.ErrorReport
import com.mikifus.padland.Utils.ErrorReporting.ErrorReportStore
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
class ErrorReportStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var directory: File
    private lateinit var errorReportStore: ErrorReportStore

    private val environment = ErrorReport.Environment("3.6", 33, "14 (API 34)", "Test device")

    private var timestamp = 1_700_000_000_000L

    private fun report(fatal: Boolean = false, message: String? = "boom"): ErrorReport {
        timestamp += 1000
        return ErrorReport.fromThrowable(
            IllegalStateException(message, RuntimeException("root cause")),
            fatal, "main", "PadListActivity", environment, timestamp = timestamp)
    }

    @Before
    fun setUp() {
        directory = File(temporaryFolder.root, ErrorReportStore.DIRECTORY_NAME)
        errorReportStore = ErrorReportStore(directory, maxReports = 3)
    }

    @Test
    fun save_andGet_roundTrip() {
        val report = report()
        assertTrue(errorReportStore.save(report))

        assertEquals(report, errorReportStore.get(report.id))
        assertEquals(1, errorReportStore.count())
    }

    @Test
    fun save_roundTrip_withNullValues() {
        val report = report(message = null).copy(screen = null)
        assertTrue(errorReportStore.save(report))

        val saved = errorReportStore.get(report.id)!!
        assertNull(saved.message)
        assertNull(saved.screen)
        assertEquals(report, saved)
    }

    @Test
    fun save_leavesNoTempFiles() {
        errorReportStore.save(report())
        val names = directory.list()!!.filter { File(directory, it).isFile }
        assertTrue(names.all { it.endsWith(".json") })
    }

    @Test
    fun list_newestFirst() {
        val first = report()
        val second = report()
        val third = report()
        // Saved out of order on purpose
        errorReportStore.save(second)
        errorReportStore.save(third)
        errorReportStore.save(first)

        assertEquals(listOf(third.id, second.id, first.id), errorReportStore.list().map { it.id })
    }

    @Test
    fun prune_keepsNewestReports_andRemovesTheirPendingMarkers() {
        val oldest = report(fatal = true)
        errorReportStore.save(oldest, pending = true)
        val others = (1..3).map { report().also { errorReportStore.save(it) } }

        assertEquals(3, errorReportStore.count())
        assertNull(errorReportStore.get(oldest.id))
        assertEquals(others.reversed().map { it.id }, errorReportStore.list().map { it.id })
        assertTrue(errorReportStore.pendingIds().isEmpty())
    }

    @Test
    fun pending_isOnlySetWhenRequested_andCanBeAcknowledged() {
        val crash = report(fatal = true)
        val error = report()
        errorReportStore.save(crash, pending = true)
        errorReportStore.save(error)

        assertEquals(listOf(crash.id), errorReportStore.pendingIds())

        errorReportStore.acknowledge(listOf(crash.id))
        assertTrue(errorReportStore.pendingIds().isEmpty())
        // Still saved
        assertNotNull(errorReportStore.get(crash.id))
    }

    @Test
    fun pendingIds_newestFirst_andSkipsMissingReports() {
        val first = report(fatal = true)
        val second = report(fatal = true)
        errorReportStore.save(first, pending = true)
        errorReportStore.save(second, pending = true)
        assertEquals(listOf(second.id, first.id), errorReportStore.pendingIds())

        File(directory, first.id + ".json").delete()
        assertEquals(listOf(second.id), errorReportStore.pendingIds())
        assertFalse(File(File(directory, ErrorReportStore.PENDING_DIRECTORY_NAME), first.id).exists())
    }

    @Test
    fun delete_removesReportAndMarker() {
        val crash = report(fatal = true)
        errorReportStore.save(crash, pending = true)

        assertTrue(errorReportStore.delete(crash.id))
        assertNull(errorReportStore.get(crash.id))
        assertTrue(errorReportStore.pendingIds().isEmpty())
    }

    @Test
    fun clear_removesEverything() {
        errorReportStore.save(report(fatal = true), pending = true)
        errorReportStore.save(report())

        errorReportStore.clear()

        assertEquals(0, errorReportStore.count())
        assertTrue(errorReportStore.list().isEmpty())
        assertTrue(errorReportStore.pendingIds().isEmpty())
    }

    @Test
    fun corruptFiles_areSkipped() {
        val report = report()
        errorReportStore.save(report)
        File(directory, "1600000000000_error_broken.json").writeText("{ not json")

        assertEquals(listOf(report.id), errorReportStore.list().map { it.id })
    }

    @Test
    fun invalidIds_areRejected() {
        assertFalse(errorReportStore.save(report().copy(id = "../escape")))
        assertNull(errorReportStore.get("../escape"))
        assertFalse(errorReportStore.delete("../escape"))
        assertFalse(File(temporaryFolder.root, "escape.json").exists())
    }

    @Test
    fun emptyStore_doesNotFail() {
        assertEquals(0, errorReportStore.count())
        assertTrue(errorReportStore.list().isEmpty())
        assertTrue(errorReportStore.pendingIds().isEmpty())
        errorReportStore.acknowledge(listOf("1_fatal_x"))
        errorReportStore.clear()
    }
}

