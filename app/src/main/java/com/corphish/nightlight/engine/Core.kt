package com.corphish.nightlight.engine

import android.content.Context
import android.os.AsyncTask

import com.corphish.nightlight.data.Constants
import com.corphish.nightlight.extensions.fromColorTemperatureToRGBIntArray
import com.corphish.nightlight.helpers.PreferenceHelper
import com.corphish.nightlight.helpers.TimeUtils
import com.corphish.nightlight.services.NightLightAppService

/**
 * Created by Avinaba on 10/4/2017.
 * Basic functions of the app
 */

object Core {
    /**
     * Enables night light based on blueLight and greenLight intensity.
     * It enables KCAL, and writes the intensity
     * Conditionally backup KCAL values if FORCE_SWITCH is off before turning it on
     * Also enable force switch when Night Light is enabled
     * @param context Context is needed for PreferenceHelper
     * @param redValue Value of red light to be set.
     * @param greenValue Value of green light to be set.
     * @param blueValue Value of blue light to be set.
     */
    private fun enableNightMode(context: Context?, redValue: Int, greenValue: Int, blueValue: Int) {
        KCALManager.enableKCAL()

        if (PreferenceHelper.getBoolean(context, Constants.KCAL_PRESERVE_SWITCH, true)) {
            // Check if FORCE_SWITCH is off or not
            // If off then only backup
            // Also backup only if "Backup every time option" is enabled
            if (!PreferenceHelper.getBoolean(context, Constants.PREF_FORCE_SWITCH) && PreferenceHelper.getBoolean(context, Constants.PREF_KCAL_BACKUP_EVERY_TIME, true))
                KCALManager.backupCurrentKCALValues(context)
        }

        val isModeBooting = PreferenceHelper.getBoolean(context, Constants.PREF_BOOT_MODE, false)

        // Assume that set on boot failed by default
        if (isModeBooting) PreferenceHelper.putBoolean(context, Constants.PREF_LAST_BOOT_RES, false)

        val ret = KCALManager.updateKCALValues(redValue, greenValue, blueValue)
        if (isModeBooting) {
            PreferenceHelper.putBoolean(context, Constants.PREF_LAST_BOOT_RES, ret)
        }

        PreferenceHelper.putBoolean(context, Constants.PREF_FORCE_SWITCH, true)
    }

    /**
     * Enables night light based on color temperature
     * It enables KCAL, and writes the intensity
     * Conditionally backup KCAL values if FORCE_SWITCH is off before turning it on
     * Also enable force switch when Night Light is enabled
     * @param context Context is needed for PreferenceHelper
     * @param temperature Color temperature for night light
     */
    private fun enableNightMode(context: Context?, temperature: Int) {
        KCALManager.enableKCAL()

        if (PreferenceHelper.getBoolean(context, Constants.KCAL_PRESERVE_SWITCH, true)) {
            // Check if FORCE_SWITCH is off or not
            // If off then only backup
            if (!PreferenceHelper.getBoolean(context, Constants.PREF_FORCE_SWITCH) && PreferenceHelper.getBoolean(context, Constants.PREF_KCAL_BACKUP_EVERY_TIME, true))
                KCALManager.backupCurrentKCALValues(context)
        }

        val isModeBooting = PreferenceHelper.getBoolean(context, Constants.PREF_BOOT_MODE, false)

        // Assume that set on boot failed by default
        if (isModeBooting) PreferenceHelper.putBoolean(context, Constants.PREF_LAST_BOOT_RES, false)

        val ret = KCALManager.updateKCALValues(temperature.fromColorTemperatureToRGBIntArray())
        if (isModeBooting) PreferenceHelper.putBoolean(context, Constants.PREF_LAST_BOOT_RES, ret)

        PreferenceHelper.putBoolean(context, Constants.PREF_FORCE_SWITCH, true)
    }

    /**
     * Disables night light by setting default color values
     * It does not disable KCAL switch though
     * But it does disable force switch
     * Set the user preserved values for KCAL only if it was enabled and only if **FORCE_SWITCH was on.**
     * @param context Context is needed to read Preference values
     */
    private fun disableNightMode(context: Context?) {
        // First check if KCAL value backup is enabled or not
        val kcalPreserved = PreferenceHelper.getBoolean(context, Constants.KCAL_PRESERVE_SWITCH, true)

        // If KCAL was preserved (enabled by default), set preserved values
        // Otherwise set default values
        if (PreferenceHelper.getBoolean(context, Constants.PREF_FORCE_SWITCH)) {
            if (kcalPreserved)
                KCALManager.updateKCALValues(PreferenceHelper.getString(context, Constants.KCAL_PRESERVE_VAL, Constants.DEFAULT_KCAL_VALUES))
            else
                KCALManager.updateKCALWithDefaultValues()
        }

        PreferenceHelper.putBoolean(context, Constants.PREF_FORCE_SWITCH, false)
    }

