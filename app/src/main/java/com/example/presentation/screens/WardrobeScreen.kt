package com.example.presentation.screens
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.ui.res.stringResource
import com.example.R

import com.example.presentation.components.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.example.core.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.BloubShape
import com.example.domain.model.BloubSkinColor
import com.example.domain.model.PetAccessory
import com.example.presentation.pet.LumiPetView
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiPink
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.example.presentation.viewmodel.PetViewModel
import com.example.presentation.viewmodel.WellnessViewModel
import kotlinx.coroutines.launch
import com.example.core.theme.spacing

/**
 * Customization Studio & Evolution Wardrobe Screen.
 * Complete RPG Shop with 3D Morphing, Clay Skins, Wearable Accessories,
 * and Long-term Memory Vault.
 */
@Composable
fun WardrobeScreen(
    onClose: () -> Unit = {},
    petViewModel: PetViewModel,
    wellnessViewModel: WellnessViewModel
) {
    val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
    val memories by wellnessViewModel.allMemories.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRenameDialog by remember { mutableStateOf(false) }
    var petNameInput by remember(petStatus.name) { mutableStateOf(petStatus.name) }

    val evolutionStageTitle = remember(petStatus.level) {
        when (petStatus.level) {
            1 -> "Sprout Spirit"
            2 -> "Starlight Orb"
            3 -> "Harmonic Luminary"
            4 -> "Celestial Guardian"
            else -> "Cosmic Oracle"
        }
    }

    val unlockedAccessories = remember(petStatus.unlockedAccessoriesCsv) {
        petStatus.unlockedAccessoriesCsv.split(",")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header with Coins and Gems Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = MaterialTheme.spacing.medium,
                        end = MaterialTheme.spacing.medium,
                        top = MaterialTheme.spacing.medium,
                        bottom = MaterialTheme.spacing.small
                    ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("close_wardrobe_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.desc_close),
                                tint = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Column {
                            Text(
                                text = stringResource(R.string.text_lumi_wardrobe_shop),
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.text_rpg_customization_accessories),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Currency Balance Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = LumiGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, LumiGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MonetizationOn,
                                    contentDescription = "Coins",
                                    tint = LumiGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Text(
                                    text = "${petStatus.coins}",
                                    color = LumiGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = LumiCyanDark.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, LumiCyanDark.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Diamond,
                                    contentDescription = "Gems",
                                    tint = LumiCyanLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Text(
                                    text = "${petStatus.gems}",
                                    color = LumiCyanLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                // Live Pet Showcase
                item(key = "pet_showcase") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(MaterialTheme.spacing.large),
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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${petStatus.name} • $evolutionStageTitle",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showRenameDialog = true },
                                modifier = Modifier.size(MaterialTheme.spacing.large)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.desc_rename_lumi),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = LumiGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(MaterialTheme.spacing.small)
                            ) {
                                Text(
                                    text = "Level ${petStatus.level}",
                                    color = LumiGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = "${petStatus.exp} / ${petStatus.expToNextLevel} XP to Level ${petStatus.level + 1}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        val progress = remember(petStatus.exp, petStatus.expToNextLevel) {
                            if (petStatus.expToNextLevel > 0) {
                                (petStatus.exp.toFloat() / petStatus.expToNextLevel).coerceIn(0f, 1f)
                            } else 1f
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(MaterialTheme.spacing.small)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = SurfaceHighlight
                        )
                    }
                }
            }

            // Stats Grid
            item(key = "stats_grid") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

            // Wearable RPG Accessories Section
            item(key = "accessory_shop") {
                LumiCard(
                    borderColor = LumiGold.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.text_wearable_accessories_shop),
                            color = LumiGold,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = LumiGold,
                            modifier = Modifier.size(MaterialTheme.spacing.medium)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = PetAccessory.entries,
                            key = { it.id }
                        ) { acc ->
                            val isUnlocked = acc.id.equals("NONE", ignoreCase = true) ||
                                acc.name.equals("NONE", ignoreCase = true) ||
                                unlockedAccessories.contains(acc.id.uppercase()) ||
                                unlockedAccessories.contains(acc.name.uppercase())
                            val isEquipped = petStatus.activeAccessory.equals(acc.id, ignoreCase = true) ||
                                petStatus.activeAccessory.equals(acc.name, ignoreCase = true)

                            Surface(
                                color = when {
                                    isEquipped -> LumiGold.copy(alpha = 0.25f)
                                    isUnlocked -> SurfaceDarkVariant
                                    else -> SurfaceDark
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = if (isEquipped) BorderStroke(1.5.dp, LumiGold) else null,
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable {
                                        if (isEquipped && !acc.id.equals("NONE", ignoreCase = true)) {
                                            petViewModel.equipAccessory("NONE")
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Unequipped ${acc.displayName}")
                                            }
                                        } else if (isUnlocked) {
                                            petViewModel.equipAccessory(acc.id)
                                            scope.launch {
                                                val msg = if (acc.id.equals("NONE", ignoreCase = true)) "Unequipped accessories" else "Equipped ${acc.displayName}!"
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        } else {
                                            petViewModel.buyAccessory(acc) { success ->
                                                scope.launch {
                                                    if (success) {
                                                        snackbarHostState.showSnackbar("Unlocked & equipped ${acc.displayName}!")
                                                    } else {
                                                        snackbarHostState.showSnackbar("Not enough coins/gems to unlock ${acc.displayName}!")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("accessory_${acc.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = acc.iconEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = acc.displayName,
                                        color = if (isEquipped) LumiGold else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                                    when {
                                        isEquipped -> {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = LumiGold,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = stringResource(R.string.text_equipped),
                                                    color = LumiGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        isUnlocked -> {
                                            Text(
                                                text = stringResource(R.string.text_equip),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        else -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                if (acc.coinCost > 0) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${acc.coinCost}",
                                                            color = LumiGold,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Filled.MonetizationOn,
                                                            contentDescription = "Coins",
                                                            tint = LumiGold,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                                if (acc.gemCost > 0) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "${acc.gemCost}",
                                                            color = LumiCyanLight,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Filled.Diamond,
                                                            contentDescription = "Gems",
                                                            tint = LumiCyanLight,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 1. Morphing Shape Customizer (Sphere, Cube, Capsule, Star, etc.)
            item(key = "shape_customizer") {
                LumiCard(
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.text_3d_morphing_shape),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = BloubShape.entries,
                            key = { it.name }
                        ) { shape ->
                            val isSelected = petStatus.bloubShape == shape ||
                                petStatus.bloubShape.name.equals(shape.name, ignoreCase = true) ||
                                petStatus.bloubShape.id.equals(shape.id, ignoreCase = true)
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .clickable {
                                        petViewModel.setBloubShape(shape)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Morphed into ${shape.displayName}!")
                                        }
                                    }
                                    .testTag("shape_${shape.name}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = shape.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                                    Text(
                                        text = shape.displayName,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Clay Color Palettes
            item(key = "color_customizer") {
                LumiCard(
                    borderColor = LumiPink.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.text_clay_skin_palettes),
                        color = LumiPink,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = BloubSkinColor.entries,
                            key = { it.name }
                        ) { skin ->
                            val isSelected = petStatus.bloubSkinColor == skin ||
                                petStatus.bloubSkinColor.name.equals(skin.name, ignoreCase = true) ||
                                petStatus.bloubSkinColor.id.equals(skin.id, ignoreCase = true)
                            Surface(
                                color = if (isSelected) Color(skin.primaryHex).copy(alpha = 0.25f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isSelected) BorderStroke(1.5.dp, Color(skin.primaryHex)) else null,
                                modifier = Modifier
                                    .clickable {
                                        petViewModel.setBloubSkinColor(skin)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Applied ${skin.displayName} skin!")
                                        }
                                    }
                                    .testTag("skin_${skin.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = MaterialTheme.spacing.small)
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
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
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

            // Long Term Memory Vault
            item(key = "memory_header") {
                Text(
                    text = stringResource(R.string.text_lumis_longterm_memory_vault),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (memories.isEmpty()) {
                item(key = "memory_empty") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.text_lumi_remembers_your_daily_habits_preferred),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = memories,
                    key = { it.id }
                ) { mem ->
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
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${mem.sentiment} • Impact: ${mem.emotionalImpact}/5",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
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

        // Rename Dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text("Rename Companion", color = TextPrimary) },
                text = {
                    OutlinedTextField(
                        value = petNameInput,
                        onValueChange = { petNameInput = it },
                        label = { Text("Companion Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (petNameInput.isNotBlank()) {
                                petViewModel.updatePetName(petNameInput.trim())
                            }
                            showRenameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}
