package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

/**
 * Real, editable departments/cost centers. Spend for each one is derived
 * live from AppRepository.entries (any Sheets entry whose costCenterCode
 * matches), so this screen — and the dropdown Sheets now offers when
 * picking a cost center — are always in sync.
 */
@Composable
fun CostCentersScreen() {
    val costCenters = AppRepository.costCenters
    val entries = AppRepository.entries

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCenter by remember { mutableStateOf<CostCenter?>(null) }

    val summaries = remember(costCenters.toList(), entries.toList()) { AppRepository.costCenterSummaries() }
    val departments = remember(costCenters.toList(), entries.toList()) { AppRepository.departmentBudgets() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(text = stringResource(R.string.cost_centers_title), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)
            Text(text = stringResource(R.string.cost_centers_subtitle), fontSize = 13.sp, color = CaeColors.OnSurfaceVariant, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

            if (costCenters.isEmpty()) {
                EmptyCostCentersState(onAdd = { showAddDialog = true })
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    summaries.forEach { summary -> CostCenterSummaryCard(summary) }
                }

                Spacer(Modifier.height(24.dp))

                Text(text = stringResource(R.string.cost_centers_distribution_title), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary, modifier = Modifier.padding(bottom = 8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    departments.forEach { dept ->
                        DepartmentBudgetCard(
                            dept = dept,
                            onClick = { editingCenter = costCenters.find { it.id == dept.costCenterId } }
                        )
                    }
                }
            }

            Spacer(Modifier.height(72.dp))
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = CaeColors.Primary,
            contentColor = CaeColors.OnPrimary
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.add_cost_center))
        }
    }

    if (showAddDialog) {
        CostCenterFormDialog(
            title = stringResource(R.string.new_cost_center_title),
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { cc -> AppRepository.addCostCenter(cc.code, cc.name, cc.icon, cc.monthlyBudget); showAddDialog = false },
            onDelete = null
        )
    }

    editingCenter?.let { cc ->
        CostCenterFormDialog(
            title = stringResource(R.string.edit_cost_center_title),
            initial = cc,
            onDismiss = { editingCenter = null },
            onSave = { updated -> AppRepository.updateCostCenter(updated); editingCenter = null },
            onDelete = { AppRepository.deleteCostCenter(cc.id); editingCenter = null }
        )
    }
}

/* ---------------- Empty state ---------------- */

@Composable
private fun EmptyCostCentersState(onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Filled.AccountTree, contentDescription = null, tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = stringResource(R.string.empty_cost_centers_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.empty_cost_centers_body),
                fontSize = 13.sp,
                color = CaeColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_cost_center))
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
                if (summary.footnoteIcon != CostCenterFootnoteIcon.NONE) {
                    Spacer(Modifier.width(4.dp))
                }
                Text(text = summary.footnote, fontSize = 14.sp, color = summary.footnoteColor)
            }
        }
    }
}

@Composable
private fun DepartmentBudgetCard(dept: DepartmentBudget, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
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
                        DepartmentIconBadge(dept.icon)
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
                            text = if (dept.budgetAmount > 0) stringResource(R.string.cost_center_footnote_percent_format, dept.percentOfBudget) else stringResource(R.string.cost_center_footnote_none),
                            fontSize = 14.sp,
                            color = if (dept.isOverBudget) CaeColors.Error else CaeColors.OnTertiaryContainer
                        )
                    }
                }

                if (dept.budgetAmount > 0) {
                    Spacer(Modifier.height(16.dp))
                    BudgetProgressBar(percent = dept.percentOfBudget, isOverBudget = dept.isOverBudget)
                }
            }
        }
    }
}

@Composable
private fun DepartmentIconBadge(icon: DepartmentIcon) {
    val (vector, bg, tint) = when (icon) {
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
        Icon(imageVector = vector, contentDescription = null, tint = tint)
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
    val clampedPercent = percent.coerceIn(0, 100)
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
}

/* ---------------- Add / edit form ---------------- */

@Composable
private fun CostCenterFormDialog(
    title: String,
    initial: CostCenter?,
    onDismiss: () -> Unit,
    onSave: (CostCenter) -> Unit,
    onDelete: (() -> Unit)?
) {
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var budgetText by remember { mutableStateOf(initial?.monthlyBudget?.takeIf { it > 0 }?.toLong()?.toString() ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: DepartmentIcon.ADMIN) }
    var iconMenuExpanded by remember { mutableStateOf(false) }

    val isValid = code.isNotBlank() && name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text(stringResource(R.string.form_label_cost_center_code)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.form_label_cost_center_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.form_label_monthly_budget)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = icon.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.form_label_icon)) },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { iconMenuExpanded = true }
                    )
                    DropdownMenu(expanded = iconMenuExpanded, onDismissRequest = { iconMenuExpanded = false }) {
                        DepartmentIcon.entries.forEach { i ->
                            DropdownMenuItem(text = { Text(i.name) }, onClick = { icon = i; iconMenuExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onSave(
                        CostCenter(
                            id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                            code = code.trim(),
                            name = name.trim(),
                            icon = icon,
                            monthlyBudget = budgetText.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text(stringResource(R.string.delete), color = CaeColors.Error) }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        }
    )
}
