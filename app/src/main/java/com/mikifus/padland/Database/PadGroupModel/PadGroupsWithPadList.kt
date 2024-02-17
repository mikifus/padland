package com.mikifus.padland.Database.PadGroupModel

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.Relation
import com.mikifus.padland.Database.PadModel.Pad

@Entity
data class PadGroupsWithPadList(
    @Embedded var padGroup: PadGroup,
    @Relation(
        entity = Pad::class,
        parentColumn = "_id",
        entityColumn = "_id",
        associateBy = Junction(
            value = PadGroupsAndPadList::class,
            parentColumn = "_id_group",
            entityColumn = "_id_pad"
        )
    )
    val padList: List<Pad>
) {
    fun isPartiallyDifferentFrom(padGroupsWithPadList: PadGroupsWithPadList): Boolean {
        return padGroup.isPartiallyDifferentFrom(padGroupsWithPadList.padGroup) ||
//                padGroup.isPartiallyDifferentFrom(padGroupsWithPadList.padGroup)
            padList.size != padGroupsWithPadList.padList.size
//            padList.any {
//                it.isPartiallyDifferentFrom(padGroupsWithPadList.padList.indexOf())
//            }
    }
}