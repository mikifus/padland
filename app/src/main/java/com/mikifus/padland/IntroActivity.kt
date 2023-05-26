package com.mikifus.padland

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment
import com.mikifus.padland.Intro.CustomAppIntroFragment
import com.mikifus.padland.Intro.LinkableAppIntroFragment

/**
 * Created by mikifus on 7/10/16.
 */
class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Note here that we DO NOT use setContentView();

        // Just set a title, description, background and image. AppIntro will do the rest.
//        addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_first_title),
                getString(R.string.intro_first_desc),
                R.drawable.intro_image1,
                ContextCompat.getColor(this, R.color.intro_first_background)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_second_title),
                getString(R.string.intro_second_desc),
                R.drawable.intro_image2,
                ContextCompat.getColor(this, R.color.intro_second_background)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_third_title),
                getString(R.string.intro_third_desc),
                R.drawable.intro_image3,
                ContextCompat.getColor(this, R.color.intro_third_background)))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_fourth_title),
                getString(R.string.intro_fourth_desc),
                R.drawable.intro_image4,
                ContextCompat.getColor(this, R.color.intro_fourth_background)))
        addSlide(
            CustomAppIntroFragment.newInstance(getString(R.string.intro_fifth_title),
                getString(R.string.intro_fifth_desc),
                R.drawable.intro_image5,
                ContextCompat.getColor(this, R.color.intro_fifth_background)))

        // OPTIONAL METHODS
        // Override bar/separator color.
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true)
        isProgressButtonEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)

        // Launch real main activity
        launchMainActivity()
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)

        // Launch real main activity
        launchMainActivity()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        // Do something when the slide changes.
    }

    protected fun launchMainActivity() {
        val mainIntent = Intent(this@IntroActivity, PadListActivity::class.java)
        startActivity(mainIntent)
        // Finish current activity to avoid back button taking the user here
        finish()
    }
}