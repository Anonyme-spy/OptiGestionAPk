package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors
import java.util.UUID

/**
 * First-run (and post-reset) screen. Handles the full multi-step onboarding
 * for account type, roles (enterprise), expertise level (particulier),
 * and data setup (template vs empty).
 *
 * Écran de première exécution (et post-réinitialisation). Gère le processus de bienvenue
 * complet en plusieurs étapes pour le type de compte, les rôles (entreprise), le niveau d'expertise
 * (particulier) et la configuration des données (modèle vs vide).
 */
@Composable
fun OnboardingScreen() {
    val scope = rememberCoroutineScope()
    var authMethod by remember { mutableStateOf<AuthMethod?>(null) }
    var accountType by remember { mutableStateOf<AccountType?>(null) }
    var enterpriseRole by remember { mutableStateOf<EnterpriseRole?>(null) }
    var appMode by remember { mutableStateOf<AppMode?>(null) }

    // Form data
    // Données du formulaire
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CaeColors.Background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = CaeColors.Error,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        if (isLoading) {
            CircularProgressIndicator(color = CaeColors.Primary)
        } else {
            when {
                authMethod == null -> {
                    AuthMethodStep(onSelected = { authMethod = it; errorMessage = null })
                }
                authMethod == AuthMethod.SIGNUP && accountType == null -> {
                    SignUpFormStep(
                        email = email, onEmailChange = { email = it },
                        password = password, onPasswordChange = { password = it },
                        name = displayName, onNameChange = { displayName = it },
                        onBack = { authMethod = null; errorMessage = null },
                        onNext = { accountType = it; errorMessage = null }
                    )
                }
                authMethod == AuthMethod.LOGIN -> {
                    LoginFormStep(
                        email = email, onEmailChange = { email = it },
                        password = password, onPasswordChange = { password = it },
                        onBack = { authMethod = null; errorMessage = null },
                        onLogin = {
                            scope.launch {
                                isLoading = true
                                AppRepository.login(email, password)
                                    .onSuccess { isLoading = false }
                                    .onFailure {
                                        isLoading = false
                                        errorMessage = it.message
                                    }
                            }
                        }
                    )
                }
                accountType == AccountType.ENTREPRISE && companyName.isEmpty() -> {
                    CompanyInfoStep(
                        name = companyName, onNameChange = { companyName = it },
                        industry = industry, onIndustryChange = { industry = it },
                        onBack = { accountType = null; errorMessage = null },
                        onNext = { companyName = it; enterpriseRole = EnterpriseRole.OWNER }
                    )
                }
                accountType == AccountType.PARTICULIER && appMode == null -> {
                    ModeSelectionStep(
                        onBack = { accountType = null; errorMessage = null },
                        onSelected = { appMode = it }
                    )
                }
                else -> {
                    val finalMode = appMode ?: AppMode.PRO
                    val useTemplate = true // Default or could ask

                    SetupSelectionStep(
                        mode = finalMode,
                        onBack = {
                            if (authMethod == AuthMethod.GUEST) authMethod = null
                            else if (accountType == AccountType.ENTREPRISE) companyName = ""
                            else if (accountType == AccountType.PARTICULIER) appMode = null
                            errorMessage = null
                        },
                        onLoadTemplate = {
                            if (authMethod == AuthMethod.GUEST) {
                                AppRepository.startGuest(finalMode, true)
                            } else {
                                scope.launch {
                                    isLoading = true
                                    val req = RegisterRequest(
                                        email = email,
                                        password = password,
                                        displayName = displayName,
                                        accountType = accountType!!,
                                        company = if (accountType == AccountType.ENTREPRISE) CompanyRegisterInfo(name = companyName, industry = industry) else null
                                    )
                                    AppRepository.signup(req)
                                        .onSuccess { isLoading = false }
                                        .onFailure {
                                            isLoading = false
                                            errorMessage = it.message
                                        }
                                }
                            }
                        },
                        onStartEmpty = {
                            if (authMethod == AuthMethod.GUEST) {
                                AppRepository.startGuest(finalMode, false)
                            } else {
                                scope.launch {
                                    isLoading = true
                                    val req = RegisterRequest(
                                        email = email,
                                        password = password,
                                        displayName = displayName,
                                        accountType = accountType!!,
                                        company = if (accountType == AccountType.ENTREPRISE) CompanyRegisterInfo(name = companyName, industry = industry) else null
                                    )
                                    AppRepository.signup(req)
                                        .onSuccess { isLoading = false }
                                        .onFailure {
                                            isLoading = false
                                            errorMessage = it.message
                                        }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

private enum class AuthMethod { GUEST, LOGIN, SIGNUP }

@Composable
private fun AuthMethodStep(onSelected: (AuthMethod) -> Unit) {
    OnboardingHeader(
        title = stringResource(R.string.onboarding_welcome_title),
        subtitle = stringResource(R.string.onboarding_welcome_subtitle)
    )

    Spacer(Modifier.height(32.dp))

    OnboardingOptionCard(
        icon = Icons.Filled.PersonAdd,
        title = stringResource(R.string.onboarding_signup),
        description = stringResource(R.string.onboarding_mode_pro_desc),
        onClick = { onSelected(AuthMethod.SIGNUP) }
    )

    Spacer(Modifier.height(16.dp))

    OnboardingOptionCard(
        icon = Icons.AutoMirrored.Filled.Login,
        title = stringResource(R.string.onboarding_login),
        description = "Déjà inscrit ? Reprenez là où vous vous étiez arrêté.",
        onClick = { onSelected(AuthMethod.LOGIN) }
    )

    Spacer(Modifier.height(16.dp))

    OnboardingOptionCard(
        icon = Icons.Filled.NoAccounts,
        title = stringResource(R.string.onboarding_start_guest),
        description = stringResource(R.string.onboarding_start_guest_desc),
        onClick = { onSelected(AuthMethod.GUEST) }
    )
}

@Composable
private fun SignUpFormStep(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    name: String, onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: (AccountType) -> Unit
) {
    OnboardingHeader(title = "Créer un compte", subtitle = "Rejoignez OptiGestion pour synchroniser vos données.", onBack = onBack)

    Spacer(Modifier.height(24.dp))

    OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text(stringResource(R.string.onboarding_form_name)) }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text(stringResource(R.string.onboarding_form_email)) }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text(stringResource(R.string.onboarding_form_password)) }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())

    Spacer(Modifier.height(24.dp))

    Text(text = "Type de compte", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnSurfaceVariant)
    Spacer(Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { onNext(AccountType.PARTICULIER) },
            modifier = Modifier.weight(1f),
            enabled = name.isNotBlank() && email.contains("@"),
            colors = ButtonDefaults.buttonColors(containerColor = CaeColors.SurfaceContainerHigh, contentColor = CaeColors.Primary)
        ) { Text("Personnel") }
        Button(
            onClick = { onNext(AccountType.ENTREPRISE) },
            modifier = Modifier.weight(1f),
            enabled = name.isNotBlank() && email.contains("@"),
            colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary)
        ) { Text("Entreprise") }
    }
}

@Composable
private fun LoginFormStep(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onLogin: () -> Unit
) {
    OnboardingHeader(title = "Bon retour !", subtitle = "Saisissez vos identifiants.", onBack = onBack)

    Spacer(Modifier.height(24.dp))

    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text(stringResource(R.string.onboarding_form_email)) }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text(stringResource(R.string.onboarding_form_password)) }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())

    Spacer(Modifier.height(32.dp))

    Button(onClick = onLogin, modifier = Modifier.fillMaxWidth(), enabled = email.isNotBlank() && password.isNotBlank()) {
        Text("Se connecter")
    }
}

