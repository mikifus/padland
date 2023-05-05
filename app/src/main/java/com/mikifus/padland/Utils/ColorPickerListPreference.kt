package com.mikifus.padland.Utils

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.ListPreference
import android.util.AttributeSet
import com.mikifus.padland.R

/**
 * Created by mikifus on 2/10/16.
 */
class ColorPickerListPreference(private val mContext: Context, attrs: AttributeSet?) : ListPreference(mContext, attrs) {
    private var dialog: HSVColorPickerDialog? = null
    private var showing = false
    override fun showDialog(state: Bundle) {
        dialog = HSVColorPickerDialog(mContext, intValue) { color -> // Do something with the selected color
            val hexColor = String.format("#%06X", 0xFFFFFF and color)
            value = hexColor
        }
        dialog!!.setTitle(R.string.settings_default_color_dialogtitle)
        dialog!!.show()
        showing = true
        dialog!!.setOnDismissListener { showing = false }
    }

    fun reload() {
        if (dialog != null && showing) {
            dialog!!.dismiss()
            val handler = Handler()
            handler.postDelayed({ showDialog(null) }, 300)
        }
    }

    private val intValue: Int
        private get() = value.substring(1).toInt(16)
}