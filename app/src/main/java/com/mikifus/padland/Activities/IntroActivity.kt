package com.mikifus.padland.Activities

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.google.android.material.color.MaterialColors
import com.mikifus.padland.Utils.Intro.LinkableAppIntroFragment
import com.mikifus.padland.R

/**
 * Created by mikifus on 7/10/16.
 */
class IntroActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDoneTextAppearance(R.style.TextAppearance_AppCompat_Large)
        setSkipTextAppearance(R.style.TextAppearance_AppCompat_Large)
        setNextArrowColor(MaterialColors.getColor(this, R.attr.colorOnSurface, 0))
        setBackArrowColor(MaterialColors.getColor(this, R.attr.colorOnSurface, 0))
        setIndicatorColor(
            MaterialColors.getColor(this, R.attr.colorOnSurface, 0),
            getColor(R.color.material_on_surface_disabled)
        )

        // Note here that we DO NOT use setContentView();

        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.createInstance(getString(R.string.intro_first_title),
                getString(R.string.intro_first_desc),
                R.drawable.intro_image1))
        addSlide(AppIntroFragment.createInstance(getString(R.string.intro_second_title),
                getString(R.string.intro_second_desc),
                R.drawable.intro_image2))
        addSlide(AppIntroFragment.createInstance(getString(R.string.intro_third_title),
                getString(R.string.intro_third_desc),
                R.drawable.intro_image3))
        addSlide(AppIntroFragment.createInstance(getString(R.string.intro_fourth_title),
                getString(R.string.intro_fourth_desc),
                R.drawable.intro_image4))
        addSlide(
            LinkableAppIntroFragment.createInstance(
            getString(R.string.intro_fifth_title),
            getString(R.string.intro_fifth_desc),
            R.mipmap.ic_launcher))

        // OPTIONAL METHODS
        // Override bar/separator color.
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));
//        setTransformer

        // Hide Skip/Done button.
        isSkipButtonEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)

        // Launch real main activity
        launchMainActivity()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)

        // Launch real main activity
        launchMainActivity()
    }

    private fun launchMainActivity() {
        val mainIntent = Intent(this@IntroActivity, PadListActivity::class.java)
        startActivity(mainIntent)
        // Finish current activity to avoid back button taking the user here
        finish()
    }
}