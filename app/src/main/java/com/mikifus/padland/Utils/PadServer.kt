package com.mikifus.padland.Utils

import java.net.URL

class PadServer private constructor(builder: PadServer.Builder) {
    val server: String?
    val host: String?
    val prefix: String?
    val padName: String?

    override fun toString(): String {
        return server?: ""
    }

    init {
        server = builder.server
        host = builder.host
        prefix = builder.prefix
        padName = builder.padName
    }

    class Builder {
        var url: String? = null
        var server: String? = null
        var host: String? = null
        var prefix: String? = null
        var padName: String? = null
        var baseUrl: String? = null

        fun padUrl(url: String): Builder {
            val urlObject = URL(url)
            this.url = url
            this.host = urlObject.host.toString()
            this.padName = urlObject.file.substring(
                urlObject.file.lastIndexOf("/") + 1
            )
            this.server = urlObject.protocol + "://" + urlObject.authority
            this.prefix = url
                .substring(0, url.lastIndexOf("/") + 1)
                .substring(this.server!!.length)
            if(prefix == "/") {
                prefix = ""
            }

            return this
        }

        fun build(): PadServer {
            return PadServer(this)
        }
    }
}