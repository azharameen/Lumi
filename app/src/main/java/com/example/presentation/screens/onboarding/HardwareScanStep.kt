package com.example.presentation.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.domain.onboarding.DeviceProfile
import com.example.presentation.components.LumiCard

@Composable
fun HardwareScanStep(
    profile: DeviceProfile?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = "Hardware Diagnostics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scanning device RAM, storage, NPU, and AI acceleration capabilities",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (profile != null) {
            val totalRamGb = profile.totalRamBytes / (1024 * 1024 * 1024)
            val freeStorageGb = profile.freeStorageBytes / (1024 * 1024 * 1024)

            LumiCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = LumiCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "RAM: ${totalRamGb} GB (${if (profile.isLowRamDevice) "Low RAM State" else "Optimal"})",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = LumiPink)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Storage Available: ${freeStorageGb} GB",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = LumiMint)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Android AICore / NPU: ${if (profile.isAiCoreAvailable) "Built-in Ready (Gemini Nano)" else "Standard CPU/GPU Delegate"}",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text(
                "View Recommended Models",
                style = MaterialTheme.typography.titleMedium,
                color = ObsidianDark,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}
