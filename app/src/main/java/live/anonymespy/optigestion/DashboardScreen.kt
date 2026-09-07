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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

/**
 * Redesigned around a single clear hierarchy instead of four same-weight
 * tiles: one hero card (net margin, with a revenue/cost breakdown bar),
 * a row of three supporting metrics, then charts and activity below.
 *
 * DeltaBadge lives in SharedComponents.kt, not here — it's also used by
 * StatsScreen.kt, and keeping it screen-local was exactly what broke that
 * file the last time this screen got redesigned.
 */
@Composable
fun DashboardScreen(onNavigate: (NavDestination) -> Unit = {}) {
    val entries = AppRepository.entries // observing this list keeps the whole screen live
    val budgetCategories = AppRepository.budgetCategories

    val netMargin = remember(entries.toList()) { AppRepository.netMargin() }
    val revenue = remember(entries.toList()) { AppRepository.totalRevenue() }
    val costs = remember(entries.toList()) { AppRepository.totalCosts() }
    val runway = remember(entries.toList(), AppRepository.cashOnHand) { AppRepository.runwayMonths() }
    val costCenterBars = remember(entries.toList()) { AppRepository.costCenterBars() }
    val recentActivity = remember(entries.toList()) { AppRepository.recentActivity() }
    val alerts = remember(budgetCategories.map { it.actualInput }, budgetCategories.map { it.budgetAmount }) { AppRepository.budgetAlerts() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        val user = AppRepository.currentUser
        val greeting = if (user != null) "Bonjour, ${user.displayName}" else stringResource(R.string.dashboard_title)
        Text(text = greeting, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)

        Spacer(Modifier.height(16.dp))

        if (entries.isEmpty()) {
            DashboardEmptyState()
        } else {
            if (alerts.isNotEmpty()) {
                BudgetAlertBanner(alerts = alerts, onClick = { onNavigate(NavDestination.ANALYSIS) })
                Spacer(Modifier.height(16.dp))
            }

            HeroMarginCard(netMargin = netMargin, revenue = revenue, costs = costs, appMode = AppRepository.appMode)

            val isPro = AppRepository.appMode == AppMode.PRO
            if (isPro) {
                Spacer(Modifier.height(16.dp))
                VatStatusCard(liability = AppRepository.vatLiability())
            }

            Spacer(Modifier.height(12.dp))

            SecondaryMetricsRow(revenue = revenue, costs = costs, runway = runway, appMode = AppRepository.appMode)

            Spacer(Modifier.height(16.dp))

            QuickActionsRow(onNavigate = onNavigate)

            Spacer(Modifier.height(24.dp))

            CostCenterSection(costCenterBars)

            Spacer(Modifier.height(24.dp))

            RecentActivitySection(recentActivity)
        }
    }
}

/* ---------------- Hero card ---------------- */

@Composable
private fun HeroMarginCard(netMargin: Double, revenue: Double, costs: Double, appMode: AppMode) {
    val isPositive = netMargin >= 0
    val isPro = appMode == AppMode.PRO

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = stringResource(if (isPro) R.string.kpi_net_margin else R.string.kpi_net_margin_simple),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    color = CaeColors.OnPrimary.copy(alpha = 0.7f)
                )
                HeroDeltaBadge(isPositive = isPositive, label = stringResource(if (isPositive) R.string.status_positive else R.string.status_negative))
            }
            Spacer(Modifier.height(8.dp))
            Text(text = formatCurrencyCompact(netMargin), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CaeColors.OnPrimary)

            Spacer(Modifier.height(16.dp))

            HeroBreakdownBar(revenue = revenue, costs = costs)

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val revenueLabel = stringResource(if (isPro) R.string.kpi_revenue else R.string.kpi_revenue_simple).lowercase().replaceFirstChar { it.uppercase() }
                val costsLabel = stringResource(if (isPro) R.string.kpi_costs else R.string.kpi_costs_simple).lowercase().replaceFirstChar { it.uppercase() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CaeColors.TertiaryFixedDim))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "$revenueLabel · ${formatCurrencyCompact(revenue)}", fontSize = 11.sp, color = CaeColors.OnPrimary.copy(alpha = 0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(CaeColors.OnPrimary.copy(alpha = 0.4f)))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "$costsLabel · ${formatCurrencyCompact(costs)}", fontSize = 11.sp, color = CaeColors.OnPrimary.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun HeroBreakdownBar(revenue: Double, costs: Double) {
    val total = (revenue + costs).takeIf { it > 0 } ?: 1.0
    val revenueFraction = (revenue / total).toFloat().coerceIn(0f, 1f)

    Box(
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(CaeColors.OnPrimary.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(revenueFraction)
                .clip(RoundedCornerShape(50))
                .background(CaeColors.TertiaryFixedDim)
        )
    }
}

