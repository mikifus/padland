package com.mikifus.padland.Intro

import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mikifus.padland.R

/**
 * Created by mikifus on 7/10/16.
 */
class LinkableAppIntroFragment : CustomAppIntroFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        val d: TextView
        if (v != null) {
            d = v.findViewById<View>(R.id.description) as TextView
            d.setLinkTextColor(R.color.intro_link_color)
            Linkify.addLinks(d, Linkify.ALL)
        }
        return v
    }
}