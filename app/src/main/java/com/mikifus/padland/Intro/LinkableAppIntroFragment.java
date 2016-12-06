package com.mikifus.padland.Intro;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikifus.padland.R;

/**
 * Created by mikifus on 7/10/16.
 */

public class LinkableAppIntroFragment extends CustomAppIntroFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        TextView d;
        if (v != null) {
            d = (TextView) v.findViewById(R.id.description);
            d.setLinkTextColor(R.color.intro_link_color);
            Linkify.addLinks(d,Linkify.ALL);
        }
        return v;
    }
}
