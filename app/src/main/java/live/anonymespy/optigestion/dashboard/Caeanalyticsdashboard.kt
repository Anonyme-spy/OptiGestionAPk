package live.anonymespy.optigestion.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ActivityIcon

object CaeColors {
    val Primary = Color(0xFF031635)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1A2B4B)
    val OnPrimaryContainer = Color(0xFF8293B8)

    val Secondary = Color(0xFF5D5F5F)
    val OnSecondaryContainer = Color(0xFF616363)
    val SecondaryContainer = Color(0xFFDFE0E0)

    val TertiaryContainer = Color(0xFF003320)
    val OnTertiaryContainer = Color(0xFF00A774)

    val Error = Color(0xFFBA1A1A)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val Background = Color(0xFFF8F9FB)
    val Surface = Color(0xFFF8F9FB)
    val SurfaceContainer = Color(0xFFEDEEF0)
    val SurfaceContainerLow = Color(0xFFF3F4F6)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFE1E2E4)
    val OnSurface = Color(0xFF191C1E)
    val OnSurfaceVariant = Color(0xFF44474E)
    val Outline = Color(0xFF75777F)
    val OutlineVariant = Color(0xFFC5C6CF)

    val InverseSurface = Color(0xFF2E3132)
    val InverseOnSurface = Color(0xFFF0F1F3)

    val SurfaceTint = Color(0xFF4E5E81)
}

/* ============================================================
 *  2. MODELES DE DONNEES
 * ============================================================ */

data class KpiCard(
    val label: String,
    val value: String,
    val deltaLabel: String,
    val isPositive: Boolean
)

data class DepartmentMargin(
    val name: String,
    val amountLabel: String,
    val heightFraction: Float,   // 0f..1f, position relative de la barre
    val highlighted: Boolean     // barre en couleur "primary" pleine vs atténuée
)

enum class ActivityIcon { TRUCK, SERVER, PAYMENT }

data class ActivityItem(
    val title: String,
    val reference: String,
    val amountLabel: String,
    val dateLabel: String,
    val isCredit: Boolean,
    val icon: live.anonymespy.optigestion.ActivityIcon
)

enum class NavDestination(val label: String) {
    DASHBOARD("Tableau de Bord"),
    SHEETS("Feuilles"),
    ANALYSIS("Analyse"),
    STATS("Stats")
}

/* ============================================================
 *  3. JEU DE DONNEES PAR DEFAUT (equivalent au HTML statique)
 * ============================================================ */

object DashboardSampleData {

    val kpis = listOf(
        KpiCard(
            label = "MARGE NETTE",
            value = "4,2 M€",
            deltaLabel = "+5.2%",
            isPositive = true
        ),
        KpiCard(
            label = "CHARGES TOTALES",
            value = "1,8 M€",
            deltaLabel = "-1.8%",
            isPositive = false
        )
    )

    val departmentMargins = listOf(
        DepartmentMargin(name = "Tech", amountLabel = "1,2 M€", heightFraction = 0.80f, highlighted = true),
        DepartmentMargin(name = "Ops", amountLabel = "800 K€", heightFraction = 0.60f, highlighted = false),
        DepartmentMargin(name = "Mktg", amountLabel = "500 K€", heightFraction = 0.40f, highlighted = false),
        DepartmentMargin(name = "Ventes", amountLabel = "1,5 M€", heightFraction = 0.90f, highlighted = true)
    )

    val recentActivity = listOf(
        ActivityItem(
            title = "Logistique",
            reference = "INV-2023-089",
            amountLabel = "-1 200,00 €",
            dateLabel = "Aujourd'hui, 09h41",
            isCredit = false,
            icon = _root_ide_package_.live.anonymespy.optigestion.ActivityIcon.TRUCK
        ),
        ActivityItem(
            title = "Services Informatiques",
            reference = "AWS-OCT-FACT",
            amountLabel = "-450,00 €",
            dateLabel = "Hier",
            isCredit = false,
            icon = _root_ide_package_.live.anonymespy.optigestion.ActivityIcon.SERVER
        ),
        ActivityItem(
            title = "Acompte Client",
            reference = "T4-ACME-CORP",
            amountLabel = "+5 000,00 €",
            dateLabel = "24 oct. 2023",
            isCredit = true,
            icon = _root_ide_package_.live.anonymespy.optigestion.ActivityIcon.PAYMENT
        )
    )
}

