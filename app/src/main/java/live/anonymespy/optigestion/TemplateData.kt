package live.anonymespy.optigestion

/**
 * Example data offered on the onboarding screen via "Charger un modèle".
 * Nothing here is loaded automatically — the app starts empty unless the
 * user explicitly picks this option (see AppRepository.loadTemplate()).
 *
 * Exemples de données proposés sur l'écran de bienvenue via "Charger un modèle".
 * Rien ici n'est chargé automatiquement — l'application démarre vide à moins que
 * l'utilisateur ne choisisse explicitement cette option (voir AppRepository.loadTemplate()).
 */
object TemplateData {

    const val periodLabel = "T3 2026"

    fun sheetEntries(): List<SheetEntry> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return listOf(
            SheetEntry(
                category = "Infrastructure Cloud",
                icon = SheetCategoryIcon.CLOUD,
                amount = 124_500.0,
                isCredit = false,
                costCenterCode = "IT-01",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 2 * day
            ),
            SheetEntry(
                category = "Campagne Marketing T3",
                icon = SheetCategoryIcon.CAMPAIGN,
                amount = 85_200.0,
                isCredit = false,
                costCenterCode = "MKT-04",
                status = EntryStatus.PENDING,
                timestampMillis = now - 20 * day
            ),
            SheetEntry(
                category = "Honoraires de Conseil",
                icon = SheetCategoryIcon.HANDSHAKE,
                amount = 45_000.0,
                isCredit = false,
                costCenterCode = "EXEC-01",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 40 * day
            ),
            SheetEntry(
                category = "Renouvellement Matériel",
                icon = SheetCategoryIcon.DEVICES,
                amount = 210_000.0,
                isCredit = false,
                costCenterCode = "IT-02",
                status = EntryStatus.REJECTED,
                timestampMillis = now - 55 * day
            ),
            SheetEntry(
                category = "Voyages Dirigeants T3",
                icon = SheetCategoryIcon.FLIGHT,
                amount = 32_450.0,
                isCredit = false,
                costCenterCode = "EXEC-02",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 70 * day
            ),
            SheetEntry(
                category = "Renouvellement Bail Bureaux",
                icon = SheetCategoryIcon.DOMAIN,
                amount = 450_000.0,
                isCredit = false,
                costCenterCode = "FAC-01",
                status = EntryStatus.PENDING,
                timestampMillis = now - 85 * day
            ),
            SheetEntry(
                category = "Acompte Client ACME",
                icon = SheetCategoryIcon.HANDSHAKE,
                amount = 500_000.0,
                isCredit = true,
                costCenterCode = "VENTE-01",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 10 * day
            ),
            SheetEntry(
                category = "Facturation Client Q4",
                icon = SheetCategoryIcon.HANDSHAKE,
                amount = 320_000.0,
                isCredit = true,
                costCenterCode = "VENTE-01",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 100 * day
            )
        )
    }

    fun budgetCategories(): List<BudgetCategoryUi> = listOf(
        BudgetCategoryUi(name = "Main-d'œuvre", icon = BudgetCategoryIcon.LABOR, budgetAmount = 868_000.0, initialActual = 850_000.0),
        BudgetCategoryUi(name = "Matériaux", icon = BudgetCategoryIcon.MATERIALS, budgetAmount = 1_071_000.0, initialActual = 1_120_000.0),
        BudgetCategoryUi(name = "Frais généraux", icon = BudgetCategoryIcon.OVERHEAD, budgetAmount = 404_000.0, initialActual = 352_600.0)
    )

    /**
     * Real, editable cost centers matching the costCenterCode values used
     * above in sheetEntries(), so the Cost Centers screen and the Sheets
     * cost-center dropdown are populated and in sync out of the box.
     *
     * Centres de coûts réels et modifiables correspondant aux valeurs costCenterCode utilisées
     * ci-dessus dans sheetEntries(), afin que l'écran des Centres de Coûts et le menu
     * déroulant des centres de coûts des Feuilles soient remplis et synchronisés dès le départ.
     */
    fun costCenters(): List<CostCenter> = listOf(
        CostCenter(code = "IT-01", name = "Infrastructure IT", icon = DepartmentIcon.ADMIN, monthlyBudget = 150_000.0),
        CostCenter(code = "IT-02", name = "Matériel IT", icon = DepartmentIcon.ADMIN, monthlyBudget = 200_000.0),
        CostCenter(code = "MKT-04", name = "Marketing", icon = DepartmentIcon.SALES, monthlyBudget = 100_000.0),
        CostCenter(code = "EXEC-01", name = "Direction / Conseil", icon = DepartmentIcon.ADMIN, monthlyBudget = 60_000.0),
        CostCenter(code = "EXEC-02", name = "Déplacements Direction", icon = DepartmentIcon.ADMIN, monthlyBudget = 40_000.0),
        CostCenter(code = "FAC-01", name = "Facilities / Bureaux", icon = DepartmentIcon.PRODUCTION, monthlyBudget = 500_000.0),
        CostCenter(code = "VENTE-01", name = "Ventes", icon = DepartmentIcon.SALES, monthlyBudget = 0.0)
    )

    fun simpleSheetEntries(): List<SheetEntry> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return listOf(
            SheetEntry(
                category = "Courses alimentaires",
                icon = SheetCategoryIcon.GENERIC,
                amount = 150.0,
                isCredit = false,
                costCenterCode = "VIE",
                status = EntryStatus.APPROVED,
                timestampMillis = now - day
            ),
            SheetEntry(
                category = "Abonnement Internet",
                icon = SheetCategoryIcon.DOMAIN,
                amount = 40.0,
                isCredit = false,
                costCenterCode = "MAISON",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 5 * day
            ),
            SheetEntry(
                category = "Salaire",
                icon = SheetCategoryIcon.HANDSHAKE,
                amount = 2500.0,
                isCredit = true,
                costCenterCode = "REVENU",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 10 * day
            ),
            SheetEntry(
                category = "Restaurant",
                icon = SheetCategoryIcon.GENERIC,
                amount = 65.0,
                isCredit = false,
                costCenterCode = "LOISIRS",
                status = EntryStatus.APPROVED,
                timestampMillis = now - 12 * day
            )
        )
    }

    fun simpleBudgetCategories(): List<BudgetCategoryUi> = listOf(
        BudgetCategoryUi(name = "Alimentation", icon = BudgetCategoryIcon.MATERIALS, budgetAmount = 400.0, initialActual = 150.0),
        BudgetCategoryUi(name = "Logement", icon = BudgetCategoryIcon.OVERHEAD, budgetAmount = 800.0, initialActual = 800.0),
        BudgetCategoryUi(name = "Loisirs", icon = BudgetCategoryIcon.LABOR, budgetAmount = 200.0, initialActual = 65.0)
    )

    fun simpleCostCenters(): List<CostCenter> = listOf(
        CostCenter(code = "VIE", name = "Vie quotidienne", icon = DepartmentIcon.PRODUCTION, monthlyBudget = 600.0),
        CostCenter(code = "MAISON", name = "Maison", icon = DepartmentIcon.ADMIN, monthlyBudget = 900.0),
        CostCenter(code = "LOISIRS", name = "Loisirs", icon = DepartmentIcon.SALES, monthlyBudget = 200.0),
        CostCenter(code = "REVENU", name = "Revenus", icon = DepartmentIcon.SALES, monthlyBudget = 0.0)
    )
}
