package com.mikifus.padland.Utils;

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.mikifus.padland.R

class PadShareHelper {

    companion object {
        fun share(activity: AppCompatActivity, subject: String, urls: List<String>) {
            val text = if(urls.size > 1) {
                urls.joinToString("\n")
            } else {
                urls[0]
            }
            ShareCompat.IntentBuilder(activity)
                .setType("text/plain")
                .setChooserTitle(activity.getText(R.string.share_document))
                .setSubject(subject)
                .setText(text)
                .startChooser()
        }
    }
}
