package com.mikifus.padland.Views.Helpers

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import androidx.appcompat.widget.ThemedSpinnerAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class SpinnerHelper(
    context: Context, attrs: AttributeSet? = null
) : MaterialAutoCompleteTextView(context, attrs) {

    var selectedItemPosition: Int = 0
        set(value) {
            adapter?.getItem(value)?.let {
                if(text.toString() != it.toString()) {
                    setText(it.toString(), false)
                }
            }
            field = value
        }

    init {
        setOnItemClickListener { _, _, position, _ ->
            selectedItemPosition = position
        }
    }
}