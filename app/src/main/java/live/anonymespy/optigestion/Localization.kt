package live.anonymespy.optigestion

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Language the app is displayed in. Actual text now lives in
 * res/values/strings.xml (French, default) and res/values-en/strings.xml
 * — this enum just identifies the choice and maps it to a BCP-47 tag for
 * [AppCompatDelegate.setApplicationLocales].
 *
 * Only Dashboard, Navigation and Settings pull from strings.xml right now
 * — Sheets/Budget/Cost Centers/Reports still hardcode French, same as
 * before; migrating them is copy-pasting this same pattern.
 *
 * Why AppCompatDelegate instead of a hand-rolled CompositionLocal (the
 * previous approach): this is the AndroidX-standard per-app-language API.
 * It persists automatically, integrates with the system's "App languages"
 * settings screen (Android 13+, via res/xml/locales_config.xml), and makes
 * every `stringResource(R.string.foo)` call anywhere in the app — Compose
 * or not — follow the selected language with zero plumbing.
 *
 * Requires the androidx.appcompat:appcompat Gradle dependency. Works fine
 * without the Activity extending AppCompatActivity — setApplicationLocales
 * is a static, process-wide call.
 */
enum class AppLanguage(val code: String, val titleResId: Int) {
    SYSTEM("system", R.string.lang_system),
    FRENCH("fr", R.string.lang_french),
    ENGLISH("en", R.string.lang_english)
}

/** Applies [language] as the app's per-app locale. Safe to call repeatedly (e.g. on every app start). */
fun applyAppLanguage(language: AppLanguage) {
    if (language == AppLanguage.SYSTEM) {
        // Clearing the app-specific locales tells the system to follow the 
        // device's language settings. It will automatically match the device 
        // language against our supported locales (fr, en) and fallback to 
        // French (default) if no match is found.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
    }
}
