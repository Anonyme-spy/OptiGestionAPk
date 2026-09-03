package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

@Composable
fun DashboardScreen(onNavigate: (NavDestination) -> Unit = {}) {
    val entries = AppRepository.entries // observing this list keeps the whole screen live
    val budgetCategories = AppRepository.budgetCategories

    val kpis = remember(entries.toList(), AppRepository.cashOnHand) {
        val runway = AppRepository.runwayMonths()
        listOf(
            KpiCard(
                label = "MARGE NETTE",
                value = formatCurrencyCompact(AppRepository.netMargin()),
                deltaLabel = if (AppRepository.netMargin() >= 0) "Positive" else "Négative",
                isPositive = AppRepository.netMargin() >= 0
            ),
            KpiCard(
                label = "RECETTES TOTALES",
                value = formatCurrencyCompact(AppRepository.totalRevenue()),
                deltaLabel = "${entries.count { it.isCredit }} écriture(s)",
                isPositive = true
            ),
            KpiCard(
                label = "COÛTS TOTAUX",
                value = formatCurrencyCompact(AppRepository.totalCosts()),
                deltaLabel = "${entries.count { !it.isCredit }} écriture(s)",
                isPositive = false
            ),
            KpiCard(
                label = "TRÉSORERIE (RUNWAY)",
                value = if (runway != null) "${formatMonths(runway)} mois" else "∞",
                deltaLabel = if (runway != null && runway < 3) "Critique" else "Stable",
                isPositive = runway == null || runway >= 3
            )
        )
    }
    val costCenterBars = remember(entries.toList()) { AppRepository.costCenterBars() }
    val recentActivity = remember(entries.toList()) { AppRepository.recentActivity() }
    val alerts = remember(budgetCategories.map { it.actualInput }, budgetCategories.map { it.budgetAmount }) { AppRepository.budgetAlerts() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Text(text = "Vue d'ensemble", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)

        Spacer(Modifier.height(16.dp))

        QuickActionsRow(onNavigate = onNavigate)

        Spacer(Modifier.height(20.dp))

        if (entries.isEmpty()) {
            DashboardEmptyState()
        } else {
            if (alerts.isNotEmpty()) {
                BudgetAlertBanner(alerts = alerts, onClick = { onNavigate(NavDestination.ANALYSIS) })
                Spacer(Modifier.height(16.dp))
            }

            KpiSection(kpis)

            Spacer(Modifier.height(24.dp))

            CostCenterSection(costCenterBars)

            Spacer(Modifier.height(24.dp))

            RecentActivitySection(recentActivity)
        }
    }
}

/* ---------------- Quick actions ---------------- */

@Composable
private fun QuickActionsRow(onNavigate: (NavDestination) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        QuickActionChip(icon = Icons.Filled.TableChart, label = "Écritures", modifier = Modifier.weight(1f)) { onNavigate(NavDestination.SHEETS) }
        QuickActionChip(icon = Icons.Filled.Savings, label = "Budget", modifier = Modifier.weight(1f)) { onNavigate(NavDestination.ANALYSIS) }
        QuickActionChip(icon = Icons.Filled.QueryStats, label = "Rapports", modifier = Modifier.weight(1f)) { onNavigate(NavDestination.STATS) }
    }
}

@Composable
private fun QuickActionChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CaeColors.SurfaceContainerLowest)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CaeColors.Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant)
    }
}

/* ---------------- Budget alert banner ---------------- */

@Composable
private fun BudgetAlertBanner(alerts: List<BudgetAlert>, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.ErrorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = CaeColors.OnErrorContainer, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${alerts.size} catégorie(s) budgétaire(s) à surveiller",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CaeColors.OnErrorContainer
                )
                Text(
                    text = alerts.take(2).joinToString(" · ") { "${it.label} (${it.percentUsed}%)" },
                    fontSize = 12.sp,
                    color = CaeColors.OnErrorContainer
                )
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CaeColors.OnErrorContainer, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun DashboardEmptyState() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Filled.QueryStats, contentDescription = null, tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Votre tableau de bord est vide",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CaeColors.Primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ajoutez vos premières écritures dans l'onglet Sheets pour voir vos indicateurs, vos coûts par centre et votre activité récente.",
                fontSize = 13.sp,
                color = CaeColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/* ---------------- KPI cards ---------------- */

@Composable
private fun KpiSection(kpis: List<KpiCard>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        kpis.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { kpi ->
                    KpiCardView(kpi = kpi, modifier = Modifier.weight(1f))
                }
                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
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
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = kpi.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = CaeColors.OnSurfaceVariant)
                DeltaBadge(text = kpi.deltaLabel, isPositive = kpi.isPositive)
            }
            Text(text = kpi.value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary)
        }
    }
}

@Composable
internal fun DeltaBadge(text: String, isPositive: Boolean) {
    val backgroundColor = if (isPositive) CaeColors.TertiaryContainer.copy(alpha = 0.1f) else CaeColors.ErrorContainer.copy(alpha = 0.2f)
    val contentColor = if (isPositive) CaeColors.OnTertiaryContainer else CaeColors.OnErrorContainer
    val icon = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown

    Row(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(backgroundColor).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, color = contentColor)
    }
}

/* ---------------- Cost by cost center (bar chart) ---------------- */

@Composable
private fun CostCenterSection(bars: List<CostCenterBar>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Coûts par Centre", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)

            Spacer(Modifier.height(24.dp))

            if (bars.isEmpty()) {
                Text(text = "Aucune dépense enregistrée pour l'instant.", fontSize = 13.sp, color = CaeColors.OnSurfaceVariant)
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    var showTooltip by remember { mutableStateOf(false) }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (showTooltip) {
                            Text(
                                text = bar.amountLabel,
                                fontSize = 11.sp,
                                color = CaeColors.InverseOnSurface,
                                modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(CaeColors.InverseSurface).padding(horizontal = 8.dp, vertical = 4.dp)
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
                bars.forEach { bar ->
                    Text(text = bar.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/* ---------------- Recent activity ---------------- */

@Composable
private fun RecentActivitySection(items: List<ActivityItem>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Activité Récente", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)

            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Text(text = "Rien à afficher pour l'instant.", fontSize = 13.sp, color = CaeColors.OnSurfaceVariant)
            } else {
                items.forEachIndexed { index, item ->
                    ActivityRow(item = item)
                    if (index != items.lastIndex) HorizontalDivider(color = CaeColors.SurfaceVariant)
                }
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
        ActivityIcon.GENERIC -> Triple(Icons.Filled.Receipt, CaeColors.SurfaceContainer, CaeColors.OnSurfaceVariant)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
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
