package com.mikifus.padland

import android.app.Application
import com.mikifus.padland.Utils.ErrorReporting.ErrorReporter

/**
 * Parent App class
 * @author mikifus
 */
class PadlandApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // First, so crashes during the rest of the start up are saved too
        ErrorReporter.install(this)
    }
}
