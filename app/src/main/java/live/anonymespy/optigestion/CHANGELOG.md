# OptiGestion — mise à niveau "vraie app de gestion financière"

Ce lot remplace vos fichiers par des versions étendues. **Le design (couleurs,
typographie, Color.kt/Theme.kt/Type.kt) n'a pas été touché** — uniquement la
structure, la navigation et les fonctionnalités.

## Ce qui a changé

### 1. Navigation à 5 onglets (au lieu de 4)
`Centres de Coût` est de retour comme un vrai onglet — l'écran existait déjà
dans votre projet mais était entièrement commenté et branché sur des données
factices (`CostCentersSampleData`), donc invisible et mort. Il est maintenant :
- branché sur `AppRepository.costCenters` (CRUD complet : ajouter, modifier,
  supprimer un centre de coût avec code, nom, icône, budget mensuel) ;
- ses dépenses ("spend") sont calculées en direct à partir des écritures
  Sheets dont le `costCenterCode` correspond — donc toujours à jour.

`NavDestination` gagne `COST_CENTERS`. `Navigation.kt` route vers
`CostCentersScreen()` réel et passe un callback `onNavigate` au Dashboard
pour les raccourcis rapides.

### 2. Tout est maintenant lié (le point demandé)
- **Sheets → Centres de Coût** : dans le formulaire d'ajout/modification
  d'une écriture, le champ "Centre de coût" est un texte libre s'il n'y a
  aucun centre créé, mais devient un **menu déroulant** listant vos vrais
  centres de coût dès qu'il y en a au moins un. Impossible de créer une
  écriture avec un code fantaisiste qui n'existe nulle part ailleurs.
- **Budget alerts → Dashboard + Rapports** : dès qu'une catégorie budgétaire
  dépasse 90% de son budget prévu, une bannière d'alerte apparaît sur le
  Dashboard (cliquable, renvoie vers Budget) et une carte dédiée apparaît
  dans Rapports.
- **Cash on hand (Settings) → Runway (Dashboard + Rapports)** : la
  trésorerie saisie dans Paramètres alimente le calcul d'autonomie
  financière affiché aux deux endroits.

### 3. Dashboard enrichi
- 4 KPI au lieu de 2 : Marge Nette, Recettes Totales, Coûts Totaux,
  Trésorerie (Runway).
- Bannière d'alertes budgétaires.
- Raccourcis rapides (Écritures / Budget / Rapports) en haut de l'écran.

### 4. "Stats" devient "Rapports" — un vrai module d'analyse financière
- Sélecteur de période (Tout / Ce mois / Ce trimestre / Cette année).
- Bandeau de 4 indicateurs financiers : Marge %, Burn Rate, Runway,
  Croissance des recettes (mois vs mois précédent).
- Résumé de la période sélectionnée (Recettes / Dépenses / Net).
- **Nouveau graphique** : flux de trésorerie (recettes vs dépenses) en
  barres groupées, 6 derniers mois.
- Classement des centres de coût par dépense, avec barre de progression
  vs budget.
- Carte d'alertes budgétaires.
- Camembert de répartition des coûts et courbe de rentabilité : conservés.
- **Export CSV réel** (au lieu du bouton factice "démo") : ouvre le
  sélecteur de fichiers Android standard (`ActivityResultContracts.
  CreateDocument`), aucune permission ni modification de manifeste requise.

### 5. Sheets
- Barre de recherche (filtre par catégorie ou centre de coût).
- Le champ centre de coût du formulaire devient un menu déroulant lié aux
  vrais centres de coût (voir point 2).

### 6. Settings
- Nouvelle section "Trésorerie" (cash on hand, pour le runway).
- Nouvelle section "Données" avec export CSV complet.

### 7. Modèle de données
- `CostCenter`, `DepartmentBudget`, `CostCenterSummary`,
  `CostCenterFootnoteIcon`, `BudgetAlert`, `PeriodFilter`, `CashFlowPoint`
  ajoutés à `Models.kt`.
- `AppRepository` gagne : persistance des centres de coût, `cashOnHand`,
  `burnRate()`, `runwayMonths()`, `revenueGrowthPercent()`,
  `marginPercent()`, `budgetAlerts()`, `costCenterSpend()`,
  `departmentBudgets()`, `costCenterSummaries()`, `cashFlowByMonth()`,
  `exportCsv()`.
- `TemplateData` fournit maintenant aussi des centres de coût d'exemple,
  alignés sur les `costCenterCode` déjà utilisés dans les écritures modèles.

## Fichiers modifiés

| Fichier | Statut |
|---|---|
| `Models.kt` | Étendu — nouveaux types |
| `AppRepository.kt` | Étendu — cost centers, KPI financiers, export CSV |
| `Formatting.kt` | Étendu — helpers de période et de mois |
| `CostCentersScreen.kt` | Réécrit — branché sur données réelles, CRUD |
| `Navigation.kt` | Étendu — 5e onglet, callback Dashboard |
| `DashboardScreen.kt` | Étendu — 2 KPI de plus, alertes, raccourcis |
| `StatsScreen.kt` | Réécrit — module Rapports complet |
| `SheetsScreen.kt` | Étendu — recherche, dropdown centre de coût |
| `SettingsScreen.kt` | Étendu — trésorerie, export CSV |
| `TemplateData.kt` | Étendu — centres de coût d'exemple |
| `BudgetVsActualScreen.kt` | Inchangé |
| `MainActivity.kt`, `Color.kt`, `Theme.kt`, `Type.kt`, `OnboardingScreen.kt` | Inchangés |

## Intégration

Remplacez vos fichiers existants par ceux-ci (mêmes noms). Aucune nouvelle
dépendance Gradle n'est nécessaire : l'export CSV utilise
`androidx.activity:activity-compose` (déjà présent, c'est ce qui fournit
`ComponentActivity`/`setContent`) et son `ActivityResultContracts.
CreateDocument`, qui ne demande aucune permission ni entrée de manifeste.

## Pistes pour aller plus loin (non implémenté ici, pour rester raisonnable)

- Transactions récurrentes / modèles de saisie rapide.
- Pièces jointes (justificatifs) sur une écriture.
- Multi-devise réelle (conversion) plutôt qu'un simple symbole d'affichage.
- Comptes/entités multiples.
- Notifications système (au lieu de la bannière in-app) pour les alertes
  budgétaires.
- Remplacer `SharedPreferences` par Room si le volume de données grandit.