/* ============================================================
 *  4. ECRAN PRINCIPAL
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaeAnalyticsDashboardScreen(
    kpis: List<KpiCard> = DashboardSampleData.kpis,
    departmentMargins: List<DepartmentMargin> = DashboardSampleData.departmentMargins,
    recentActivity: List<ActivityItem> = DashboardSampleData.recentActivity,
    onImportExcel: () -> Unit = {},
    onExportExcel: () -> Unit = {},
    onExportPdf: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onSeeDepartmentDetails: () -> Unit = {}
) {
    var selectedDestination by remember { mutableStateOf(_root_ide_package_.live.anonymespy.optigestion.NavDestination.DASHBOARD) }

    Scaffold(
        containerColor = CaeColors.Background,
        topBar = { CaeTopAppBar() },
        bottomBar = {
            CaeBottomNavBar(
                selected = selectedDestination,
                onSelect = { selectedDestination = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Vue d'ensemble",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = CaeColors.Primary
            )

            Spacer(Modifier.height(24.dp))

            // Section 1 : Cartes KPI
            KpiSection(kpis)

            Spacer(Modifier.height(24.dp))

            // Section 3 : Marge par département
            DepartmentMarginSection(
                data = departmentMargins,
                onSeeDetails = onSeeDepartmentDetails
            )

            Spacer(Modifier.height(24.dp))

            // Section 2 : Activité récente
            RecentActivitySection(
                items = recentActivity,
                onSeeAll = onSeeAllTransactions
            )

            Spacer(Modifier.height(24.dp))

            // Section 4 : Import / Export
            ImportExportSection(
                onImportExcel = onImportExcel,
                onExportExcel = onExportExcel,
                onExportPdf = onExportPdf
            )
        }
    }
}

/* ============================================================
 *  5. TOP APP BAR
 * ============================================================ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaeTopAppBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CaeColors.SurfaceContainer)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CAE Analytics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CaeColors.Primary
                )
            }
        },
        actions = {
            IconButton(onClick = { /* notifications */ }) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = CaeColors.Primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CaeColors.Surface,
            titleContentColor = CaeColors.Primary
        )
    )
}

/* ============================================================
 *  6. SECTION KPI
 * ============================================================ */

@Composable
private fun KpiSection(kpis: List<KpiCard>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        kpis.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { kpi ->
                    KpiCardView(kpi = kpi, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KpiCardView(kpi: KpiCard, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(128.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = kpi.label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    color = CaeColors.OnSurfaceVariant
                )
                DeltaBadge(text = kpi.deltaLabel, isPositive = kpi.isPositive)
            }
            Text(
                text = kpi.value,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = CaeColors.Primary
            )
        }
    }
}

@Composable
private fun DeltaBadge(text: String, isPositive: Boolean) {
    val backgroundColor = if (isPositive) CaeColors.TertiaryContainer.copy(alpha = 0.1f)
    else CaeColors.ErrorContainer.copy(alpha = 0.2f)
    val contentColor = if (isPositive) CaeColors.OnTertiaryContainer else CaeColors.OnErrorContainer
    val icon = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, color = contentColor)
    }
}

/* ============================================================
 *  7. SECTION MARGE PAR DEPARTEMENT (graphique en barres)
 * ============================================================ */

