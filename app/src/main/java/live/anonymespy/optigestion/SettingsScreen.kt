package live.anonymespy.optigestion

import android.app.Activity
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors
import live.anonymespy.optigestion.ui.theme.ThemeMode

/**
 * Theme + language + currency + cash-on-hand pickers, plus data export —
 * read/write AppRepository directly, so every screen updates immediately
 * (colours via CaeColors, text via strings.xml + AppCompatDelegate, amounts
 * via formatCurrency, runway via cashOnHand).
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    // Recreating is the reliable fallback: AppCompatDelegate.setApplicationLocales()
    // (called inside AppRepository.selectLanguage()) is supposed to recreate an
    // AppCompatActivity automatically, but forcing it here removes any doubt
    // instead of hoping the OS-version-dependent auto path fires.
    val activity = context as? Activity

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(AppRepository.exportCsv().toByteArray())
            }
            Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        SectionHeader(title = stringResource(R.string.settings_language), subtitle = stringResource(R.string.settings_language_subtitle))

        Spacer(Modifier.height(16.dp))

        SettingsCard {
            AppLanguage.entries.forEachIndexed { index, option ->
                LanguageRow(
                    option = option,
                    selected = AppRepository.language == option,
                    onSelect = {
                        AppRepository.selectLanguage(option)
                        activity?.recreate()
                    }
                )
                if (index != AppLanguage.entries.lastIndex) {
                    HorizontalDivider(color = CaeColors.SurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        SectionHeader(title = stringResource(R.string.settings_appearance), subtitle = stringResource(R.string.settings_appearance_subtitle))

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

        SectionHeader(title = stringResource(R.string.settings_currency), subtitle = stringResource(R.string.settings_currency_subtitle))

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

        SectionHeader(title = stringResource(R.string.settings_cash), subtitle = stringResource(R.string.settings_cash_subtitle))

        Spacer(Modifier.height(16.dp))

        SettingsCard {
            CashOnHandRow()
        }

        Spacer(Modifier.height(28.dp))

        SectionHeader(title = stringResource(R.string.settings_data), subtitle = stringResource(R.string.settings_data_subtitle))

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
                    Text(text = stringResource(R.string.settings_export_csv), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
                    Text(
                        text = stringResource(R.string.settings_export_csv_subtitle_format, AppRepository.entries.size),
                        fontSize = 12.sp,
                        color = CaeColors.OnSurfaceVariant
                    )
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
private fun LanguageRow(option: AppLanguage, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Language, contentDescription = null, tint = CaeColors.OnSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text = stringResource(option.titleResId), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
        }
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = CaeColors.Primary)
        )
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
                text = stringResource(R.string.settings_currency_example_format, if (option.symbolAfter) "1 234 ${option.symbol}" else "${option.symbol}1,234"),
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
            Text(text = stringResource(R.string.settings_cash_label), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = CaeColors.OnSurface)
            Text(text = stringResource(R.string.settings_cash_helper), fontSize = 12.sp, color = CaeColors.OnSurfaceVariant)
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
