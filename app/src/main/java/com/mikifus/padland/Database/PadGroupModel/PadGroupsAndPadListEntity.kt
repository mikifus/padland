package com.mikifus.padland.Database.PadGroupModel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.mikifus.padland.Database.PadModel.Pad

@Entity(
    tableName = "padlist_padgroups",
    primaryKeys = ["_id_group", "_id_pad"],
    foreignKeys = [
        ForeignKey(
            entity = PadGroup::class,
            parentColumns = ["_id"],
            childColumns = ["_id_group"],
        ),
        ForeignKey(
            entity = Pad::class,
            parentColumns = ["_id"],
            childColumns = ["_id_pad"],
        )
    ]
)
data class PadGroupsAndPadListEntity(
    @ColumnInfo(name = "_id_group") val mGroupId: Long,
    @ColumnInfo(name = "_id_pad") val mPadId: Long,
)