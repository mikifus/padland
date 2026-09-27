package com.mikifus.padland.Utils.ErrorReporting

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Saves error reports in the app private storage, one JSON file per report:
 *
 * ```
 * files/error_reports/<id>.json      the reports
 * files/error_reports/pending/<id>   empty markers: crashes not yet shown to the user
 * ```
 *
 * Writes are synchronous and atomic (temp file + rename), so it is safe to call
 * [save] from an uncaught exception handler right before the process dies.
 */
class ErrorReportStore(
    private val directory: File,
    private val maxReports: Int = DEFAULT_MAX_REPORTS,
) {
    constructor(context: Context) : this(File(context.filesDir, DIRECTORY_NAME))

    private val pendingDirectory = File(directory, PENDING_DIRECTORY_NAME)

    /**
     * @param pending mark the report to be shown to the user on the next run
     * @return true if the report was saved
     */
    @Synchronized
    fun save(report: ErrorReport, pending: Boolean = false): Boolean {
        if (!ErrorReport.isValidId(report.id)) return false

        return try {
            if (!directory.isDirectory && !directory.mkdirs()) return false

            val tmpFile = File(directory, report.id + TMP_EXTENSION)
            FileOutputStream(tmpFile).use { stream ->
                stream.write(report.toJson().toByteArray(Charsets.UTF_8))
                stream.flush()
                stream.fd.sync()
            }

            val reportFile = fileFor(report.id)
            if (!tmpFile.renameTo(reportFile)) {
                tmpFile.delete()
                return false
            }

            if (pending && (pendingDirectory.isDirectory || pendingDirectory.mkdirs())) {
                File(pendingDirectory, report.id).createNewFile()
            }

            prune()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * All the saved reports, newest first. Unreadable files are skipped.
     */
    @Synchronized
    fun list(): List<ErrorReport> = reportIds().mapNotNull { read(it) }

    @Synchronized
    fun count(): Int = reportIds().size

    @Synchronized
    fun get(id: String): ErrorReport? {
        if (!ErrorReport.isValidId(id)) return null
        return read(id)
    }

    @Synchronized
    fun delete(id: String): Boolean {
        if (!ErrorReport.isValidId(id)) return false
        File(pendingDirectory, id).delete()
        return fileFor(id).delete()
    }

    @Synchronized
    fun clear() {
        directory.listFiles()?.forEach { file ->
            if (file.isFile) file.delete()
        }
        pendingDirectory.listFiles()?.forEach { it.delete() }
    }

    /**
     * Ids of the crashes that were not shown to the user yet, newest first.
     * Markers whose report no longer exists are removed.
     */
    @Synchronized
    fun pendingIds(): List<String> {
        val names = pendingDirectory.list() ?: return listOf()
        return names
            .filter { name ->
                val exists = ErrorReport.isValidId(name) && fileFor(name).isFile
                if (!exists) File(pendingDirectory, name).delete()
                exists
            }
            .sortedWith(NEWEST_FIRST)
    }

    /**
     * The user has seen these reports, they are not pending anymore (but still saved).
     */
    @Synchronized
    fun acknowledge(ids: Collection<String>) {
        ids.filter { ErrorReport.isValidId(it) }
            .forEach { File(pendingDirectory, it).delete() }
    }

    private fun fileFor(id: String) = File(directory, id + EXTENSION)

    private fun reportIds(): List<String> {
        val names = directory.list() ?: return listOf()
        return names
            .filter { it.endsWith(EXTENSION) }
            .map { it.removeSuffix(EXTENSION) }
            .filter { ErrorReport.isValidId(it) }
            .sortedWith(NEWEST_FIRST)
    }

    private fun read(id: String): ErrorReport? {
        val file = fileFor(id)
        if (!file.isFile) return null
        return try {
            ErrorReport.fromJson(id, file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Keeps only the newest [maxReports] reports and removes leftover temp files.
     */
    private fun prune() {
        reportIds().drop(maxReports).forEach { id ->
            fileFor(id).delete()
            File(pendingDirectory, id).delete()
        }
        directory.listFiles { file -> file.name.endsWith(TMP_EXTENSION) }
            ?.forEach { it.delete() }
    }

    companion object {
        const val DIRECTORY_NAME = "error_reports"
        const val PENDING_DIRECTORY_NAME = "pending"
        const val DEFAULT_MAX_REPORTS = 50

        private const val EXTENSION = ".json"
        private const val TMP_EXTENSION = ".tmp"

        private val NEWEST_FIRST: Comparator<String> =
            compareByDescending<String> { ErrorReport.timestampFromId(it) ?: Long.MIN_VALUE }
                .thenByDescending { it }
    }
}

