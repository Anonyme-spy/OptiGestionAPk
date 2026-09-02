package live.anonymespy.optigestion

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChartOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

    val totalCostLabel = remember(categories.map { it.actualAmount }) {
        formatCurrencyCompact(categories.sumOf { it.actualAmount })
    }
    val costDistribution = remember(categories.map { it.actualAmount }) { AppRepository.costDistribution() }
    val profitabilityTrend = remember(entries.toList()) { AppRepository.profitabilityTrend() }
    val profitabilityDeltaLabel = remember(entries.toList()) { AppRepository.profitabilityDeltaLabel() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        CostDistributionSection(totalCostLabel, costDistribution)

        Spacer(Modifier.height(24.dp))

        ProfitabilityTrendSection(profitabilityDeltaLabel, profitabilityTrend)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { Toast.makeText(context, "Rapport exporté (démo)", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary, contentColor = CaeColors.OnPrimary)
        ) {
            Icon(imageVector = Icons.Filled.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(text = "Exporter le Rapport", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
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
                EmptyChartPlaceholder(text = "Ajoutez des budgets par catégorie dans l'onglet Analysis pour voir la répartition des coûts.")
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
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                slices.forEach { slice ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
