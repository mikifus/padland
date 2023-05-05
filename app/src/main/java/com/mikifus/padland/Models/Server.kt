package com.mikifus.padland.Models

/**
 * Created by mikifus on 29/05/16.
 */
class Server {
    var id = 0
    var name: String? = null
    var url: String? = null
    var url_padprefix: String? = null
    var jquery = false
    var position: String? = null
    val padPrefix: String
        get() = url_padprefix!!.replace(url!!, "")
    val padPrefixWithUrl: String?
        get() = if (!url_padprefix!!.startsWith("http")) {
            url + url_padprefix
        } else url_padprefix
}