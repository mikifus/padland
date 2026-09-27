package com.mikifus.padland.Utils.Import

import android.database.sqlite.SQLiteConstraintException
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.Database.PadGroupModel.PadGroupsAndPadList
import com.mikifus.padland.Database.PadListDatabase
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.R
import com.mikifus.padland.Utils.PadClipboardHelper
import com.mikifus.padland.Utils.PadServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Imports a list of pad URLs (one per line) into the unclassified list.
 *
 * @see PadUrlImport for the parsing and counting rules.
 */
class PadClipboardImporter(private val activity: AppCompatActivity) {

    data class Result(
        val imported: Int,
        val ignored: Int,
        val failed: Int,
        /** Subset of the failed URLs: valid, but from a server that is not whitelisted */
        val unknownHostUrls: List<String>,
    )

    private val database by lazy { PadListDatabase.getInstance(activity.applicationContext) }

    /**
     * Must be called from the main thread while the activity has focus,
     * otherwise Android (10+) denies clipboard access.
     */
    fun readClipboard(): String = PadClipboardHelper.getAllTextFromClipboard(activity)

    /**
     * Imports the valid URLs of an already parsed text.
     * The caller is expected to check [PadUrlImport.ParseResult.validUrls] is not empty.
     *
     * @param groupId target group, 0 for unclassified
     */
    suspend fun importParsed(parsed: PadUrlImport.ParseResult, groupId: Long = 0): Result {
        val result = importUrls(parsed.validUrls, groupId)
        return result.copy(
            ignored = result.ignored + parsed.duplicateCount,
            failed = result.failed + parsed.invalidCount,
        )
    }

    /**
     * Imports already validated and deduplicated URLs.
     * Used as well to retry the URLs from unknown servers once one is added.
     *
     * @param groupId target group, 0 for unclassified
     */
    suspend fun importUrls(urls: List<String>, groupId: Long = 0): Result = withContext(Dispatchers.IO) {
        val classification = PadUrlImport.classify(
            urls,
            database.padDao().getAllUrls(),
            getHostsWhitelist(),
        )

        val pads = mutableListOf<Pad>()
        var buildFailedCount = 0
        classification.toImport.forEach { url ->
            val pad = makePad(url)
            if (pad != null) pads.add(pad) else buildFailedCount++
        }

        if (pads.isNotEmpty()) {
            insertPads(pads, groupId)
        }

        Result(
            imported = pads.size,
            ignored = classification.alreadySavedCount,
            failed = classification.unknownHostUrls.size + buildFailedCount,
            unknownHostUrls = classification.unknownHostUrls,
        )
    }

    /**
     * Inserts the pads and their group relation atomically. If the group no longer
     * exists (deleted meanwhile, foreign key failure) the transaction is rolled back
     * and the pads are imported as unclassified instead.
     */
    private fun insertPads(pads: List<Pad>, groupId: Long) {
        if (groupId <= 0) {
            database.padDao().insertAll(pads)
            return
        }

        try {
            database.runInTransaction(Runnable {
                val padIds = database.padDao().insertAll(pads)
                database.padGroupDao().insertPadGroupsWithPadlistBlocking(
                    padIds.map { PadGroupsAndPadList(mGroupId = groupId, mPadId = it) }
                )
            })
        } catch (e: SQLiteConstraintException) {
            database.padDao().insertAll(pads)
        }
    }

    /**
     * Same sources as the pad viewer: enabled servers plus the built-in whitelist.
     */
    private suspend fun getHostsWhitelist(): List<String> {
        val serverHosts = database.serverDao().getAllEnabledList().mapNotNull {
            try {
                URL(it.mUrl).host
            } catch (e: Exception) {
                null
            }
        }
        return serverHosts + activity.resources.getStringArray(R.array.etherpad_servers_whitelist)
    }

    /**
     * Equivalent to Pad.fromUrl() but without LiveData, so it can run off the main thread.
     */
    private fun makePad(url: String): Pad? {
        return try {
            val padServer = PadServer.Builder().padUrl(url, activity).build()
            Pad().copy(
                mUrl = url,
                mName = padServer.padName ?: "",
                mServer = padServer.server ?: "",
            )
        } catch (e: Exception) {
            null
        }
    }
}

