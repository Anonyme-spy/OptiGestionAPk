package live.anonymespy.optigestion

import live.anonymespy.optigestion.ui.theme.CaeColors

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun BudgetVsActualScreen() {
    val categories = AppRepository.budgetCategories
    var showAddDialog by remember { mutableStateOf(false) }

    val plannedBudgetTotal = AppRepository.plannedBudgetTotal
    val totalActual = categories.sumOf { it.actualAmount }
    val varianceAmount = plannedBudgetTotal - totalActual
    val variancePercent = if (plannedBudgetTotal != 0.0) (varianceAmount / plannedBudgetTotal) * 100 else 0.0
    val isUnderBudget = varianceAmount >= 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderRow(periodLabel = AppRepository.periodLabel, appMode = AppRepository.appMode)

        Spacer(Modifier.height(24.dp))

        if (categories.isEmpty()) {
            EmptyBudgetState(onAddCategory = { showAddDialog = true })
        } else {
            OverviewCard(
                plannedBudgetTotal = plannedBudgetTotal,
                totalActual = totalActual,
                varianceAmount = varianceAmount,
                variancePercent = variancePercent,
                isUnderBudget = isUnderBudget
            )

            Spacer(Modifier.height(16.dp))

            CategoryBreakdownCard(
                categories = categories,
                onActualChanged = { AppRepository.persist() },
                onAddCategory = { showAddDialog = true },
                onDeleteCategory = { AppRepository.deleteBudgetCategory(it) },
                appMode = AppRepository.appMode
            )
        }

        Spacer(Modifier.height(16.dp))
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, icon, amount, ledgerAccount ->
                AppRepository.addBudgetCategory(name, icon, amount, ledgerAccount)
                showAddDialog = false
            },
            appMode = AppRepository.appMode
        )
    }
}

/* ---------------- Header ---------------- */
/* ---------------- En-tête ---------------- */

@Composable
private fun HeaderRow(periodLabel: String, appMode: AppMode) {
    val isPro = appMode == AppMode.PRO
    Column {
        Text(text = stringResource(if (isPro) R.string.budget_header_title else R.string.budget_header_title_simple), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)
        Text(
            text = stringResource(if (isPro) R.string.budget_header_subtitle_format else R.string.budget_header_subtitle_format_simple, periodLabel),
            fontSize = 14.sp,
            color = CaeColors.OnSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/* ---------------- Empty state ---------------- */
/* ---------------- État vide ---------------- */

@Composable
private fun EmptyBudgetState(onAddCategory: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Filled.Savings, contentDescription = null, tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(text = stringResource(R.string.empty_budget_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.empty_budget_body),
                fontSize = 13.sp,
                color = CaeColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAddCategory, colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_category))
            }
        }
    }
}

/* ---------------- Overview KPI card ---------------- */
/* ---------------- Carte KPI de vue d'ensemble ---------------- */

