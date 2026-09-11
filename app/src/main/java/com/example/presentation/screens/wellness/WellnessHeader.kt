package com.example.presentation.screens.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiPink
import com.example.core.theme.TextPrimary

@Composable
fun WellnessHeader(
    onNavigateBack: () -> Unit,
    onOpenBreathing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.desc_back),
                    tint = TextPrimary
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.text_holistic_wellness),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.text_mindfulness_hydration_energy_balance),
                    color = LumiPink,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.5.sp
                )
            }
        }

        IconButton(
            onClick = onOpenBreathing,
            modifier = Modifier
                .size(42.dp)
                .background(LumiGreen.copy(alpha = 0.18f), CircleShape)
                .testTag("wellness_breathing_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Air,
                contentDescription = stringResource(id = R.string.desc_breathing_exercise),
                tint = LumiGreen,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
