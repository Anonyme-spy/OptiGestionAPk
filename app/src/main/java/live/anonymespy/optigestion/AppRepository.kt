package live.anonymespy.optigestion

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import live.anonymespy.optigestion.ui.theme.CaeColors
import live.anonymespy.optigestion.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Single, app-wide source of truth. Every screen reads from and writes to
 * this object, so a value changed on one screen (e.g. adding a Sheets
 * entry) is instantly reflected everywhere it's used (Dashboard KPIs,
 * Stats charts, Cost Centers, etc). State also survives process death via
 * SharedPreferences.
 */
object AppRepository {

    private const val PREFS_NAME = "optigestion_prefs"
    private const val KEY_INITIALIZED = "initialized"
    private const val KEY_PERIOD = "period_label"
    private const val KEY_ENTRIES = "entries_json"
    private const val KEY_CATEGORIES = "categories_json"
    private const val KEY_COST_CENTERS = "cost_centers_json"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_CASH_ON_HAND = "cash_on_hand"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_APP_MODE = "app_mode"

    private lateinit var prefs: SharedPreferences

    /** True once the user has picked "Load template" or "Start empty" on this device. */
    var hasChosenSetup by mutableStateOf(false)
        private set

    var periodLabel by mutableStateOf(currentPeriodLabel())

    /** App-wide currency, changeable any time from the Settings screen. */
    var currency by mutableStateOf(Currency.USD)

    /** App-wide light/dark/AMOLED preference, changeable any time from the Settings screen. */
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    /** Cash currently available, used to compute the runway KPI on Dashboard/Reports. */
    var cashOnHand by mutableStateOf(0.0)
        private set

    /** Display language. Only Dashboard/Navigation/Settings are localized so far — see Strings.kt. */
    var language by mutableStateOf(AppLanguage.FRENCH)
        private set

    /** The current UI mode (Simple vs Pro). */
    var appMode by mutableStateOf(AppMode.PRO)
        private set

    /** The transactional ledger shown on the Sheets screen. Empty by default. */
    val entries: SnapshotStateList<SheetEntry> = mutableStateListOf()

    /** Category-level budgets shown on the Budget vs Actual screen. Empty by default. */
    val budgetCategories: SnapshotStateList<BudgetCategoryUi> = mutableStateListOf()

    /** Real, editable departments/cost centers shown on the Cost Centers screen. */
    val costCenters: SnapshotStateList<CostCenter> = mutableStateListOf()

