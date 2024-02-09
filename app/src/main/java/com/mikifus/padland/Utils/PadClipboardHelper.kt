package com.mikifus.padland.Utils;

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService


public class PadClipboardHelper {

    companion object {
        fun copyToClipboard(activity: AppCompatActivity, urls: List<String>) {
            val text = if(urls.size > 1) {
                urls.joinToString("\n")
            } else {
                urls[0]
            }

            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("URLs", text)

            clipboard.setPrimaryClip(clip)
        }

        fun getFromClipboard(activity: AppCompatActivity): String {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            return clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        }
    }
}
