package com.mikifus.padland

import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Utils.Export.ImportMatcher
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.sql.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34]) // SDK 37 sandbox requires Java 21, tests run with the Java 17 toolchain
class ImportMatcherTest {

    private val date = Date.valueOf("2024-01-01")
    private val pad = Pad(5, "test", "", "https://pad.riseup.net",
        "https://pad.riseup.net/p/test", date, date, 3)

    @Test
    fun filterNew_skipsIdenticalIgnoringIdAndUsage() {
        val imported = pad.copy(mId = 0, mLastUsedDate = Date.valueOf("2020-01-01"), mAccessCount = 0)

        assertEquals(emptyList<Pad>(), ImportMatcher.filterNew(listOf(imported), listOf(pad)))
    }

    @Test
    fun filterNew_keepsDifferent() {
        val imported = listOf(pad.copy(mLocalName = "alias"), pad.copy(mOfflineAccess = true))

        assertEquals(imported, ImportMatcher.filterNew(imported, listOf(pad)))
    }

    @Test
    fun filterNew_comparesDatesAsExported() {
        // Exports keep the day only
        val existing = pad.copy(mCreateDate = Date(date.time + 3_600_000))

        assertEquals(emptyList<Pad>(), ImportMatcher.filterNew(listOf(pad), listOf(existing)))
    }

    @Test
    fun filterNew_skipsRepeatedInImport() {
        val group = PadGroup().copy(mName = "group", mCreateDate = date)

        assertEquals(listOf(group), ImportMatcher.filterNew(listOf(group, group.copy(mId = 7)), emptyList()))
    }
}

