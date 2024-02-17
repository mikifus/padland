package com.mikifus.padland.Adapters.DiffUtilCallbacks.Payloads

sealed interface ServerPayload {

    data class Name(val name: String) : ServerPayload

    data class Url(val url: String) : ServerPayload
}