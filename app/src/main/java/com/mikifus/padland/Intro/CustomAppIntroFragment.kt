package com.mikifus.padland.Intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.github.paolorotolo.appintro.AppIntroBaseFragment
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder
import com.mikifus.padland.R

/**
 * Created by mikifus on 7/10/16.
 */
open class CustomAppIntroFragment : AppIntroBaseFragment(), ISlideBackgroundColorHolder {
    private var mView: View? = null
    override fun getLayoutId(): Int {
        return R.layout.fragment_intro
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = super.onCreateView(inflater, container, savedInstanceState)
        return mView
    }

    override fun getDefaultBackgroundColor(): Int {
        // Return the default background color of the slide.
        return requireArguments().getInt(ARG_BG_COLOR)
    }

    override fun setBackgroundColor(@ColorInt backgroundColor: Int) {
        // Set the background color of the view within your slide to which the transition should be applied.
        if (mView != null) {
            mView!!.setBackgroundColor(backgroundColor)
        }
    }

    companion object {
        @JvmOverloads
        fun newInstance(title: CharSequence, description: CharSequence,
                        @DrawableRes imageDrawable: Int, @ColorInt bgColor: Int,
                        @ColorInt titleColor: Int = 0, @ColorInt descColor: Int = 0): LinkableAppIntroFragment {
            val slide = LinkableAppIntroFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title.toString())
            args.putString(ARG_TITLE_TYPEFACE, null)
            args.putString(ARG_DESC, description.toString())
            args.putString(ARG_DESC_TYPEFACE, null)
            args.putInt(ARG_DRAWABLE, imageDrawable)
            args.putInt(ARG_BG_COLOR, bgColor)
            args.putInt(ARG_TITLE_COLOR, titleColor)
            args.putInt(ARG_DESC_COLOR, descColor)
            slide.arguments = args
            return slide
        }
    }
}