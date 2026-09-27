package com.mikifus.padland.Utils.ErrorReporting

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.mikifus.padland.Activities.InitialActivity
import com.mikifus.padland.Activities.IntroActivity
import com.mikifus.padland.Dialogs.ErrorReportDialog
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

/**
 * Central point for error reporting.
 *
 * - Every reported error is saved with [ErrorReportStore], whatever the settings.
 * - Crashes (uncaught exceptions) are saved synchronously and marked as pending,
 *   then shown in a popup the next time the app runs.
 * - Non fatal errors reported with [reportNonFatal] are shown in a popup right away.
 * - Popups are only shown when enabled in the settings ([KEY_SHOW_POPUP]).
 *
 * Call [install] once from [Application.onCreate].
 */
object ErrorReporter {
    private const val TAG = "ERROR_REPORTER"

    const val KEY_SHOW_POPUP = "padland_debug_error_popup"
    const val DEFAULT_SHOW_POPUP = false

    /**
     * What a popup has to show. [previousCrash] popups acknowledge
     * the pending markers when closed by the user.
     */
    data class PopupRequest(val ids: List<String>, val previousCrash: Boolean)

    @Volatile
    private var store: ErrorReportStore? = null

    @Volatile
    private var environment: ErrorReport.Environment = ErrorReport.Environment.UNKNOWN

    @Volatile
    private var resumedActivity: WeakReference<AppCompatActivity>? = null

    /** Last screen seen by the user, kept after it is paused to name the crash origin */
    @Volatile
    private var lastScreenName: String? = null

    private val executor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "padland-error-reporter").apply { isDaemon = true }
        }
    }
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    // Only accessed from the main thread
    private val popupQueue = ArrayDeque<PopupRequest>()
    private var pendingCrashesChecked = false

    private val handlingCrash = AtomicBoolean(false)

    fun install(application: Application) {
        store = ErrorReportStore(application)
        environment = ErrorReport.Environment.fromContext(application)

        val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
        if (currentHandler !is CrashHandler) {
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(currentHandler))
        }

        application.registerActivityLifecycleCallbacks(LifecycleCallbacks)
    }

    /**
     * The store in use, or a new one if [install] was not called (tests).
     */
    fun getStore(context: Context): ErrorReportStore {
        return store ?: synchronized(this) {
            store ?: ErrorReportStore(context.applicationContext).also { store = it }
        }
    }

    fun isPopupEnabled(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_SHOW_POPUP, DEFAULT_SHOW_POPUP)

    /**
     * Saves an unexpected (but caught) error and shows it if popups are enabled.
     * Do not use it for validation or expected failures.
     */
    fun reportNonFatal(throwable: Throwable, screen: String? = null) {
        Log.e(TAG, "Non fatal error reported", throwable)
        if (store == null) return

        val threadName = Thread.currentThread().name
        val currentScreen = screen ?: currentScreenName()

        executor.execute {
            val report = saveReport(throwable, false, threadName, currentScreen, pending = false)
                ?: return@execute
            mainHandler.post {
                enqueuePopup(PopupRequest(listOf(report.id), previousCrash = false))
            }
        }
    }

    fun reportNonFatal(throwable: Throwable, activity: Activity) {
        reportNonFatal(throwable, activity.javaClass.simpleName)
    }

    /**
     * Called by [ErrorReportDialog] when the user closes it.
     */
    fun onPopupClosed(request: PopupRequest) {
        if (request.previousCrash) {
            val currentStore = store ?: return
            executor.execute { currentStore.acknowledge(request.ids) }
        }
    }

    /**
     * Called by [ErrorReportDialog] when it is destroyed. If the user did not close it
     * (e.g. the activity finished) the request is shown again in the next screen.
     */
    fun onPopupDestroyed(request: PopupRequest?, closedByUser: Boolean) {
        mainHandler.post {
            if (request != null && !closedByUser) {
                popupQueue.addFirst(request)
            }
            showNextPopup()
        }
    }

    private fun saveReport(
        throwable: Throwable,
        fatal: Boolean,
        threadName: String,
        screen: String?,
        pending: Boolean,
    ): ErrorReport? {
        val currentStore = store ?: return null
        val report = ErrorReport.fromThrowable(throwable, fatal, threadName, screen, environment)
        return if (currentStore.save(report, pending)) report else null
    }

    private fun currentScreenName(): String? = lastScreenName

    private fun enqueuePopup(request: PopupRequest) {
        popupQueue.addLast(request)
        showNextPopup()
    }

    private fun canShowPopupIn(activity: AppCompatActivity): Boolean =
        activity !is InitialActivity && activity !is IntroActivity && !activity.isFinishing

    /**
     * Shows the next queued popup in the resumed activity, one at a time.
     */
    private fun showNextPopup() {
        val activity = resumedActivity?.get() ?: return
        if (!canShowPopupIn(activity)) return

        val fragmentManager = activity.supportFragmentManager
        if (fragmentManager.isDestroyed || fragmentManager.isStateSaved) return
        if (fragmentManager.findFragmentByTag(ErrorReportDialog.DIALOG_TAG) != null) return

        if (!isPopupEnabled(activity)) {
            // Reports stay saved, just don't bother the user
            val requests = popupQueue.toList()
            popupQueue.clear()
            requests.filter { it.previousCrash }.forEach { onPopupClosed(it) }
            return
        }

        val request = popupQueue.removeFirstOrNull() ?: return
        try {
            val dialog = ErrorReportDialog()
            dialog.setReportIds(request.ids,
                if (request.previousCrash) ErrorReportDialog.MODE_PREVIOUS_CRASH
                else ErrorReportDialog.MODE_POPUP)
            dialog.show(fragmentManager, ErrorReportDialog.DIALOG_TAG)
        } catch (e: IllegalStateException) {
            popupQueue.addFirst(request)
        }
    }

    /**
     * Once per process, when the first real screen shows up, look for crashes
     * saved during the previous run.
     */
    private fun checkPendingCrashes() {
        if (pendingCrashesChecked) return
        val currentStore = store ?: return
        pendingCrashesChecked = true

        executor.execute {
            val ids = currentStore.pendingIds()
            if (ids.isEmpty()) return@execute
            mainHandler.post {
                enqueuePopup(PopupRequest(ids, previousCrash = true))
            }
        }
    }

    private class CrashHandler(
        private val previousHandler: Thread.UncaughtExceptionHandler?):
        Thread.UncaughtExceptionHandler {

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            // Avoid saving twice if several threads crash at the same time
            if (handlingCrash.compareAndSet(false, true)) {
                try {
                    saveReport(throwable, true, thread.name, currentScreenName(), pending = true)
                } catch (e: Throwable) {
                    // Never hide the original crash
                } finally {
                    handlingCrash.set(false)
                }
            }

            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, throwable)
            } else {
                Process.killProcess(Process.myPid())
                exitProcess(10)
            }
        }
    }

    private object LifecycleCallbacks: Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            lastScreenName = activity.javaClass.simpleName
            val appCompatActivity = activity as? AppCompatActivity ?: return
            resumedActivity = WeakReference(appCompatActivity)

            if (canShowPopupIn(appCompatActivity)) {
                checkPendingCrashes()
                showNextPopup()
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (resumedActivity?.get() === activity) {
                resumedActivity = null
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}

