package live.anonymespy.optigestion

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors
import live.anonymespy.optigestion.ui.theme.ThemeMode

/**
 * Theme + currency + cash-on-hand pickers, plus data export — read/write
 * AppRepository directly, so every screen updates immediately (colours via
 * CaeColors, amounts via formatCurrency, runway via cashOnHand).
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(AppRepository.exportCsv().toByteArray())
            }
            Toast.makeText(context, "Données exportées avec succès", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Échec de l'export", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader(title = "Apparence", subtitle = "Choisissez le thème de couleurs de l'app.")

        Spacer(Modifier.height(16.dp))

        SettingsCard {
            ThemeMode.entries.forEachIndexed { index, option ->
                ThemeRow(
                    option = option,
                    selected = AppRepository.themeMode == option,
                    onSelect = { AppRepository.selectThemeMode(option) }
                )
                if (index != ThemeMode.entries.lastIndex) {
                    HorizontalDivider(color = CaeColors.SurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        SectionHeader(title = "Devise", subtitle = "Utilisée pour tous les montants affichés dans l'app.")

        Spacer(Modifier.height(16.dp))

        SettingsCard {
            Currency.entries.forEachIndexed { index, option ->
                CurrencyRow(
                    option = option,
                    selected = AppRepository.currency == option,
                    onSelect = { AppRepository.selectCurrency(option) }
                )
                if (index != Currency.entries.lastIndex) {
                    HorizontalDivider(color = CaeColors.SurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        SectionHeader(title = "Trésorerie", subtitle = "Utilisée pour calculer votre autonomie financière (runway) sur le Dashboard et les Rapports.")

        Spacer(Modifier.height(16.dp))

        SettingsCard {
            CashOnHandRow()
        }

        Spacer(Modifier.height(28.dp))

        SectionHeader(title = "Données", subtitle = "Exportez l'ensemble de vos écritures au format CSV, exploitable dans Excel ou Google Sheets.")

        Spacer(Modifier.height(16.dp))

        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val filename = "optigestion_export_${System.currentTimeMillis() / 1000}.csv"
                        exportLauncher.launch(filename)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Exporter toutes les écritures (CSV)", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
                    Text(text = "${AppRepository.entries.size} écriture(s) seront incluses", fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
                }
                Icon(imageVector = Icons.Filled.Download, contentDescription = null, tint = CaeColors.Primary)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp, color = CaeColors.OnSurfaceVariant)
    Spacer(Modifier.height(4.dp))
    Text(text = subtitle, fontSize = 13.sp, color = CaeColors.OnSurfaceVariant)
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun ThemeRow(option: ThemeMode, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = option.label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = CaeColors.Primary)
        )
    }
}

@Composable
private fun CurrencyRow(option: Currency, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = option.displayName, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
            Text(
                text = "Exemple : " + if (option.symbolAfter) "1 234 ${option.symbol}" else "${option.symbol}1,234",
                fontSize = 12.sp,
                color = CaeColors.OnSurfaceVariant
            )
        }
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = CaeColors.Primary)
        )
    }
}

@Composable
private fun CashOnHandRow() {
    var cashText by remember { mutableStateOf(AppRepository.cashOnHand.takeIf { it > 0 }?.toLong()?.toString() ?: "") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Trésorerie disponible", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
            Text(text = "Utilisée pour le calcul du runway", fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
        }
        OutlinedTextField(
            value = cashText,
            onValueChange = { new ->
                cashText = new.filter { it.isDigit() }
                AppRepository.selectCashOnHand(cashText.toDoubleOrNull() ?: 0.0)
            },
            modifier = Modifier.width(140.dp),
            singleLine = true,
            placeholder = { Text("0") },
            suffix = { Text(text = AppRepository.currency.symbol, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CaeColors.Primary, unfocusedBorderColor = CaeColors.OutlineVariant)
        )
    }
}
