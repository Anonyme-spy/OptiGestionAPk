package live.anonymespy.optigestion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import live.anonymespy.optigestion.ui.theme.CaeColors

/**
 * First-run (and post-reset) screen. Nothing is loaded until the person
 * makes a choice here — "empty" is always the safe default action.
 */
@Composable
fun OnboardingScreen(
    onLoadTemplate: () -> Unit,
    onStartEmpty: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CaeColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(CaeColors.PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.AutoAwesome, contentDescription = null, tint = CaeColors.OnPrimaryContainer)
        }

        Spacer(Modifier.height(24.dp))

        Text(text = stringResource(R.string.onboarding_welcome_title), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CaeColors.Primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(R.string.onboarding_welcome_subtitle), fontSize = 14.sp, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center)

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

        Spacer(Modifier.height(24.dp))

        Text(text = stringResource(R.string.onboarding_footer), fontSize = 12.sp, color = CaeColors.OnSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun OnboardingOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    recommended: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaeColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CaeColors.Primary, contentColor = CaeColors.OnPrimary)
            ) {
                Text(text = title, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
