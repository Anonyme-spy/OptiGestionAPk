# OptiGestion — passage de la vitrine à une vraie app

## Ce qui a changé

**Avant** : chaque écran affichait ses propres données statiques (`SampleData.kt`),
indépendantes les unes des autres, avec des champs "éditables" qui ne
persistaient nulle part et pas de vrai bouton d'ajout/suppression.

**Maintenant** : un seul objet, `AppRepository`, est la source de vérité pour
toute l'app :

- `AppRepository.entries` — le grand livre (Sheets) : chaque écriture a un
  montant réel, un centre de coût, un statut, un type (dépense/recette) et
  un horodatage.
- `AppRepository.budgetCategories` — les catégories budgétaires (Analysis) :
  budget prévu + dépense réelle saisie par l'utilisateur.

Le Dashboard (marge nette, coûts totaux, coûts par centre, activité
récente) et les Stats (répartition des coûts, tendance de rentabilité)
ne contiennent plus aucun chiffre codé en dur : tout est calculé à la
volée à partir de ces deux listes. Ajoutez une écriture dans Sheets, et
elle apparaît immédiatement dans le Dashboard et les Stats.

Les données sont sauvegardées dans les `SharedPreferences` (JSON), donc
elles survivent à la fermeture de l'app.

## Démarrage : vide par défaut, modèle en option

Au premier lancement (ou après un reset), `OnboardingScreen` propose :
- **Commencer vide** (recommandé, par défaut) — aucune donnée pré-remplie.
- **Charger un modèle d'exemple** — remplit l'app avec des écritures et
  budgets fictifs (`TemplateData.kt`), utile pour explorer l'app.

Un bouton **Réinitialiser les données** est disponible dans le menu (⋮)
de la barre du haut, sur n'importe quel écran, pour tout effacer et
refaire ce choix.

## Interactions ajoutées

- **Sheets** : bouton "+" pour ajouter une écriture (catégorie, montant,
  centre de coût, dépense/recette, statut, icône) ; toucher une ligne pour
  la modifier ou la supprimer ; filtre par statut et tri par montant
  fonctionnels ; état vide avec appel à l'action.
- **Analysis (Budget vs Réalisé)** : le champ "Dépenses Réelles" est
  maintenant relié au dépôt partagé (persisté) ; bouton "+" pour ajouter
  une catégorie budgétaire, "✕" pour en supprimer une ; état vide.
- **Dashboard / Stats** : entièrement dérivés des données ci-dessus, avec
  des états vides explicites tant qu'il n'y a rien à afficher.

## Fichiers modifiés / ajoutés

| Fichier | Statut |
|---|---|
| `AppRepository.kt` | **Nouveau** — source de vérité + persistance |
| `Formatting.kt` | **Nouveau** — formatage monnaie/date partagé |
| `OnboardingScreen.kt` | **Nouveau** — choix modèle / vide |
| `TemplateData.kt` | Remplace `SampleData.kt` — jeu de données optionnel |
| `Models.kt` | Modifié — `SheetEntry`/`BudgetCategoryUi` portent maintenant de vraies valeurs |
| `MainActivity.kt` | Modifié — initialise `AppRepository` |
| `Navigation.kt` | Modifié — ajoute `OptiGestionRoot` (onboarding) + reset |
| `SheetsScreen.kt` | Réécrit — CRUD complet |
| `DashboardScreen.kt` | Réécrit — tout est dérivé du dépôt |
| `StatsScreen.kt` | Réécrit — tout est dérivé du dépôt |
| `BudgetVsActualScreen.kt` | Réécrit — relié au dépôt partagé |
| `CostCentersScreen.kt` | Inchangé — toujours désactivé (commenté), non branché dans la navigation |

**Supprimé** : `SampleData.kt` (remplacé par `TemplateData.kt`), les anciens
imports vers un sous-package `dashboard` inexistant qui empêchaient
probablement le projet de compiler tel quel.

## Intégration

1. Remplacez vos fichiers actuels par ceux-ci (mêmes noms sauf
   `SampleData.kt` → `TemplateData.kt`).
2. Vérifiez que ces dépendances sont dans votre `build.gradle` (déjà
   nécessaires pour l'app d'origine) :
   - `androidx.navigation:navigation-compose`
   - `androidx.compose.material:material-icons-extended`
3. Aucune nouvelle dépendance n'est nécessaire — la persistance utilise
   `SharedPreferences` + `org.json`, tous deux déjà dans le SDK Android.

## Pistes pour aller plus loin

- Remplacer `SharedPreferences` par Room si le volume de données grandit.
- Lier chaque écriture Sheets à une catégorie budgétaire (aujourd'hui les
  deux listes sont volontairement indépendantes : Sheets = grand livre,
  Analysis = suivi par catégorie).
- Ajouter un vrai export PDF/Excel au bouton "Exporter le Rapport".