@Composable
private fun HeroDeltaBadge(isPositive: Boolean, label: String) {
    val icon = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CaeColors.OnPrimary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CaeColors.OnPrimary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, color = CaeColors.OnPrimary)
    }
}

/* ---------------- Secondary metrics row ---------------- */

@Composable
private fun SecondaryMetricsRow(revenue: Double, costs: Double, runway: Double?, appMode: AppMode) {
    val isPro = appMode == AppMode.PRO
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SecondaryMetricTile(
            label = stringResource(if (isPro) R.string.kpi_revenue else R.string.kpi_revenue_simple),
            value = formatCurrencyCompact(revenue),
            modifier = Modifier.weight(1f)
        )
        SecondaryMetricTile(
            label = stringResource(if (isPro) R.string.kpi_costs else R.string.kpi_costs_simple),
            value = formatCurrencyCompact(costs),
            modifier = Modifier.weight(1f)
        )
        SecondaryMetricTile(
            label = stringResource(if (isPro) R.string.kpi_runway else R.string.kpi_runway_simple),
            value = if (runway != null) "${formatMonths(runway)} ${stringResource(R.string.months_suffix)}" else stringResource(R.string.infinite_symbol),
            valueColor = if (runway != null && runway < 3) CaeColors.Error else CaeColors.Primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SecondaryMetricTile(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = CaeColors.Primary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp, color = CaeColors.OnSurfaceVariant, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor, maxLines = 1)
        }
    }
}

/* ---------------- Quick actions ---------------- */

@Composable
private fun QuickActionsRow(onNavigate: (NavDestination) -> Unit) {
    val isPro = AppRepository.appMode == AppMode.PRO
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        QuickActionChip(icon = Icons.Filled.TableChart, label = stringResource(R.string.quick_action_entries), modifier = Modifier.weight(1f)) { onNavigate(NavDestination.SHEETS) }
        QuickActionChip(icon = Icons.Filled.Savings, label = stringResource(if (isPro) R.string.quick_action_budget else R.string.nav_budget_simple), modifier = Modifier.weight(1f)) { onNavigate(NavDestination.ANALYSIS) }
        QuickActionChip(icon = Icons.Filled.QueryStats, label = stringResource(if (isPro) R.string.quick_action_reports else R.string.nav_reports_simple), modifier = Modifier.weight(1f)) { onNavigate(NavDestination.STATS) }
    }
}

@Composable
private fun QuickActionChip(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CaeColors.SurfaceContainerLowest)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CaeColors.Primary, modifier = Modifier.size(18.dp))
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
                    text = stringResource(R.string.alert_banner_title_format, alerts.size),
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
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = CaeColors.OnErrorContainer, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun VatStatusCard(liability: Double) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(CaeColors.TertiaryContainer.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = CaeColors.OnTertiaryContainer, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "TVA À PAYER (ESTIMATION)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CaeColors.OnSurfaceVariant)
                Text(text = formatCurrency(liability), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (liability > 0) CaeColors.Error else CaeColors.OnTertiaryContainer)
            }
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
                text = stringResource(R.string.dashboard_empty_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = CaeColors.Primary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dashboard_empty_body),
                fontSize = 13.sp,
                color = CaeColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
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
            Text(text = stringResource(R.string.cost_center_section_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)

            Spacer(Modifier.height(24.dp))

            if (bars.isEmpty()) {
                Text(text = stringResource(R.string.cost_center_empty), fontSize = 13.sp, color = CaeColors.OnSurfaceVariant)
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
            Text(text = stringResource(R.string.activity_section_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)

            Spacer(Modifier.height(12.dp))

            if (items.isEmpty()) {
                Text(text = stringResource(R.string.activity_empty), fontSize = 13.sp, color = CaeColors.OnSurfaceVariant)
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
