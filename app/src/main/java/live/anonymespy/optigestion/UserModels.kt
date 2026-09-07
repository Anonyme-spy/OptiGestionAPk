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

enum class AccountType { PARTICULIER, ENTREPRISE }

/**
 * Only meaningful when [User.accountType] is [AccountType.ENTREPRISE].
 * - ADMIN: full access — manage cost centers, budgets, users, settings.
 * - RH: HR-scoped — likely payroll/labor cost centers, not full financials.
 * - EMPLOYE: submits entries (e.g. expense claims) for approval, limited
 *   read access to company-wide dashboards.
 *
 * Exact permission boundaries are a backend/API decision (see
 * BACKEND_ROADMAP.md § Roles & permissions) — this enum only names the
 * three roles so the client can branch its UI once accounts exist.
 */
enum class EnterpriseRole { ADMIN, RH, EMPLOYE }

/**
 * A signed-in user, as the future API would return it. [companyId] is
 * null for PARTICULIER accounts and set for ENTREPRISE accounts;
 * [enterpriseRole] is null for PARTICULIER accounts.
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val accountType: AccountType,
    val companyId: String? = null,
    val companyName: String? = null,
    val enterpriseRole: EnterpriseRole? = null
)

/** A company/organization — the shared workspace for ENTREPRISE users. */
data class Company(
    val id: String,
    val name: String
)
