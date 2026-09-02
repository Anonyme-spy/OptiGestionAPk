package live.anonymespy.optigestion

/**
 * Example data offered on the onboarding screen via "Charger un modèle".
 * Nothing here is loaded automatically — the app starts empty unless the
 * user explicitly picks this option (see AppRepository.loadTemplate()).
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
}