@Composable
private fun CompanyInfoStep(
    name: String, onNameChange: (String) -> Unit,
    industry: String, onIndustryChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: (String) -> Unit
) {
    OnboardingHeader(title = stringResource(R.string.onboarding_company_title), subtitle = "Détails de votre organisation.", onBack = onBack)

    Spacer(Modifier.height(24.dp))

    OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text(stringResource(R.string.onboarding_company_name_label)) }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(value = industry, onValueChange = onIndustryChange, label = { Text(stringResource(R.string.onboarding_company_industry_label)) }, modifier = Modifier.fillMaxWidth())

    Spacer(Modifier.height(32.dp))

    Button(onClick = { onNext(name) }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank()) {
        Text(stringResource(R.string.onboarding_finish))
    }
}

@Composable
private fun ModeSelectionStep(onBack: () -> Unit, onSelected: (AppMode) -> Unit) {
    OnboardingHeader(
        title = stringResource(R.string.onboarding_mode_title),
        subtitle = stringResource(R.string.onboarding_type_particulier_desc),
        onBack = onBack
    )

    Spacer(Modifier.height(24.dp))

    OnboardingOptionCard(
        icon = Icons.Filled.Bolt,
        title = stringResource(R.string.onboarding_mode_simple),
        description = stringResource(R.string.onboarding_mode_simple_desc),
        recommended = true,
        onClick = { onSelected(AppMode.SIMPLE) }
    )

    Spacer(Modifier.height(16.dp))

    OnboardingOptionCard(
        icon = Icons.Filled.SettingsSuggest,
        title = stringResource(R.string.onboarding_mode_pro),
        description = stringResource(R.string.onboarding_mode_pro_desc),
        recommended = false,
        onClick = { onSelected(AppMode.PRO) }
    )
}

