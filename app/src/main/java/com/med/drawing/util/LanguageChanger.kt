package com.med.drawing.util

import android.content.Context
import android.util.Log
import java.util.Locale

/**
 * @author Ahmed Guedmioui
 */
object LanguageChanger {
    fun changeAppLanguage(languageCode: String, context: Context) {

        val config = context.resources.configuration
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        config.setLocale(locale)

        context.createConfigurationContext(config)

        Log.d("tag_language", "languageCode: $languageCode")
        Log.d("tag_language", "locale: ${locale.language}")

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}