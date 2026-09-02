package live.anonymespy.optigestion
/**
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.CostCenterFootnoteIcon
import live.anonymespy.optigestion.CostCenterSummary
import live.anonymespy.optigestion.DepartmentBudget
import live.anonymespy.optigestion.DepartmentIcon
import live.anonymespy.optigestion.ui.theme.CaeColors

@Composable
fun CostCentersScreen(
    summaries: List<CostCenterSummary> = CostCentersSampleData.summaries,
    departments: List<DepartmentBudget> = CostCentersSampleData.departments
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // Summary KPI section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            summaries.forEach { summary ->
                CostCenterSummaryCard(summary)
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Department Breakdown",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = CaeColors.Primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            departments.forEach { dept ->
                DepartmentBudgetCard(dept)
            }
        }
    }
}

@Composable
private fun CostCenterSummaryCard(summary: CostCenterSummary) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = summary.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                color = CaeColors.OnSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = summary.value,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = summary.valueColor
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (summary.footnoteIcon) {
                    CostCenterFootnoteIcon.TRENDING_DOWN -> Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = summary.footnoteColor,
                        modifier = Modifier.size(16.dp)
                    )
                    CostCenterFootnoteIcon.WARNING -> Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = summary.footnoteColor,
                        modifier = Modifier.size(16.dp)
                    )
                    else -> {}
                }
                if (summary.footnoteIcon != null && summary.footnoteIcon != CostCenterFootnoteIcon.NONE) {
                    Spacer(Modifier.width(4.dp))
                }
                Text(text = summary.footnote, fontSize = 14.sp, color = summary.footnoteColor)
            }
        }
    }
}

@Composable
private fun DepartmentBudgetCard(dept: DepartmentBudget) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (dept.isOverBudget) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(CaeColors.Error)
                )
            }
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DepartmentIconBadge(dept)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = dept.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CaeColors.OnSurface
                            )
                            Text(
                                text = dept.costCenterCode,
                                fontSize = 14.sp,
                                color = CaeColors.OnSurfaceVariant
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = dept.amountLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dept.isOverBudget) CaeColors.Error else CaeColors.OnSurface
                        )
                        Text(
                            text = "${dept.percentOfBudget}% of budget",
                            fontSize = 14.sp,
                            color = if (dept.isOverBudget) CaeColors.Error else CaeColors.OnTertiaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                BudgetProgressBar(percent = dept.percentOfBudget, isOverBudget = dept.isOverBudget)
            }
        }
    }
}

@Composable
private fun DepartmentIconBadge(dept: DepartmentBudget) {
    val (icon, bg, tint) = when (dept.icon) {
        DepartmentIcon.PRODUCTION -> Triple(Icons.Filled.Build, CaeColors.SurfaceContainer, CaeColors.Primary)
        DepartmentIcon.RESEARCH -> Triple(Icons.Filled.Science, CaeColors.ErrorContainer, CaeColors.Error)
        DepartmentIcon.ADMIN -> Triple(Icons.Filled.Domain, CaeColors.SurfaceContainer, CaeColors.Primary)
        DepartmentIcon.SALES -> Triple(Icons.Filled.TrendingUp, CaeColors.ErrorContainer, CaeColors.Error)
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

/**
 * Renders the budget progress bar.
 * - Under budget (<=100%): a single primary-colored fill up to [percent].
 * - Over budget (>100%): a full primary fill, plus a red overflow segment
 *   representing the amount past 100%, mirroring the original design.
 */
@Composable
private fun BudgetProgressBar(percent: Int, isOverBudget: Boolean) {
    val clampedPercent = percent.coerceAtMost(100)
    val overflowPercent = (percent - 100).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(CaeColors.SurfaceContainer)
    ) {
        if (!isOverBudget) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = clampedPercent / 100f)
                    .background(CaeColors.OnTertiaryContainer)
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(100f)
                        .background(CaeColors.Primary)
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(overflowPercent.coerceAtLeast(1).toFloat())
                        .background(CaeColors.Error)
                )
            }
        }
    }
} **/
