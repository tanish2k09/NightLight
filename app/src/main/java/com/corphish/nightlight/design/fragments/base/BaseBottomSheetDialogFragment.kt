package com.corphish.nightlight.design.fragments.base

import android.os.Bundle
import android.view.View
import com.corphish.nightlight.R
import com.corphish.nightlight.data.Constants
import com.corphish.nightlight.design.ThemeUtils
import com.corphish.nightlight.helpers.PreferenceHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, ThemeUtils.getBottomSheetTheme(context!!))
    }

    public fun registerInfoViews(vararg views: View?) {
        var showInfo = PreferenceHelper.getBoolean(context, Constants.PREF_SHOW_INFO, Constants.DEFAULT_SHOW_INFO)
        if (!showInfo) {
            for (v in views) {
                if (v != null) v.visibility = View.GONE
            }
        }
    }
}