package live.anonymespy.optigestion

import live.anonymespy.optigestion.ui.theme.CaeColors
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * "Feuille de Saisie" — the data-entry spreadsheet screen. Reads and writes
 * directly to AppRepository so every entry created here immediately shows
 * up in Dashboard KPIs, the cost-center chart and the Stats screen.
 */
@Composable
fun SheetsScreen() {
    val context = LocalContext.current
    val entries = AppRepository.entries

    var statusFilter by remember { mutableStateOf<EntryStatus?>(null) }
    var sortDescending by remember { mutableStateOf(true) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<SheetEntry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val visibleEntries = remember(entries.toList(), statusFilter, sortDescending, searchQuery) {
        entries
            .filter { statusFilter == null || it.status == statusFilter }
            .filter {
                searchQuery.isBlank() ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.costCenterCode.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith(compareBy<SheetEntry> { it.amount }.let { if (sortDescending) it.reversed() else it })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ControlsBar(
                periodLabel = AppRepository.periodLabel,
                activeFilter = statusFilter,
                showFilterMenu = showFilterMenu,
                onToggleFilterMenu = { showFilterMenu = !showFilterMenu },
                onSelectFilter = { statusFilter = it; showFilterMenu = false },
                onSort = { sortDescending = !sortDescending },
                onImportExcel = { Toast.makeText(context, "Import Excel — bientôt disponible", Toast.LENGTH_SHORT).show() }
            )

            if (visibleEntries.isEmpty()) {
                EmptySheetsState(
                    hasAnyEntries = entries.isNotEmpty(),
                    onAddEntry = { showAddDialog = true }
                )
            } else {
                SheetTable(entries = visibleEntries, onRowClick = { editingEntry = it })
            }

            Text(
                text = "${entries.size} écriture(s) au total",
                fontSize = 12.sp,
                color = CaeColors.OnSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(72.dp))
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = CaeColors.Primary,
            contentColor = CaeColors.OnPrimary
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Ajouter une écriture")
        }
    }

    if (showAddDialog) {
        EntryFormDialog(
            title = "Nouvelle écriture",
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { entry -> AppRepository.addEntry(entry); showAddDialog = false },
            onDelete = null
        )
    }

    editingEntry?.let { entry ->
        EntryFormDialog(
            title = "Modifier l'écriture",
            initial = entry,
            onDismiss = { editingEntry = null },
            onSave = { updated -> AppRepository.updateEntry(updated); editingEntry = null },
            onDelete = { AppRepository.deleteEntry(entry.id); editingEntry = null }
        )
    }
}

/* ---------------- Empty state ---------------- */

@Composable
private fun EmptySheetsState(hasAnyEntries: Boolean, onAddEntry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Inbox,
            contentDescription = null,
            tint = CaeColors.OnSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (hasAnyEntries) "Aucune écriture ne correspond à ce filtre" else "Aucune écriture pour le moment",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = CaeColors.OnSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (!hasAnyEntries) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Ajoutez votre première écriture pour commencer à alimenter le tableau de bord.",
                fontSize = 12.sp,
                color = CaeColors.OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAddEntry, colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Ajouter une écriture")
            }
        }
    }
}

/* ---------------- Search ---------------- */

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        placeholder = { Text("Rechercher une catégorie ou un centre de coût…") },
        leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = CaeColors.OnSurfaceVariant) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Effacer", tint = CaeColors.OnSurfaceVariant)
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CaeColors.Primary,
            unfocusedBorderColor = CaeColors.OutlineVariant
        )
    )
}

/* ---------------- Controls bar ---------------- */

@Composable
private fun ControlsBar(
    periodLabel: String,
    activeFilter: EntryStatus?,
    showFilterMenu: Boolean,
    onToggleFilterMenu: () -> Unit,
    onSelectFilter: (EntryStatus?) -> Unit,
    onSort: () -> Unit,
    onImportExcel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box {
                ControlChip(
                    icon = Icons.Filled.FilterList,
                    label = activeFilter?.label ?: "Filtrer",
                    onClick = onToggleFilterMenu
                )
                DropdownMenu(expanded = showFilterMenu, onDismissRequest = onToggleFilterMenu) {
                    DropdownMenuItem(text = { Text("Toutes") }, onClick = { onSelectFilter(null) })
                    EntryStatus.entries.forEach { status ->
                        DropdownMenuItem(text = { Text(status.label) }, onClick = { onSelectFilter(status) })
                    }
                }
            }
            ControlChip(icon = Icons.Filled.Sort, label = "Trier", onClick = onSort)
            ControlChip(icon = Icons.Filled.UploadFile, label = "Importer Excel", onClick = onImportExcel)
        }
        Text(text = periodLabel, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant)
    }
}

@Composable
private fun ControlChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CaeColors.SurfaceContainerLow)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant)
    }
}

/* ---------------- Table ---------------- */

private const val COL_CATEGORY_WEIGHT = 2f
private const val COL_AMOUNT_WEIGHT = 1f
private const val COL_COST_CENTER_WEIGHT = 1f
private const val COL_STATUS_WEIGHT = 1f

@Composable
private fun SheetTable(entries: List<SheetEntry>, onRowClick: (SheetEntry) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CaeColors.SurfaceContainerLowest)
    ) {
        TableHeader()
        entries.forEach { entry ->
            TableRow(entry, onClick = { onRowClick(entry) })
        }
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CaeColors.SurfaceContainerLow)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Catégorie", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant, modifier = Modifier.weight(COL_CATEGORY_WEIGHT).padding(horizontal = 12.dp))
        Text("Montant", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(COL_AMOUNT_WEIGHT).padding(horizontal = 12.dp))
        Text("Centre de Coût", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(COL_COST_CENTER_WEIGHT).padding(horizontal = 4.dp))
        Text("Statut", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(COL_STATUS_WEIGHT).padding(horizontal = 4.dp))
    }
    HorizontalDivider(color = CaeColors.SurfaceVariant, thickness = 2.dp)
}

