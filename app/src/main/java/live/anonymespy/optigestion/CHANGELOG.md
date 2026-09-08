# OptiGestion — mise à niveau "vraie app de gestion financière"

## Round 17 — Initialisation Automatique du Backend

### Amélioration de la robustesse du Backend
- **Auto-initialisation de la base de données** : Correction de l'erreur `ER_NO_SUCH_TABLE` au premier démarrage.
    - Le backend Node.js vérifie désormais l'existence des tables au lancement.
    - Création automatique du schéma (tables, index, enums) si nécessaire via [`db.service.ts`](file:///mnt/extra/My project/optigestion-backend/src/services/db.service.ts).
    - Plus besoin de lancer manuellement `npm run db:init` avant de démarrer le serveur.

## Round 16 — Sécurisation du Réseau & Autorisation Cleartext

### Correction du blocage réseau
- **Autorisation du trafic en clair (HTTP)** : Correction de l'erreur "Cleartext communication not permitted".
    - Création du fichier [`network_security_config.xml`](file:///mnt/My_files/My_project_source/androidStudioProjects/OptiGestion/app/src/main/res/xml/network_security_config.xml).
    - Configuration spécifique pour autoriser les adresses locales (`192.168.178.25`, `10.0.2.2`, `localhost`).
    - Mise à jour du [`AndroidManifest.xml`](file:///mnt/My_files/My_project_source/androidStudioProjects/OptiGestion/app/src/main/AndroidManifest.xml) pour inclure la permission `INTERNET` et lier la configuration de sécurité.

## Round 15 — Configuration Réseau & Backend Readiness

### Préparation de la Connexion API
- **Nouveau fichier `NetworkConfig.kt`** : Centralisation de la configuration réseau.
    - Définition de la `BASE_URL` (par défaut `http://10.0.2.2:4000` pour l'émulateur).
    - Ajout des préfixes de versioning API (`/api/v1`).
- **Instructions de Test** : Documentation de la procédure pour relier l'app Android au backend Node.js local.

## Round 14 — Profils Enrichis & Consistance Visuelle

### Identité Visuelle Avancée
- **Avatar Intelligent** : La barre de titre affiche désormais en priorité l'avatar de l'utilisateur (ou son initiale). En l'absence d'avatar, elle utilise l'icône personnalisée choisie dans les Paramètres.
- **Bibliothèque d'Icônes Étendue** : Ajout de 4 nouvelles icônes professionnelles (Arborescence, Paiements, Reçu, Magasin) pour la personnalisation de la marque.
- **Navigation Optimisée** : La barre de titre permet un accès direct au profil, tandis que la navigation principale reste épurée pour les outils métiers.

### Profil Utilisateur Complet
- **Champs Additionnels** : Le profil supporte désormais des informations riches pour les utilisateurs et les organisations :
    - Utilisateur : Poste/Titre, Biographie, Téléphone.
    - Entreprise : Secteur d'activité, Site Web, Identifiant fiscal (TIN), Adresse.
- **Onboarding Guest Flexible** : Les utilisateurs invités peuvent maintenant configurer intégralement leur expérience (Mode Simple/Pro + Template) dès le premier lancement.

### Infrastructure & Roadmap Backend
- **Prêt pour le Cloud** : Alignement total des modèles de données avec le futur backend Node.js.
- **Schéma MariaDB Finalisé** : Mise à jour de `BACKEND_ROADMAP.md` avec toutes les colonnes de profils et de métadonnées pour assurer une synchronisation sans perte.

## Round 13 — Plan de Production Backend & Finalisation Mobile

### Alignement Final des Modèles
- **Consistance Totale** : Ajout des champs `createdByUserId` et `updatedAtMillis` à l'ensemble des modèles de données ([`CostCenter`](file:///mnt/My_files/My_project_source/androidStudioProjects/OptiGestion/app/src/main/java/live/anonymespy/optigestion/Models.kt), [`BudgetCategoryUi`](file:///mnt/My_files/My_project_source/androidStudioProjects/OptiGestion/app/src/main/java/live/anonymespy/optigestion/Models.kt)).
- **Prêt pour l'Audit** : Chaque changement budgétaire ou structurel est désormais traçable par utilisateur et horodaté.

### Plan Directeur pour l'IA Backend
- **Roadmap Backend v3** : Création d'un document [`BACKEND_ROADMAP.md`](file:///mnt/My_files/My_project_source/androidStudioProjects/OptiGestion/app/src/main/java/live/anonymespy/optigestion/BACKEND_ROADMAP.md) ultra-détaillé, conçu comme une "fiche de mission" pour une IA ou un développeur Node.js.
- **Protocole de Sync Avancé** : Définition de la logique de synchronisation par lots (Batch) avec résolution de conflits basée sur l'horodatage serveur.
- **Sécurité et RBAC** : Spécification des middlewares Node.js pour garantir que les employés ne voient jamais les données sensibles (budgets globaux, TVA, etc.).

### Optimisations de la Page Profil
- **Robustesse de l'UI** : Correction d'un bug de récursion dans le formulaire de profil.
- **Persistance Locale** : Mise à jour de l'[`AppRepository`](file:///mnt/My_files/My_project_source/androidStudioProjects/OptiGestion/app/src/main/java/live/anonymespy/optigestion/AppRepository.kt) pour mémoriser l'intégralité des nouveaux champs techniques (versioning, sync status) même en mode déconnecté.

## Round 12 — Fluidité Offline & Indicateurs Fiscaux

### Expérience Utilisateur et Navigation
- **Allègement de la Navbar** : Retrait de l'onglet Profil de la barre de navigation pour maximiser l'espace pour les outils financiers. Le profil reste accessible via l'avatar en haut à gauche et les Paramètres.
- **Indicateur de Synchronisation** : Ajout d'un badge visuel (icône Sync) dans la barre de titre qui s'active lorsqu'il y a des données en attente de synchronisation cloud.

### Nouveaux Outils du Mode Pro
- **Estimation TVA en temps réel** : Ajout d'une carte "TVA à payer" sur le Dashboard (réservé aux rôles Admin/Comptable). Elle calcule instantanément la différence entre TVA collectée et déductible.
- **Support Offline Robuste** : Toutes les nouvelles écritures, budgets et centres de coût sont désormais marqués "En attente" (`PENDING`) localement, garantissant qu'aucune donnée n'est perdue sans connexion.

### Infrastructure & Roadmap
- **Protocole de Sync v2** : Mise à jour de `BACKEND_ROADMAP.md` détaillant la logique de synchronisation par lot et la gestion des conflits (Server-Wins).
- **Multipart/Files** : Définition de la stratégie d'upload pour les logos d'organisation et avatars utilisateurs vers le stockage cloud.

## Round 11 — Profil Utilisateur & Navigation Visuelle

### Identité Visuelle et Profil
- **Avatar dans la Barre de Titre** : L'icône générique de l'application dans la barre de titre est remplacée par la photo de profil (avatar) de l'utilisateur.
- **Accès Rapide au Profil** : Cliquer sur l'avatar dans la barre de titre ouvre directement la nouvelle page de profil.
- **Page de Profil Dédiée** : Un nouvel écran `ProfileScreen` permet de visualiser et de modifier les informations personnelles :
    - Nom d'affichage, Email, Téléphone.
    - URL de l'Avatar.
    - Détails de la société et rôle (pour les comptes Entreprise).
- **Intégration dans les Paramètres** : La section profil dans les Paramètres est désormais interactive et permet de naviguer vers la page de modification du profil.

### Infrastructure Backend & Roadmap
- **Gestion des Avatars** : Mise à jour de `BACKEND_ROADMAP.md` pour inclure les endpoints d'upload d'avatar (multipart) et de mise à jour du profil.
- **Modèle Utilisateur Complet** : Finalisation du lien entre les modèles Kotlin et les futurs objets JSON de l'API pour une transition transparente vers le cloud.

## Round 10 — Personnalisation & Préparation Offline/Cloud

### Onboarding et Guest Mode enrichis
- **Liberté pour les Invités** : Les utilisateurs en mode Guest peuvent désormais choisir entre le mode **Simple** (quotidien) et **Pro** (expert), comme les utilisateurs enregistrés.
- **Flux Unifié** : Standardisation du parcours d'onboarding pour tous les types de comptes.

### Personnalisation de la Marque
- **Logo Dynamique** : Ajout d'une option dans les Paramètres pour changer l'icône de l'application affichée dans la barre de titre.
- **Sélection Professionnelle** : Plusieurs icônes prédéfinies (Analytics, Épargne, Business, Banque) sont disponibles pour adapter l'app à l'identité visuelle de l'utilisateur ou de l'entreprise.

### Architecture Offline-First & Sync
L'application est maintenant structurellement prête pour une synchronisation cloud robuste tout en restant 100% fonctionnelle hors-ligne.
- **Versioning des Données** : Chaque enregistrement (Écriture, Budget, Centre) dispose désormais d'un champ `version` et d'un `syncStatus` (Synchronisé, En attente, Erreur).
- **Logique de Conflit** : Préparation des modèles pour gérer les fusions de données lors de la reconnexion au futur backend Node.js.
- **Roadmap Backend v2** : Mise à jour de `BACKEND_ROADMAP.md` détaillant le protocole de synchronisation par version et les uploads multipart pour les logos.

## Round 9 — Expérience API-First & Système de Comptes Avancé

### Refonte de l'Authentification et Onboarding
L'application prépare son passage au cloud avec un flux d'accueil totalement repensé, centré sur l'utilisateur et son organisation.

- **Mode Invité (Guest)** : Possibilité d'utiliser l'app sans compte. Les données restent locales sur l'appareil.
- **Création de Compte** : Nouveau formulaire d'inscription (Email, MDP, Nom) prêt pour une synchronisation cloud.
- **Parcours "Owner" Entreprise** : Les créateurs d'organisation peuvent désormais renseigner les détails de leur société (Secteur, SIRET) dès l'onboarding.
- **Multi-étapes Fluide** : Un nouveau système de navigation par étapes dans l'accueil pour une configuration sans friction.

### Modèle de Données Étendu (JSON-Ready)
Les modèles internes ont été enrichis pour correspondre à 100% aux futurs contrats d'API Node.js.
- **Profils Enrichis** : Ajout du téléphone, de l'URL d'avatar et de la date de création.
- **Détails Société** : Support pour l'immatriculation légale, l'adresse et le secteur d'activité.
- **Rôle Propriétaire** : Introduction du rôle `OWNER` pour distinguer le créateur de l'entreprise des simples administrateurs.

### Vision Backend 2.0
- **Roadmap API-First** : Mise à jour de `BACKEND_ROADMAP.md` pour refléter une architecture basée sur des échanges JSON purs.
- **Transition Invité → Cloud** : Planification de la logique de migration des données locales vers un compte distant.

## Round 8 — Système de Comptes & Préparation Backend

### Introduction des Comptes et Rôles
L'application franchit une étape majeure vers le multi-utilisateur avec l'intégration d'un système de profils et de rôles (RBAC).

- **Types de Comptes** : Distinction nette entre **Compte Personnel** (Particulier) et **Compte Entreprise**.
- **Rôles Entreprise** : Implémentation de 4 rôles avec permissions distinctes :
    - **Admin** : Contrôle total sur l'organisation et les données.
    - **RH** : Accès focalisé sur les frais de personnel et les centres de coût liés à la main-d'œuvre.
    - **Comptable** : Visibilité financière complète, gestion de la TVA et clôtures.
    - **Employé** : Mode "Saisie seule" — ne voit que ses propres notes de frais, accès restreint aux indicateurs globaux.

### Sécurité et Confidentialité
- **Filtrage des Données** : Les écritures (Sheets) sont désormais filtrées par rôle. Un employé ne peut plus consulter les transactions globales de l'entreprise.
- **Traçabilité** : Chaque écriture mémorise désormais son créateur (`createdByUserId`).

### Cohérence Terminologique et UX
- **Navigation Dynamique** : Les onglets (Budget, Centres, Rapports) s'affichent ou se masquent automatiquement selon le rôle de l'utilisateur.
- **Section Profil** : Ajout d'une vue "Profil Utilisateur" dans les Paramètres pour visualiser son compte, son email et son rôle.
- **Harmonisation FR/EN** : Finalisation de la traduction et de la cohérence des termes entre les modes Simple et Pro.

### Stratégie Backend (Ready for Node.js)
- **Feuille de Route Mise à Jour** : Le document `BACKEND_ROADMAP.md` a été enrichi avec le schéma SQL multi-tenant et la logique de gestion des organisations.
- **Modèles Prêts** : Les modèles Kotlin (`User`, `SheetEntry`) sont déjà alignés sur la future API REST.

## Round 7 — Expérience Dual-Mode : Simple vs Pro

### Double expérience utilisateur
L'application propose désormais deux modes distincts pour s'adapter au profil de l'utilisateur, sans mélanger les usages.

- **Mode Simplifié (Simple)** : Destiné aux particuliers ou aux petites activités. Utilise un langage courant ("Objectifs" au lieu de "Budget", "Dépenses moyennes" au lieu de "Burn Rate") et masque les champs complexes.
- **Mode Professionnel (Pro)** : Une véritable comptabilité analytique d'entreprise avec gestion de la TVA, comptes comptables et analyse de résultat.

### Nouveautés du Mode Pro (Comptabilité Générale)
- **Support TVA (HT/TTC)** : Les écritures supportent désormais un taux de TVA et une bascule HT/TTC. L'app calcule automatiquement les composantes taxes.
- **Comptes Comptables** : Ajout d'un champ "Compte" (ex: 606 - Fournitures) sur les écritures et les catégories budgétaires pour l'imputation comptable.
- **Analyse P&L (Profit & Loss)** : Nouveau bloc dans les Rapports présentant le Résultat d'Exploitation (HT) et le détail de la TVA (Collectée vs Déductible).
- **Indicateurs HT** : En mode Pro, les KPI de marge et de coûts basculent sur des valeurs Hors Taxes pour une analyse de rentabilité fidèle.
- **Export CSV étendu** : Le fichier exporté inclut toutes les nouvelles colonnes comptables (Compte, Taux TVA, Montant HT).

### Nouveautés du Mode Simple (Usage Quotidien)
- **Simplification terminologique** : Remplacement du jargon expert par des termes accessibles ("Mes Projets", "Ma Survie Estimée", etc.).
- **Interface minimaliste** : Masquage des statuts d'approbation et des champs fiscaux pour une saisie ultra-rapide.
- **Focus sur le Cash** : Tous les indicateurs restent en TTC (ce qui est réellement payé/reçu).

### Évolution de l'Onboarding
- **Sélecteur de Mode** : Un nouveau parcours d'accueil permet de choisir son niveau d'expertise avant de configurer ses données.
- **Données de Modèle (Templates)** : Chaque mode dispose de son propre jeu de données d'exemple (dépenses ménagères pour Simple, infrastructure IT pour Pro).
- **Réinitialisation Intelligente** : Le bouton "Réinitialiser" renvoie désormais au choix du mode, permettant de basculer d'une expérience à l'autre proprement.

## Round 6 — Infrastructure : support JDK 25 et Gradle 9.7.1

### Mise à jour de l'environnement de build
Le projet a été mis à jour pour être compatible avec **JDK 25** et les dernières versions des outils de build Android.

- **Gradle 9.7.1** : Montée de version depuis Gradle 8.13 pour assurer la compatibilité avec les nouveaux runtimes Java et les plugins Android récents.
- **Android Gradle Plugin (AGP) 9.4.0** : Migration vers AGP 9.4.0. Cette version majeure change la gestion de Kotlin : le support Kotlin est désormais nativement intégré au plugin Android, rendant le plugin séparé obsolète.
- **Kotlin 2.4.10** : Mise à jour nécessaire pour corriger l'erreur `java.lang.IllegalArgumentException: 25.0.2` lors de l'analyse de la version du JDK par le compilateur Kotlin.

### Nettoyage et Optimisation des fichiers `build.gradle.kts`
- **Suppression du plugin Kotlin Android** : Les lignes `alias(libs.plugins.kotlin.android)` ont été supprimées des fichiers `build.gradle.kts` (racine et app). AGP 9.0+ gère désormais Kotlin automatiquement dès que le plugin application ou library est présent.
- **Retrait de `kotlinOptions`** : Le bloc `kotlinOptions { jvmTarget = "11" }` a été supprimé car il est désormais déprécié/redondant avec les `compileOptions` standards dans les versions récentes de Gradle et AGP.
- **Mise à jour de `libs.versions.toml`** : Centralisation des nouvelles versions (`agp = "9.4.0"`, `kotlin = "2.4.10"`) et mise à jour du wrapper Gradle.

Ce changement assure la pérennité du projet sur des machines de développement utilisant les versions les plus récentes de Java.

## Round 5 — fix : crash au démarrage après le passage à AppCompatActivity

### Le crash
`MainActivity : AppCompatActivity()` (Round 4) plante au lancement.
Cause quasi certaine : `AppCompatActivity.onCreate()` vérifie que le thème
déclaré dans le manifeste descend de `Theme.AppCompat` (ou
`Theme.MaterialComponents`), et lève
`IllegalStateException: You need to use a Theme.AppCompat theme (or
descendant) with this activity` sinon. Un projet 100% Compose généré par
le template standard d'Android Studio utilise un thème Material3/plateforme
classique dans le manifeste, pas un thème AppCompat — d'où le crash
immédiat à l'ouverture.

### Le fix : revenir à `ComponentActivity`, sans perdre le changement de langue
Plutôt que de vous demander de modifier votre thème (fichier que je n'ai
pas), `MainActivity` redevient un `ComponentActivity` classique, mais
**surcharge `attachBaseContext()`** pour appliquer elle-même la locale
mémorisée — exactement ce qu'`AppCompatActivity` aurait fait pour nous,
sans aucune exigence de thème :

```kotlin
override fun attachBaseContext(newBase: Context) {
    val locale = AppCompatDelegate.getApplicationLocales().get(0)
    val context = if (locale != null) {
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        newBase.createConfigurationContext(config)
    } else newBase
    super.attachBaseContext(context)
}
```

`AppCompatDelegate.getApplicationLocales()` lit ce que
`AppRepository.selectLanguage()` vient de stocker via
`AppCompatDelegate.setApplicationLocales(...)`. Le `activity?.recreate()`
déjà en place dans `SettingsScreen.kt` (Round 4) redéclenche
`attachBaseContext()` avec la nouvelle valeur déjà stockée — c'est ce qui
rend le changement visible immédiatement, sans exiger `AppCompatActivity`
ni toucher au thème.

La dépendance Gradle `androidx.appcompat:appcompat:1.7.0` reste
nécessaire (on utilise toujours la classe `AppCompatDelegate`, seulement
plus comme classe mère de l'Activity).



## Round 4 — fix : le changement de langue ne s'appliquait pas

### Le bug
`AppCompatDelegate.setApplicationLocales(...)` (appelé depuis
`AppRepository.selectLanguage()`) stocke bien le choix de langue, mais ne
rafraîchit l'écran affiché que si l'Activity qui l'entoure sait réagir à
ce changement. `MainActivity` héritait de `ComponentActivity` (le type de
base standard pour une app 100% Compose) — qui n'a **aucun** hook pour ça.
Résultat : le réglage changeait bien en interne (le radio bouton dans
Settings se déplaçait correctement), mais l'UI restait figée dans
l'ancienne langue tant que l'app n'était pas complètement redémarrée.

### Le fix (deux parties, l'une renforçant l'autre)
1. **`MainActivity` hérite maintenant de `AppCompatActivity`** au lieu de
   `ComponentActivity`. C'est le hook manquant : le mécanisme de rétro-
   compatibilité "per-app language" d'AppCompat (nécessaire sous API 33)
   s'accroche au cycle de vie d'une `AppCompatActivity` pour ré-appliquer
   la langue et recréer l'activité automatiquement. `AppCompatActivity`
   est elle-même une `ComponentActivity` en interne, donc `setContent {}`
   et tout le reste de Compose continue de fonctionner à l'identique —
   rien d'autre ne change côté UI.
2. **Appel explicite à `activity?.recreate()`** juste après
   `AppRepository.selectLanguage(option)` dans `SettingsScreen.kt`. Ce
   n'est pas censé être nécessaire une fois le point 1 en place (le
   rafraîchissement est censé être automatique), mais je le garde comme
   filet de sécurité : le comportement exact du chemin automatique varie
   selon la version d'Android (framework `LocaleManager` sur API 33+ vs
   rétro-compatibilité AppCompat en dessous), et forcer `recreate()`
   supprime toute incertitude au prix d'un redémarrage d'écran quasi
   instantané. Effet de bord mineur à connaître : comme l'écran Réglages
   est un simple état booléen (`showSettings`) à l'intérieur de
   `CaeAnalyticsApp` plutôt qu'une vraie destination de navigation,
   `recreate()` referme Réglages et revient sur Dashboard — c'est
   attendu, pas un bug.

### Rappel : dépendance Gradle requise
Toujours nécessaire pour que ça compile (voir Round 3) :
```
implementation("androidx.appcompat:appcompat:1.7.0")
```



## Round 3 — migration vers strings.xml + fix DeltaBadge

### 1. Migration Strings.kt → res/values/strings.xml + AppCompatDelegate
Remplace complètement l'approche Round 2 (CompositionLocal maison) par
l'API standard AndroidX :
- `res/values/strings.xml` (français, ressource par défaut) et
  `res/values-en/strings.xml` (anglais) — un fichier `Strings.kt` fait
  place à `Localization.kt`, qui ne contient plus qu'un enum `AppLanguage`
  et une fonction `applyAppLanguage()`.
- `AppRepository.selectLanguage()` appelle maintenant
  `AppCompatDelegate.setApplicationLocales(...)` — c'est l'API standard
  "per-app language" d'AndroidX : elle persiste elle-même, s'intègre à
  l'écran système Réglages > Applis > Langues (Android 13+, via
  `res/xml/locales_config.xml`), et fait que **tout** appel
  `stringResource(R.string.xxx)`, Compose ou non, suit automatiquement la
  langue choisie sans aucune plomberie supplémentaire.
- Tous les écrans localisés (Dashboard, Navigation, Settings) utilisent
  maintenant `stringResource(R.string.xxx)` au lieu de
  `LocalStrings.current.xxx`.
- **Dépendance Gradle à ajouter** (pas encore dans le projet a priori) :
  ```
  implementation("androidx.appcompat:appcompat:1.7.0")
  ```
  Fonctionne même si `MainActivity` reste un `ComponentActivity` simple —
  `setApplicationLocales`/`getApplicationLocales` sont des appels
  statiques au niveau du processus, pas besoin d'`AppCompatActivity`.
- **Ligne de manifeste à ajouter** sur la balise `<application>` pour que
  l'écran système "Langues de l'appli" (Android 13+) liste bien
  français/anglais plutôt que toutes les langues de l'appareil :
  ```xml
  <application
      android:localeConfig="@xml/locales_config"
      ... >
  ```
  (`res/xml/locales_config.xml` est fourni dans ce lot.)

Pourquoi ce changement par rapport à Round 2 : voir la discussion —
XML + AppCompatDelegate gagne sur presque tous les points qui comptent
à partir du moment où vous avez un vrai projet Android (intégration
système, outillage de traduction, pluriels), et le coût de la migration
était minimal tant que seuls 3 écrans étaient concernés.

### 2. Fix : `DeltaBadge` non résolu dans StatsScreen.kt
Le redesign du Dashboard (Round 2) a supprimé le composable `DeltaBadge`
qui vivait dans `DashboardScreen.kt` sans remarquer que
`StatsScreen.kt` (section Tendance de Rentabilité) l'utilisait aussi —
erreur de compilation "Unresolved reference: DeltaBadge".

Corrigé en extrayant `DeltaBadge` dans son propre fichier,
**`SharedComponents.kt`**, possédé par aucun des deux écrans. Les deux
l'utilisent depuis là. Ça évite que ce type de régression se reproduise
la prochaine fois qu'un des deux écrans est redessiné.



## Round 2 — Langue (EN/FR) + Dashboard redesign + préparation backend

### 1. Système de langue (anglais / français)
- Nouveau `Strings.kt` : un type `Strings` avec un champ par texte affiché,
  deux instances (`FrenchStrings`, `EnglishStrings`), et un
  `CompositionLocal` (`LocalStrings`) fourni au sommet de l'app dans
  `MainActivity.kt` — exactement le même schéma que `CaeColors.applyMode`
  pour le thème, donc la bascule de langue est instantanée, sans redémarrage.
- `AppRepository.language` (persisté) + `selectLanguage()`.
- Nouvelle section "Langue" dans Settings, au-dessus des autres (c'est le
  réglage qu'on change une fois et qu'on ne retouche plus, mais il doit
  être visible en premier pour qui ne lit pas le français).
- **Localisé maintenant** : Dashboard, Navigation (bottom nav, side rail,
  barre du haut, dialogue de reset), Settings (titres de section).
- **Pas encore localisé** : Sheets, Budget vs Actual, Cost Centers,
  Reports — ils utilisent toujours des chaînes françaises en dur. C'est
  volontaire : vous avez demandé "commençons simple", donc le pattern est
  posé et prouvé sur un écran complexe (Dashboard) plutôt que fait à
  moitié partout. Étendre `Strings.kt` à ces écrans suit exactement le
  même schéma line par ligne.
- `NavDestination` ne stocke plus de `label` en dur — `navLabel(strings)`
  et `topBarTitle(strings)` (dans `Navigation.kt`) le dérivent de la
  langue courante.

### 2. Dashboard redessiné (moins "plat", plus de hiérarchie)
L'ancienne version avait 4 cartes KPI de poids visuel identique dans une
grille 2x2 — rien n'indiquait au regard où regarder en premier. Nouveau :
- **Carte héro** (fond `CaeColors.Primary`, pleine largeur) : Marge Nette
  en gros, badge de tendance, et une **mini-barre de répartition**
  recettes vs coûts intégrée — d'un coup d'œil, la taille relative des
  deux masses.
- **Ligne de 3 tuiles secondaires**, plus petites, sous la carte héro :
  Recettes, Coûts, Runway.
- Raccourcis rapides et alertes budgétaires conservés, replacés autour de
  cette nouvelle hiérarchie plutôt que mélangés aux KPI.
- Le graphique "Coûts par Centre" et "Activité Récente" sont inchangés
  dans leur logique, juste re-brachés sur `LocalStrings`.

### 3. Préparation du futur backend (Node.js + MariaDB)
Rien n'est branché — ce sont des fondations pour ne pas avoir à tout
refaire une fois le serveur là :
- **`UserModels.kt`** (nouveau, inutilisé pour l'instant) : `AccountType`
  (PARTICULIER / ENTREPRISE), `EnterpriseRole` (ADMIN / RH / EMPLOYE),
  `User`, `Company` — la forme que l'API renverra plus tard.
- **`BACKEND_ROADMAP.md`** (nouveau) : schéma MariaDB suggéré (users,
  companies, company_memberships, entries, cost_centers,
  budget_categories, settings — modelé quasi 1:1 sur vos types Kotlin
  actuels), forme des endpoints REST (Express + JWT), stratégie
  d'authentification côté Android (Retrofit + EncryptedSharedPreferences),
  pourquoi ça marche aussi tel quel pour le futur client Python, et un
  ordre de construction suggéré en 5 étapes.

### 4. Fichier orphelin détecté : `Caeanalyticsdashboard.kt`
Ce fichier (package `live.anonymespy.optigestion.dashboard`) n'est
appelé nulle part dans `Navigation.kt` — c'est un scaffold/prototype
laissé de côté, avec ses propres `CaeColors`/`NavDestination`/
`ActivityIcon` locaux qui font doublon avec les vrais, et un `TODO()`
dans `ActivityRow` qui plante si `ActivityIcon.GENERIC` est atteint.
Recommandation : le supprimer. Deux idées qu'il contenait valent
cependant d'être reprises plus tard dans le vrai Dashboard :
- un lien "Voir Toutes les Transactions" en bas de la section Activité
  Récente (renvoyant vers Sheets) ;
- une section Import/Export avec des boutons dédiés Excel/PDF — au-delà
  du CSV déjà fonctionnel, l'export Excel/PDF réel demande une librairie
  (Apache POI / iText côté Android, ou mieux : le futur backend Node
  peut générer ces fichiers server-side et les streamer, ce qui évite
  d'alourdir l'APK).



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
