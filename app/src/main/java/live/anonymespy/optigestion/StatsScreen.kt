package live.anonymespy.optigestion

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChartOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val entries = AppRepository.entries
    val categories = AppRepository.budgetCategories
    val costCenters = AppRepository.costCenters

    var periodFilter by remember { mutableStateOf(PeriodFilter.ALL) }

    val periodEntries = remember(entries.toList(), periodFilter) {
        val start = periodFilterStartMillis(periodFilter)
        if (start == null) entries.toList() else entries.filter { it.timestampMillis >= start }
    }

    val totalCostLabel = remember(categories.map { it.actualAmount }) {
        formatCurrencyCompact(categories.sumOf { it.actualAmount })
    }
    val costDistribution = remember(categories.map { it.actualAmount }) { AppRepository.costDistribution() }
    val profitabilityTrend = remember(entries.toList()) { AppRepository.profitabilityTrend() }
    val profitabilityDeltaLabel = remember(entries.toList()) { AppRepository.profitabilityDeltaLabel() }
    val cashFlow = remember(entries.toList()) { AppRepository.cashFlowByMonth() }
    val departments = remember(costCenters.toList(), entries.toList()) { AppRepository.departmentBudgets() }
    val alerts = remember(categories.map { it.actualInput }, categories.map { it.budgetAmount }) { AppRepository.budgetAlerts() }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(AppRepository.exportCsv().toByteArray())
            }
            Toast.makeText(context, "Rapport exporté avec succès", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Échec de l'export", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = "Rapports & Analytique", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)
        Text(
            text = "Vue financière complète, période par période.",
            fontSize = 13.sp,
            color = CaeColors.OnSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        PeriodSelectorRow(selected = periodFilter, onSelect = { periodFilter = it })

        Spacer(Modifier.height(16.dp))

        KpiStripSection()

        Spacer(Modifier.height(16.dp))

        PeriodSummaryCard(filtered = periodEntries, filter = periodFilter)

        if (alerts.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            BudgetAlertsSection(alerts)
        }

        Spacer(Modifier.height(24.dp))

        CashFlowSection(cashFlow)

        Spacer(Modifier.height(24.dp))

        CostDistributionSection(totalCostLabel, costDistribution)

        Spacer(Modifier.height(24.dp))

        ProfitabilityTrendSection(profitabilityDeltaLabel, profitabilityTrend)

        Spacer(Modifier.height(24.dp))

        CostCenterRankingSection(departments)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                val filename = "optigestion_rapport_${System.currentTimeMillis() / 1000}.csv"
                exportLauncher.launch(filename)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary, contentColor = CaeColors.OnPrimary)
        ) {
            Icon(imageVector = Icons.Filled.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = "Exporter le Rapport (CSV)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

/* ---------------- Period selector ---------------- */

@Composable
private fun PeriodSelectorRow(selected: PeriodFilter, onSelect: (PeriodFilter) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
    ) {
        PeriodFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CaeColors.PrimaryContainer,
                    selectedLabelColor = CaeColors.OnPrimaryContainer
                )
            )
        }
    }
}

/* ---------------- KPI strip (margin, burn rate, runway, growth) ---------------- */

@Composable
private fun KpiStripSection() {
    val margin = AppRepository.marginPercent()
    val burn = AppRepository.burnRate()
    val runway = AppRepository.runwayMonths()
    val growth = AppRepository.revenueGrowthPercent()

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        MetricTile("MARGE", if (margin != null) "${formatPercent(margin)}%" else "—", modifier = Modifier.weight(1f))
        MetricTile("BURN RATE", formatCurrencyCompact(burn), modifier = Modifier.weight(1f))
        MetricTile("RUNWAY", if (runway != null) "${formatMonths(runway)} mois" else "∞", modifier = Modifier.weight(1f))
        MetricTile(
            "CROISSANCE",
            if (growth != null) "${if (growth >= 0) "+" else ""}${formatPercent(growth)}%" else "—",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant, letterSpacing = 0.4.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary)
        }
    }
}

/* ---------------- Period summary ---------------- */

@Composable
private fun PeriodSummaryCard(filtered: List<SheetEntry>, filter: PeriodFilter) {
    val income = filtered.filter { it.isCredit }.sumOf { it.amount }
    val expense = filtered.filter { !it.isCredit }.sumOf { it.amount }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "RÉSUMÉ — ${filter.label.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = CaeColors.OnSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Recettes", fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
                    Text(formatCurrencyCompact(income), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CaeColors.OnTertiaryContainer)
                }
                Column {
                    Text("Dépenses", fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
                    Text(formatCurrencyCompact(expense), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CaeColors.Error)
                }
                Column {
                    Text("Net", fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
                    Text(formatCurrencyCompact(income - expense), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary)
                }
            }
        }
    }
}

/* ---------------- Budget alerts ---------------- */

