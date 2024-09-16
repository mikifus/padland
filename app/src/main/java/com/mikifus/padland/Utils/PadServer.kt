package com.mikifus.padland.Utils

import java.net.URL

class PadServer private constructor(builder: PadServer.Builder) {
    val server: String?
    val host: String?
    val prefix: String?
    val padName: String?
    val baseUrl: String?

    override fun toString(): String {
        return baseUrl?: ""
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
            this.padName = urlObject.file.substring(
                urlObject.file.lastIndexOf("/") + 1
            )
            this.server = urlObject.protocol + "://" + urlObject.authority
            this.prefix = getPrefixFromUrl(url)
            if(this.prefix == "/") {
                this.prefix = ""
            }
            this.baseUrl = this.server + this.prefix
            if(!this.baseUrl!!.endsWith("/")) {
                this.baseUrl += "/"
            }

            return this
        }

        private fun getPrefixFromUrl(url: String): String {
            val cutUrl =  url.substring(this.server!!.length)
            return cutUrl.substring(0, cutUrl.lastIndexOf("/") + 1)
        }

        fun build(): PadServer {
            return PadServer(this)
        }
    }
}