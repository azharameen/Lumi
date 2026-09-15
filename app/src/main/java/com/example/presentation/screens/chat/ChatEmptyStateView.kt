package com.example.presentation.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.domain.model.LumiRemoteConfig

@Composable
fun ChatEmptyStateView(
    remoteConfig: LumiRemoteConfig?,
    onSelectStarter: (String) -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Ambient Orb Card
        Surface(
            color = SurfaceDark.copy(alpha = 0.85f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(LumiCyan.copy(alpha = 0.4f), LumiViolet.copy(alpha = 0.4f), LumiPink.copy(alpha = 0.4f))
                )
            ),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Brush.radialGradient(listOf(LumiCyan.copy(alpha = 0.3f), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨",
                        style = MaterialTheme.typography.displayLarge
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = remoteConfig?.welcomeGreeting ?: "Hello! I'm Lumi, your Neural Companion.",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = remoteConfig?.companionTipOfTheDay ?: "I can help break down goals, plan your morning, guide coherent breathing, and keep you company. Ask me anything!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Suggested Explorations",
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        val starters = listOf(
            StarterPrompt("🌿 Start 4-7-8 Breathing", "Calm your nervous system with guided pacing", LumiMint),
            StarterPrompt("📅 Plan My Schedule for Today", "Prioritize tasks and organize time blocks", LumiCyan),
            StarterPrompt("🎯 Break Down a Goal", "Create a decomposed milestone action swarm", LumiAmber),
            StarterPrompt("💧 Log Hydration & Mood", "Record 2 cups of water and energetic mood", LumiPink),
            StarterPrompt("💡 Personalized Wellness Insights", "Review patterns and optimize daily rhythm", LumiViolet)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            starters.forEach { item ->
                Surface(
                    color = SurfaceDarkVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptics.performSuccess()
                            onSelectStarter(item.title.drop(2).trim())
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title.take(2),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title.drop(2).trim(),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.subtitle,
                                color = TextTertiary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = item.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class StarterPrompt(
    val title: String,
    val subtitle: String,
    val accentColor: Color
)
