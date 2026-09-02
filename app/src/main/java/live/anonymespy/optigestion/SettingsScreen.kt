package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors
import live.anonymespy.optigestion.ui.theme.ThemeMode

/**
 * Theme + currency pickers — read/write AppRepository directly, so every
 * screen updates immediately (colours via CaeColors, amounts via
 * formatCurrency).
 */
@Composable
fun SettingsScreen() {
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