    /**
     * Fixes Night Mode setting depending on user preferences
     * @param context Context is needed to fetch user preferences and automation schedules
     * @param prevState Previous state of night light. Will be used in case automation isn't used
     */
    fun fixNightMode(context: Context?, prevState: Boolean = false) {
        val autoEnabled = PreferenceHelper.getBoolean(context, Constants.PREF_AUTO_SWITCH, false)
        val startTime = PreferenceHelper.getString(context, Constants.PREF_START_TIME, Constants.DEFAULT_START_TIME)
        val endTime = PreferenceHelper.getString(context, Constants.PREF_END_TIME, Constants.DEFAULT_END_TIME)

        applyNightModeAsync(
                (!autoEnabled && prevState) || (autoEnabled && startTime != null && endTime != null && TimeUtils.determineWhetherNLShouldBeOnOrNot(startTime, endTime)),
                context
        )
    }

    /**
     * Driver method to enable/disable night light
     * @param e A boolean indicating whether night light should be turned on or off
     * @param context Context is needed to read Preference values
     * @param redValue Value of red light to be set.
     * @param greenValue Value of green light to be set.
     * @param blueValue Value of blue light to be set.
     */
    fun applyNightMode(e: Boolean, context: Context?, redValue: Int, greenValue: Int, blueValue: Int) {
        if (e)
            enableNightMode(context, redValue, greenValue, blueValue)
        else
            disableNightMode(context)
    }

    /**
     * Driver method to enable/disable night light
     * @param e A boolean indicating whether night light should be turned on or off
     * @param context Context is needed to read Preference values
     * @param temperature Color temperature for Night Light
     */
    fun applyNightMode(e: Boolean, context: Context?, temperature: Int) {
        if (e)
            enableNightMode(context, temperature)
        else
            disableNightMode(context)
    }

    /**
     * Driver method to enable/disable night light asynchronously.
     * This is used by QS Tile, AlarmManagers and BroadcastReceivers to do the changes in background
     * @param b A boolean indicating whether night light should be turned on or off
     * @param context Context is needed to read Preference values
     * @param redValue Value of red light to be set.
     * @param greenValue Value of green light to be set.
     * @param blueValue Value of blue light to be set.
     */
    fun applyNightModeAsync(b: Boolean, context: Context?, redValue: Int, greenValue: Int, blueValue: Int) {
        NightModeApplier(b, context, redValue, greenValue, blueValue, true).execute()
    }

    /**
     * Driver method to enable/disable night light asynchronously.
     * This is used by QS Tile, AlarmManagers and BroadcastReceivers to do the changes in background
     * @param b A boolean indicating whether night light should be turned on or off
     * @param context Context is needed to read Preference values
     * @param redValue Value of red light to be set.
     * @param blueValue Value of blue light to be set.
     * @param greenValue Value of green light to be set.
     * @param toUpdateGlobalState Boolean indicating whether or not global state should be updated
     */
    fun applyNightModeAsync(b: Boolean, context: Context?, redValue: Int, greenValue: Int, blueValue: Int, toUpdateGlobalState: Boolean) {
        NightModeApplier(b, context, redValue, greenValue, blueValue, toUpdateGlobalState).execute()
    }

    /**
     * Driver method to enable/disable night light asynchronously.
     * This is used by QS Tile, AlarmManagers and BroadcastReceivers to do the changes in background
     * @param b A boolean indicating whether night light should be turned on or off
     * @param context Context is needed to read Preference values
     * @param temperature Color Temperature for night light
     */
    fun applyNightModeAsync(b: Boolean, context: Context?, temperature: Int) {
        NightModeApplier(b, context, temperature, true).execute()
    }

    /**
     * Driver method to enable/disable night light asynchronously.
     * This is used by QS Tile, AlarmManagers and BroadcastReceivers to do the changes in background
     * @param b A boolean indicating whether night light should be turned on or off
     * @param context Context is needed to read Preference values
     * @param temperature Color temperature for Night Light
     * @param toUpdateGlobalState Boolean indicating whether or not global state should be updated
     */
    fun applyNightModeAsync(b: Boolean, context: Context?, temperature: Int, toUpdateGlobalState: Boolean) {
        NightModeApplier(b, context, temperature, toUpdateGlobalState).execute()
    }

