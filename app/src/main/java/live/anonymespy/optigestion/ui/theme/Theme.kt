package live.anonymespy.optigestion.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Colour tokens shared by every OptiGestion screen (Dashboard, Sheets,
 * Analysis, Stats). Each token is backed by `mutableStateOf`, so calling
 * [CaeColors.applyMode] updates every screen that reads a CaeColors.*
 * property live, with no extra plumbing required.
 *
 * Jetons de couleur partagés par chaque écran d'OptiGestion (Tableau de bord, Feuilles,
 * Analyse, Statistiques). Chaque jeton est soutenu par `mutableStateOf`, donc l'appel à
 * [applyMode] met à jour chaque écran qui lit une propriété CaeColors.*
 * en direct, sans plomberie supplémentaire requise.
 */
object CaeColors {
    var Primary by mutableStateOf(LightPalette.Primary)
    var OnPrimary by mutableStateOf(LightPalette.OnPrimary)
    var PrimaryContainer by mutableStateOf(LightPalette.PrimaryContainer)
    var OnPrimaryContainer by mutableStateOf(LightPalette.OnPrimaryContainer)
    var PrimaryFixed by mutableStateOf(LightPalette.PrimaryFixed)

    var Secondary by mutableStateOf(LightPalette.Secondary)
    var OnSecondaryContainer by mutableStateOf(LightPalette.OnSecondaryContainer)
    var SecondaryContainer by mutableStateOf(LightPalette.SecondaryContainer)

    var TertiaryContainer by mutableStateOf(LightPalette.TertiaryContainer)
    var OnTertiaryContainer by mutableStateOf(LightPalette.OnTertiaryContainer)
    var TertiaryFixedDim by mutableStateOf(LightPalette.TertiaryFixedDim)

    var Error by mutableStateOf(LightPalette.Error)
    var ErrorContainer by mutableStateOf(LightPalette.ErrorContainer)
    var OnErrorContainer by mutableStateOf(LightPalette.OnErrorContainer)

    var Background by mutableStateOf(LightPalette.Background)
    var Surface by mutableStateOf(LightPalette.Surface)
    var SurfaceContainer by mutableStateOf(LightPalette.SurfaceContainer)
    var SurfaceContainerHigh by mutableStateOf(LightPalette.SurfaceContainerHigh)
    var SurfaceContainerLow by mutableStateOf(LightPalette.SurfaceContainerLow)
    var SurfaceContainerLowest by mutableStateOf(LightPalette.SurfaceContainerLowest)
    var SurfaceVariant by mutableStateOf(LightPalette.SurfaceVariant)
    var OnSurface by mutableStateOf(LightPalette.OnSurface)
    var OnSurfaceVariant by mutableStateOf(LightPalette.OnSurfaceVariant)
    var Outline by mutableStateOf(LightPalette.Outline)
    var OutlineVariant by mutableStateOf(LightPalette.OutlineVariant)

    var InverseSurface by mutableStateOf(LightPalette.InverseSurface)
    var InverseOnSurface by mutableStateOf(LightPalette.InverseOnSurface)

    var SurfaceTint by mutableStateOf(LightPalette.SurfaceTint)

    /** Elevated "card" surface used on the Stats screen. Literal white only in light mode.
     * Surface de "carte" surélevée utilisée sur l'écran des Statistiques. Blanc littéral uniquement en mode clair. */
    var White by mutableStateOf(LightPalette.White)

    // Status pill colors used on the Sheets screen.
    // Couleurs des pilules de statut utilisées sur l'écran des Feuilles.
    var ApprovedBg by mutableStateOf(LightPalette.ApprovedBg)
    var ApprovedText by mutableStateOf(LightPalette.ApprovedText)
    var PendingBg by mutableStateOf(LightPalette.PendingBg)
    var PendingText by mutableStateOf(LightPalette.PendingText)
    var RejectedBg by mutableStateOf(LightPalette.RejectedBg)
    var RejectedText by mutableStateOf(LightPalette.RejectedText)

