package live.anonymespy.optigestion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.util.UUID

/* ============================================================
 *  COMMON
 *  COMMUN
 * ============================================================ */

enum class SyncStatus { SYNCED, PENDING, ERROR }

/* ============================================================
 *  NAVIGATION
 *  NAVIGATION
 * ============================================================ */

/**
 * The five bottom-nav / side-nav destinations shared by every screen.
 * Labels are NOT stored here anymore — they come from [Strings] via
 * [live.anonymespy.optigestion.navLabel] / [live.anonymespy.optigestion.topBarTitle]
 * so the nav bar follows the selected [AppLanguage] instead of being frozen in French.
 *
 * Les cinq destinations de navigation inférieure / latérale partagées par chaque écran.
 * Les étiquettes ne sont PLUS stockées ici — elles proviennent de [Strings] via
 * [navLabel] / [topBarTitle]
 * afin que la barre de navigation suive la [AppLanguage] sélectionnée au lieu d'être figée en français.
 */
enum class NavDestination(val route: String) {
    DASHBOARD("dashboard"),
    SHEETS("sheets"),
    ANALYSIS("analysis"),       // -> Budget vs Réalisé screen / -> écran Budget vs Réalisé
    COST_CENTERS("cost_centers"), // -> Cost Centers screen / -> écran Centres de Coûts
    STATS("stats"),               // -> Analytics & Reports screen / -> écran Analyses et Rapports
    PROFILE("profile")           // -> User Profile screen / -> écran Profil Utilisateur
}

/* ============================================================
 *  DASHBOARD SCREEN (all values below are DERIVED — see AppRepository)
 *  ÉCRAN TABLEAU DE BORD (toutes les valeurs ci-dessous sont DÉRIVÉES — voir AppRepository)
 * ============================================================ */

data class KpiCard(
    val label: String,
    val value: String,
    val deltaLabel: String,
    val isPositive: Boolean
)

/** One bar in the "cost by cost center" chart on the dashboard.
 * Une barre dans le graphique "coût par centre de coûts" sur le tableau de bord. */
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
 *
 *  ÉCRAN FEUILLES — le grand livre transactionnel. Cette liste est la
 *  source unique de vérité pour les KPI du tableau de bord, l'activité récente et
 *  le graphique des coûts par centre.
 * ============================================================ */

enum class SheetCategoryIcon { CLOUD, CAMPAIGN, HANDSHAKE, DEVICES, FLIGHT, DOMAIN, GENERIC }

enum class EntryStatus(val labelResId: Int) {
    APPROVED(R.string.entry_status_approved),
    PENDING(R.string.entry_status_pending),
    REJECTED(R.string.entry_status_rejected)
}

/**
 * A single ledger line. [amount] is always positive; [isCredit] says
 * whether it's incoming (revenue) or outgoing (a cost). [timestampMillis]
 * lets the Stats screen build a real month-by-month trend instead of a
 * static mock. [costCenterCode] should match a [CostCenter.code] when one
 * exists, which is what links a transaction to real budget tracking.
 *
 * Une seule ligne de grand livre. [amount] est toujours positif ; [isCredit] indique
 * s'il s'agit d'une entrée (revenu) ou d'une sortie (un coût). [timestampMillis]
 * permet à l'écran des Statistiques de construire une réelle tendance mois par mois au lieu d'une
 * maquette statique. [costCenterCode] doit correspondre à un [CostCenter.code] lorsqu'il
 * existe, ce qui lie une transaction au suivi budgétaire réel.
 */
data class SheetEntry(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val icon: SheetCategoryIcon,
    val amount: Double,
    val isCredit: Boolean = false,
    val costCenterCode: String,
    val status: EntryStatus,
    val timestampMillis: Long = System.currentTimeMillis(),
    // Professional fields (Pro mode only)
    // Champs professionnels (mode Pro uniquement)
    val taxRate: Double = 0.0,
    val isTtc: Boolean = true,
    val ledgerAccount: String = "",
    val createdByUserId: String = "",
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val version: Int = 1,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val amountLabel: String get() = (if (isCredit) "+" else "-") + formatCurrency(amount)

    /** Returns the amount excluding tax (HT). If isTtc is true, it decalculates tax.
     * Retourne le montant hors taxes (HT). Si isTtc est vrai, il décalcule la taxe. */
    val amountHt: Double get() = if (isTtc && taxRate > 0) amount / (1 + (taxRate / 100)) else amount

    /** Returns the amount including tax (TTC). If isTtc is false, it adds tax to the base amount.
     * Retourne le montant toutes taxes comprises (TTC). Si isTtc est faux, il ajoute la taxe au montant de base. */
    val amountTtc: Double get() = if (!isTtc && taxRate > 0) amount * (1 + (taxRate / 100)) else amount

    /** The tax component (TVA).
     * La composante fiscale (TVA). */
    val taxAmount: Double get() = amountTtc - amountHt
}

/* ============================================================
 *  BUDGET VS ACTUAL SCREEN (editable) — category-level budgets,
 *  independent of the Sheets ledger. Kept in AppRepository so the
 *  Stats screen's cost-distribution pie reads the same numbers.
 *
 *  ÉCRAN BUDGET VS RÉALISÉ (modifiable) — budgets au niveau des catégories,
 *  indépendants du grand livre des Feuilles. Conservé dans AppRepository pour que le
 *  camembert de distribution des coûts de l'écran des Statistiques lise les mêmes chiffres.
 * ============================================================ */

enum class BudgetCategoryIcon { LABOR, MATERIALS, OVERHEAD, OTHER }

