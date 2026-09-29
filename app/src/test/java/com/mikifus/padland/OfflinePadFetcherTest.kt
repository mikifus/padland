package com.mikifus.padland

import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.ServerModel.Server
import com.mikifus.padland.Utils.Offline.OfflinePadFetcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class OfflinePadFetcherTest {

    @Test
    fun isAvailable_builtInServers() {
        assertTrue(isAvailable("https://pad.riseup.net/p/test"))
        // Without pad prefix
        assertTrue(isAvailable("https://pad.ouvaton.coop/test"))
        // CryptPad
        assertFalse(isAvailable("https://cryptpad.fr/pad/#/2/pad/edit/abcdefghijklmnop/"))
    }

    @Test
    fun isAvailable_unknownServer_isNot() {
        assertFalse(isAvailable("https://pad.example.org/p/test"))
    }

    @Test
    fun isAvailable_savedServers() {
        val lite = Server().copy(mUrl = "https://pad.example.org", mPadprefix = "/p/",
            mJquery = true, mCryptPad = false)
        val notLite = Server().copy(mUrl = "https://other.example.org", mPadprefix = "/p/",
            mJquery = false, mCryptPad = false)

        assertTrue(isAvailable("https://pad.example.org/p/test", listOf(lite, notLite)))
        assertFalse(isAvailable("https://other.example.org/p/test", listOf(lite, notLite)))
    }

    @Test
    fun isAvailable_savedServer_overridesBuiltIn() {
        val notLite = Server().copy(mUrl = "https://pad.riseup.net", mPadprefix = "/p/",
            mJquery = false, mCryptPad = false)

        assertFalse(isAvailable("https://pad.riseup.net/p/test", listOf(notLite)))
    }

    @Test
    fun isAvailable_otherHostStartingTheSame_isNot() {
        val lite = Server().copy(mUrl = "https://pad.example.org", mPadprefix = "",
            mJquery = true, mCryptPad = false)

        assertTrue(isAvailable("https://pad.example.org/test", listOf(lite)))
        assertFalse(isAvailable("https://pad.example.org.evil.com/test", listOf(lite)))
    }

    private fun isAvailable(padUrl: String, servers: List<Server> = listOf()): Boolean {
        return OfflinePadFetcher.isAvailable(
            Pad().copy(mUrl = padUrl),
            servers,
            RuntimeEnvironment.getApplication().resources
        )
    }
}

