package com.mikifus.padland.Models

/**
 * Created by mikifus on 29/05/16.
 */
class Server {
    var id: Long = 0
    var name: String? = null
    var url: String? = null
    var urlPadprefix: String? = null
    var jquery = false
    var position: String? = null
    val padPrefix: String
        get() = urlPadprefix!!.replace(url!!, "")
    val padPrefixWithUrl: String?
        get() = if (!urlPadprefix!!.startsWith("http")) {
            url + urlPadprefix
        } else urlPadprefix
}