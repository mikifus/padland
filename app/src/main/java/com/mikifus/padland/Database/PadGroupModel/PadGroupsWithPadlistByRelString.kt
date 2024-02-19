package com.mikifus.padland.Database.PadGroupModel

data class PadGroupsWithPadlistByRelString(
    val mPadRelString: String = "",
    val mPadGroupRelString: String = ""

) {
    constructor() : this(
        "", ""
    )
}