@Composable
private fun OverviewCard(
    plannedBudgetTotal: Double,
    totalActual: Double,
    varianceAmount: Double,
    variancePercent: Double,
    isUnderBudget: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(text = stringResource(R.string.budget_overview_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = CaeColors.OnSurfaceVariant)

            Spacer(Modifier.height(16.dp))

            LabeledAmount(label = stringResource(R.string.budget_planned_label), amount = plannedBudgetTotal)
            Spacer(Modifier.height(12.dp))
            LabeledAmount(label = stringResource(R.string.budget_actuals_label), amount = totalActual)

            HorizontalDivider(color = CaeColors.SurfaceVariant, modifier = Modifier.padding(vertical = 16.dp))

            Text(text = stringResource(R.string.budget_total_variance_label), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = CaeColors.OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatCurrency(abs(varianceAmount)), fontSize = 36.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary)
                Spacer(Modifier.width(8.dp))
                VarianceBadge(percent = variancePercent, isUnderBudget = isUnderBudget)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isUnderBudget) stringResource(R.string.budget_under_text) else stringResource(R.string.budget_over_text),
                fontSize = 12.sp,
                color = CaeColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun LabeledAmount(label: String, amount: Double) {
    Column {
        Text(text = label, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant)
        Text(text = formatCurrency(amount), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = CaeColors.Primary)
    }
}

@Composable
private fun VarianceBadge(percent: Double, isUnderBudget: Boolean) {
    val bg = if (isUnderBudget) CaeColors.TertiaryContainer.copy(alpha = 0.1f) else CaeColors.ErrorContainer.copy(alpha = 0.2f)
    val fg = if (isUnderBudget) CaeColors.OnTertiaryContainer else CaeColors.Error
    val icon = if (isUnderBudget) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward

    Row(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(bg).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = "${formatPercent(abs(percent))}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

/* ---------------- Category breakdown (editable) ---------------- */
/* ---------------- Répartition par catégorie (modifiable) ---------------- */

@Composable
private fun CategoryBreakdownCard(
    categories: List<BudgetCategoryUi>,
    onActualChanged: () -> Unit,
    onAddCategory: () -> Unit,
    onDeleteCategory: (String) -> Unit,
    appMode: AppMode
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "RÉPARTITION PAR CATÉGORIE", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = CaeColors.OnSurfaceVariant)
                IconButton(onClick = onAddCategory, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Filled.AddCircle, contentDescription = stringResource(R.string.add_category), tint = CaeColors.Primary)
                }
            }

            Spacer(Modifier.height(20.dp))

            val referenceMax = (categories.maxOfOrNull { it.budgetAmount } ?: 1.0) * 1.2

            categories.forEachIndexed { index, state ->
                CategoryRow(state = state, referenceMax = referenceMax, onActualChanged = onActualChanged, onDelete = { onDeleteCategory(state.id) }, appMode = appMode)
                if (index != categories.lastIndex) Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CategoryRow(state: BudgetCategoryUi, referenceMax: Double, onActualChanged: () -> Unit, onDelete: () -> Unit, appMode: AppMode) {
    val isPro = appMode == AppMode.PRO
    val budgetAmount = state.budgetAmount
    val actualAmount = state.actualAmount
    val variancePercent = if (budgetAmount != 0.0) ((actualAmount - budgetAmount) / budgetAmount) * 100 else 0.0
    val isOverBudget = actualAmount > budgetAmount
    val varianceColor = if (isOverBudget) CaeColors.Error else CaeColors.OnTertiaryContainer
    val sign = if (variancePercent >= 0) "+" else ""

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(CaeColors.SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = state.icon.toImageVector(), contentDescription = null, tint = CaeColors.Primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = state.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)
                    if (isPro && state.ledgerAccount.isNotBlank()) {
                        Text(text = "Compte: ${state.ledgerAccount}", fontSize = 11.sp, color = CaeColors.OnSurfaceVariant)
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Supprimer", tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(14.dp))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                OutlinedTextField(
                    value = state.actualInput,
                    onValueChange = { new -> state.actualInput = new.filter { it.isDigit() }; onActualChanged() },
                    modifier = Modifier.width(140.dp),
                    singleLine = true,
                    placeholder = { Text("0") },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, color = CaeColors.Primary),
                    suffix = { Text(text = AppRepository.currency.symbol, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CaeColors.Primary, unfocusedBorderColor = CaeColors.OutlineVariant)
                )
                Spacer(Modifier.height(2.dp))
                Text(text = "$sign${formatPercent(variancePercent)}% (vs ${formatCurrencyCompact(budgetAmount)})", fontSize = 12.sp, color = varianceColor)
            }
        }

        Spacer(Modifier.height(12.dp))

        DualProgressBar(
            budgetFraction = (budgetAmount / referenceMax).coerceIn(0.0, 1.0).toFloat(),
            actualFraction = (actualAmount / referenceMax).coerceIn(0.0, 1.0).toFloat(),
            isOverBudget = isOverBudget
        )
    }
}

@Composable
private fun DualProgressBar(budgetFraction: Float, actualFraction: Float, isOverBudget: Boolean) {
    val actualColor = if (isOverBudget) CaeColors.Error else CaeColors.TertiaryFixedDim

    Box(
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(CaeColors.SurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(budgetFraction).background(CaeColors.OutlineVariant.copy(alpha = 0.5f)))
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(actualFraction).background(actualColor))
    }
}

private fun BudgetCategoryIcon.toImageVector(): ImageVector = when (this) {
    BudgetCategoryIcon.LABOR -> Icons.Filled.Engineering
    BudgetCategoryIcon.MATERIALS -> Icons.Filled.Inventory2
    BudgetCategoryIcon.OVERHEAD -> Icons.Filled.Domain
    BudgetCategoryIcon.OTHER -> Icons.Filled.Category
}

/* ---------------- Add category dialog ---------------- */
/* ---------------- Dialogue d'ajout de catégorie ---------------- */

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String, BudgetCategoryIcon, Double, String) -> Unit, appMode: AppMode) {
    var name by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(BudgetCategoryIcon.OTHER) }
    var ledgerAccount by remember { mutableStateOf("") }
    var iconMenuExpanded by remember { mutableStateOf(false) }

    val isPro = appMode == AppMode.PRO
    val isValid = name.isNotBlank() && (budgetText.toDoubleOrNull() ?: 0.0) > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_budget_category_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.form_label_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())

                if (isPro) {
                    OutlinedTextField(
                        value = ledgerAccount,
                        onValueChange = { ledgerAccount = it },
                        label = { Text(stringResource(R.string.form_label_ledger_account)) },
                        placeholder = { Text(stringResource(R.string.form_label_ledger_account_example)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.budget_planned_label)) },
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
                        BudgetCategoryIcon.entries.forEach { i ->
                            DropdownMenuItem(text = { Text(i.name) }, onClick = { icon = i; iconMenuExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = isValid, onClick = { onSave(name.trim(), icon, budgetText.toDoubleOrNull() ?: 0.0, ledgerAccount.trim()) }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
