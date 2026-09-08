package live.anonymespy.optigestion

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import live.anonymespy.optigestion.ui.theme.CaeColors

/**
 * OptiGestion Main Activity. Now extends AppCompatActivity to support
 * per-app language switching via AppCompatDelegate.
 *
 * Activité principale d'OptiGestion. Étend désormais AppCompatActivity pour prendre en charge
 * le changement de langue par application via AppCompatDelegate.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Loads any previously saved entries/budgets/preferences (including
        // language, which also applies the per-app locale via
        // AppCompatDelegate — see AppRepository.init()/Localization.kt), or
        // leaves everything empty if this is the first launch —
        // OptiGestionRoot decides from there whether to show onboarding or
        // the app itself.
        //
        // Charge toutes les entrées/budgets/préférences précédemment enregistrés (y compris
        // la langue, qui applique également le paramètre régional par application via
        // AppCompatDelegate — voir AppRepository.init()/Localization.kt), ou
        // laisse tout vide s'il s'agit du premier lancement —
        // OptiGestionRoot décide à partir de là s'il faut afficher l'onboarding ou
        // l'application elle-même.
        SecurePrefs.init(applicationContext)
        AppRepository.init(applicationContext)

        setContent {
            // Reading themeMode here means this recomposes (and re-applies
            // the palette) the instant the user changes it in Settings.
            //
            // Lire themeMode ici signifie que cela se recompose (et réapplique
            // la palette) à l'instant même où l'utilisateur le modifie dans les Paramètres.
            CaeColors.applyMode(AppRepository.themeMode, isSystemInDarkTheme())

            MaterialTheme {
                Surface(modifier = Modifier) {
                    // Pass isWideScreen = true on tablets / foldables / desktop
                    // (e.g. based on WindowSizeClass) to switch to the side rail.
                    //
                    // Passez isWideScreen = true sur les tablettes / pliables / bureau
                    // (ex: basé sur WindowSizeClass) pour basculer vers le rail latéral.
                    OptiGestionRoot(isWideScreen = false)
                }
            }
        }
    }
}
