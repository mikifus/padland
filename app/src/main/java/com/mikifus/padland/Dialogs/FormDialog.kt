package com.mikifus.padland.Dialogs


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.annotation.StyleableRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.transition.Slide
import com.google.android.material.color.DynamicColors
import com.google.android.material.transition.MaterialContainerTransform
import com.mikifus.padland.R
import java.util.regex.Pattern


interface IFormDialog {
    var dismissed: Boolean
    var positiveButtonCallback: OnClickListener?
    var negativeButtonCallback: OnClickListener?
    var dismissCallback: (() -> Unit)?
    var resumeCallback: (() -> Unit)?
    var animationOriginView: View?
    var toolbar: Toolbar?
    fun setPositiveButtonCallback(callback: (Map<String, Any>) -> Unit)
    fun setNegativeButtonCallback(callback: () -> Unit)
    fun validateForm(): Boolean { return false }
    fun setFormData(data: HashMap<String, Any>) {}
    fun getFormData(): Map<String, Any> { return mapOf() }
    fun clearForm() {}
    fun initEvents()
    fun initToolBar()
}

open class FormDialog: DialogFragment(), IFormDialog {
    override var dismissed = false

    override var toolbar: Toolbar? = null
    override var animationOriginView: View? = null
    override var positiveButtonCallback: OnClickListener? = null
    override var negativeButtonCallback: OnClickListener? = null
    override var dismissCallback: (() -> Unit)? = null
    override var resumeCallback: (() -> Unit)? = null

    override fun getTheme(): Int {
        return R.style.DialogStyleWhenLarge
    }

    override fun setPositiveButtonCallback(callback: (Map<String, Any>) -> Unit) {
        positiveButtonCallback = OnClickListener { view ->
            if (validateForm()) {
                callback(getFormData())
            }
        }
    }

    override fun setNegativeButtonCallback(callback: () -> Unit) {
        negativeButtonCallback = OnClickListener { view ->
            callback()
            dismissed = true
            dismiss()
        }
    }

    fun setOnResumeCallback(callback: () -> Unit) {
        resumeCallback = callback
    }

    override fun initEvents() {
        val positiveButton = toolbar?.findViewById<Button>(R.id.dialog_positive_button)
        positiveButton?.setOnClickListener(positiveButtonCallback)
    }

    override fun initToolBar() {
        toolbar!!.title = getString(R.string.edit)
        toolbar!!.setNavigationOnClickListener(
            if(negativeButtonCallback != null) {
                negativeButtonCallback
            } else OnClickListener {
                dismiss()
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = view.findViewById(R.id.dialog_toolbar)
        initToolBar()
        initEvents()
    }

    override fun onResume() {
        super.onResume()
        resumeCallback?.let { it() }
    }

    override fun onDestroy() {
        super.onDestroy()
        runDismissCallback()
    }

    override fun dismiss() {
        super.dismiss()

        requireActivity().supportFragmentManager.beginTransaction().apply {
            remove(this@FormDialog)
            commit()
        }.runOnCommit {
            runDismissCallback()
        }
    }

    private fun runDismissCallback() {
        if(!dismissed) {
            dismissCallback?.let { it() }
        }
        dismissed = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // init animations
        val dynamicColorContext: Context = DynamicColors.wrapContextIfAvailable(
            context,
            com.mikifus.padland.R.style.Theme_Material3_DynamicColors_DayNight
        )
        val typedArray = dynamicColorContext.obtainStyledAttributes(intArrayOf(
            R.attr.colorSecondary,
            R.attr.colorSurface,
        ))
        @StyleableRes
        var i = 0
        val secondary = typedArray.getColor(i++, 0)
        val surface = typedArray.getColor(i, 0)
        typedArray.recycle() // recycle TypedArray

        enterTransition = MaterialContainerTransform().apply {
            animationOriginView?.let {
//                drawingViewId = android.R.id.content
                startView = animationOriginView
                endView = view
                duration =
                resources.getInteger(R.integer.material_motion_duration_long_1).toLong() * 2
                scrimColor = Color.TRANSPARENT
                containerColor = Color.TRANSPARENT
                startContainerColor = Color.TRANSPARENT
                endContainerColor = surface
//            setPathMotion(MaterialArcMotion())
                isElevationShadowEnabled = true
            }
        }
        returnTransition = Slide().apply {
            duration = resources.getInteger(R.integer.material_motion_duration_long_1).toLong()
            addTarget(R.id.dialog_layout)
        }
    }

    companion object {
        val NAME_VALIDATION: Pattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\@\\ \\:\\/\\?\\!\\¿\\¡\\#\\|\\&\\=\\·\\$]{2,256}"
        )
    }
}