    /** Swaps every token over to the palette matching [mode] (resolving SYSTEM against [systemInDarkTheme]).
     * Bascule chaque jeton vers la palette correspondant au [mode] (résolution de SYSTEM par rapport à [systemInDarkTheme]). */
    fun applyMode(mode: ThemeMode, systemInDarkTheme: Boolean) {
        val palette: Palette = when (mode) {
            ThemeMode.SYSTEM -> if (systemInDarkTheme) DarkPalette else LightPalette
            ThemeMode.LIGHT -> LightPalette
            ThemeMode.DARK -> DarkPalette
            ThemeMode.AMOLED -> AmoledPalette
        }
        Primary = palette.Primary
        OnPrimary = palette.OnPrimary
        PrimaryContainer = palette.PrimaryContainer
        OnPrimaryContainer = palette.OnPrimaryContainer
        PrimaryFixed = palette.PrimaryFixed
        Secondary = palette.Secondary
        OnSecondaryContainer = palette.OnSecondaryContainer
        SecondaryContainer = palette.SecondaryContainer
        TertiaryContainer = palette.TertiaryContainer
        OnTertiaryContainer = palette.OnTertiaryContainer
        TertiaryFixedDim = palette.TertiaryFixedDim
        Error = palette.Error
        ErrorContainer = palette.ErrorContainer
        OnErrorContainer = palette.OnErrorContainer
        Background = palette.Background
        Surface = palette.Surface
        SurfaceContainer = palette.SurfaceContainer
        SurfaceContainerHigh = palette.SurfaceContainerHigh
        SurfaceContainerLow = palette.SurfaceContainerLow
        SurfaceContainerLowest = palette.SurfaceContainerLowest
        SurfaceVariant = palette.SurfaceVariant
        OnSurface = palette.OnSurface
        OnSurfaceVariant = palette.OnSurfaceVariant
        Outline = palette.Outline
        OutlineVariant = palette.OutlineVariant
        InverseSurface = palette.InverseSurface
        InverseOnSurface = palette.InverseOnSurface
        SurfaceTint = palette.SurfaceTint
        White = palette.White
        ApprovedBg = palette.ApprovedBg
        ApprovedText = palette.ApprovedText
        PendingBg = palette.PendingBg
        PendingText = palette.PendingText
        RejectedBg = palette.RejectedBg
        RejectedText = palette.RejectedText
    }
}

/** How the person wants the app's colour scheme chosen. Persisted in AppRepository.
 * Comment la personne souhaite que le schéma de couleurs de l'application soit choisi. Persisté dans AppRepository. */
enum class ThemeMode(val label: String) {
    SYSTEM("Système"),
    LIGHT("Clair"),
    DARK("Sombre"),
    AMOLED("Sombre (AMOLED)")
}

/* ---------------- Palettes ---------------- */
/* ---------------- Palettes ---------------- */

/**
 * One full set of colour tokens. LightPalette/DarkPalette/AmoledPalette are
 * all instances of this same type, which is what lets `applyMode`'s `when`
 * expression read `.Primary`, `.Background`, etc. off whichever one it picks.
 *
 * Un ensemble complet de jetons de couleur. LightPalette/DarkPalette/AmoledPalette sont
 * toutes des instances de ce même type, ce qui permet à l'expression `when` de `applyMode`
 * de lire `.Primary`, `.Background`, etc. à partir de celle qu'elle choisit.
 */
private data class Palette(
    val Primary: Color,
    val OnPrimary: Color,
    val PrimaryContainer: Color,
    val OnPrimaryContainer: Color,
    val PrimaryFixed: Color,
    val Secondary: Color,
    val OnSecondaryContainer: Color,
    val SecondaryContainer: Color,
    val TertiaryContainer: Color,
    val OnTertiaryContainer: Color,
    val TertiaryFixedDim: Color,
    val Error: Color,
    val ErrorContainer: Color,
    val OnErrorContainer: Color,
    val Background: Color,
    val Surface: Color,
    val SurfaceContainer: Color,
    val SurfaceContainerHigh: Color,
    val SurfaceContainerLow: Color,
    val SurfaceContainerLowest: Color,
    val SurfaceVariant: Color,
    val OnSurface: Color,
    val OnSurfaceVariant: Color,
    val Outline: Color,
    val OutlineVariant: Color,
    val InverseSurface: Color,
    val InverseOnSurface: Color,
    val SurfaceTint: Color,
    val White: Color, // elevated "card" surface — literal white only in the light palette
                      // surface de "carte" surélevée — blanc littéral uniquement dans la palette claire
    val ApprovedBg: Color,
    val ApprovedText: Color,
    val PendingBg: Color,
    val PendingText: Color,
    val RejectedBg: Color,
    val RejectedText: Color
)

private val LightPalette = Palette(
    Primary = Color(0xFF031635),
    OnPrimary = Color(0xFFFFFFFF),
    PrimaryContainer = Color(0xFF1A2B4B),
    OnPrimaryContainer = Color(0xFF8293B8),
    PrimaryFixed = Color(0xFFD8E2FF),
    Secondary = Color(0xFF5D5F5F),
    OnSecondaryContainer = Color(0xFF616363),
    SecondaryContainer = Color(0xFFDFE0E0),
    TertiaryContainer = Color(0xFF003320),
    OnTertiaryContainer = Color(0xFF00A774),
    TertiaryFixedDim = Color(0xFF4EDEA3),
    Error = Color(0xFFBA1A1A),
    ErrorContainer = Color(0xFFFFDAD6),
    OnErrorContainer = Color(0xFF93000A),
    Background = Color(0xFFF8F9FB),
    Surface = Color(0xFFF8F9FB),
    SurfaceContainer = Color(0xFFEDEEF0),
    SurfaceContainerHigh = Color(0xFFE7E8EA),
    SurfaceContainerLow = Color(0xFFF3F4F6),
    SurfaceContainerLowest = Color(0xFFFFFFFF),
    SurfaceVariant = Color(0xFFE1E2E4),
    OnSurface = Color(0xFF191C1E),
    OnSurfaceVariant = Color(0xFF44474E),
    Outline = Color(0xFF75777F),
    OutlineVariant = Color(0xFFC5C6CF),
    InverseSurface = Color(0xFF2E3132),
    InverseOnSurface = Color(0xFFF0F1F3),
    SurfaceTint = Color(0xFF4E5E81),
    White = Color(0xFFFFFFFF),
    ApprovedBg = Color(0xFFE6F4EA),
    ApprovedText = Color(0xFF00512E),
    PendingBg = Color(0xFFFEF7E0),
    PendingText = Color(0xFFB08B00),
    RejectedBg = Color(0xFFFCE8E6),
    RejectedText = Color(0xFF93000A)
)

