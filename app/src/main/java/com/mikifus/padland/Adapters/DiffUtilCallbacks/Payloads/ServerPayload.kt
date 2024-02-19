package com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads

sealed interface ServerPayload {

    data class NameUrl(val name: String, val url: String) : ServerPayload

    data class Url(val url: String) : ServerPayload
}