package live.anonymespy.optigestion
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import live.anonymespy.optigestion.ui.theme.CaeColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Loads any previously saved entries/budgets/preferences, or leaves
        // everything empty if this is the first launch — OptiGestionRoot
        // decides from there whether to show onboarding or the app itself.
        AppRepository.init(applicationContext)

        setContent {
            // Reading themeMode here means this recomposes (and re-applies
            // the palette) the instant the user changes it in Settings.
            CaeColors.applyMode(AppRepository.themeMode, isSystemInDarkTheme())

            MaterialTheme {
                Surface(modifier = Modifier) {
                    // Pass isWideScreen = true on tablets / foldables / desktop
                    // (e.g. based on WindowSizeClass) to switch to the side rail.
                    OptiGestionRoot(isWideScreen = false)
                }
            }
        }
    }
}