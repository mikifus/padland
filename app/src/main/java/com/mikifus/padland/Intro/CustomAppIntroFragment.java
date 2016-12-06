package com.mikifus.padland.Intro;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.AppIntroBaseFragment;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.mikifus.padland.R;

/**
 * Created by mikifus on 7/10/16.
 */

public class CustomAppIntroFragment extends AppIntroBaseFragment implements ISlideBackgroundColorHolder {

    protected View mView;

    public static LinkableAppIntroFragment newInstance(CharSequence title, CharSequence description,
                                                       @DrawableRes int imageDrawable,
                                                       @ColorInt int bgColor) {
        return newInstance(title, description, imageDrawable, bgColor, 0, 0);
    }

    public static LinkableAppIntroFragment newInstance(CharSequence title, CharSequence description,
                                                       @DrawableRes int imageDrawable, @ColorInt int bgColor,
                                                       @ColorInt int titleColor, @ColorInt int descColor) {
        LinkableAppIntroFragment slide = new LinkableAppIntroFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title.toString());
        args.putString(ARG_TITLE_TYPEFACE, null);
        args.putString(ARG_DESC, description.toString());
        args.putString(ARG_DESC_TYPEFACE, null);
        args.putInt(ARG_DRAWABLE, imageDrawable);
        args.putInt(ARG_BG_COLOR, bgColor);
        args.putInt(ARG_TITLE_COLOR, titleColor);
        args.putInt(ARG_DESC_COLOR, descColor);
        slide.setArguments(args);

        return slide;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_intro;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);
        return mView;
    }

    @Override
    public int getDefaultBackgroundColor() {
        // Return the default background color of the slide.
        return getArguments().getInt(ARG_BG_COLOR);
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        // Set the background color of the view within your slide to which the transition should be applied.
        if (mView != null) {
            mView.setBackgroundColor(backgroundColor);
        }
    }
}
