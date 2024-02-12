package com.mikifus.padland.Intro

import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.github.appintro.AppIntroBaseFragment
import com.github.appintro.AppIntroFragment
import com.github.appintro.SlideBackgroundColorHolder
import com.github.appintro.model.SliderPage
import com.mikifus.padland.R


class LinkableAppIntroFragment(override val layoutId: Int) : AppIntroBaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.description).movementMethod = LinkMovementMethod.getInstance()
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun createInstance(
            title: CharSequence? = null,
            description: CharSequence? = null,
            @DrawableRes imageDrawable: Int = 0,
            @ColorRes backgroundColorRes: Int = 0,
            @ColorRes titleColorRes: Int = 0,
            @ColorRes descriptionColorRes: Int = 0,
            @FontRes titleTypefaceFontRes: Int = 0,
            @FontRes descriptionTypefaceFontRes: Int = 0,
            @DrawableRes backgroundDrawable: Int = 0
        ) : LinkableAppIntroFragment {
            return createInstance(
                SliderPage(
                    title = title,
                    description = description,
                    imageDrawable = imageDrawable,
                    backgroundColorRes = backgroundColorRes,
                    titleColorRes = titleColorRes,
                    descriptionColorRes = descriptionColorRes,
                    titleTypefaceFontRes = titleTypefaceFontRes,
                    descriptionTypefaceFontRes = descriptionTypefaceFontRes,
                    backgroundDrawable = backgroundDrawable
                )
            )
        }

        /**
         * Generates an [AppIntroFragment] from a given [SliderPage]
         *
         * @param sliderPage the [SliderPage] object which contains all attributes for
         * the current slide
         *
         * @return An [AppIntroFragment] created instance
         */
        @JvmStatic
        fun createInstance(sliderPage: SliderPage): LinkableAppIntroFragment {
            val slide = LinkableAppIntroFragment(com.github.appintro.R.layout.appintro_fragment_intro)
            slide.arguments = sliderPage.toBundle()
            return slide
        }
    }
}