/** Regular dark theme — dark grey surfaces, not pure black.
 * Thème sombre normal — surfaces gris foncé, pas de noir pur. */
private val DarkPalette = Palette(
    Primary = Color(0xFFAFC6FF),
    OnPrimary = Color(0xFF0A2350),
    PrimaryContainer = Color(0xFF29406B),
    OnPrimaryContainer = Color(0xFFD8E2FF),
    PrimaryFixed = Color(0xFFD8E2FF),
    Secondary = Color(0xFFC5C6CF),
    OnSecondaryContainer = Color(0xFFC5C6CF),
    SecondaryContainer = Color(0xFF404244),
    TertiaryContainer = Color(0xFF00522F),
    OnTertiaryContainer = Color(0xFF4EDEA3),
    TertiaryFixedDim = Color(0xFF4EDEA3),
    Error = Color(0xFFFFB4AB),
    ErrorContainer = Color(0xFF93000A),
    OnErrorContainer = Color(0xFFFFDAD6),
    Background = Color(0xFF121316),
    Surface = Color(0xFF121316),
    SurfaceContainer = Color(0xFF1E2023),
    SurfaceContainerHigh = Color(0xFF292B2E),
    SurfaceContainerLow = Color(0xFF191B1E),
    SurfaceContainerLowest = Color(0xFF0C0D0F),
    SurfaceVariant = Color(0xFF3D4046),
    OnSurface = Color(0xFFE3E2E6),
    OnSurfaceVariant = Color(0xFFC4C6D0),
    Outline = Color(0xFF8E9099),
    OutlineVariant = Color(0xFF44474E),
    InverseSurface = Color(0xFFE3E2E6),
    InverseOnSurface = Color(0xFF2E3132),
    SurfaceTint = Color(0xFFAFC6FF),
    White = Color(0xFF1E2023),
    ApprovedBg = Color(0xFF14432B),
    ApprovedText = Color(0xFF6FDE9C),
    PendingBg = Color(0xFF473510),
    PendingText = Color(0xFFF5CB5C),
    RejectedBg = Color(0xFF4C1A17),
    RejectedText = Color(0xFFFFB4AB)
)

/** AMOLED theme — same accents as Dark but true black surfaces to save battery on OLED screens.
 * Thème AMOLED — mêmes accents que Sombre mais surfaces noir pur pour économiser la batterie sur les écrans OLED. */
private val AmoledPalette = Palette(
    Primary = DarkPalette.Primary,
    OnPrimary = DarkPalette.OnPrimary,
    PrimaryContainer = Color(0xFF162238),
    OnPrimaryContainer = DarkPalette.OnPrimaryContainer,
    PrimaryFixed = DarkPalette.PrimaryFixed,
    Secondary = DarkPalette.Secondary,
    OnSecondaryContainer = DarkPalette.OnSecondaryContainer,
    SecondaryContainer = Color(0xFF1C1D1F),
    TertiaryContainer = Color(0xFF00301B),
    OnTertiaryContainer = DarkPalette.OnTertiaryContainer,
    TertiaryFixedDim = DarkPalette.TertiaryFixedDim,
    Error = DarkPalette.Error,
    ErrorContainer = DarkPalette.ErrorContainer,
    OnErrorContainer = DarkPalette.OnErrorContainer,
    Background = Color(0xFF000000),
    Surface = Color(0xFF000000),
    SurfaceContainer = Color(0xFF0A0A0A),
    SurfaceContainerHigh = Color(0xFF141414),
    SurfaceContainerLow = Color(0xFF050505),
    SurfaceContainerLowest = Color(0xFF000000),
    SurfaceVariant = Color(0xFF262626),
    OnSurface = DarkPalette.OnSurface,
    OnSurfaceVariant = DarkPalette.OnSurfaceVariant,
    Outline = DarkPalette.Outline,
    OutlineVariant = Color(0xFF303030),
    InverseSurface = DarkPalette.InverseSurface,
    InverseOnSurface = DarkPalette.InverseOnSurface,
    SurfaceTint = DarkPalette.SurfaceTint,
    White = Color(0xFF0A0A0A),
    ApprovedBg = DarkPalette.ApprovedBg,
    ApprovedText = DarkPalette.ApprovedText,
    PendingBg = DarkPalette.PendingBg,
    PendingText = DarkPalette.PendingText,
    RejectedBg = DarkPalette.RejectedBg,
    RejectedText = DarkPalette.RejectedText
)