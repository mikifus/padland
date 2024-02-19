package com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads

import com.mikifus.padland.Database.PadModel.Pad

sealed interface PadGroupPayload {

    data class Title(val title: String) : PadGroupPayload

    data class TitlePadList(val title: String, val padList: List<Pad>) : PadGroupPayload
}