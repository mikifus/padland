package com.mikifus.padland

import androidx.test.core.app.ApplicationProvider
import com.mikifus.padland.Utils.ErrorReporting.ErrorReport
import com.mikifus.padland.Utils.ErrorReporting.ErrorReportStore
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
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
    private lateinit var store: ErrorReportStore

    private val environment = ErrorReport.Environment("3.6", 33, "14 (API 34)", "Test device")

    private var t = 1_700_000_000_000L

    private fun report(fatal: Boolean = false, message: String? = "boom"): ErrorReport {
        t += 1000
        return ErrorReport.fromThrowable(
            IllegalStateException(message, RuntimeException("root cause")),
            fatal, "main", "PadListActivity", environment, timestamp = t)
    }

    @Before
    fun setUp() {
        directory = File(temporaryFolder.root, ErrorReportStore.DIRECTORY_NAME)
        store = ErrorReportStore(directory, maxReports = 3)
    }

    @Test
    fun save_andGet_roundTrip() {
        val report = report()
        assertTrue(store.save(report))

        assertEquals(report, store.get(report.id))
        assertEquals(1, store.count())
    }

    @Test
    fun save_roundTrip_withNullValues() {
        val report = report(message = null).copy(screen = null)
        assertTrue(store.save(report))

        val saved = store.get(report.id)!!
        assertNull(saved.message)
        assertNull(saved.screen)
        assertEquals(report, saved)
    }

    @Test
    fun save_leavesNoTempFiles() {
        store.save(report())
        val names = directory.list()!!.filter { File(directory, it).isFile }
        assertTrue(names.all { it.endsWith(".json") })
    }

    @Test
    fun list_newestFirst() {
        val first = report()
        val second = report()
        val third = report()
        // Saved out of order on purpose
        store.save(second)
        store.save(third)
        store.save(first)

        assertEquals(listOf(third.id, second.id, first.id), store.list().map { it.id })
    }

    @Test
    fun prune_keepsNewestReports_andRemovesTheirPendingMarkers() {
        val oldest = report(fatal = true)
        store.save(oldest, pending = true)
        val others = (1..3).map { report().also { store.save(it) } }

        assertEquals(3, store.count())
        assertNull(store.get(oldest.id))
        assertEquals(others.reversed().map { it.id }, store.list().map { it.id })
        assertTrue(store.pendingIds().isEmpty())
    }

    @Test
    fun pending_isOnlySetWhenRequested_andCanBeAcknowledged() {
        val crash = report(fatal = true)
        val error = report()
        store.save(crash, pending = true)
        store.save(error)

        assertEquals(listOf(crash.id), store.pendingIds())

        store.acknowledge(listOf(crash.id))
        assertTrue(store.pendingIds().isEmpty())
        // Still saved
        assertNotNull(store.get(crash.id))
    }

    @Test
    fun pendingIds_newestFirst_andSkipsMissingReports() {
        val first = report(fatal = true)
        val second = report(fatal = true)
        store.save(first, pending = true)
        store.save(second, pending = true)
        assertEquals(listOf(second.id, first.id), store.pendingIds())

        File(directory, first.id + ".json").delete()
        assertEquals(listOf(second.id), store.pendingIds())
        assertFalse(File(File(directory, ErrorReportStore.PENDING_DIRECTORY_NAME), first.id).exists())
    }

    @Test
    fun delete_removesReportAndMarker() {
        val crash = report(fatal = true)
        store.save(crash, pending = true)

        assertTrue(store.delete(crash.id))
        assertNull(store.get(crash.id))
        assertTrue(store.pendingIds().isEmpty())
    }

    @Test
    fun clear_removesEverything() {
        store.save(report(fatal = true), pending = true)
        store.save(report())

        store.clear()

        assertEquals(0, store.count())
        assertTrue(store.list().isEmpty())
        assertTrue(store.pendingIds().isEmpty())
    }

    @Test
    fun corruptFiles_areSkipped() {
        val report = report()
        store.save(report)
        File(directory, "1600000000000_error_broken.json").writeText("{ not json")

        assertEquals(listOf(report.id), store.list().map { it.id })
    }

    @Test
    fun invalidIds_areRejected() {
        assertFalse(store.save(report().copy(id = "../escape")))
        assertNull(store.get("../escape"))
        assertFalse(store.delete("../escape"))
        assertFalse(File(temporaryFolder.root, "escape.json").exists())
    }

    @Test
    fun emptyStore_doesNotFail() {
        assertEquals(0, store.count())
        assertTrue(store.list().isEmpty())
        assertTrue(store.pendingIds().isEmpty())
        store.acknowledge(listOf("1_fatal_x"))
        store.clear()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class ErrorReportTest {

    private val environment = ErrorReport.Environment("3.6", 33, "14 (API 34)", "Test device")

    @Test
    fun fromThrowable_fillsFields_andIncludesCauses() {
        val report = ErrorReport.fromThrowable(
            IllegalStateException("outer", IllegalArgumentException("inner")),
            fatal = true, threadName = "main", screen = "PadViewActivity",
            environment = environment, timestamp = 1_700_000_000_000L)

        assertTrue(ErrorReport.isValidId(report.id))
        assertTrue(report.id.startsWith("1700000000000_fatal_"))
        assertEquals(1_700_000_000_000L, ErrorReport.timestampFromId(report.id))
        assertEquals("java.lang.IllegalStateException", report.exceptionClass)
        assertEquals("IllegalStateException", report.shortExceptionClass)
        assertEquals("outer", report.message)
        assertTrue(report.stackTrace.contains("Caused by: java.lang.IllegalArgumentException: inner"))
    }

    @Test
    fun reportText_containsDetailsAndTrace() {
        val report = ErrorReport.fromThrowable(
            RuntimeException("boom"), fatal = false, threadName = "worker",
            screen = "SettingsActivity", environment = environment)

        val text = report.toReportText()
        assertTrue(text.contains("Type: Error"))
        assertTrue(text.contains("App version: 3.6 (33)"))
        assertTrue(text.contains("Android: 14 (API 34)"))
        assertTrue(text.contains("Device: Test device"))
        assertTrue(text.contains("Thread: worker"))
        assertTrue(text.contains("Screen: SettingsActivity"))
        assertTrue(text.contains("java.lang.RuntimeException: boom"))
    }

    @Test
    fun longTraces_areTruncated() {
        val long = "x".repeat(ErrorReport.MAX_STACK_TRACE_CHARS + 100)
        val truncated = ErrorReport.truncate(long)

        assertEquals(
            ErrorReport.MAX_STACK_TRACE_CHARS + ErrorReport.TRUNCATED_SUFFIX.length,
            truncated.length)
        assertTrue(truncated.endsWith(ErrorReport.TRUNCATED_SUFFIX))
        assertEquals("short", ErrorReport.truncate("short"))
    }

    @Test
    fun fromJson_invalid_returnsNull() {
        assertNull(ErrorReport.fromJson("1_error_x", "{}"))
        assertNull(ErrorReport.fromJson("1_error_x", "garbage"))
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class ErrorReporterCrashHandlerTest {

    private var originalHandler: Thread.UncaughtExceptionHandler? = null

    @Before
    fun setUp() {
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
        ErrorReporter.getStore(ApplicationProvider.getApplicationContext()).clear()
    }

    @Test
    fun uncaughtException_isSavedAsPending_andForwardedToPreviousHandler() {
        val app = ApplicationProvider.getApplicationContext<PadlandApp>()
        var forwarded: Throwable? = null
        Thread.setDefaultUncaughtExceptionHandler { _, e -> forwarded = e }

        ErrorReporter.install(app)
        val store = ErrorReporter.getStore(app)
        store.clear()

        val crash = IllegalStateException("crash!")
        Thread.getDefaultUncaughtExceptionHandler()!!.uncaughtException(Thread.currentThread(), crash)

        assertSame(crash, forwarded)
        val pending = store.pendingIds()
        assertEquals(1, pending.size)
        val saved = store.get(pending.first())!!
        assertTrue(saved.fatal)
        assertEquals("crash!", saved.message)
    }

    @Test
    fun install_twice_doesNotWrapTheHandlerTwice() {
        val app = ApplicationProvider.getApplicationContext<PadlandApp>()
        var forwardedCount = 0
        Thread.setDefaultUncaughtExceptionHandler { _, _ -> forwardedCount++ }

        ErrorReporter.install(app)
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        ErrorReporter.install(app)
        assertSame(handler, Thread.getDefaultUncaughtExceptionHandler())

        val store = ErrorReporter.getStore(app)
        store.clear()
        handler!!.uncaughtException(Thread.currentThread(), RuntimeException("once"))

        assertEquals(1, forwardedCount)
        assertEquals(1, store.count())
    }
}

