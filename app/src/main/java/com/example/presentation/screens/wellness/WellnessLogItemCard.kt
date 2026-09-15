package com.example.presentation.screens.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.domain.model.WellnessLog
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun WellnessLogItemCard(
    log: WellnessLog,
    dateFormat: SimpleDateFormat,
    onIncrementHydration: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
        shape = RoundedCornerShape(MaterialTheme.spacing.cornerMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.extraSmall),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = log.moodLabel,
                        color = LumiPink,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = "Mood ${log.moodScore}/10 • Energy ${log.energyLevel}/10",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                Text(
                    text = "💧 ${log.hydrationCups} cups water • ${dateFormat.format(Date(log.timestamp))}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
                if (!log.gratitudeNote.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = "\"${log.gratitudeNote}\"",
                        color = TextPrimary.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onIncrementHydration(log.id) },
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = stringResource(id = R.string.desc_add_water),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MaterialTheme.spacing.medium)
                )
            }
        }
    }
}
