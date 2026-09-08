package live.anonymespy.optigestion

/**
 * ============================================================
 *  ACCOUNTS / ROLES — SCAFFOLDING ONLY, NOT WIRED UP YET
 * ============================================================
 *
 * These types describe the shape of the account system the Node +
 * MariaDB backend will introduce (see BACKEND_ROADMAP.md). Nothing in
 * the app currently creates, stores or reads a [User] — AppRepository
 * is still single-user, local-only (SharedPreferences). This file exists
 * so that when login/accounts land, the client-side shapes already match
 * what the API will return, instead of everything being modeled twice.
 *
 * Two account kinds:
 * - PARTICULIER: an individual managing their own personal/freelance
 *   finances. One user = one set of data, same as the app works today.
 * - ENTREPRISE: a company account shared by several users, each with a
 *   role that controls what they can see/edit (see [EnterpriseRole]).
 *
 * ============================================================
 *  COMPTES / RÔLES — ÉCHAFAUDAGE UNIQUEMENT, PAS ENCORE CONNECTÉ
 * ============================================================
 *
 * Ces types décrivent la forme du système de compte que le backend Node +
 * MariaDB introduira (voir BACKEND_ROADMAP.md). Rien dans
 * l'application ne crée, ne stocke ou ne lit actuellement un [User] — AppRepository
 * est toujours mono-utilisateur, local uniquement (SharedPreferences). Ce fichier existe
 * pour que lorsque les comptes/connexions arriveront, les formes côté client correspondent déjà
 * à ce que l'API retournera, au lieu que tout soit modélisé deux fois.
 *
 * Deux types de comptes :
 * - PARTICULIER : un individu gérant ses propres finances personnelles/freelance.
 *   Un utilisateur = un ensemble de données, de la même manière que l'application fonctionne aujourd'hui.
 * - ENTREPRISE : un compte d'entreprise partagé par plusieurs utilisateurs, chacun avec un
 *   rôle qui contrôle ce qu'ils peuvent voir/éditer (voir [EnterpriseRole]).
 */

/**
 * Account kinds:
 * - GUEST: Anonymous local session. No email required. Data stays on device.
 * - PARTICULIER: Personal account synced to cloud.
 * - ENTREPRISE: Organization account with multi-user roles.
 *
 * Types de comptes :
 * - GUEST : Session locale anonyme. Aucun e-mail requis. Les données restent sur l'appareil.
 * - PARTICULIER : Compte personnel synchronisé sur le cloud.
 * - ENTREPRISE : Compte d'organisation avec des rôles multi-utilisateurs.
 */
enum class AccountType { GUEST, PARTICULIER, ENTREPRISE }

/**
 * Roles for Enterprise accounts.
 * - OWNER: The person who created the company. Full legal/admin rights.
 * - ADMIN: Manages organization, users, and financials.
 * - RH: Personnel costs, approvals, and labor budgets.
 * - COMPTABLE: Full financial visibility, VAT, and period closing.
 * - EMPLOYE: Submission only (expense claims).
 *
 * Rôles pour les comptes d'entreprise.
 * - OWNER : La personne qui a créé l'entreprise. Droits légaux/admin complets.
 * - ADMIN : Gère l'organisation, les utilisateurs et les finances.
 * - RH : Coûts du personnel, approbations et budgets de main-d'œuvre.
 * - COMPTABLE : Visibilité financière complète, TVA et clôture de période.
 * - EMPLOYE : Soumission uniquement (demandes de remboursement de frais).
 */
enum class EnterpriseRole { OWNER, ADMIN, RH, COMPTABLE, EMPLOYE }

/**
 * A user profile.
 * Un profil utilisateur.
 */
data class User(
    val id: String,
    val email: String? = null,
    val displayName: String,
    val accountType: AccountType,
    val companyId: String? = null,
    val companyName: String? = null,
    val companyIndustry: String? = null,
    val enterpriseRole: EnterpriseRole? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val jobTitle: String? = null,
    val bio: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

/** A company/organization.
 * Une entreprise / organisation. */
data class Company(
    val id: String,
    val name: String,
    val registrationNumber: String? = null, // e.g. SIRET/SIREN
    val industry: String? = null,
    val address: String? = null,
    val website: String? = null,
    val taxId: String? = null,
    val ownerId: String
)
