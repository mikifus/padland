package com.mikifus.padland;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.mikifus.padland.Intro.LinkableAppIntroFragment;

/**
 * Created by mikifus on 7/10/16.
 */

public class IntroActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Note here that we DO NOT use setContentView();

        // Just set a title, description, background and image. AppIntro will do the rest.
//        addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_first_title),
                getString(R.string.intro_first_desc),
                R.drawable.intro_image1,
                ContextCompat.getColor(this, R.color.intro_first_background)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_second_title),
                getString(R.string.intro_second_desc),
                R.drawable.intro_image2,
                ContextCompat.getColor(this, R.color.intro_second_background)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_third_title),
                getString(R.string.intro_third_desc),
                R.drawable.intro_image3,
                ContextCompat.getColor(this, R.color.intro_third_background)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_fourth_title),
                getString(R.string.intro_fourth_desc),
                R.drawable.intro_image4,
                ContextCompat.getColor(this, R.color.intro_fourth_background)));

        addSlide(LinkableAppIntroFragment.newInstance(getString(R.string.intro_fifth_title),
                getString(R.string.intro_fifth_desc),
                R.drawable.intro_image5,
                ContextCompat.getColor(this, R.color.intro_fifth_background)));

        // OPTIONAL METHODS
        // Override bar/separator color.
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        // Launch real main activity
        launchMainActivity();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        // Launch real main activity
        launchMainActivity();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }

    protected void launchMainActivity() {
        Intent mainIntent = new Intent(IntroActivity.this, PadListActivity.class);
        startActivity(mainIntent);
        // Finish current activity to avoid back button taking the user here
        IntroActivity.this.finish();
    }
}
