package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

/**
 * Small pill showing a trend delta with an up/down arrow — a "+5.2%" or
 * "-1.8%" style badge. Used by Dashboard's KPI-adjacent sections and by
 * Reports' profitability trend header.
 *
 * Deliberately its own file, owned by neither screen: it used to live
 * inside DashboardScreen.kt and broke StatsScreen.kt's build the moment
 * Dashboard got redesigned and no longer needed it locally. A shared
 * widget used by two screens shouldn't be private to either one.
 */
@Composable
fun DeltaBadge(text: String, isPositive: Boolean) {
    val backgroundColor = if (isPositive) CaeColors.TertiaryContainer.copy(alpha = 0.1f) else CaeColors.ErrorContainer.copy(alpha = 0.2f)
    val contentColor = if (isPositive) CaeColors.OnTertiaryContainer else CaeColors.OnErrorContainer
    val icon = if (isPositive) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown

    Row(
        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(backgroundColor).padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text = text, fontSize = 12.sp, color = contentColor)
    }
}
