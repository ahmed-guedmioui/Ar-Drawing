package com.ardrawing.sketchtrace.util

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import java.util.Locale

/**
 * @author Ahmed Guedmioui
 */
object LanguageChanger {
    fun changeAppLanguage(languageCode: String, context: Context): ContextWrapper {

        val config = context.resources.configuration
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        config.setLocale(locale)

        context.createConfigurationContext(config)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        return ContextWrapper(context)
    }
}