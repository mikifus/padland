package com.mikifus.padland.Utils.ErrorReporting

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * A single saved error (exception) with everything needed to report it.
 *
 * Stored as one JSON file per report by [ErrorReportStore].
 */
data class ErrorReport(
    /** Also the file name (without extension), see [ErrorReportStore] */
    val id: String,
    val timestamp: Long,
    /** True when the error crashed the app (uncaught exception) */
    val fatal: Boolean,
    val exceptionClass: String,
    val message: String?,
    val stackTrace: String,
    val threadName: String,
    /** Screen (activity) in the foreground when the error happened, if known */
    val screen: String?,
    val appVersionName: String,
    val appVersionCode: Long,
    val androidVersion: String,
    val device: String,
) {

    val shortExceptionClass: String
        get() = exceptionClass.substringAfterLast('.')

    fun toJson(): String = JSONObject().apply {
        put(KEY_ID, id)
        put(KEY_TIMESTAMP, timestamp)
        put(KEY_FATAL, fatal)
        put(KEY_EXCEPTION_CLASS, exceptionClass)
        put(KEY_MESSAGE, message ?: JSONObject.NULL)
        put(KEY_STACK_TRACE, stackTrace)
        put(KEY_THREAD_NAME, threadName)
        put(KEY_SCREEN, screen ?: JSONObject.NULL)
        put(KEY_APP_VERSION_NAME, appVersionName)
        put(KEY_APP_VERSION_CODE, appVersionCode)
        put(KEY_ANDROID_VERSION, androidVersion)
        put(KEY_DEVICE, device)
    }.toString()

    /**
     * Plain text version meant to be copied and pasted in a bug report.
     */
    fun toReportText(): String = buildString {
        append("Padland error report\n")
        append("Type: ").append(if (fatal) "Crash" else "Error").append('\n')
        append("Date: ").append(formatTimestamp(timestamp)).append('\n')
        append("App version: ").append(appVersionName).append(" (").append(appVersionCode).append(")\n")
        append("Android: ").append(androidVersion).append('\n')
        append("Device: ").append(device).append('\n')
        append("Thread: ").append(threadName).append('\n')
        screen?.let { append("Screen: ").append(it).append('\n') }
        append('\n')
        append(stackTrace)
    }

    /**
     * App and device details. Resolved once, so they are ready when the app is crashing.
     */
    data class Environment(
        val appVersionName: String,
        val appVersionCode: Long,
        val androidVersion: String,
        val device: String,
    ) {
        companion object {
            val UNKNOWN = Environment("unknown", 0, deviceAndroidVersion(), deviceName())

            fun fromContext(context: Context): Environment {
                return try {
                    val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName, PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }
                    Environment(
                        appVersionName = packageInfo.versionName ?: "unknown",
                        appVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                        androidVersion = deviceAndroidVersion(),
                        device = deviceName(),
                    )
                } catch (e: Exception) {
                    UNKNOWN
                }
            }

            private fun deviceAndroidVersion(): String =
                "${Build.VERSION.RELEASE ?: "?"} (API ${Build.VERSION.SDK_INT})"

            private fun deviceName(): String =
                "${Build.MANUFACTURER ?: "?"} ${Build.MODEL ?: "?"}"
        }
    }

    companion object {
        /** Keeps files and clipboard contents at a reasonable size */
        const val MAX_STACK_TRACE_CHARS = 32_000
        const val TRUNCATED_SUFFIX = "\n… (truncated)"

        private val ID_PATTERN = Regex("^[A-Za-z0-9_]{1,100}$")

        private const val KEY_ID = "id"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_FATAL = "fatal"
        private const val KEY_EXCEPTION_CLASS = "exceptionClass"
        private const val KEY_MESSAGE = "message"
        private const val KEY_STACK_TRACE = "stackTrace"
        private const val KEY_THREAD_NAME = "threadName"
        private const val KEY_SCREEN = "screen"
        private const val KEY_APP_VERSION_NAME = "appVersionName"
        private const val KEY_APP_VERSION_CODE = "appVersionCode"
        private const val KEY_ANDROID_VERSION = "androidVersion"
        private const val KEY_DEVICE = "device"

        /**
         * Ids are used as file names, only allow safe characters.
         */
        fun isValidId(id: String): Boolean = ID_PATTERN.matches(id)

        /**
         * Ids start with the timestamp, so sorting by name also sorts by date.
         */
        fun makeId(timestamp: Long, fatal: Boolean): String {
            val random = UUID.randomUUID().toString().replace("-", "").take(8)
            return "${timestamp}_${if (fatal) "fatal" else "error"}_$random"
        }

        /** Timestamp encoded in the id, null if the id has another format */
        fun timestampFromId(id: String): Long? = id.substringBefore('_').toLongOrNull()

        fun fromThrowable(
            throwable: Throwable,
            fatal: Boolean,
            threadName: String,
            screen: String?,
            environment: Environment,
            timestamp: Long = System.currentTimeMillis(),
        ): ErrorReport {
            return ErrorReport(
                id = makeId(timestamp, fatal),
                timestamp = timestamp,
                fatal = fatal,
                exceptionClass = throwable.javaClass.name,
                message = throwable.message,
                stackTrace = truncate(safeStackTrace(throwable)),
                threadName = threadName,
                screen = screen,
                appVersionName = environment.appVersionName,
                appVersionCode = environment.appVersionCode,
                androidVersion = environment.androidVersion,
                device = environment.device,
            )
        }

        /**
         * @param id taken from the file name, it wins over the one inside the JSON
         * @return null if the JSON is not a valid report
         */
        fun fromJson(id: String, json: String): ErrorReport? {
            return try {
                val obj = JSONObject(json)
                ErrorReport(
                    id = id,
                    timestamp = obj.getLong(KEY_TIMESTAMP),
                    fatal = obj.optBoolean(KEY_FATAL, false),
                    exceptionClass = obj.getString(KEY_EXCEPTION_CLASS),
                    message = obj.optStringOrNull(KEY_MESSAGE),
                    stackTrace = obj.getString(KEY_STACK_TRACE),
                    threadName = obj.optString(KEY_THREAD_NAME, ""),
                    screen = obj.optStringOrNull(KEY_SCREEN),
                    appVersionName = obj.optString(KEY_APP_VERSION_NAME, "unknown"),
                    appVersionCode = obj.optLong(KEY_APP_VERSION_CODE, 0),
                    androidVersion = obj.optString(KEY_ANDROID_VERSION, ""),
                    device = obj.optString(KEY_DEVICE, ""),
                )
            } catch (e: JSONException) {
                null
            }
        }

        fun formatTimestamp(timestamp: Long): String =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US).format(Date(timestamp))

        internal fun truncate(text: String, maxChars: Int = MAX_STACK_TRACE_CHARS): String {
            if (text.length <= maxChars) return text
            return text.take(maxChars) + TRUNCATED_SUFFIX
        }

        private fun safeStackTrace(throwable: Throwable): String {
            return try {
                throwable.stackTraceToString()
            } catch (e: Throwable) {
                // Broken toString() or similar, keep at least something useful
                "${throwable.javaClass.name}\n(stack trace unavailable: ${e.javaClass.name})"
            }
        }

        private fun JSONObject.optStringOrNull(key: String): String? =
            if (isNull(key)) null else optString(key)
    }
}