/**
 * Live, observable state for one budget line. [actualInput] is the raw
 * text the user is typing; [actualAmount] parses it back to a Double,
 * falling back to 0 while the field is empty or invalid. Lives in
 * AppRepository (not `remember`) so every screen sees the same value.
 *
 * État en direct et observable pour une ligne budgétaire. [actualInput] est le texte brut
 * que l'utilisateur saisit ; [actualAmount] le convertit en Double,
 * revenant à 0 tant que le champ est vide ou invalide. Réside dans
 * AppRepository (pas dans `remember`) pour que chaque écran voie la même valeur.
 */
class BudgetCategoryUi(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: BudgetCategoryIcon,
    budgetAmount: Double,
    initialActual: Double = 0.0,
    ledgerAccount: String = "",
    var syncStatus: SyncStatus = SyncStatus.SYNCED,
    var version: Int = 1,
    var createdByUserId: String = "",
    var updatedAtMillis: Long = System.currentTimeMillis()
) {
    var budgetAmount by mutableStateOf(budgetAmount)
    var actualInput by mutableStateOf(if (initialActual == 0.0) "" else initialActual.toLong().toString())
    var ledgerAccount by mutableStateOf(ledgerAccount)
    val actualAmount: Double get() = actualInput.toDoubleOrNull() ?: 0.0
}

data class TrendVariancePoint(
    val monthLabel: String,
    val percentLabel: String,
    val heightFraction: Float
)

/** Raised on the Dashboard / Reports whenever a budget category is at or past a spend threshold.
 * Déclenché sur le Tableau de bord / Rapports chaque fois qu'une catégorie budgétaire atteint ou dépasse un seuil de dépense. */
data class BudgetAlert(
    val label: String,
    val percentUsed: Int,
    val isOverBudget: Boolean
)

/* ============================================================
 *  COST CENTERS SCREEN — real, editable departments/cost centers.
 *  Spend for each is DERIVED from Sheets entries whose costCenterCode
 *  matches (see AppRepository.departmentBudgets()).
 *
 *  ÉCRAN CENTRES DE COÛTS — départements/centres de coûts réels et modifiables.
 *  La dépense pour chacun est DÉRIVÉE des entrées des Feuilles dont le costCenterCode
 *  correspond (voir AppRepository.departmentBudgets()).
 * ============================================================ */

enum class DepartmentIcon { PRODUCTION, RESEARCH, ADMIN, SALES }

data class CostCenter(
    val id: String = UUID.randomUUID().toString(),
    val code: String,
    val name: String,
    val icon: DepartmentIcon,
    /** 0 for pure revenue centers (e.g. Sales) where "over budget" doesn't apply.
     * 0 pour les centres de revenus purs (ex: Ventes) où "dépassement de budget" ne s'applique pas. */
    val monthlyBudget: Double,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val version: Int = 1,
    val createdByUserId: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
)

enum class CostCenterFootnoteIcon { NONE, TRENDING_DOWN, WARNING }

data class CostCenterSummary(
    val label: String,
    val value: String,
    val valueColor: Color,
    val footnote: String,
    val footnoteColor: Color,
    val footnoteIcon: CostCenterFootnoteIcon = CostCenterFootnoteIcon.NONE
)

/** One department/cost-center row on the Cost Centers screen — budget vs actual spend.
 * Une ligne de département/centre de coûts sur l'écran des Centres de Coûts — budget vs dépenses réelles. */
data class DepartmentBudget(
    val costCenterId: String,
    val name: String,
    val costCenterCode: String,
    val icon: DepartmentIcon,
    val budgetAmount: Double,
    val spendAmount: Double,
    val amountLabel: String,
    val percentOfBudget: Int,
    val isOverBudget: Boolean
)

/* ============================================================
 *  ANALYTICS & STATS / REPORTS SCREEN (all DERIVED from budget
 *  categories, cost centers and sheet entries — see AppRepository)
 *
 *  ÉCRAN ANALYSES ET STATISTIQUES / RAPPORTS (tous DÉRIVÉS des catégories budgétaires,
 *  des centres de coûts et des entrées de feuilles — voir AppRepository)
 * ============================================================ */

data class CostDistributionSlice(
    val label: String,
    val percent: Int,
    val color: Color
)

/** A single point on the profitability trend line chart.
 * Un point unique sur le graphique linéaire de tendance de rentabilité. */
data class TrendPoint(
    val monthLabel: String,
    val value: Float   // 0f (bottom) .. 100f (top)
)

/** One month of income vs expense, for the cash-flow bar chart on Reports.
 * Un mois de revenus par rapport aux dépenses, pour le graphique à barres de flux de trésorerie dans les Rapports. */
data class CashFlowPoint(
    val monthLabel: String,
    val income: Double,
    val expense: Double
)

/** Reporting period presets on the Reports screen.
 * Préréglages de période de rapport sur l'écran des Rapports. */
enum class PeriodFilter(val labelResId: Int) {
    ALL(R.string.period_all),
    MONTH(R.string.period_month),
    QUARTER(R.string.period_quarter),
    YEAR(R.string.period_year)
}

/** Palette cycled through when there are more budget categories than base colors.
 * Palette parcourue lorsqu'il y a plus de catégories budgétaires que de couleurs de base. */
val DistributionPalette: List<Color> = listOf(
    Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFF9CA3AF),
    Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFF8B5CF6)
)

/* ============================================================
 *  SETTINGS — app-wide currency, chosen from the Settings screen.
 *  PARAMÈTRES — devise à l'échelle de l'application, choisie depuis l'écran des Paramètres.
 * ============================================================ */

enum class Currency(val symbol: String, val displayName: String, val symbolAfter: Boolean = false) {
    ARIARY("Ar", "Ariary (Ar)", symbolAfter = true),
    GBP("£", "Livre Sterling (£)"),
    EUR("€", "Euro (€)"),
    USD("$", "Dollar (\$)"),
    JPY("¥", "Yen (¥)")
}
