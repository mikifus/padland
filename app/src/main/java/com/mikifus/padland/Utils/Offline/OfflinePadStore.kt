package com.mikifus.padland.Utils.Offline

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Saves the offline copies of the pads in the app private storage,
 * one HTML file per pad:
 *
 * ```
 * files/offline_pads/<pad id>.html
 * ```
 *
 * The copies are a cache, they are not exported.
 * Writes are atomic (temp file + rename), a copy is never left half written.
 */
class OfflinePadStore(private val directory: File) {
    constructor(context: Context) : this(File(context.filesDir, DIRECTORY_NAME))

    @Synchronized
    fun save(padId: Long, html: String): Boolean {
        return try {
            if (!directory.isDirectory && !directory.mkdirs()) return false

            val tmpFile = File(directory, padId.toString() + TMP_EXTENSION)
            FileOutputStream(tmpFile).use { stream ->
                stream.write(html.toByteArray(Charsets.UTF_8))
                stream.flush()
                stream.fd.sync()
            }

            if (!tmpFile.renameTo(fileFor(padId))) {
                tmpFile.delete()
                return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    fun get(padId: Long): String? {
        val file = fileFor(padId)
        if (!file.isFile) return null
        return try {
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    fun has(padId: Long): Boolean = fileFor(padId).isFile

    /**
     * @return when the copy was saved, in milliseconds, or null if there is no copy
     */
    fun getSavedTime(padId: Long): Long? {
        val file = fileFor(padId)
        return if (file.isFile) file.lastModified() else null
    }

    @Synchronized
    fun delete(padId: Long): Boolean = fileFor(padId).delete()

    fun count(): Int = directory.listFiles { file -> file.name.endsWith(EXTENSION) }?.size ?: 0

    /**
     * Removes all the copies, i.e. to free space.
     */
    @Synchronized
    fun clear() {
        directory.listFiles()?.forEach { it.delete() }
    }

    /**
     * Removes the copies of the pads not in [padIds], i.e. deleted pads or pads
     * without offline access anymore, and any leftover temp file.
     */
    @Synchronized
    fun keepOnly(padIds: Collection<Long>) {
        directory.listFiles()?.forEach { file ->
            val padId = file.name.removeSuffix(EXTENSION).toLongOrNull()
            if (!file.name.endsWith(EXTENSION) || padId == null || padId !in padIds) {
                file.delete()
            }
        }
    }

    private fun fileFor(padId: Long) = File(directory, padId.toString() + EXTENSION)

    companion object {
        const val DIRECTORY_NAME = "offline_pads"

        private const val EXTENSION = ".html"
        private const val TMP_EXTENSION = ".tmp"
    }
}

