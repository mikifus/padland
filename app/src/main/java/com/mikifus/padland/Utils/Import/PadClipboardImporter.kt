package com.mikifus.padland.Utils.Import

import androidx.appcompat.app.AppCompatActivity
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
     * @return null if the text does not contain any valid URL.
     */
    suspend fun importText(text: String): Result? {
        val parsed = PadUrlImport.parse(text)
        if (parsed.validUrls.isEmpty()) {
            return null
        }

        val result = importUrls(parsed.validUrls)
        return result.copy(
            ignored = result.ignored + parsed.duplicateCount,
            failed = result.failed + parsed.invalidCount,
        )
    }

    /**
     * Imports already validated and deduplicated URLs.
     * Used as well to retry the URLs from unknown servers once one is added.
     */
    suspend fun importUrls(urls: List<String>): Result = withContext(Dispatchers.IO) {
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
            database.padDao().insertAll(pads)
        }

        Result(
            imported = pads.size,
            ignored = classification.alreadySavedCount,
            failed = classification.unknownHostUrls.size + buildFailedCount,
            unknownHostUrls = classification.unknownHostUrls,
        )
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

