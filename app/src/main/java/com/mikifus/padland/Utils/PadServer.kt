package com.mikifus.padland.Utils

import java.net.URL

class PadServer private constructor(builder: PadServer.Builder) {
    val server: String?
    val host: String?
    val prefix: String?
    val padName: String?
    val baseUrl: String?

    override fun toString(): String {
        return server?: ""
    }

    init {
        server = builder.server
        host = builder.host
        prefix = builder.prefix
        padName = builder.padName
        baseUrl = builder.baseUrl
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
            this.padName = urlObject.file.toString()
            this.server = url.substring(0, url.lastIndexOf("/") + 1)
            this.baseUrl = urlObject.protocol + "://" + urlObject.host;
            this.prefix = url
                .substring(0, url.lastIndexOf("/") + 1)
                .substring(this.baseUrl!!.length)

            return this
        }

        fun build(): PadServer {
            return PadServer(this)
        }
    }
}