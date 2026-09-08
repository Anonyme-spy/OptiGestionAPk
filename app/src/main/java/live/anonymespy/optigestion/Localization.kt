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
 *
 * Langue dans laquelle l'application est affichée. Le texte réel réside désormais dans
 * res/values/strings.xml (français, par défaut) et res/values-en/strings.xml
 * — cette énumération identifie simplement le choix et le mappe à une balise BCP-47 pour
 * [AppCompatDelegate.setApplicationLocales].
 *
 * Seuls le Tableau de bord, la Navigation et les Paramètres tirent de strings.xml pour l'instant
 * — Feuilles/Budget/Centres de Coûts/Rapports ont toujours le français codé en dur, comme
 * auparavant ; les migrer revient à copier-coller ce même modèle.
 *
 * Pourquoi AppCompatDelegate au lieu d'un CompositionLocal fait main (l'approche
 * précédente) : il s'agit de l'API standard AndroidX pour la langue par application.
 * Elle persiste automatiquement, s'intègre à l'écran des paramètres "Langues de l'application"
 * du système (Android 13+, via res/xml/locales_config.xml), et fait en sorte que chaque
 * appel `stringResource(R.string.foo)` n'importe où dans l'application — Compose
 * ou non — suive la langue sélectionnée sans plomberie supplémentaire.
 *
 * Nécessite la dépendance Gradle androidx.appcompat:appcompat. Fonctionne parfaitement
 * sans que l'Activity n'étende AppCompatActivity — setApplicationLocales
 * est un appel statique à l'échelle du processus.
 */
enum class AppLanguage(val code: String, val titleResId: Int) {
    SYSTEM("system", R.string.lang_system),
    FRENCH("fr", R.string.lang_french),
    ENGLISH("en", R.string.lang_english)
}

/** Applies [language] as the app's per-app locale. Safe to call repeatedly (e.g. on every app start).
 * Applique [language] comme paramètre régional par application. Peut être appelé à plusieurs reprises sans danger (par exemple, à chaque démarrage de l'application). */
fun applyAppLanguage(language: AppLanguage) {
    if (language == AppLanguage.SYSTEM) {
        // Clearing the app-specific locales tells the system to follow the 
        // device's language settings. It will automatically match the device 
        // language against our supported locales (fr, en) and fallback to 
        // French (default) if no match is found.
        //
        // L'effacement des paramètres régionaux spécifiques à l'application indique au système de suivre les
        // paramètres de langue de l'appareil. Il fera automatiquement correspondre la langue de l'appareil
        // avec nos paramètres régionaux pris en charge (fr, en) et reviendra au
        // français (par défaut) si aucune correspondance n'est trouvée.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
    }
}
