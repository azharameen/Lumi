package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BloubShape
import com.example.domain.model.BloubSkinColor
import com.example.ui.pet.LumiPetView

import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiPink

import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LumiViewModel

@Composable
fun WardrobeScreen(onClose: () -> Unit = {}, 
    petViewModel: com.example.ui.viewmodel.PetViewModel, wellnessViewModel: com.example.ui.viewmodel.WellnessViewModel
) {
    val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
    val memories by wellnessViewModel.allMemories.collectAsStateWithLifecycle()

    val evolutionStageTitle = when (petStatus.level) {
        1 -> "Sprout Spirit"
        2 -> "Starlight Orb"
        3 -> "Harmonic Luminary"
        4 -> "Celestial Guardian"
        else -> "Cosmic Oracle"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.IconButton(onClick = onClose) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Morphing Studio & Evolution",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Customize 3D shapes, clay skin palettes & review Lumi's memory bank",
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 56.dp)
            )
        }
        
        // Live Pet Showcase
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LumiPetView(
                        petStatus = petStatus,
                        size = 180.dp,
                        onPetTouched = { petViewModel.onPetTouched() },
                        onPetPetted = { petViewModel.onPetPetted() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${petStatus.name} • $evolutionStageTitle",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = LumiGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Level ${petStatus.level}",
                                color = LumiGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${petStatus.exp} / ${petStatus.expToNextLevel} XP to Level ${petStatus.level + 1}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val progress = (petStatus.exp.toFloat() / petStatus.expToNextLevel).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        trackColor = SurfaceHighlight
                    )
                }
            }
        }

        // Stats Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Bond Score",
                    value = "${petStatus.bondScore}%",
                    icon = Icons.Default.Favorite,
                    color = LumiPink,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Days Together",
                    value = "${petStatus.daysTogether}d",
                    icon = Icons.Default.Star,
                    color = LumiGold,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Interactions",
                    value = "${petStatus.totalInteractions}",
                    icon = Icons.Default.AutoAwesome,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 1. Morphing Shape Customizer (Sphere, Cube, Capsule)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3D MORPHING SHAPE",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BloubShape.entries) { shape ->
                            val isSelected = petStatus.bloubShape == shape
                            Surface(
                                color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .clickable { petViewModel.setBloubShape(shape) }
                                    .testTag("shape_${shape.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = shape.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = shape.displayName,
                                        color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Clay Color Palettes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLAY SKIN PALETTES",
                        color = LumiPink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BloubSkinColor.entries) { skin ->
                            val isSelected = petStatus.bloubSkinColor == skin
                            Surface(
                                color = if (isSelected) Color(skin.primaryHex).copy(alpha = 0.25f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(skin.primaryHex)) else null,
                                modifier = Modifier
                                    .clickable { petViewModel.setBloubSkinColor(skin) }
                                    .testTag("skin_${skin.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(skin.primaryHex),
                                                        Color(skin.endHex)
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = skin.displayName,
                                        color = if (isSelected) Color(skin.primaryHex) else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Long Term Memory Vault
        item {
            Text(
                text = "Lumi's Long-Term Memory Vault",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (memories.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lumi remembers your daily habits, preferred meeting times, and productivity rhythms automatically as you chat and interact.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(memories) { mem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = mem.category.uppercase(),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${mem.sentiment} • Impact: ${mem.emotionalImpact}/5",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mem.memoryText,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