@Composable
private fun OnboardingHeader(title: String, subtitle: String, onBack: (() -> Unit)? = null) {
    if (onBack != null) {
        TextButton(onClick = onBack, modifier = Modifier.padding(bottom = 8.dp)) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = null)
            Text(stringResource(R.string.onboarding_back))
        }
    } else {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(CaeColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = CaeColors.OnPrimaryContainer)
        }
        Spacer(Modifier.height(24.dp))
    }

    Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary, textAlign = TextAlign.Center)
    Spacer(Modifier.height(8.dp))
    Text(text = subtitle, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center)
}

@Composable
private fun SetupSelectionStep(
    mode: AppMode,
    onBack: () -> Unit,
    onLoadTemplate: () -> Unit,
    onStartEmpty: () -> Unit
) {
    val modeName = if (mode == AppMode.SIMPLE) stringResource(R.string.onboarding_mode_simple) else stringResource(R.string.onboarding_mode_pro)

    OnboardingHeader(
        title = stringResource(R.string.onboarding_setup_title, modeName),
        subtitle = stringResource(R.string.onboarding_footer),
        onBack = onBack
    )

    Spacer(Modifier.height(32.dp))

    OnboardingOptionCard(
        icon = Icons.Filled.NoteAdd,
        title = stringResource(R.string.onboarding_start_empty_title),
        description = stringResource(R.string.onboarding_start_empty_desc),
        recommended = true,
        onClick = onStartEmpty
    )

    Spacer(Modifier.height(16.dp))

    OnboardingOptionCard(
        icon = Icons.Filled.AutoAwesome,
        title = stringResource(R.string.onboarding_load_template_title),
        description = stringResource(R.string.onboarding_load_template_desc),
        recommended = false,
        onClick = onLoadTemplate
    )
}

@Composable
private fun OnboardingOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    recommended: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(CaeColors.SurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = CaeColors.Primary)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.Primary)
                    if (recommended) {
                        Text(text = stringResource(R.string.onboarding_recommended), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CaeColors.OnTertiaryContainer)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(text = description, fontSize = 13.sp, color = CaeColors.OnSurfaceVariant)
        }
    }
}
