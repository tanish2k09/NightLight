package com.corphish.nightlight.design.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.corphish.nightlight.BuildConfig

import com.corphish.nightlight.helpers.ExternalLink
import com.corphish.nightlight.R
import kotlinx.android.synthetic.main.layout_info.*

/**
 * Created by Avinaba on 10/24/2017.
 * About fragment
 */

class AboutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        view?.setOnClickListener { ExternalLink.open(context, "market://details?id=" + context!!.packageName) }
        contributorsLink.setOnClickListener { ExternalLink.open(context, "https://github.com/corphish/NightLight/graphs/contributors") }
        githubLink.setOnClickListener { ExternalLink.open(context, "https://github.com/corphish/NightLight/") }
        xdaLink.setOnClickListener { ExternalLink.open(context, "https://forum.xda-developers.com/android/apps-games/app-night-light-kcal-t3689090") }

        val versionText = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        version.text = versionText
    }
}
