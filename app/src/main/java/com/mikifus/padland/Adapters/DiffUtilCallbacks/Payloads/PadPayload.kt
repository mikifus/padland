package com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads

sealed interface PadPayload {

    data class NameUrl(val name: String, val url: String) : PadPayload

    data class Url(val url: String) : PadPayload
}