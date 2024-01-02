package com.med.drawing.util

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.util.Log
import java.util.Locale

/**
 * @author Ahmed Guedmioui
 */
object LocaleManager {

    fun setLocale(context: Context, language: String): Context {

        val locale = Locale(language)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLocales(localeList)

        Log.d("selected_language", "Locale set to: $locale")
        Log.d("selected_language", "Locale list: $localeList")
        Log.d("selected_language", "language: $language")

        return context.createConfigurationContext(config)

    }

}