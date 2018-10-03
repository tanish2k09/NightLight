package com.corphish.nightlight.design.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.corphish.nightlight.data.Constants
import com.corphish.nightlight.engine.Core
import com.corphish.nightlight.helpers.PreferenceHelper
import com.corphish.nightlight.R
import com.corphish.nightlight.design.utils.FontUtils
import com.corphish.nightlight.services.NightLightAppService
import kotlinx.android.synthetic.main.layout_force_switch.*

/**
 * Created by Avinaba on 10/24/2017.
 * Force switch fragment
 */

class ForceSwitchFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_force_switch, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        FontUtils().setCustomFont(context!!, forceSwitch)

        forceSwitch.isChecked = PreferenceHelper.getBoolean(context, Constants.PREF_FORCE_SWITCH)
        forceSwitch.setOnCheckedChangeListener { _, b ->
            // Preference for this is handled in Core now
            Core.applyNightModeAsync(b, context)
        }
    }

    fun updateSwitch(newState: Boolean) {
        if (forceSwitch != null) forceSwitch.isChecked = newState
    }
}
