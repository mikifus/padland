package com.mikifus.padland.Utils

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import com.mikifus.padland.R
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

        fun padUrl(url: String, context: AppCompatActivity? = null): Builder {
            val urlObject = URL(url)
            this.url = url
            this.host = urlObject.host.toString()

            this.padName = getNameFromCryptPadUrl(url, context?.resources)
            this.padName = this.padName.toString().ifBlank { urlObject.file.substring(
                urlObject.file.lastIndexOf("/") + 1
            ) }

            this.server = urlObject.protocol + "://" + urlObject.authority

            this.prefix = getPrefixFromUrl(url, context?.resources)
            if(this.prefix == "/") {
                this.prefix = ""
            }

            this.baseUrl = getBaseUrlFromServerAndPrefix(this.server.toString(), this.prefix.toString(), context?.resources)

            return this
        }

        private fun getPrefixFromUrl(url: String, resources: Resources? = null): String {
            val cutUrl =  url.substring(this.server!!.length)

            if (resources !== null) {
                val cryptPadPrefixes = resources.getStringArray(R.array.prefixes_cryptpad)
                for (prefix in cryptPadPrefixes) {
                    if (cutUrl.contains(prefix)) {
                        return cutUrl.substring(cutUrl.indexOf(prefix), prefix.length)
                    }
                }
            }

            return cutUrl.substring(0, cutUrl.lastIndexOf("/") + 1)
        }

        private fun getNameFromCryptPadUrl(url: String, resources: Resources?): String {
            if (resources !== null) {
                val cryptPadPrefixes = resources.getStringArray(R.array.prefixes_cryptpad)
                for(prefix in cryptPadPrefixes) {
                    if(url.contains(prefix)) {
                        return url.substring(url.lastIndexOf(prefix) + prefix.length)
                    }
                }
            }
            return ""
        }

        private fun getBaseUrlFromServerAndPrefix(server: String, padPrefix: String, resources: Resources? = null): String {
            var baseUrl = server + padPrefix

            if (resources !== null) {
                val cryptPadPrefixes = resources.getStringArray(R.array.prefixes_cryptpad)
                for(prefix in cryptPadPrefixes) {
                    if(padPrefix == prefix) {
                        baseUrl = server
                        break
                    }
                }
            }

            if(!baseUrl.endsWith("/")) {
                baseUrl += "/"
            }

            return baseUrl
        }

        fun build(): PadServer {
            return PadServer(this)
        }
    }
}