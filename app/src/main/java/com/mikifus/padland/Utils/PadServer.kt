package com.mikifus.padland.Utils

class PadServer private constructor(builder: PadServer.Builder) {
    val server: String?
    val padName: String?

    override fun toString(): String {
        return server?: ""
    }

    init {
        server = builder.server
        padName = builder.padName
    }

    class Builder {
        var url: String? = null
        var server: String? = null
        var padName: String? = null

        fun padUrl(url: String): Builder {
            this.url = url
            this.server = url.substring(0, url.lastIndexOf("/") + 1)
            this.padName = url.substring(url.lastIndexOf("/") + 1)
            return this
        }

        fun build(): PadServer {
            return PadServer(this)
        }
    }
}