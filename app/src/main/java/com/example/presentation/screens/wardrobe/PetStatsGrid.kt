package com.example.presentation.screens.wardrobe

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiPink
import com.example.domain.model.PetStatus
import com.example.presentation.components.LumiStatCard

@Composable
fun PetStatsGrid(
    petStatus: PetStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LumiStatCard(
            title = "Bond Score",
            value = "${petStatus.bondScore}%",
            icon = Icons.Default.Favorite,
            accentColor = LumiPink,
            modifier = Modifier.weight(1f)
        )
        LumiStatCard(
            title = "Days Together",
            value = "${petStatus.daysTogether}d",
            icon = Icons.Default.Star,
            accentColor = LumiGold,
            modifier = Modifier.weight(1f)
        )
        LumiStatCard(
            title = "Interactions",
            value = "${petStatus.totalInteractions}",
            icon = Icons.Default.AutoAwesome,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}
