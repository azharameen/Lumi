package com.example.presentation.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.domain.onboarding.DeviceProfile
import com.example.domain.onboarding.ModelRecommendationEngine

@Composable
fun ModelDownloadStep(
    profile: DeviceProfile?,
    onComplete: () -> Unit
) {
    val engine = remember { ModelRecommendationEngine() }
    val recommendations = remember(profile) {
        if (profile != null) engine.getRecommendations(profile) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = "Recommended Models",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Optimal models selected for your hardware footprint",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            recommendations.forEach { rec ->
                Surface(
                    color = if (rec.isRecommended) LumiCyan.copy(alpha = 0.15f) else SurfaceDarkVariant,
                    border = BorderStroke(1.dp, if (rec.isRecommended) LumiCyan else SurfaceHighlight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = if (rec.isRecommended) LumiCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rec.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text(rec.description, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(rec.sizeDisplay, color = LumiCyan, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(R.string.text_complete_setup_start_lumi), style = MaterialTheme.typography.titleMedium, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}
