package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import live.anonymespy.optigestion.ui.theme.CaeColors

/**
 * Top-level gate: shows the onboarding template-choice screen until the
 * user has made a choice (fresh install, or after a manual reset), then
 * hands off to the real app.
 */
@Composable
fun OptiGestionRoot(isWideScreen: Boolean = false) {
    if (!AppRepository.hasChosenSetup) {
        OnboardingScreen(
            onLoadTemplate = { AppRepository.loadTemplate() },
            onStartEmpty = { AppRepository.startEmpty() }
        )
    } else {
        CaeAnalyticsApp(isWideScreen = isWideScreen)
    }
}

/**
 * Root composable that ties together all five CAE Analytics screens
 * (Dashboard, Sheets, Analysis -> Budget vs Actual, Cost Centers, Stats ->
 * Reports) with a shared top bar, bottom nav (mobile) and side rail (wide
 * screens).
 *
 * Requires the Navigation Compose dependency:
 *   implementation("androidx.navigation:navigation-compose:2.8.0")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaeAnalyticsApp(isWideScreen: Boolean = false) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.hierarchy?.firstOrNull()?.route
    val currentDestination = NavDestination.entries.find { it.route == currentRoute } ?: NavDestination.DASHBOARD
    var showResetConfirm by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    fun navigateTo(destination: NavDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    AppRepository.resetToOnboarding()
                }) { Text(stringResource(R.string.reset_confirm), color = CaeColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text(stringResource(R.string.reset_cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = CaeColors.Background,
        topBar = {
            if (showSettings) {
                SettingsTopAppBar(onBack = { showSettings = false })
            } else {
                CaeTopAppBar(
                    title = currentDestination.topBarTitle(),
                    periodLabel = AppRepository.periodLabel,
                    onResetClick = { showResetConfirm = true },
                    onSettingsClick = { showSettings = true }
                )
            }
        },
        bottomBar = {
            if (!isWideScreen && !showSettings) {
                CaeBottomNavBar(selected = currentDestination, onSelect = ::navigateTo)
            }
        }
    ) { innerPadding ->
        if (showSettings) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                SettingsScreen()
            }
        } else {
            Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                if (isWideScreen) {
                    CaeSideNavRail(selected = currentDestination, onSelect = ::navigateTo)
                }
                NavHost(
                    navController = navController,
                    startDestination = NavDestination.DASHBOARD.route,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(NavDestination.DASHBOARD.route) { DashboardScreen(onNavigate = ::navigateTo) }
                    composable(NavDestination.SHEETS.route) { SheetsScreen() }
                    composable(NavDestination.ANALYSIS.route) { BudgetVsActualScreen() }
                    composable(NavDestination.COST_CENTERS.route) { CostCentersScreen() }
                    composable(NavDestination.STATS.route) { StatsScreen() }
                }
            }
        }
    }
}

/** Localized label for the bottom nav / side rail. */
@Composable
fun NavDestination.navLabel(): String = when (this) {
    NavDestination.DASHBOARD -> stringResource(R.string.nav_dashboard)
    NavDestination.SHEETS -> stringResource(R.string.nav_sheets)
    NavDestination.ANALYSIS -> stringResource(R.string.nav_budget)
    NavDestination.COST_CENTERS -> stringResource(R.string.nav_cost_centers)
    NavDestination.STATS -> stringResource(R.string.nav_reports)
}

/** Localized title shown in the shared top app bar. */
@Composable
fun NavDestination.topBarTitle(): String = when (this) {
    NavDestination.DASHBOARD -> stringResource(R.string.top_bar_dashboard)
    NavDestination.SHEETS -> stringResource(R.string.top_bar_sheets)
    NavDestination.ANALYSIS -> stringResource(R.string.top_bar_budget)
    NavDestination.COST_CENTERS -> stringResource(R.string.top_bar_cost_centers)
    NavDestination.STATS -> stringResource(R.string.top_bar_reports)
}

/* ============================================================
 *  SHARED TOP APP BAR
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaeTopAppBar(title: String, periodLabel: String, onResetClick: () -> Unit, onSettingsClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(CaeColors.SurfaceContainer)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CaeColors.Primary)
                    Text(text = periodLabel, fontSize = 11.sp, color = CaeColors.OnSurfaceVariant)
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Menu", tint = CaeColors.Primary)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_settings)) },
                        leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onSettingsClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.menu_reset)) },
                        leadingIcon = { Icon(Icons.Filled.RestartAlt, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onResetClick()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CaeColors.Surface,
            titleContentColor = CaeColors.Primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopAppBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.top_bar_settings), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CaeColors.Primary) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = CaeColors.Primary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CaeColors.Surface,
            titleContentColor = CaeColors.Primary
        )
    )
}

/* ============================================================
 *  NAV ICON MAPPING (shared by bottom bar + side rail)
 * ============================================================ */

private fun NavDestination.icon(): ImageVector = when (this) {
    NavDestination.DASHBOARD -> Icons.Filled.Dashboard
    NavDestination.SHEETS -> Icons.Filled.TableChart
    NavDestination.ANALYSIS -> Icons.Filled.Analytics
    NavDestination.COST_CENTERS -> Icons.Filled.AccountTree
    NavDestination.STATS -> Icons.Filled.QueryStats
}

/* ============================================================
 *  BOTTOM NAV BAR (mobile)
 * ============================================================ */

@Composable
private fun CaeBottomNavBar(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    NavigationBar(containerColor = CaeColors.SurfaceContainer) {
        NavDestination.entries.forEach { destination ->
            val label = destination.navLabel()
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = { Icon(imageVector = destination.icon(), contentDescription = label) },
                label = { Text(text = label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CaeColors.OnPrimaryContainer,
                    selectedTextColor = CaeColors.OnPrimaryContainer,
                    indicatorColor = CaeColors.PrimaryContainer,
                    unselectedIconColor = CaeColors.OnSecondaryContainer,
                    unselectedTextColor = CaeColors.OnSecondaryContainer
                )
            )
        }
    }
}

/* ============================================================
 *  SIDE NAV RAIL (wide / desktop screens)
 * ============================================================ */

@Composable
private fun CaeSideNavRail(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(CaeColors.SurfaceContainer)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        NavDestination.entries.forEach { destination ->
            val isSelected = selected == destination
            val label = destination.navLabel()
            Column(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) CaeColors.PrimaryContainer else CaeColors.SurfaceContainer)
                    .clickable { onSelect(destination) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = destination.icon(),
                    contentDescription = label,
                    tint = if (isSelected) CaeColors.OnPrimaryContainer else CaeColors.OnSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (isSelected) CaeColors.OnPrimaryContainer else CaeColors.OnSecondaryContainer
                )
            }
        }
    }
}
