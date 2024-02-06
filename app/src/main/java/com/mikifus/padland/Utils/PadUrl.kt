package com.mikifus.padland.Utils

import java.net.MalformedURLException
import java.net.URL

/**
 * Created by mikifus on 16/03/17.
 */
class PadUrl private constructor(builder: Builder) {
    val padName: String?
    val padServer: String?
    private val padPrefix: String?

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

    private fun makeBaseUrl(): String {
        var localPrefix = padPrefix
        if (localPrefix!!.isEmpty()) {
            throw RuntimeException("The pad url was not correctly built. Check the configuration for this server ($padServer).")
        }
        // Must end with /
        if (!localPrefix.endsWith("/")) {
            localPrefix = "$localPrefix/"
        }
        return localPrefix
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
            val parsedServer = server!!.replace("/$".toRegex(), "") // Remove trailing slash
            this.server = parsedServer
            return this
        }

        fun padPrefix(prefix: String?): Builder {
            val parsedPrefix = prefix!!.replace("/$".toRegex(), "") // Remove trailing slash
            this.prefix = parsedPrefix
            return this
        }

        fun build(): PadUrl {
            return PadUrl(this)
        }
    }
}