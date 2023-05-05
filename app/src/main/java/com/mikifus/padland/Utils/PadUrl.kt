package com.mikifus.padland.Utils

import java.net.MalformedURLException
import java.net.URL

/**
 * Created by mikifus on 16/03/17.
 */
class PadUrl private constructor(builder: Builder) {
    val padName: String?
    val padServer: String?
    val padPrefix: String?

    init {
        padName = builder.name
        padServer = builder.server
        padPrefix = builder.prefix
    }

    @get:Throws(MalformedURLException::class)
    val url: URL
        get() = URL(string)
    val string: String
        get() = makeBaseUrl() + padName

    private fun makeBaseUrl(): String? {
        var local_prefix = padPrefix
        if (local_prefix!!.isEmpty()) {
            throw RuntimeException("The pad url was not correctly built. Check the fconfiguration for this server (" + padServer + ").")
        }
        // Must end with /
        if (!local_prefix.endsWith("/")) {
            local_prefix = "$local_prefix/"
        }
        return local_prefix
    }

    override fun toString(): String {
        return string
    }

    class Builder {
        var name: String? = null
        var server: String? = null
        var prefix: String? = null
        fun padName(name: String): Builder {
            var name = name
            name = name.replace(" ".toRegex(), "_")
            this.name = name
            return this
        }

        fun padServer(server: String?): Builder {
            var server = server
            server = server!!.replace("/$".toRegex(), "") // Remove trailing slash
            this.server = server
            return this
        }

        fun padPrefix(prefix: String?): Builder {
            server = server!!.replace("/$".toRegex(), "") // Remove trailing slash
            this.prefix = prefix
            return this
        }

        fun build(): PadUrl {
            return PadUrl(this)
        }
    }
}