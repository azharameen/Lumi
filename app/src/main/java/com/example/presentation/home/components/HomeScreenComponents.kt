package com.example.presentation.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.PetStatus
import kotlin.random.Random

/**
 * Minimal Pet Speech Card: Left = Response Text, Right = Mood Badge.
 */
@Composable
fun MinimalPetSpeechCard(
    petStatus: PetStatus,
    petPrimary: Color,
    onClick: () -> Unit,
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics()
) {
    val speechText = petStatus.speechBubbleText ?: "Hey friend! How are you feeling today?"

    Surface(
        color = SurfaceDark.copy(alpha = 0.88f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, petPrimary.copy(alpha = 0.3f)),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptics.performTick()
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: AI / Pet Response Text
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = petPrimary,
                    modifier = Modifier.size(MaterialTheme.spacing.medium)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = speechText,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Mood Badge
            Surface(
                color = SurfaceDarkVariant,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.6f))
            ) {
                Text(
                    text = petStatus.currentEmotion.displayName.split(" ")[0],
                    color = petPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Server-driven Dynamic Seasonal / Event Banner powered by Firebase Remote Config
 */
@Composable
fun RemoteConfigSeasonalBanner(
    bannerText: String,
    seasonalThemeName: String = "",
    petPrimary: Color = LumiCyan,
    onClick: () -> Unit = {},
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics()
) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.9f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(petPrimary.copy(alpha = 0.8f), LumiGold.copy(alpha = 0.8f)))),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptics.performTick()
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✨", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (seasonalThemeName.isNotBlank()) {
                    Text(
                        text = seasonalThemeName.uppercase(),
                        color = LumiGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = bannerText,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuestItemRow(task: TaskEntity, onToggle: (Boolean) -> Unit) {
    Surface(
        color = SurfaceDarkVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = LumiGreen,
                    uncheckedColor = TextSecondary
                ),
                modifier = Modifier.size(MaterialTheme.spacing.large)
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priorityColor = when (task.priority.uppercase()) {
                        "HIGH", "CRITICAL" -> LumiCoral
                        "MEDIUM" -> LumiYellow
                        else -> LumiMint
                    }
                    Text(
                        text = task.priority.uppercase(),
                        color = priorityColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(stringResource(id = R.string.bullet_point), color = TextTertiary, fontSize = 9.sp)
                    Text(
                        text = "${task.estimatedMinutes}m",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            Surface(
                color = LumiGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.text_50_xp),
                    color = LumiGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CyberAmbientStarsBackground(primaryColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val stars = remember {
        List(30) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { (nx, ny, starRadius) ->
            val x = nx * size.width
            val y = ((ny + offsetY) % 1f) * size.height
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                radius = starRadius.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