    /**
     * Driver method to enable/disable night light asynchronously.
     * @param b A boolean indicating whether night light should be turned on or off
     * @param context A context parameter to read the intensity values from preferences
     * @param toUpdateGlobalState Boolean indicating whether or not global state should be updated
     * @param intensityType Intensity type. Null if type is to be fetched
     */
    @JvmOverloads
    fun applyNightModeAsync(b: Boolean, context: Context?, toUpdateGlobalState: Boolean = true, intensityType: Int? = null) {
        val mode = PreferenceHelper.getInt(context, Constants.PREF_SETTING_MODE, Constants.NL_SETTING_MODE_TEMP)
        val type = intensityType ?: PreferenceHelper.getInt(context, Constants.PREF_INTENSITY_TYPE, Constants.INTENSITY_TYPE_MAXIMUM)
        if (mode == Constants.NL_SETTING_MODE_MANUAL) {
            applyNightModeAsync(b,
                    context,
                    PreferenceHelper.getInt(context, Constants.PREF_RED_COLOR[type], Constants.DEFAULT_RED_COLOR[type]),
                    PreferenceHelper.getInt(context, Constants.PREF_GREEN_COLOR[type], Constants.DEFAULT_GREEN_COLOR[type]),
                    PreferenceHelper.getInt(context, Constants.PREF_BLUE_COLOR[type], Constants.DEFAULT_BLUE_COLOR[type]),
                    toUpdateGlobalState)
        } else {
            applyNightModeAsync(b,
                    context,
                    PreferenceHelper.getInt(context, Constants.PREF_COLOR_TEMP[type], Constants.DEFAULT_COLOR_TEMP[type]),
                    toUpdateGlobalState)
        }
    }

    /**
     * Driver method to enable/disable night light asynchronously.
     * @param b A boolean indicating whether night light should be turned on or off
     * @param context Tough love for context eh?
     * @param mode Mode of night light setting
     * @param settings Settings for night light
     * @param toUpdateGlobalState Boolean indicating whether or not global state should be updated
     */
    @JvmOverloads
    fun applyNightModeAsync(b: Boolean, context: Context?, mode: Int, settings: IntArray, toUpdateGlobalState: Boolean = false) {
        NightModeApplier(b, context, mode, settings, toUpdateGlobalState).execute()
    }

    /**
     * Toggles intensities, and then applies it
     * @param context Tough love for context eh?
     */
    fun toggleIntensities(context: Context?) {
        var type = PreferenceHelper.getInt(context, Constants.PREF_INTENSITY_TYPE, Constants.INTENSITY_TYPE_MAXIMUM)

        type = (type + 1) % 2

        PreferenceHelper.putInt(context, Constants.PREF_INTENSITY_TYPE, type)

        applyNightModeAsync(true, context)
    }

    /**
     * AsyncTask to enable/disable night light
     */
    private class NightModeApplier : AsyncTask<Any, Any, Any> {
        internal var enabled: Boolean = false
        internal var toUpdateGlobalState: Boolean = false
        internal var mode: Int = 0
        internal var redColor: Int = 0
        internal var greenColor: Int = 0
        internal var blueColor: Int = 0
        internal var temperature: Int = 0
        internal var context: Context?

        internal constructor(enabled: Boolean, context: Context?, redValue: Int, greenValue: Int, blueValue: Int, toUpdateGlobalState: Boolean) {
            this.enabled = enabled
            this.context = context
            this.redColor = redValue
            this.greenColor = greenValue
            this.blueColor = blueValue
            this.toUpdateGlobalState = toUpdateGlobalState

            mode = Constants.NL_SETTING_MODE_MANUAL
        }

        internal constructor(enabled: Boolean, context: Context?, temperature: Int, toUpdateGlobalState: Boolean) {
            this.enabled = enabled
            this.context = context
            this.temperature = temperature
            this.toUpdateGlobalState = toUpdateGlobalState

            mode = Constants.NL_SETTING_MODE_TEMP
        }

        internal constructor(enabled: Boolean, context: Context?, mode: Int, settings: IntArray, toUpdateGlobalState: Boolean) {
            this.enabled = enabled
            this.mode = mode
            this.context = context
            this.toUpdateGlobalState = toUpdateGlobalState

            if (mode == Constants.NL_SETTING_MODE_MANUAL) {
                redColor = settings[0]
                greenColor = settings[1]
                blueColor = settings[2]
            } else if (mode == Constants.NL_SETTING_MODE_TEMP) {
                temperature = settings[0]
            } else {/* There to filter out invalid modes if any */
            }
        }

        override fun doInBackground(vararg bubbles: Any): Any? {
            if (mode == Constants.NL_SETTING_MODE_MANUAL)
                applyNightMode(enabled, context, redColor, greenColor, blueColor)
            else
                applyNightMode(enabled, context, temperature)
            return null
        }

        override fun onPostExecute(bubble: Any?) {
            // If this is run by set on boot units, set BOOT_MODE false
            if (PreferenceHelper.getBoolean(context, Constants.PREF_BOOT_MODE, false))
                PreferenceHelper.putBoolean(context, Constants.PREF_BOOT_MODE, false)

            if (NightLightAppService.instance.isAppServiceRunning && toUpdateGlobalState)
                NightLightAppService.instance.notifyUpdatedState(enabled)
        }
    }
}
