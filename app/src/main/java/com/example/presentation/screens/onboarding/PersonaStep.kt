package com.example.presentation.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.domain.account.LumiPersonaTone
import com.example.presentation.utils.accentColor
import com.example.presentation.utils.icon

@Composable
fun PersonaStep(
    currentPersona: LumiPersonaTone,
    onSelect: (LumiPersonaTone) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = stringResource(R.string.text_choose_a_persona),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.text_how_would_you_like_lumi_to),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            LumiPersonaTone.entries.forEach { persona ->
                val isSelected = currentPersona == persona
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.spacing.medium))
                        .clickable { onSelect(persona) },
                    color = if (isSelected) persona.accentColor.copy(alpha = 0.15f) else SurfaceDarkVariant.copy(alpha = 0.6f),
                    border = if (isSelected) BorderStroke(1.5.dp, persona.accentColor) else BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(MaterialTheme.spacing.medium)
                ) {
                    Row(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) persona.accentColor else SurfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = null,
                                tint = if (isSelected) ObsidianDark else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) persona.accentColor else TextPrimary
                            )
                            Text(
                                text = persona.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = currentPersona.accentColor),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text(
                text = stringResource(id = R.string.text_continue),
                fontSize = 16.sp,
                color = ObsidianDark,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}
