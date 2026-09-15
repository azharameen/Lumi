package com.example.presentation.screens.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.presentation.components.LumiCard

@Composable
fun DailyCheckInCard(
    moodScore: Float,
    onMoodChange: (Float) -> Unit,
    energyLevel: Float,
    onEnergyChange: (Float) -> Unit,
    hydrationCups: Int,
    onHydrationChange: (Int) -> Unit,
    gratitudeText: String,
    onGratitudeChange: (String) -> Unit,
    isSubmittedToday: Boolean,
    onSubmit: () -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    val moodEmojis = listOf("😔", "😕", "😐", "🙂", "😊", "🤩")
    val selectedEmojiIndex = ((moodScore - 1) / 1.8f).toInt().coerceIn(0, moodEmojis.size - 1)

    LumiCard(
        borderColor = LumiPink.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = LumiPink.copy(alpha = 0.18f),
                    shape = CircleShape,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = LumiPink,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.text_daily_wellness_checkin),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = moodEmojis[selectedEmojiIndex],
                style = MaterialTheme.typography.displaySmall
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mood Slider
        Text(
            text = "Mood State: ${moodScore.toInt()}/10",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = moodScore,
            onValueChange = {
                if (moodScore != it) haptics.performTick()
                onMoodChange(it)
            },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = LumiPink,
                activeTrackColor = LumiPink,
                inactiveTrackColor = SurfaceHighlight
            )
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Energy Level Slider
        Text(
            text = "Energy Battery: ${energyLevel.toInt()}/10",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        Slider(
            value = energyLevel,
            onValueChange = {
                if (energyLevel != it) haptics.performTick()
                onEnergyChange(it)
            },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = LumiYellow,
                activeTrackColor = LumiYellow,
                inactiveTrackColor = SurfaceHighlight
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Hydration Stepper Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hydration: $hydrationCups cups (Goal: 8)",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (hydrationCups > 0) onHydrationChange(hydrationCups - 1) },
                    modifier = Modifier
                        .size(MaterialTheme.spacing.extraLarge)
                        .background(SurfaceHighlight, CircleShape)
                ) {
                    Text(
                        text = stringResource(id = R.string.text_minus),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                IconButton(
                    onClick = { onHydrationChange(hydrationCups + 1) },
                    modifier = Modifier
                        .size(MaterialTheme.spacing.extraLarge)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.desc_add_cup),
                        tint = ObsidianDark,
                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gratitude Note
        OutlinedTextField(
            value = gratitudeText,
            onValueChange = onGratitudeChange,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.text_what_are_you_grateful_for_toda),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LumiPink,
                unfocusedBorderColor = SurfaceHighlight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gratitude_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = LumiPink, contentColor = ObsidianDark),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("save_wellness_button")
        ) {
            Text(
                text = if (isSubmittedToday) "Logged! ✨" else "Save Daily Check-In (+25 Pet XP)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}
