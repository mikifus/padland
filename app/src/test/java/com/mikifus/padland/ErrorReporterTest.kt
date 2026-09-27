package com.mikifus.padland

import androidx.test.core.app.ApplicationProvider
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class ErrorReporterTest {

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
        val errorReportStore = ErrorReporter.getStore(app)
        errorReportStore.clear()

        val crash = IllegalStateException("crash!")
        Thread.getDefaultUncaughtExceptionHandler()!!.uncaughtException(Thread.currentThread(), crash)

        assertSame(crash, forwarded)
        val pending = errorReportStore.pendingIds()
        assertEquals(1, pending.size)
        val saved = errorReportStore.get(pending.first())!!
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

        val errorReportStore = ErrorReporter.getStore(app)
        errorReportStore.clear()
        handler!!.uncaughtException(Thread.currentThread(), RuntimeException("once"))

        assertEquals(1, forwardedCount)
        assertEquals(1, errorReportStore.count())
    }
}

