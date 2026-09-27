package com.mikifus.padland.Utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity


class PadClipboardHelper {

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

        /**
         * Text of every item in the clipboard (rich text and URIs coerced to text),
         * joined with new lines.
         */
        fun getAllTextFromClipboard(activity: AppCompatActivity): String {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip ?: return ""

            return (0 until clip.itemCount)
                .mapNotNull { clip.getItemAt(it)?.coerceToText(activity)?.toString() }
                .joinToString("\n")
        }
    }
}
