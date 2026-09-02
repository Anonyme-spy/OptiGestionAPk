package live.anonymespy.optigestion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.util.UUID

/* ============================================================
 *  NAVIGATION
 * ============================================================ */

/** The four bottom-nav / side-nav destinations shared by every screen. */
enum class NavDestination(val route: String, val label: String) {
    DASHBOARD("dashboard", "Dashboard"),
    SHEETS("sheets", "Sheets"),
    ANALYSIS("analysis", "Analysis"),   // -> Budget vs Réalisé screen
    STATS("stats", "Stats")             // -> Analytics & Stats screen
}

/* ============================================================
 *  DASHBOARD SCREEN (all values below are DERIVED — see AppRepository)
 * ============================================================ */

data class KpiCard(
    val label: String,
    val value: String,
    val deltaLabel: String,
    val isPositive: Boolean
)

/** One bar in the "cost by cost center" chart on the dashboard. */
data class CostCenterBar(
    val name: String,
    val amountLabel: String,
    val heightFraction: Float,
    val highlighted: Boolean
)

enum class ActivityIcon { TRUCK, SERVER, PAYMENT, GENERIC }

data class ActivityItem(
    val title: String,
    val reference: String,
    val amountLabel: String,
    val dateLabel: String,
    val isCredit: Boolean,
    val icon: ActivityIcon
)

/* ============================================================
 *  SHEETS SCREEN — the transactional ledger. This list is the
 *  single source of truth for Dashboard KPIs, recent activity and
 *  the cost-by-center chart.
 * ============================================================ */

enum class SheetCategoryIcon { CLOUD, CAMPAIGN, HANDSHAKE, DEVICES, FLIGHT, DOMAIN, GENERIC }

enum class EntryStatus(val label: String) {
    APPROVED("Approuvé"),
    PENDING("En attente"),
    REJECTED("Rejeté")
}

/**
 * A single ledger line. [amount] is always positive; [isCredit] says
 * whether it's incoming (revenue) or outgoing (a cost). [timestampMillis]
 * lets the Stats screen build a real month-by-month trend instead of a
 * static mock.
 */
data class SheetEntry(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val icon: SheetCategoryIcon,
    val amount: Double,
    val isCredit: Boolean = false,
    val costCenterCode: String,
    val status: EntryStatus,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val amountLabel: String get() = (if (isCredit) "+" else "-") + formatCurrency(amount)
}

/* ============================================================
 *  BUDGET VS ACTUAL SCREEN (editable) — category-level budgets,
 *  independent of the Sheets ledger. Kept in AppRepository so the
 *  Stats screen's cost-distribution pie reads the same numbers.
 * ============================================================ */

enum class BudgetCategoryIcon { LABOR, MATERIALS, OVERHEAD, OTHER }

/**
 * Live, observable state for one budget line. [actualInput] is the raw
 * text the user is typing; [actualAmount] parses it back to a Double,
 * falling back to 0 while the field is empty or invalid. Lives in
 * AppRepository (not `remember`) so every screen sees the same value.
 */
class BudgetCategoryUi(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: BudgetCategoryIcon,
    budgetAmount: Double,
    initialActual: Double = 0.0
) {
    var budgetAmount by mutableStateOf(budgetAmount)
    var actualInput by mutableStateOf(if (initialActual == 0.0) "" else initialActual.toLong().toString())
    val actualAmount: Double get() = actualInput.toDoubleOrNull() ?: 0.0
}

data class TrendVariancePoint(
    val monthLabel: String,
    val percentLabel: String,
    val heightFraction: Float
)

/* ============================================================
 *  ANALYTICS & STATS SCREEN (all DERIVED from budget categories
 *  and sheet entries — see AppRepository)
 * ============================================================ */

data class CostDistributionSlice(
    val label: String,
    val percent: Int,
    val color: Color
)

/** A single point on the profitability trend line chart. */
data class TrendPoint(
    val monthLabel: String,
    val value: Float   // 0f (bottom) .. 100f (top)
)

/** Palette cycled through when there are more budget categories than base colors. */
val DistributionPalette: List<Color> = listOf(
    Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF9CA3AF),
    Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
)

/* ============================================================
 *  SETTINGS — app-wide currency, chosen from the Settings screen.
 * ============================================================ */

enum class Currency(val symbol: String, val displayName: String, val symbolAfter: Boolean = false) {
    ARIARY("Ar", "Ariary (Ar)", symbolAfter = true),
    GBP("£", "Livre Sterling (£)"),
    EUR("€", "Euro (€)"),
    USD("$", "Dollar (\$)"),
    JPY("¥", "Yen (¥)")
}