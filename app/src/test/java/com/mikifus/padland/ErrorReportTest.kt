package com.mikifus.padland

import com.mikifus.padland.Utils.ErrorReporting.ErrorReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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

