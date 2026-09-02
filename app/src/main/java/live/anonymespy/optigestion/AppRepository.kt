package live.anonymespy.optigestion

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import live.anonymespy.optigestion.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * Single, app-wide source of truth. Every screen reads from and writes to
 * this object, so a value changed on one screen (e.g. adding a Sheets
 * entry) is instantly reflected everywhere it's used (Dashboard KPIs,
 * Stats charts, etc). State also survives process death via SharedPreferences.
 */
object AppRepository {

    private const val PREFS_NAME = "optigestion_prefs"
    private const val KEY_INITIALIZED = "initialized"
    private const val KEY_PERIOD = "period_label"
    private const val KEY_ENTRIES = "entries_json"
    private const val KEY_CATEGORIES = "categories_json"
    private const val KEY_CURRENCY = "currency"
    private const val KEY_THEME_MODE = "theme_mode"

    private lateinit var prefs: SharedPreferences

    /** True once the user has picked "Load template" or "Start empty" on this device. */
    var hasChosenSetup by mutableStateOf(false)
        private set

    var periodLabel by mutableStateOf(currentPeriodLabel())

    /** App-wide currency, changeable any time from the Settings screen. */
    var currency by mutableStateOf(Currency.USD)

    /** App-wide light/dark/AMOLED preference, changeable any time from the Settings screen. */
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    /** The transactional ledger shown on the Sheets screen. Empty by default. */
    val entries: SnapshotStateList<SheetEntry> = mutableStateListOf()

    /** Category-level budgets shown on the Budget vs Actual screen. Empty by default. */
    val budgetCategories: SnapshotStateList<BudgetCategoryUi> = mutableStateListOf()

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

    /* ---------------- Onboarding ---------------- */

    fun loadTemplate() {
        entries.clear()
        entries.addAll(TemplateData.sheetEntries())
        budgetCategories.clear()
        budgetCategories.addAll(TemplateData.budgetCategories())
        periodLabel = TemplateData.periodLabel
        hasChosenSetup = true
        persist()
    }


    fun startEmpty() {
        entries.clear()
        budgetCategories.clear()
        periodLabel = currentPeriodLabel()
        hasChosenSetup = true
        persist()
    }

    /** Wipes all data and sends the user back to the template-choice screen. Currency is kept. */
    fun resetToOnboarding() {
        entries.clear()
        budgetCategories.clear()
        hasChosenSetup = false
        prefs.edit()
            .remove(KEY_INITIALIZED)
            .remove(KEY_PERIOD)
            .remove(KEY_ENTRIES)
            .remove(KEY_CATEGORIES)
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

    fun addBudgetCategory(name: String, icon: BudgetCategoryIcon, budgetAmount: Double) {
        budgetCategories.add(BudgetCategoryUi(name = name, icon = icon, budgetAmount = budgetAmount))
        persist()
    }

    fun deleteBudgetCategory(id: String) {
        budgetCategories.removeAll { it.id == id }
        persist()
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
                })
            }
        }
        prefs.edit()
            .putBoolean(KEY_INITIALIZED, hasChosenSetup)
            .putString(KEY_PERIOD, periodLabel)
            .putString(KEY_ENTRIES, entriesJson.toString())
            .putString(KEY_CATEGORIES, categoriesJson.toString())
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
                        timestampMillis = o.getLong("timestampMillis")
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
                    budgetAmount = o.getDouble("budgetAmount")
                )
                ui.actualInput = o.optString("actualInput", "")
                budgetCategories.add(ui)
            }
        }
    }

    /* ---------------- Derived analytics (Dashboard + Stats read these) ---------------- */

    fun netMargin(): Double {
        val credits = entries.filter { it.isCredit }.sumOf { it.amount }
        val debits = entries.filter { !it.isCredit }.sumOf { it.amount }
        return credits - debits
    }

    fun totalCosts(): Double = entries.filter { !it.isCredit }.sumOf { it.amount }

    /** Top cost centers by spend, for the Dashboard bar chart. */
    fun costCenterBars(limit: Int = 4): List<CostCenterBar> {
        val byCenterCode = entries.filter { !it.isCredit }
            .groupBy { it.costCenterCode }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
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
        val byMonth = entries.groupBy { monthKeyAndLabel(it.timestampMillis) }
            .toList()
            .sortedBy { it.first.first } // sort by "yyyy-M" key
            .takeLast(6)
            .map { (keyLabel, list) ->
                val net = list.sumOf { if (it.isCredit) it.amount else -it.amount }
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
}