@Composable
private fun TableRow(entry: SheetEntry, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(COL_CATEGORY_WEIGHT).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = entry.icon.toImageVector(), contentDescription = null, tint = CaeColors.Primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = entry.category, fontSize = 14.sp, color = CaeColors.OnSurface)
            }
            Text(
                text = entry.amountLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (entry.isCredit) CaeColors.OnTertiaryContainer else CaeColors.OnSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(COL_AMOUNT_WEIGHT).padding(horizontal = 12.dp)
            )
            Text(entry.costCenterCode, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(COL_COST_CENTER_WEIGHT).padding(horizontal = 4.dp))
            Box(modifier = Modifier.weight(COL_STATUS_WEIGHT), contentAlignment = Alignment.Center) {
                StatusPill(status = entry.status)
            }
        }
        HorizontalDivider(color = CaeColors.SurfaceVariant)
    }
}

@Composable
private fun StatusPill(status: EntryStatus) {
    val (bg, text) = when (status) {
        EntryStatus.APPROVED -> CaeColors.ApprovedBg to CaeColors.ApprovedText
        EntryStatus.PENDING -> CaeColors.PendingBg to CaeColors.PendingText
        EntryStatus.REJECTED -> CaeColors.RejectedBg to CaeColors.RejectedText
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = status.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = text)
    }
}

private fun SheetCategoryIcon.toImageVector(): ImageVector = when (this) {
    SheetCategoryIcon.CLOUD -> Icons.Filled.Cloud
    SheetCategoryIcon.CAMPAIGN -> Icons.Filled.Campaign
    SheetCategoryIcon.HANDSHAKE -> Icons.Filled.Handshake
    SheetCategoryIcon.DEVICES -> Icons.Filled.Devices
    SheetCategoryIcon.FLIGHT -> Icons.Filled.FlightTakeoff
    SheetCategoryIcon.DOMAIN -> Icons.Filled.Domain
    SheetCategoryIcon.GENERIC -> Icons.Filled.Receipt
}

/* ---------------- Add / edit form ---------------- */

@Composable
private fun EntryFormDialog(
    title: String,
    initial: SheetEntry?,
    onDismiss: () -> Unit,
    onSave: (SheetEntry) -> Unit,
    onDelete: (() -> Unit)?
) {
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toLong()?.toString() ?: "") }
    var costCenterCode by remember { mutableStateOf(initial?.costCenterCode ?: "") }
    var isCredit by remember { mutableStateOf(initial?.isCredit ?: false) }
    var status by remember { mutableStateOf(initial?.status ?: EntryStatus.PENDING) }
    var icon by remember { mutableStateOf(initial?.icon ?: SheetCategoryIcon.GENERIC) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var iconMenuExpanded by remember { mutableStateOf(false) }

    val isValid = category.isNotBlank() && costCenterCode.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                    label = { Text("Montant") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                val availableCostCenters = AppRepository.costCenters
                if (availableCostCenters.isNotEmpty()) {
                    var ccMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = availableCostCenters.find { it.code == costCenterCode }
                                ?.let { "${it.code} — ${it.name}" } ?: costCenterCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Centre de coût") },
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { ccMenuExpanded = true }
                        )
                        DropdownMenu(expanded = ccMenuExpanded, onDismissRequest = { ccMenuExpanded = false }) {
                            availableCostCenters.forEach { cc ->
                                DropdownMenuItem(
                                    text = { Text("${cc.code} — ${cc.name}") },
                                    onClick = { costCenterCode = cc.code; ccMenuExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = costCenterCode,
                        onValueChange = { costCenterCode = it.uppercase() },
                        label = { Text("Centre de coût (ex: IT-01)") },
                        supportingText = { Text("Astuce : créez des centres de coût dans l'onglet Centres pour choisir dans une liste.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Type :", fontSize = 14.sp, color = CaeColors.OnSurfaceVariant, modifier = Modifier.weight(1f))
                    FilterChip(selected = !isCredit, onClick = { isCredit = false }, label = { Text("Dépense") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = isCredit, onClick = { isCredit = true }, label = { Text("Recette") })
                }

                Box {
                    OutlinedTextField(
                        value = status.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Statut") },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // A read-only OutlinedTextField swallows taps before a
                    // Modifier.clickable placed directly on it ever fires.
                    // A transparent box drawn on top actually catches the tap.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { statusMenuExpanded = true }
                    )
                    DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                        EntryStatus.entries.forEach { s ->
                            DropdownMenuItem(text = { Text(s.label) }, onClick = { status = s; statusMenuExpanded = false })
                        }
                    }
                }

                Box {
                    OutlinedTextField(
                        value = icon.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Icône") },
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
                        SheetCategoryIcon.entries.forEach { i ->
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
                        SheetEntry(
                            id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                            category = category.trim(),
                            icon = icon,
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            isCredit = isCredit,
                            costCenterCode = costCenterCode.trim(),
                            status = status,
                            timestampMillis = initial?.timestampMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            ) { Text("Enregistrer") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("Supprimer", color = CaeColors.Error) }
                }
                TextButton(onClick = onDismiss) { Text("Annuler") }
            }
        }
    )
}