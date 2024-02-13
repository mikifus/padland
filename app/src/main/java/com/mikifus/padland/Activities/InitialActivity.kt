package com.mikifus.padland.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mikifus.padland.Database.PadListDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InitialActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force Room DB migration
        lifecycleScope.launch(Dispatchers.Main) {
            PadListDatabase.migrateBeforeRoom(this@InitialActivity)
        }

        launchNext()
    }

    private fun launchNext() {

        val userDetails = getSharedPreferences(packageName + "_preferences",
            MODE_PRIVATE
        )
        if(userDetails.getBoolean(OPTION_FIRST_START, true)) {
            // Save first start
            userDetails.edit().putBoolean(OPTION_FIRST_START, false).apply()

            //  Launch app intro
            val i = Intent(this, IntroActivity::class.java)
            startActivity(i)
            finish()
        } else {
            // Launch real main activity
            startActivity(Intent(this, PadListActivity::class.java))
            finish()
        }
    }

    companion object {
        private const val OPTION_FIRST_START = "is_first_start"
    }
}