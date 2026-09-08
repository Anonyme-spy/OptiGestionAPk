package live.anonymespy.optigestion

/**
 * DTOs for API requests and responses.
 * DTO pour les requêtes et réponses API.
 */

data class AuthResponse(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val companyId: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val accountType: AccountType,
    val jobTitle: String? = null,
    val bio: String? = null,
    val company: CompanyRegisterInfo? = null
)

data class CompanyRegisterInfo(
    val name: String,
    val registrationNumber: String? = null,
    val industry: String? = null,
    val address: String? = null,
    val website: String? = null,
    val taxId: String? = null
)
