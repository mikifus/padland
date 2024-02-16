package com.mikifus.padland.Utils.Export.Maps

import com.mikifus.padland.Database.PadGroupModel.PadGroup
import com.mikifus.padland.Database.PadModel.Pad
import com.mikifus.padland.Database.ServerModel.Server

class DatabaseMap(
    override val app: String,
    override val className: String,
    override val version: Double,
    val padland_servers: List<Server>? = null,
    val padgroups: List<PadGroup>? = null,
    val padlist: List<Pad>? = null
): IGenericMap