@Composable
private fun DepartmentMarginSection(
    data: List<DepartmentMargin>,
    onSeeDetails: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Marge par Département",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CaeColors.Primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSeeDetails() }
                ) {
                    Text(
                        text = "Voir détails",
                        fontSize = 14.sp,
                        color = CaeColors.OnSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CaeColors.OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { bar ->
                    var showTooltip by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showTooltip) {
                            Text(
                                text = bar.amountLabel,
                                fontSize = 11.sp,
                                color = CaeColors.InverseOnSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CaeColors.InverseSurface)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(bar.heightFraction)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (bar.highlighted) CaeColors.Primary else CaeColors.SurfaceTint)
                                .clickable { showTooltip = !showTooltip }
                        )
                    }
                }
            }

            HorizontalDivider(color = CaeColors.SurfaceVariant, modifier = Modifier.padding(top = 8.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                data.forEach { bar ->
                    Text(
                        text = bar.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CaeColors.OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/* ============================================================
 *  8. SECTION ACTIVITE RECENTE
 * ============================================================ */

@Composable
private fun RecentActivitySection(
    items: List<ActivityItem>,
    onSeeAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activité Récente",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CaeColors.Primary
                )
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = CaeColors.OnSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            items.forEachIndexed { index, item ->
                ActivityRow(item = item)
                if (index != items.lastIndex) {
                    HorizontalDivider(color = CaeColors.SurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSeeAll,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CaeColors.Primary)
            ) {
                Text(text = "Voir Toutes les Transactions", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityItem) {
    val (icon, iconBg, iconTint) = when (item.icon) {
        ActivityIcon.TRUCK -> Triple(Icons.Filled.LocalShipping, CaeColors.SurfaceContainer, CaeColors.OnSurfaceVariant)
        ActivityIcon.SERVER -> Triple(Icons.Filled.Dns, CaeColors.SurfaceContainer, CaeColors.OnSurfaceVariant)
        ActivityIcon.PAYMENT -> Triple(Icons.Filled.Payments, CaeColors.TertiaryContainer.copy(alpha = 0.1f), CaeColors.OnTertiaryContainer)
        ActivityIcon.GENERIC -> TODO()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text = item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = CaeColors.Primary)
                Text(text = item.reference, fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.amountLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isCredit) CaeColors.OnTertiaryContainer else CaeColors.OnErrorContainer
            )
            Text(text = item.dateLabel, fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
        }
    }
}

/* ============================================================
 *  9. SECTION IMPORT / EXPORT
 * ============================================================ */

@Composable
private fun ImportExportSection(
    onImportExcel: () -> Unit,
    onExportExcel: () -> Unit,
    onExportPdf: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Import / Export",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CaeColors.Primary
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ImportExportButton(
                    icon = Icons.Filled.UploadFile,
                    label = "Importer Excel",
                    onClick = onImportExcel,
                    modifier = Modifier.weight(1f)
                )
                ImportExportButton(
                    icon = Icons.Filled.Download,
                    label = "Exporter Excel",
                    onClick = onExportExcel,
                    modifier = Modifier.weight(1f)
                )
                ImportExportButton(
                    icon = Icons.Filled.PictureAsPdf,
                    label = "Exporter PDF",
                    onClick = onExportPdf,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ImportExportButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CaeColors.Primary)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(4.dp))
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        }
    }
}

/* ============================================================
 *  10. BARRE DE NAVIGATION INFERIEURE
 * ============================================================ */

@Composable
private fun CaeBottomNavBar(
    selected: live.anonymespy.optigestion.NavDestination,
    onSelect: (live.anonymespy.optigestion.NavDestination) -> Unit
) {
    NavigationBar(containerColor = CaeColors.SurfaceContainer) {
        val icons = mapOf(
            _root_ide_package_.live.anonymespy.optigestion.NavDestination.DASHBOARD to Icons.Filled.Dashboard,
            _root_ide_package_.live.anonymespy.optigestion.NavDestination.SHEETS to Icons.Filled.TableChart,
            _root_ide_package_.live.anonymespy.optigestion.NavDestination.ANALYSIS to Icons.Filled.Analytics,
            _root_ide_package_.live.anonymespy.optigestion.NavDestination.STATS to Icons.Filled.QueryStats
        )

        _root_ide_package_.live.anonymespy.optigestion.NavDestination.values().forEach { destination ->
            NavigationBarItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = {
                    Icon(
                        imageVector = icons.getValue(destination),
                        contentDescription = destination.label
                    )
                },
                label = { Text(text = destination.label, fontSize = 11.sp) },
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
 *  11. APERCU (Preview) - facultatif, pour Android Studio
 * ============================================================ */

@Preview(showBackground = true)
@Composable
private fun CaeAnalyticsDashboardPreview() {
    MaterialTheme {
        CaeAnalyticsDashboardScreen()
    }
}