    /** Planned total is simply the sum of every category's planned budget — one source of truth. */
    val plannedBudgetTotal: Double get() = budgetCategories.sumOf { it.budgetAmount }

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.getString(KEY_CURRENCY, null)?.let { saved ->
            currency = runCatching { Currency.valueOf(saved) }.getOrDefault(Currency.USD)
        }
        prefs.getString(KEY_THEME_MODE, null)?.let { saved ->
            themeMode = runCatching { ThemeMode.valueOf(saved) }.getOrDefault(ThemeMode.SYSTEM)
        }
        cashOnHand = prefs.getFloat(KEY_CASH_ON_HAND, 0f).toDouble()
        prefs.getString(KEY_LANGUAGE, null)?.let { saved ->
            language = runCatching { AppLanguage.valueOf(saved) }.getOrDefault(AppLanguage.FRENCH)
        }
        prefs.getString(KEY_APP_MODE, null)?.let { saved ->
            appMode = runCatching { AppMode.valueOf(saved) }.getOrDefault(AppMode.PRO)
        }
        // Bootstrap: make sure the per-app locale matches our saved preference on
        // cold start (covers the very first run after this feature ships, before
        // AppCompatDelegate has its own record of a choice). selectLanguage()
        // keeps the two in sync from here on for every subsequent change.
        applyAppLanguage(language)
        hasChosenSetup = prefs.getBoolean(KEY_INITIALIZED, false)
        if (hasChosenSetup) restoreFromPrefs()
    }

    /** Currency is a device setting, kept even across a full data reset. */
    fun selectCurrency(newCurrency: Currency) {
        currency = newCurrency
        if (::prefs.isInitialized) prefs.edit().putString(KEY_CURRENCY, newCurrency.name).apply()
    }

    /** Theme mode is a device setting, kept even across a full data reset. */
    fun selectThemeMode(newMode: ThemeMode) {
        themeMode = newMode
        if (::prefs.isInitialized) prefs.edit().putString(KEY_THEME_MODE, newMode.name).apply()
    }

    /** Cash on hand feeds the runway KPI. Kept even across a full data reset. */
    fun selectCashOnHand(amount: Double) {
        cashOnHand = amount
        if (::prefs.isInitialized) prefs.edit().putFloat(KEY_CASH_ON_HAND, amount.toFloat()).apply()
    }

    /** Display language is a device setting, kept even across a full data reset. */
    fun selectLanguage(newLanguage: AppLanguage) {
        language = newLanguage
        if (::prefs.isInitialized) prefs.edit().putString(KEY_LANGUAGE, newLanguage.name).apply()
        applyAppLanguage(newLanguage)
    }

    /* ---------------- Onboarding ---------------- */

    fun loadTemplate(mode: AppMode) {
        appMode = mode
        entries.clear()
        if (mode == AppMode.PRO) {
            entries.addAll(TemplateData.sheetEntries())
            budgetCategories.clear()
            budgetCategories.addAll(TemplateData.budgetCategories())
            costCenters.clear()
            costCenters.addAll(TemplateData.costCenters())
        } else {
            entries.addAll(TemplateData.simpleSheetEntries())
            budgetCategories.clear()
            budgetCategories.addAll(TemplateData.simpleBudgetCategories())
            costCenters.clear()
            costCenters.addAll(TemplateData.simpleCostCenters())
        }
        periodLabel = TemplateData.periodLabel
        hasChosenSetup = true
        persist()
    }

    fun startEmpty(mode: AppMode) {
        appMode = mode
        entries.clear()
        budgetCategories.clear()
        costCenters.clear()
        periodLabel = currentPeriodLabel()
        hasChosenSetup = true
        persist()
    }

    /** Wipes all data and sends the user back to the template-choice screen. Currency/cash are kept. */
    fun resetToOnboarding() {
        entries.clear()
        budgetCategories.clear()
        costCenters.clear()
        hasChosenSetup = false
        prefs.edit()
            .remove(KEY_INITIALIZED)
            .remove(KEY_PERIOD)
            .remove(KEY_ENTRIES)
            .remove(KEY_CATEGORIES)
            .remove(KEY_COST_CENTERS)
            .remove(KEY_APP_MODE)
            .apply()
    }

    /* ---------------- Sheets entries ---------------- */

    fun addEntry(entry: SheetEntry) {
        entries.add(0, entry)
        persist()
    }

    fun updateEntry(updated: SheetEntry) {
        val idx = entries.indexOfFirst { it.id == updated.id }
        if (idx != -1) entries[idx] = updated
        persist()
    }

    fun deleteEntry(id: String) {
        entries.removeAll { it.id == id }
        persist()
    }

    /* ---------------- Budget categories ---------------- */

    fun addBudgetCategory(name: String, icon: BudgetCategoryIcon, budgetAmount: Double, ledgerAccount: String = "") {
        budgetCategories.add(BudgetCategoryUi(name = name, icon = icon, budgetAmount = budgetAmount, ledgerAccount = ledgerAccount))
        persist()
    }

    fun deleteBudgetCategory(id: String) {
        budgetCategories.removeAll { it.id == id }
        persist()
    }

    /** Budget categories whose actual spend has hit [thresholdPercent] of their planned budget. */
    fun budgetAlerts(thresholdPercent: Double = 90.0): List<BudgetAlert> =
        budgetCategories.mapNotNull { c ->
            if (c.budgetAmount <= 0.0) return@mapNotNull null
            val percent = (c.actualAmount / c.budgetAmount) * 100
            if (percent >= thresholdPercent) BudgetAlert(c.name, percent.toInt(), c.actualAmount > c.budgetAmount) else null
        }.sortedByDescending { it.percentUsed }

    /* ---------------- Cost centers ---------------- */

    fun addCostCenter(code: String, name: String, icon: DepartmentIcon, monthlyBudget: Double) {
        costCenters.add(CostCenter(code = code, name = name, icon = icon, monthlyBudget = monthlyBudget))
        persist()
    }

    fun updateCostCenter(updated: CostCenter) {
        val idx = costCenters.indexOfFirst { it.id == updated.id }
        if (idx != -1) costCenters[idx] = updated
        persist()
    }

    fun deleteCostCenter(id: String) {
        costCenters.removeAll { it.id == id }
        persist()
    }

    /** Total spend (debits only) posted against a given cost-center code. */
    fun costCenterSpend(code: String): Double {
        val useHt = appMode == AppMode.PRO
        return entries.filter { !it.isCredit && it.costCenterCode == code }.sumOf { if (useHt) it.amountHt else it.amountTtc }
    }

    /** Every cost center with its live spend, sorted by how close to (or past) budget it is. */
    fun departmentBudgets(): List<DepartmentBudget> =
        costCenters.map { cc ->
            val spend = costCenterSpend(cc.code)
            val percent = if (cc.monthlyBudget > 0) ((spend / cc.monthlyBudget) * 100).toInt() else 0
            DepartmentBudget(
                costCenterId = cc.id,
                name = cc.name,
                costCenterCode = cc.code,
                icon = cc.icon,
                budgetAmount = cc.monthlyBudget,
                spendAmount = spend,
                amountLabel = formatCurrencyCompact(spend),
                percentOfBudget = percent,
                isOverBudget = cc.monthlyBudget > 0 && spend > cc.monthlyBudget
            )
        }.sortedByDescending { it.percentOfBudget }

    fun costCenterSummaries(): List<CostCenterSummary> {
        val totalBudget = costCenters.sumOf { it.monthlyBudget }
        val totalSpend = costCenters.sumOf { costCenterSpend(it.code) }
        val overCount = departmentBudgets().count { it.isOverBudget }
        return listOf(
            CostCenterSummary(
                label = "BUDGET TOTAL",
                value = formatCurrencyCompact(totalBudget),
                valueColor = CaeColors.Primary,
                footnote = "${costCenters.size} centre(s) de coût",
                footnoteColor = CaeColors.OnSurfaceVariant
            ),
            CostCenterSummary(
                label = "DÉPENSÉ",
                value = formatCurrencyCompact(totalSpend),
                valueColor = CaeColors.Primary,
                footnote = if (totalBudget > 0) "${formatPercent((totalSpend / totalBudget) * 100)}% du budget" else "Aucun budget défini",
                footnoteColor = CaeColors.OnSurfaceVariant
            ),
            CostCenterSummary(
                label = "EN DÉPASSEMENT",
                value = overCount.toString(),
                valueColor = if (overCount > 0) CaeColors.Error else CaeColors.Primary,
                footnote = if (overCount > 0) "Nécessite votre attention" else "Tout est sous contrôle",
                footnoteColor = if (overCount > 0) CaeColors.Error else CaeColors.OnTertiaryContainer,
                footnoteIcon = if (overCount > 0) CostCenterFootnoteIcon.WARNING else CostCenterFootnoteIcon.TRENDING_DOWN
            )
        )
    }

    /** Call after mutating a BudgetCategoryUi's actualInput/budgetAmount so it survives restart. */
    fun persist() {
        if (!::prefs.isInitialized) return
        val entriesJson = JSONArray().apply {
            entries.forEach { e ->
                put(JSONObject().apply {
                    put("id", e.id)
                    put("category", e.category)
                    put("icon", e.icon.name)
                    put("amount", e.amount)
                    put("isCredit", e.isCredit)
                    put("costCenterCode", e.costCenterCode)
                    put("status", e.status.name)
                    put("timestampMillis", e.timestampMillis)
                    // New fields
                    put("taxRate", e.taxRate)
                    put("isTtc", e.isTtc)
                    put("ledgerAccount", e.ledgerAccount)
                })
            }
        }
        val categoriesJson = JSONArray().apply {
            budgetCategories.forEach { c ->
                put(JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("icon", c.icon.name)
                    put("budgetAmount", c.budgetAmount)
                    put("actualInput", c.actualInput)
                    put("ledgerAccount", c.ledgerAccount)
                })
            }
        }
        val costCentersJson = JSONArray().apply {
            costCenters.forEach { cc ->
                put(JSONObject().apply {
                    put("id", cc.id)
                    put("code", cc.code)
                    put("name", cc.name)
                    put("icon", cc.icon.name)
                    put("monthlyBudget", cc.monthlyBudget)
                })
            }
        }
        prefs.edit()
            .putBoolean(KEY_INITIALIZED, hasChosenSetup)
            .putString(KEY_PERIOD, periodLabel)
            .putString(KEY_ENTRIES, entriesJson.toString())
            .putString(KEY_CATEGORIES, categoriesJson.toString())
            .putString(KEY_COST_CENTERS, costCentersJson.toString())
            .putString(KEY_APP_MODE, appMode.name)
            .apply()
    }

    private fun restoreFromPrefs() {
        periodLabel = prefs.getString(KEY_PERIOD, currentPeriodLabel()) ?: currentPeriodLabel()

        entries.clear()
        prefs.getString(KEY_ENTRIES, null)?.let { raw ->
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                entries.add(
                    SheetEntry(
                        id = o.getString("id"),
                        category = o.getString("category"),
                        icon = SheetCategoryIcon.valueOf(o.getString("icon")),
                        amount = o.getDouble("amount"),
                        isCredit = o.getBoolean("isCredit"),
                        costCenterCode = o.getString("costCenterCode"),
                        status = EntryStatus.valueOf(o.getString("status")),
                        timestampMillis = o.getLong("timestampMillis"),
                        taxRate = o.optDouble("taxRate", 0.0),
                        isTtc = o.optBoolean("isTtc", true),
                        ledgerAccount = o.optString("ledgerAccount", "")
                    )
                )
            }
        }

        budgetCategories.clear()
        prefs.getString(KEY_CATEGORIES, null)?.let { raw ->
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val ui = BudgetCategoryUi(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    icon = BudgetCategoryIcon.valueOf(o.getString("icon")),
                    budgetAmount = o.getDouble("budgetAmount"),
                    ledgerAccount = o.optString("ledgerAccount", "")
                )
                ui.actualInput = o.optString("actualInput", "")
                budgetCategories.add(ui)
            }
        }

        costCenters.clear()
        prefs.getString(KEY_COST_CENTERS, null)?.let { raw ->
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                costCenters.add(
                    CostCenter(
                        id = o.getString("id"),
                        code = o.getString("code"),
                        name = o.getString("name"),
                        icon = DepartmentIcon.valueOf(o.getString("icon")),
                        monthlyBudget = o.getDouble("monthlyBudget")
                    )
                )
            }
        }
    }

    /* ---------------- Derived analytics (Dashboard + Stats read these) ---------------- */

    fun netMargin(): Double {
        val useHt = appMode == AppMode.PRO
        val credits = entries.filter { it.isCredit }.sumOf { if (useHt) it.amountHt else it.amountTtc }
        val debits = entries.filter { !it.isCredit }.sumOf { if (useHt) it.amountHt else it.amountTtc }
        return credits - debits
    }

    fun totalCosts(): Double {
        val useHt = appMode == AppMode.PRO
        return entries.filter { !it.isCredit }.sumOf { if (useHt) it.amountHt else it.amountTtc }
    }

    fun totalRevenue(): Double {
        val useHt = appMode == AppMode.PRO
        return entries.filter { it.isCredit }.sumOf { if (useHt) it.amountHt else it.amountTtc }
    }

    /** Net margin as a percentage of revenue, or null when there's no revenue to divide by. */
    fun marginPercent(): Double? {
        val revenue = totalRevenue()
        if (revenue == 0.0) return null
        return (netMargin() / revenue) * 100
    }

    /** Average monthly cash outflow over the most recent (up to 3) months with expense data. */
    fun burnRate(): Double {
        // Burn rate is a cash-flow KPI, so always use TTC (actual cash leaving)
        val byMonth = entries.filter { !it.isCredit }
            .groupBy { monthKeyAndLabel(it.timestampMillis).first }
            .mapValues { (_, list) -> list.sumOf { it.amountTtc } }
            .toList()
            .sortedByDescending { it.first }
            .take(3)
        if (byMonth.isEmpty()) return 0.0
        return byMonth.sumOf { it.second } / byMonth.size
    }

    /** Months of runway left at the current burn rate. Null means burn rate is 0 (infinite runway). */
    fun runwayMonths(): Double? {
        val burn = burnRate()
        if (burn <= 0.0) return null
        return cashOnHand / burn
    }

    /** % change in monthly revenue between the two most recent months that had any revenue. */
    fun revenueGrowthPercent(): Double? {
        val byMonth = entries.filter { it.isCredit }
            .groupBy { monthKeyAndLabel(it.timestampMillis).first }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.first }
            .take(2)
        if (byMonth.size < 2) return null
        val latest = byMonth[0].second
        val previous = byMonth[1].second
        if (previous == 0.0) return null
        return ((latest - previous) / previous) * 100
    }

    /** Top cost centers by spend, for the Dashboard bar chart. */
    fun costCenterBars(limit: Int = 4): List<CostCenterBar> {
        val useHt = appMode == AppMode.PRO
        val byCenterCode = entries.filter { !it.isCredit }
            .groupBy { it.costCenterCode }
            .mapValues { (_, list) -> list.sumOf { if (useHt) it.amountHt else it.amountTtc } }
            .toList()
            .sortedByDescending { it.second }
            .take(limit)
        if (byCenterCode.isEmpty()) return emptyList()
        val max = byCenterCode.maxOf { it.second }
        return byCenterCode.mapIndexed { index, (code, total) ->
            CostCenterBar(
                name = code,
                amountLabel = formatCurrencyCompact(total),
                heightFraction = if (max > 0) (total / max).toFloat().coerceIn(0.05f, 1f) else 0f,
                highlighted = index < 2
            )
        }
    }

    fun recentActivity(limit: Int = 6): List<ActivityItem> =
        entries.sortedByDescending { it.timestampMillis }.take(limit).map { e ->
            ActivityItem(
                title = e.category,
                reference = e.costCenterCode,
                amountLabel = e.amountLabel,
                dateLabel = formatDateLabel(e.timestampMillis),
                isCredit = e.isCredit,
                icon = when (e.icon) {
                    SheetCategoryIcon.FLIGHT, SheetCategoryIcon.DOMAIN -> ActivityIcon.TRUCK
                    SheetCategoryIcon.CLOUD, SheetCategoryIcon.DEVICES -> ActivityIcon.SERVER
                    SheetCategoryIcon.HANDSHAKE, SheetCategoryIcon.CAMPAIGN -> ActivityIcon.PAYMENT
                    SheetCategoryIcon.GENERIC -> ActivityIcon.GENERIC
                }
            )
        }

    fun costDistribution(): List<CostDistributionSlice> {
        val total = budgetCategories.sumOf { it.actualAmount }
        if (total <= 0.0) return emptyList()
        return budgetCategories.mapIndexed { index, c ->
            CostDistributionSlice(
                label = c.name,
                percent = ((c.actualAmount / total) * 100).let { Math.round(it).toInt() },
                color = DistributionPalette[index % DistributionPalette.size]
            )
        }.filter { it.percent > 0 }
    }

    /** Net margin per month (last 6 months with data), normalized to 0..100 for the line chart. */
    fun profitabilityTrend(): List<TrendPoint> {
        if (entries.isEmpty()) return emptyList()
        val useHt = appMode == AppMode.PRO
        val byMonth = entries.groupBy { monthKeyAndLabel(it.timestampMillis) }
            .toList()
            .sortedBy { it.first.first } // sort by "yyyy-M" key
            .takeLast(6)
            .map { (keyLabel, list) ->
                val net = list.sumOf {
                    val valToUse = if (useHt) it.amountHt else it.amountTtc
                    if (it.isCredit) valToUse else -valToUse
                }
                keyLabel.second to net
            }
        if (byMonth.size < 2) return emptyList()
        val min = byMonth.minOf { it.second }
        val max = byMonth.maxOf { it.second }
        val range = (max - min).takeIf { it != 0.0 } ?: 1.0
        return byMonth.map { (label, net) ->
            TrendPoint(monthLabel = label, value = (((net - min) / range) * 100).toFloat())
        }
    }

    fun profitabilityDeltaLabel(): String {
        val trend = profitabilityTrend()
        if (trend.size < 2) return "—"
        val delta = trend.last().value - trend.first().value
        val sign = if (delta >= 0) "+" else ""
        return "$sign${formatPercent(delta.toDouble())}%"
    }

    /** Income vs expense per month (last [limit] months with data), for the Reports cash-flow chart. */
    fun cashFlowByMonth(limit: Int = 6): List<CashFlowPoint> {
        if (entries.isEmpty()) return emptyList()
        // Cash flow is about actual cash, so always use TTC
        return entries.groupBy { monthKeyAndLabel(it.timestampMillis) }
            .toList()
            .sortedBy { it.first.first }
            .takeLast(limit)
            .map { (keyLabel, list) ->
                CashFlowPoint(
                    monthLabel = keyLabel.second,
                    income = list.filter { it.isCredit }.sumOf { it.amountTtc },
                    expense = list.filter { !it.isCredit }.sumOf { it.amountTtc }
                )
            }
    }

    /** Full ledger as CSV text, for the Reports/Settings export feature. */
    fun exportCsv(): String {
        val isPro = appMode == AppMode.PRO
        val sb = StringBuilder()
        if (isPro) {
            sb.append("Date,Catégorie,Compte,Centre,Type,Montant Saisi,HT/TTC,Taux TVA,Montant HT,TVA,Montant TTC,Statut\n")
        } else {
            sb.append("Date,Intitulé,Projet,Type,Montant,Statut\n")
        }
        entries.sortedByDescending { it.timestampMillis }.forEach { e ->
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.FRENCH).format(Date(e.timestampMillis))
            val type = if (e.isCredit) "Recette" else "Dépense"
            val categoryEscaped = "\"" + e.category.replace("\"", "\"\"") + "\""
            if (isPro) {
                val modeLabel = if (e.isTtc) "TTC" else "HT"
                sb.append("$dateStr,$categoryEscaped,${e.ledgerAccount},${e.costCenterCode},$type,${e.amount},$modeLabel,${e.taxRate},${e.amountHt},${e.taxAmount},${e.amountTtc},${e.status.name}\n")
            } else {
                sb.append("$dateStr,$categoryEscaped,${e.costCenterCode},$type,${e.amount},${e.status.name}\n")
            }
        }
        return sb.toString()
    }
}
