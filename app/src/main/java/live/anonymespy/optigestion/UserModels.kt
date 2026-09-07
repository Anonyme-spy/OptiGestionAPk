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
 */

/**
 * Account kinds:
 * - GUEST: Anonymous local session. No email required. Data stays on device.
 * - PARTICULIER: Personal account synced to cloud.
 * - ENTREPRISE: Organization account with multi-user roles.
 */
enum class AccountType { GUEST, PARTICULIER, ENTREPRISE }

/**
 * Roles for Enterprise accounts.
 * - OWNER: The person who created the company. Full legal/admin rights.
 * - ADMIN: Manages organization, users, and financials.
 * - RH: Personnel costs, approvals, and labor budgets.
 * - COMPTABLE: Full financial visibility, VAT, and period closing.
 * - EMPLOYE: Submission only (expense claims).
 */
enum class EnterpriseRole { OWNER, ADMIN, RH, COMPTABLE, EMPLOYE }

/**
 * A user profile.
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

/** A company/organization. */
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
