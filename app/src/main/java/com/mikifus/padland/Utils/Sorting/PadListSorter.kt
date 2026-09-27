package com.mikifus.padland.Utils.Sorting

import com.mikifus.padland.Database.PadGroupModel.PadGroupsWithPadList
import com.mikifus.padland.Database.PadModel.Pad
import java.text.Collator

/**
 * Sorts the nested pad list: groups and the documents inside each group.
 *
 * - Alphabetical: locale aware, case and accent insensitive, ascending.
 * - Last access: most recently accessed first.
 *   For groups, the last access is the most recent access of the documents
 *   it contains. Empty groups go at the end.
 *
 * Ties are always resolved deterministically (by name, then by id) so the
 * list does not "jump" between refreshes.
 */
object PadListSorter {

    /**
     * Dates written by the database default value (strftime('%s','now')) are
     * stored in seconds, while the app writes milliseconds. Any value below this
     * threshold (~ year 1973 in millis, ~ year 5138 in seconds) is treated as seconds.
     */
    private const val SECONDS_THRESHOLD = 100_000_000_000L

    fun padDisplayName(pad: Pad): String = pad.mLocalName.ifBlank { pad.mName }

    fun padLastAccessMillis(pad: Pad): Long {
        val time = pad.mLastUsedDate.time
        return if (time in 1 until SECONDS_THRESHOLD) time * 1000 else time
    }

    /**
     * Group last access, derived from its documents. Null if the group is empty.
     */
    fun groupLastAccessMillis(group: PadGroupsWithPadList): Long? =
        group.padList.maxOfOrNull { padLastAccessMillis(it) }

    fun sortPads(pads: List<Pad>, order: PadListSortOrder): List<Pad> {
        val alphabetical = alphabeticalComparator<Pad>({ padDisplayName(it) }, { it.mId })

        val comparator = when (order) {
            PadListSortOrder.ALPHABETICAL -> alphabetical
            PadListSortOrder.LAST_ACCESS ->
                compareByDescending<Pad> { padLastAccessMillis(it) }.then(alphabetical)
        }

        return pads.sortedWith(comparator)
    }

    fun sortGroups(
        groups: List<PadGroupsWithPadList>,
        groupOrder: PadListSortOrder,
        padOrder: PadListSortOrder
    ): List<PadGroupsWithPadList> {
        val groupsWithSortedPads = groups.map {
            it.copy(padList = sortPads(it.padList, padOrder))
        }

        val alphabetical = alphabeticalComparator<PadGroupsWithPadList>(
            { it.padGroup.mName }, { it.padGroup.mId })

        val comparator = when (groupOrder) {
            PadListSortOrder.ALPHABETICAL -> alphabetical
            PadListSortOrder.LAST_ACCESS ->
                // Most recent first, empty groups (null) last
                compareBy<PadGroupsWithPadList, Long?>(nullsLast(reverseOrder<Long>())) {
                    groupLastAccessMillis(it)
                }.then(alphabetical)
        }

        return groupsWithSortedPads.sortedWith(comparator)
    }

    private fun <T> alphabeticalComparator(
        name: (T) -> String,
        id: (T) -> Long
    ): Comparator<T> {
        // Collator is not thread safe: a new instance per sort operation
        val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
        return Comparator<T> { a, b -> collator.compare(name(a), name(b)) }
            .thenBy(name)
            .thenBy(id)
    }
}



