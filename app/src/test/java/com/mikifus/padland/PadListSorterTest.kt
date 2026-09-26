package com.mikifus.padland

import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Utils.Sorting.PadListSortOrder
import com.mikifus.padland.Utils.Sorting.PadListSorter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.sql.Date

class PadListSorterTest {

    private fun pad(id: Long, name: String, lastUsed: Long, localName: String = "") =
        Pad().copy(mId = id, mName = name, mLocalName = localName, mLastUsedDate = Date(lastUsed))

    private fun group(id: Long, name: String, pads: List<Pad>) =
        PadGroupsWithPadList(PadGroup().copy(mId = id, mName = name), pads)

    // Millisecond timestamps (as written by the app)
    private val t1 = 1_700_000_000_000L
    private val t2 = 1_700_000_100_000L
    private val t3 = 1_700_000_200_000L

    @Test
    fun padsAlphabetical_isCaseInsensitive_andUsesLocalName() {
        val pads = listOf(
            pad(1, "zeta", t1),
            pad(2, "Beta", t2),
            pad(3, "unused", t3, localName = "alpha"),
        )

        val sorted = PadListSorter.sortPads(pads, PadListSortOrder.ALPHABETICAL)

        assertEquals(listOf(3L, 2L, 1L), sorted.map { it.mId })
    }

    @Test
    fun padsLastAccess_mostRecentFirst_tiesAlphabetical() {
        val pads = listOf(
            pad(1, "b", t1),
            pad(2, "c", t3),
            pad(3, "a", t1),
        )

        val sorted = PadListSorter.sortPads(pads, PadListSortOrder.LAST_ACCESS)

        assertEquals(listOf(2L, 3L, 1L), sorted.map { it.mId })
    }

    @Test
    fun padsLastAccess_handlesDatesStoredInSeconds() {
        // DB default value stores seconds: t2 / 1000 is later than t1 in millis
        val pads = listOf(
            pad(1, "a", t1),
            pad(2, "b", t2 / 1000),
        )

        val sorted = PadListSorter.sortPads(pads, PadListSortOrder.LAST_ACCESS)

        assertEquals(listOf(2L, 1L), sorted.map { it.mId })
    }

    @Test
    fun groupsAlphabetical_sortsGroupsAndInnerPads() {
        val groups = listOf(
            group(1, "Work", listOf(pad(10, "b", t1), pad(11, "a", t3))),
            group(2, "home", listOf(pad(20, "y", t3), pad(21, "x", t1))),
        )

        val sorted = PadListSorter.sortGroups(
            groups, PadListSortOrder.ALPHABETICAL, PadListSortOrder.ALPHABETICAL)

        assertEquals(listOf(2L, 1L), sorted.map { it.padGroup.mId })
        assertEquals(listOf(21L, 20L), sorted[0].padList.map { it.mId })
        assertEquals(listOf(11L, 10L), sorted[1].padList.map { it.mId })
    }

    @Test
    fun groupsLastAccess_usesMostRecentPad_emptyGroupsLast() {
        val groups = listOf(
            group(1, "A", listOf(pad(10, "a", t1), pad(11, "b", t2))),
            group(2, "Empty", listOf()),
            group(3, "B", listOf(pad(30, "c", t3))),
            group(4, "Another empty", listOf()),
        )

        val sorted = PadListSorter.sortGroups(
            groups, PadListSortOrder.LAST_ACCESS, PadListSortOrder.LAST_ACCESS)

        assertEquals(listOf(3L, 1L, 4L, 2L), sorted.map { it.padGroup.mId })
        assertEquals(listOf(11L, 10L), sorted[1].padList.map { it.mId })
    }

    @Test
    fun groupsAndPadsOrdersAreIndependent() {
        val groups = listOf(
            group(1, "B", listOf(pad(10, "a", t1), pad(11, "z", t3))),
            group(2, "A", listOf(pad(20, "a", t2))),
        )

        val sorted = PadListSorter.sortGroups(
            groups, PadListSortOrder.ALPHABETICAL, PadListSortOrder.LAST_ACCESS)

        assertEquals(listOf(2L, 1L), sorted.map { it.padGroup.mId })
        assertEquals(listOf(11L, 10L), sorted[1].padList.map { it.mId })
    }

    @Test
    fun emptyGroupHasNoLastAccess() {
        assertNull(PadListSorter.groupLastAccessMillis(group(1, "x", listOf())))
    }

    @Test
    fun sortOrderFromPreference_fallsBackToDefault() {
        assertEquals(PadListSortOrder.LAST_ACCESS,
            PadListSortOrder.fromPreferenceValue("last_access"))
        assertEquals(PadListSortOrder.DEFAULT,
            PadListSortOrder.fromPreferenceValue(null))
        assertEquals(PadListSortOrder.DEFAULT,
            PadListSortOrder.fromPreferenceValue("garbage"))
    }
}