@Composable
private fun BudgetAlertsSection(alerts: List<BudgetAlert>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.ErrorContainer.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = CaeColors.Error, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = "Alertes Budgétaires", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Error)
            }
            Spacer(Modifier.height(8.dp))
            alerts.forEach { a ->
                Text(
                    text = "•  ${a.label} — ${a.percentUsed}% du budget" + if (a.isOverBudget) " (dépassé)" else "",
                    fontSize = 13.sp,
                    color = CaeColors.OnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

/* ---------------- Cash flow (income vs expense bar chart) ---------------- */

@Composable
private fun CashFlowSection(data: List<CashFlowPoint>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "FLUX DE TRÉSORERIE (6 DERNIERS MOIS)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = CaeColors.OnSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            if (data.size < 1 || data.all { it.income == 0.0 && it.expense == 0.0 }) {
                EmptyChartPlaceholder(text = "Ajoutez des écritures pour voir vos recettes et dépenses mois par mois.")
            } else {
                CashFlowBarChart(data = data, modifier = Modifier.fillMaxWidth().height(160.dp))

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    data.forEach { point ->
                        Text(text = point.monthLabel.uppercase(), fontSize = 10.sp, color = CaeColors.OnSurfaceVariant, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(color = CaeColors.OnTertiaryContainer, label = "Recettes")
                    LegendDot(color = CaeColors.Error, label = "Dépenses")
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
    }
}

@Composable
private fun CashFlowBarChart(data: List<CashFlowPoint>, modifier: Modifier = Modifier) {
    val maxVal = (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0).coerceAtLeast(1.0)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        data.forEach { point ->
            Row(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight((point.income / maxVal).toFloat().coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(CaeColors.OnTertiaryContainer)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight((point.expense / maxVal).toFloat().coerceIn(0.02f, 1f))
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(CaeColors.Error)
                )
            }
        }
    }
}

/* ---------------- Cost center ranking ---------------- */

@Composable
private fun CostCenterRankingSection(departments: List<DepartmentBudget>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "TOP CENTRES DE COÛT",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = CaeColors.OnSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            if (departments.isEmpty()) {
                EmptyChartPlaceholder(text = "Créez des centres de coût dans l'onglet Centres pour voir le classement des dépenses.")
            } else {
                departments.take(5).forEachIndexed { index, dept ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "${dept.name} (${dept.costCenterCode})", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
                            Text(
                                text = dept.amountLabel + if (dept.budgetAmount > 0) " · ${dept.percentOfBudget}%" else "",
                                fontSize = 13.sp,
                                color = if (dept.isOverBudget) CaeColors.Error else CaeColors.OnSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(CaeColors.SurfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((dept.percentOfBudget / 100f).coerceIn(0.02f, 1f))
                                    .background(if (dept.isOverBudget) CaeColors.Error else CaeColors.Primary)
                            )
                        }
                    }
                    if (index != departments.take(5).lastIndex) HorizontalDivider(color = CaeColors.SurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

/* ---------------- Cost distribution (donut / pie chart) ---------------- */

@Composable
private fun CostDistributionSection(totalCostLabel: String, slices: List<CostDistributionSlice>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RÉPARTITION DES COÛTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = CaeColors.OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            if (slices.isEmpty()) {
                EmptyChartPlaceholder(text = "Ajoutez des budgets par catégorie dans l'onglet Budget pour voir la répartition des coûts.")
                return@Column
            }

            Box(modifier = Modifier.size(192.dp), contentAlignment = Alignment.Center) {
                PieChart(slices = slices, modifier = Modifier.fillMaxSize())

                Box(
                    modifier = Modifier.size(160.dp).clip(CircleShape).background(CaeColors.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = totalCostLabel, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary)
                        Text(text = "Coût Total", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                slices.forEach { slice ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(slice.color))
                            Spacer(Modifier.width(4.dp))
                            Text(text = slice.label, fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(text = "${slice.percent}%", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChart(slices: List<CostDistributionSlice>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        slices.forEach { slice ->
            val sweep = 360f * (slice.percent / 100f)
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter, diameter)
            )
            startAngle += sweep
        }
    }
}

/* ---------------- Profitability trend (line chart) ---------------- */

@Composable
private fun ProfitabilityTrendSection(deltaLabel: String, trend: List<TrendPoint>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TENDANCE DE RENTABILITÉ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    color = CaeColors.OnSurfaceVariant
                )
                if (trend.size >= 2) {
                    DeltaBadge(text = deltaLabel, isPositive = trend.last().value >= trend.first().value)
                }
            }

            Spacer(Modifier.height(16.dp))

            if (trend.size < 2) {
                EmptyChartPlaceholder(text = "Ajoutez des écritures sur au moins deux mois différents pour voir la tendance de rentabilité.")
            } else {
                LineChart(trend = trend, modifier = Modifier.fillMaxWidth().height(160.dp))

                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    trend.forEach { point ->
                        Text(text = point.monthLabel.uppercase(), fontSize = 10.sp, color = CaeColors.OnSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Filled.PieChartOutline, contentDescription = null, tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(36.dp))
        Spacer(Modifier.height(8.dp))
        Text(text = text, fontSize = 12.sp, color = CaeColors.OnSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun LineChart(trend: List<TrendPoint>, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (trend.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val stepX = width / (trend.size - 1)

        val gridColor = CaeColors.SurfaceVariant
        drawLine(gridColor, Offset(0f, height), Offset(width, height), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, height / 2f), Offset(width, height / 2f), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1.dp.toPx())

        fun yFor(value: Float) = height - (value.coerceIn(0f, 100f) / 100f) * height

        val points = trend.mapIndexed { index, point -> Offset(index * stepX, yFor(point.value)) }

        val areaPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(colors = listOf(CaeColors.Primary.copy(alpha = 0.2f), Color.Transparent))
        )

        val linePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path = linePath, color = CaeColors.Primary, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

        points.forEach { point -> drawCircle(color = CaeColors.Primary, radius = 3.dp.toPx(), center = point) }
    }
}
