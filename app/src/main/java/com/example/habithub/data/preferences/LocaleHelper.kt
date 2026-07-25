package com.example.habithub.data.preferences

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Handles the in-app language selection. The chosen language is stored in
 * SharedPreferences (read synchronously so it can be applied in
 * Activity.attachBaseContext) and applied by wrapping the base context with a
 * configuration that uses the selected locale.
 */
object LocaleHelper {

    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "app_language"

    /** Returns the stored language code (e.g. "de", "en") or null if none is set (follow system). */
    fun getLanguage(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    /** Wraps [context] with the stored locale, or returns it unchanged when no language is set. */
    fun wrap(context: Context): Context {
        val language = getLanguage(context) ?: return context
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
