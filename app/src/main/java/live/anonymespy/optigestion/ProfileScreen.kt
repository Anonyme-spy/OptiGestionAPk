package live.anonymespy.optigestion

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val user = AppRepository.currentUser ?: return
    val isPro = AppRepository.appMode == AppMode.PRO

    var displayName by remember { mutableStateOf(user.displayName) }
    var email by remember { mutableStateOf(user.email ?: "") }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var avatarUrl by remember { mutableStateOf(user.avatarUrl ?: "") }
    var jobTitle by remember { mutableStateOf(user.jobTitle ?: "") }
    var bio by remember { mutableStateOf(user.bio ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with big avatar
        // En-tête avec un grand avatar
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(CaeColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUrl.isNotBlank()) {
                    // In a real app, use Coil/Glide to load URL. For now, show initial.
                    // Dans une application réelle, utilisez Coil/Glide pour charger l'URL. Pour l'instant, affichez l'initiale.
                    Text(text = displayName.firstOrNull()?.toString() ?: "?", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = CaeColors.OnPrimaryContainer)
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = CaeColors.OnPrimaryContainer)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(text = displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary)
            val typeLabel = if (user.accountType == AccountType.ENTREPRISE) "Compte Entreprise" else if (user.accountType == AccountType.GUEST) "Mode Invité" else "Compte Personnel"
            Text(text = typeLabel, fontSize = 14.sp, color = CaeColors.OnSurfaceVariant)
        }

        ProfileSectionHeader(title = stringResource(R.string.profile_section_personal))
        ProfileInfoCard {
            ProfileEditField(label = stringResource(R.string.profile_label_display_name), value = displayName, onValueChange = { displayName = it }, icon = Icons.Filled.Person)
            HorizontalDivider(color = CaeColors.SurfaceVariant)
            ProfileEditField(label = stringResource(R.string.profile_label_email), value = email, onValueChange = { email = it }, icon = Icons.Filled.Email, enabled = user.accountType != AccountType.GUEST)
            HorizontalDivider(color = CaeColors.SurfaceVariant)
            ProfileEditField(label = stringResource(R.string.profile_label_phone), value = phone, onValueChange = { phone = it }, icon = Icons.Filled.Phone)
            HorizontalDivider(color = CaeColors.SurfaceVariant)
            ProfileEditField(label = "Poste / Titre", value = jobTitle, onValueChange = { jobTitle = it }, icon = Icons.Filled.Work)
            HorizontalDivider(color = CaeColors.SurfaceVariant)
            ProfileEditField(label = "Biographie", value = bio, onValueChange = { bio = it }, icon = Icons.Filled.Description)
            HorizontalDivider(color = CaeColors.SurfaceVariant)
            ProfileEditField(label = stringResource(R.string.profile_label_avatar_url), value = avatarUrl, onValueChange = { avatarUrl = it }, icon = Icons.Filled.Link)
        }

        if (user.accountType == AccountType.ENTREPRISE) {
            Spacer(Modifier.height(24.dp))
            ProfileSectionHeader(title = stringResource(R.string.profile_section_company))
            ProfileInfoCard {
                ProfileDisplayField(label = stringResource(R.string.profile_label_company_name), value = user.companyName ?: "—", icon = Icons.Filled.Business)
                HorizontalDivider(color = CaeColors.SurfaceVariant)
                ProfileDisplayField(label = "Secteur", value = user.companyIndustry ?: "—", icon = Icons.Filled.Work)
                HorizontalDivider(color = CaeColors.SurfaceVariant)
                ProfileDisplayField(label = "Rôle", value = user.enterpriseRole?.name ?: "—", icon = Icons.Filled.Person)
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                val updatedUser = user.copy(
                    displayName = displayName.trim(),
                    email = email.trim().takeIf { it.isNotEmpty() },
                    phone = phone.trim().takeIf { it.isNotEmpty() },
                    avatarUrl = avatarUrl.trim().takeIf { it.isNotEmpty() },
                    jobTitle = jobTitle.trim().takeIf { it.isNotEmpty() },
                    bio = bio.trim().takeIf { it.isNotEmpty() }
                )
                AppRepository.updateUserProfile(updatedUser)
                Toast.makeText(context, context.getString(R.string.profile_edit_success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary)
        ) {
            Text(text = stringResource(R.string.profile_save_changes), fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun ProfileSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color = CaeColors.OnSurfaceVariant,
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun ProfileInfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(4.dp), content = content)
    }
}

@Composable
private fun ProfileEditField(label: String, value: String, onValueChange: (String) -> Unit, icon: ImageVector, enabled: Boolean = true) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CaeColors.Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = CaeColors.OnSurfaceVariant)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, color = if (enabled) CaeColors.OnSurface else CaeColors.OnSurfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ProfileDisplayField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CaeColors.Primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = CaeColors.OnSurfaceVariant)
            Text(text = value, fontSize = 15.sp, color = CaeColors.OnSurface, fontWeight = FontWeight.Medium)
        }
    }
}
