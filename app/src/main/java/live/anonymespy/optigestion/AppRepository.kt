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
import java.util.UUID

/**
 * Single, app-wide source of truth. Every screen reads from and writes to
 * this object, so a value changed on one screen (e.g. adding a Sheets
 * entry) is instantly reflected everywhere it's used (Dashboard KPIs,
 * Stats charts, Cost Centers, etc). State also survives process death via
 * SharedPreferences.
 *
 * Source unique de vérité à l'échelle de l'application. Chaque écran lit et écrit dans
 * cet objet, de sorte qu'une valeur modifiée sur un écran (ex: ajout d'une entrée
 * dans Feuilles) est instantanément reflétée partout où elle est utilisée (KPI du Tableau de bord,
 * graphiques de Statistiques, Centres de Coûts, etc.). L'état survit également à la fermeture du processus via
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
    private const val KEY_USER_JSON = "user_json"
    private const val KEY_APP_LOGO = "app_logo"

    private lateinit var prefs: SharedPreferences

    /** True once the user has picked "Load template" or "Start empty" on this device.
     * Vrai une fois que l'utilisateur a choisi "Charger un modèle" ou "Démarrer vide" sur cet appareil. */
    var hasChosenSetup by mutableStateOf(false)
        private set

    var periodLabel by mutableStateOf(currentPeriodLabel())

    /** App-wide currency, changeable any time from the Settings screen.
     * Devise à l'échelle de l'application, modifiable à tout moment depuis l'écran des Paramètres. */
    var currency by mutableStateOf(Currency.USD)

    /** App-wide light/dark/AMOLED preference, changeable any time from the Settings screen.
     * Préférence clair/sombre/AMOLED à l'échelle de l'application, modifiable à tout moment depuis l'écran des Paramètres. */
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)

    /** Cash currently available, used to compute the runway KPI on Dashboard/Reports.
     * Liquidités actuellement disponibles, utilisées pour calculer le KPI de piste sur le Tableau de bord / Rapports. */
    var cashOnHand by mutableStateOf(0.0)
        private set

    /** Display language. Only Dashboard/Navigation/Settings are localized so far — see Strings.kt.
     * Langue d'affichage. Seuls le Tableau de bord, la Navigation et les Paramètres sont localisés pour l'instant — voir Strings.kt. */
    var language by mutableStateOf(AppLanguage.FRENCH)
        private set

    /** The current UI mode (Simple vs Pro).
     * Le mode d'interface actuel (Simple vs Pro). */
    var appMode by mutableStateOf(AppMode.PRO)
        private set

    /** The current logged-in user profile (mocked).
     * Le profil utilisateur actuellement connecté (simulé). */
    var currentUser by mutableStateOf<User?>(null)
        private set

    /** App logo / icon identifier. Could be a resource name or a local file URI.
     * Identifiant du logo / de l'icône de l'application. Peut être un nom de ressource ou un URI de fichier local. */
    var appLogo by mutableStateOf("default")
        private set

    /** The transactional ledger shown on the Sheets screen. Empty by default.
     * Le grand livre transactionnel affiché sur l'écran des Feuilles. Vide par défaut. */
    val entries: SnapshotStateList<SheetEntry> = mutableStateListOf()

    /** Category-level budgets shown on the Budget vs Actual screen. Empty by default.
     * Budgets au niveau des catégories affichés sur l'écran Budget vs Réalisé. Vide par défaut. */
    val budgetCategories: SnapshotStateList<BudgetCategoryUi> = mutableStateListOf()

    /** Real, editable departments/cost centers shown on the Cost Centers screen.
     * Départements/centres de coûts réels et modifiables affichés sur l'écran des Centres de Coûts. */
    val costCenters: SnapshotStateList<CostCenter> = mutableStateListOf()

    /** Planned total is simply the sum of every category's planned budget — one source of truth.
     * Le total prévu est simplement la somme du budget prévu de chaque catégorie — une seule source de vérité. */
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
        prefs.getString(KEY_USER_JSON, null)?.let { json ->
            currentUser = runCatching {
                val o = JSONObject(json)
                User(
                    id = o.getString("id"),
                    email = o.optString("email").takeIf { it.isNotEmpty() },
                    displayName = o.getString("displayName"),
                    accountType = AccountType.valueOf(o.getString("accountType")),
                    companyId = o.optString("companyId").takeIf { it.isNotEmpty() },
                    companyName = o.optString("companyName").takeIf { it.isNotEmpty() },
                    companyIndustry = o.optString("companyIndustry").takeIf { it.isNotEmpty() },
                    enterpriseRole = o.optString("enterpriseRole").takeIf { it.isNotEmpty() }?.let { EnterpriseRole.valueOf(it) },
                    phone = o.optString("phone").takeIf { it.isNotEmpty() },
                    avatarUrl = o.optString("avatarUrl").takeIf { it.isNotEmpty() },
                    jobTitle = o.optString("jobTitle").takeIf { it.isNotEmpty() },
                    bio = o.optString("bio").takeIf { it.isNotEmpty() },
                    createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis())
                )
            }.getOrNull()
        }
        // Bootstrap: make sure the per-app locale matches our saved preference on
        // cold start (covers the very first run after this feature ships, before
        // AppCompatDelegate has its own record of a choice). selectLanguage()
        // keeps the two in sync from here on for every subsequent change.
        //
        // Amorçage : assurez-vous que les paramètres régionaux par application correspondent à notre préférence enregistrée au
        // démarrage à froid (couvre la toute première exécution après le déploiement de cette fonctionnalité, avant
        // que AppCompatDelegate n'ait son propre enregistrement d'un choix). selectLanguage()
        // maintient les deux en synchronisation à partir de là pour chaque changement ultérieur.
        applyAppLanguage(language)
        appLogo = prefs.getString(KEY_APP_LOGO, "default") ?: "default"
        hasChosenSetup = prefs.getBoolean(KEY_INITIALIZED, false)
        if (hasChosenSetup) restoreFromPrefs()
    }

    /** Currency is a device setting, kept even across a full data reset.
     * La devise est un paramètre de l'appareil, conservé même après une réinitialisation complète des données. */
    fun selectCurrency(newCurrency: Currency) {
        currency = newCurrency
        if (::prefs.isInitialized) prefs.edit().putString(KEY_CURRENCY, newCurrency.name).apply()
    }

    /** Theme mode is a device setting, kept even across a full data reset.
     * Le mode de thème est un paramètre de l'appareil, conservé même après une réinitialisation complète des données. */
    fun selectThemeMode(newMode: ThemeMode) {
        themeMode = newMode
        if (::prefs.isInitialized) prefs.edit().putString(KEY_THEME_MODE, newMode.name).apply()
    }

    /** Cash on hand feeds the runway KPI. Kept even across a full data reset.
     * Les liquidités disponibles alimentent le KPI de piste. Conservé même après une réinitialisation complète des données. */
    fun selectCashOnHand(amount: Double) {
        cashOnHand = amount
        if (::prefs.isInitialized) prefs.edit().putFloat(KEY_CASH_ON_HAND, amount.toFloat()).apply()
    }

    /** Display language is a device setting, kept even across a full data reset.
     * La langue d'affichage est un paramètre de l'appareil, conservé même après une réinitialisation complète des données. */
    fun selectLanguage(newLanguage: AppLanguage) {
        language = newLanguage
        if (::prefs.isInitialized) prefs.edit().putString(KEY_LANGUAGE, newLanguage.name).apply()
        applyAppLanguage(newLanguage)
    }

    /** App logo is a device setting.
     * Le logo de l'application est un paramètre de l'appareil. */
    fun selectAppLogo(logo: String) {
        appLogo = logo
        if (::prefs.isInitialized) prefs.edit().putString(KEY_APP_LOGO, logo).apply()
    }

    /** Updates the current user profile and persists it.
     * Met à jour le profil de l'utilisateur actuel et le persiste. */
    fun updateUserProfile(updated: User) {
        this.currentUser = updated
        persist()
    }

    /* ---------------- Onboarding & Auth ---------------- */
    /* ---------------- Accueil & Auth ---------------- */

    suspend fun signup(request: RegisterRequest): Result<User> {
        return try {
            val response = NetworkModule.authService.register(request)
            if (response.isSuccessful) {
                val auth = response.body()!!
                SecurePrefs.accessToken = auth.accessToken
                SecurePrefs.refreshToken = auth.refreshToken
                this.currentUser = auth.user
                this.appMode = if (auth.user.accountType == AccountType.PARTICULIER) appMode else AppMode.PRO
                hasChosenSetup = true
                persist()
                Result.success(auth.user)
            } else {
                Result.failure(Exception("Signup failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = NetworkModule.authService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val auth = response.body()!!
                SecurePrefs.accessToken = auth.accessToken
                SecurePrefs.refreshToken = auth.refreshToken
                this.currentUser = auth.user
                this.appMode = if (auth.user.accountType == AccountType.PARTICULIER) appMode else AppMode.PRO
                hasChosenSetup = true
                persist()
                Result.success(auth.user)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startGuest(mode: AppMode, useTemplate: Boolean) {
        val user = User(
            id = UUID.randomUUID().toString(),
            displayName = "Invité",
            accountType = AccountType.GUEST,
            createdAtMillis = System.currentTimeMillis()
        )
        this.currentUser = user
        this.appMode = mode
        if (useTemplate) {
            loadTemplate(mode)
        } else {
            startEmpty(mode)
        }
    }

    fun completeOnboarding(user: User, mode: AppMode, useTemplate: Boolean) {
        this.currentUser = user
        this.appMode = mode
        if (useTemplate) {
            loadTemplate(mode)
        } else {
            startEmpty(mode)
        }
    }

    private fun loadTemplate(mode: AppMode) {
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

    /** Wipes all data and sends the user back to the template-choice screen. Currency/cash are kept.
     * Efface toutes les données et renvoie l'utilisateur à l'écran de choix de modèle. La devise et les liquidités sont conservées. */
    fun resetToOnboarding() {
        entries.clear()
        budgetCategories.clear()
        costCenters.clear()
        hasChosenSetup = false
        currentUser = null
        prefs.edit()
            .remove(KEY_INITIALIZED)
            .remove(KEY_PERIOD)
            .remove(KEY_ENTRIES)
            .remove(KEY_CATEGORIES)
            .remove(KEY_COST_CENTERS)
            .remove(KEY_APP_MODE)
            .remove(KEY_USER_JSON)
            .apply()
    }

    /** Total TVA component (Collected - Deductible).
     * Composante TVA totale (Collectée - Déductible). */
    fun vatLiability(): Double {
        val collected = entries.filter { it.isCredit }.sumOf { it.taxAmount }
        val deductible = entries.filter { !it.isCredit }.sumOf { it.taxAmount }
        return collected - deductible
    }

    /** Count of items waiting to be synced.
     * Nombre d'éléments en attente de synchronisation. */
    fun pendingSyncCount(): Int {
        return entries.count { it.syncStatus == SyncStatus.PENDING } +
               budgetCategories.count { it.syncStatus == SyncStatus.PENDING } +
               costCenters.count { it.syncStatus == SyncStatus.PENDING }
    }

    /* ---------------- Sheets entries ---------------- */
    /* ---------------- Entrées des Feuilles ---------------- */

    fun addEntry(entry: SheetEntry) {
        val toAdd = entry.copy(syncStatus = if (currentUser?.accountType == AccountType.GUEST) SyncStatus.SYNCED else SyncStatus.PENDING)
        entries.add(0, toAdd)
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
    /* ---------------- Catégories budgétaires ---------------- */

    fun addBudgetCategory(name: String, icon: BudgetCategoryIcon, budgetAmount: Double, ledgerAccount: String = "") {
        val sync = if (currentUser?.accountType == AccountType.GUEST) SyncStatus.SYNCED else SyncStatus.PENDING
        budgetCategories.add(BudgetCategoryUi(name = name, icon = icon, budgetAmount = budgetAmount, ledgerAccount = ledgerAccount, syncStatus = sync))
        persist()
    }

    fun deleteBudgetCategory(id: String) {
        budgetCategories.removeAll { it.id == id }
        persist()
    }

    /** Budget categories whose actual spend has hit [thresholdPercent] of their planned budget.
     * Catégories budgétaires dont les dépenses réelles ont atteint [thresholdPercent] de leur budget prévu. */
    fun budgetAlerts(thresholdPercent: Double = 90.0): List<BudgetAlert> =
        budgetCategories.mapNotNull { c ->
            if (c.budgetAmount <= 0.0) return@mapNotNull null
            val percent = (c.actualAmount / c.budgetAmount) * 100
            if (percent >= thresholdPercent) BudgetAlert(c.name, percent.toInt(), c.actualAmount > c.budgetAmount) else null
        }.sortedByDescending { it.percentUsed }

    /* ---------------- Cost centers ---------------- */
    /* ---------------- Centres de coûts ---------------- */

    fun addCostCenter(code: String, name: String, icon: DepartmentIcon, monthlyBudget: Double) {
        val sync = if (currentUser?.accountType == AccountType.GUEST) SyncStatus.SYNCED else SyncStatus.PENDING
        costCenters.add(CostCenter(code = code, name = name, icon = icon, monthlyBudget = monthlyBudget, syncStatus = sync))
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

    /** Total spend (debits only) posted against a given cost-center code.
     * Dépense totale (débits uniquement) imputée à un code de centre de coûts donné. */
    fun costCenterSpend(code: String): Double {
        val useHt = appMode == AppMode.PRO
        return entries.filter { !it.isCredit && it.costCenterCode == code }.sumOf { if (useHt) it.amountHt else it.amountTtc }
    }

    /** Every cost center with its live spend, sorted by how close to (or past) budget it is.
     * Chaque centre de coûts avec ses dépenses en direct, trié selon sa proximité (ou son dépassement) par rapport au budget. */
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

    /** Call after mutating a BudgetCategoryUi's actualInput/budgetAmount so it survives restart.
     * Appel après avoir muté le actualInput/budgetAmount d'un BudgetCategoryUi pour qu'il survive au redémarrage. */
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
                    put("createdByUserId", e.createdByUserId)
                    put("syncStatus", e.syncStatus.name)
                    put("version", e.version)
                    put("updatedAtMillis", e.updatedAtMillis)
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
                    put("syncStatus", c.syncStatus.name)
                    put("version", c.version)
                    put("createdByUserId", c.createdByUserId)
                    put("updatedAtMillis", c.updatedAtMillis)
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
                    put("syncStatus", cc.syncStatus.name)
                    put("version", cc.version)
                    put("createdByUserId", cc.createdByUserId)
                    put("updatedAtMillis", cc.updatedAtMillis)
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
            .apply {
                currentUser?.let { user ->
                    val userJson = JSONObject().apply {
                        put("id", user.id)
                        put("email", user.email)
                        put("displayName", user.displayName)
                        put("accountType", user.accountType.name)
                        put("companyId", user.companyId)
                        put("companyName", user.companyName)
                        put("companyIndustry", user.companyIndustry)
                        put("enterpriseRole", user.enterpriseRole?.name)
                        put("phone", user.phone)
                        put("avatarUrl", user.avatarUrl)
                        put("jobTitle", user.jobTitle)
                        put("bio", user.bio)
                        put("createdAtMillis", user.createdAtMillis)
                    }
                    putString(KEY_USER_JSON, userJson.toString())
                }
            }
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
                        ledgerAccount = o.optString("ledgerAccount", ""),
                        createdByUserId = o.optString("createdByUserId", ""),
                        syncStatus = runCatching { SyncStatus.valueOf(o.optString("syncStatus")) }.getOrDefault(SyncStatus.SYNCED),
                        version = o.optInt("version", 1),
                        updatedAtMillis = o.optLong("updatedAtMillis", System.currentTimeMillis())
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
                    ledgerAccount = o.optString("ledgerAccount", ""),
                    syncStatus = runCatching { SyncStatus.valueOf(o.optString("syncStatus")) }.getOrDefault(SyncStatus.SYNCED),
                    version = o.optInt("version", 1),
                    createdByUserId = o.optString("createdByUserId", ""),
                    updatedAtMillis = o.optLong("updatedAtMillis", System.currentTimeMillis())
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
                        monthlyBudget = o.getDouble("monthlyBudget"),
                        syncStatus = runCatching { SyncStatus.valueOf(o.optString("syncStatus")) }.getOrDefault(SyncStatus.SYNCED),
                        version = o.optInt("version", 1),
                        createdByUserId = o.optString("createdByUserId", ""),
                        updatedAtMillis = o.optLong("updatedAtMillis", System.currentTimeMillis())
                    )
                )
            }
        }
    }

    /* ---------------- Derived analytics (Dashboard + Stats read these) ---------------- */
    /* ---------------- Analyses dérivées (Tableau de bord + Statistiques les lisent) ---------------- */

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

    /** Net margin as a percentage of revenue, or null when there's no revenue to divide by.
     * Marge nette en pourcentage du revenu, ou null lorsqu'il n'y a pas de revenu par lequel diviser. */
    fun marginPercent(): Double? {
        val revenue = totalRevenue()
        if (revenue == 0.0) return null
        return (netMargin() / revenue) * 100
    }

    /** Average monthly cash outflow over the most recent (up to 3) months with expense data.
     * Sortie de fonds mensuelle moyenne sur les mois les plus récents (jusqu'à 3) avec des données de dépenses. */
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

    /** Months of runway left at the current burn rate. Null means burn rate is 0 (infinite runway).
     * Mois de piste restants au taux de combustion actuel. Null signifie que le taux de combustion est de 0 (piste infinie). */
    fun runwayMonths(): Double? {
        val burn = burnRate()
        if (burn <= 0.0) return null
        return cashOnHand / burn
    }

    /** % change in monthly revenue between the two most recent months that had any revenue.
     * Variation en % du revenu mensuel entre les deux mois les plus récents ayant eu des revenus. */
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

    /** Top cost centers by spend, for the Dashboard bar chart.
     * Principaux centres de coûts par dépense, pour le graphique à barres du Tableau de bord. */
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

    /** Net margin per month (last 6 months with data), normalized to 0..100 for the line chart.
     * Marge nette par mois (6 derniers mois avec données), normalisée de 0 à 100 pour le graphique linéaire. */
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

    /** Income vs expense per month (last [limit] months with data), for the Reports cash-flow chart.
     * Revenus vs dépenses par mois (derniers [limit] mois avec données), pour le graphique de flux de trésorerie des Rapports. */
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

    /** Full ledger as CSV text, for the Reports/Settings export feature.
     * Grand livre complet au format texte CSV, pour la fonctionnalité d'exportation des Rapports/Paramètres. */
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
