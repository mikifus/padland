package com.mikifus.padland.Utils.Views.Helpers

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.mikifus.padland.R


/**
 * Material Spinners do not exist, an autocomplete view must be used.
 * This is highly problematic for a basic feature.
 *
 * This class eases a bit the trouble, yet it is far from perfect.
 *
 * WARNING: setAdapter() will only work properly if called in onResume()
 *
 * @see https://rmirabelle.medium.com/there-is-no-material-design-spinner-for-android-3261b7c77da8
 */
class SpinnerHelper @JvmOverloads constructor(context: Context,
                                              attributeSet: AttributeSet? = null,
                                              defStyleAttr: Int = R.attr.autoCompleteTextViewStyle)
    : MaterialAutoCompleteTextView(context, attributeSet, defStyleAttr) {

    var helperOnItemSelectedListener: OnItemSelectedListener? = null
    var selectedItemPosition: Int = 0
        /**
         * On setting this the input will display
         * the text for the selected position.
         */
        set(value) {
            if(value < 0) {
                return
            }
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
        setOnItemSelectedListener(object: OnItemSelectedListener {
            override fun onItemSelected(adapter: AdapterView<*>?, view: View?, pos: Int, p3: Long) {
                helperOnItemSelectedListener?.onItemSelected(adapter, view, pos, p3)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                helperOnItemSelectedListener?.onNothingSelected(p0)
            }
        })

        // This doesn't work until a second click, idk why.
        // Yes, I tried setting an OnClickListener that would
        // also not fire until the second click. And I tried
        // an OnTouchListener too.
        inputType = InputType.TYPE_NULL

        // This is terrible but the only way to overcome the a bug
        // that shows the keyboard on click. No workaround, no
        // real solution, it happens for autocomplete views in
        // dialogs.
        isFocusable = false
    }
}