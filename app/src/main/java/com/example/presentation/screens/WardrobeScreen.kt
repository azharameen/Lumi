package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.theme.*
import com.example.presentation.screens.wardrobe.*
import com.example.presentation.viewmodel.PetViewModel
import com.example.presentation.viewmodel.WellnessViewModel
import kotlinx.coroutines.launch
import java.util.Locale

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
    val remoteConfigManager = remember {
        try {
            org.koin.core.context.GlobalContext.get().getOrNull<com.example.data.firebase.LumiRemoteConfigManager>()
        } catch (_: Exception) {
            null
        }
    }
    val remoteConfig = remoteConfigManager?.config?.collectAsStateWithLifecycle(initialValue = com.example.domain.model.LumiRemoteConfig())?.value
        ?: com.example.domain.model.LumiRemoteConfig()
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
            .map { it.trim().uppercase(Locale.ROOT) }
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
            WardrobeHeader(
                coins = petStatus.coins,
                gems = petStatus.gems,
                onClose = onClose
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                // Live Pet Showcase
                item(key = "pet_showcase") {
                    PetShowcaseCard(
                        petStatus = petStatus,
                        evolutionStageTitle = evolutionStageTitle,
                        onPetTouched = { petViewModel.onPetTouched() },
                        onPetPetted = { petViewModel.onPetPetted() },
                        onRenameClick = { showRenameDialog = true }
                    )
                }

                // Stats Grid
                item(key = "stats_grid") {
                    PetStatsGrid(petStatus = petStatus)
                }

                // Wearable RPG Accessories Section
                if (remoteConfig.enablePetAccessories) {
                    item(key = "accessory_shop") {
                        AccessoryShopSection(
                            petStatus = petStatus,
                            unlockedAccessories = unlockedAccessories,
                            remoteConfig = remoteConfig,
                            onEquipAccessory = { acc, isUnlocked, isEquipped ->
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
                                } else if (!remoteConfig.enablePurchaseButtons) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Purchases are currently disabled.")
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
                        )
                    }
                }

                // 1. Morphing Shape Customizer (Sphere, Cube, Capsule, Star, etc.)
                item(key = "shape_customizer") {
                    MorphingShapeSection(
                        currentShape = petStatus.bloubShape,
                        onSelectShape = { shape ->
                            petViewModel.setBloubShape(shape)
                            scope.launch {
                                snackbarHostState.showSnackbar("Morphed into ${shape.displayName}!")
                            }
                        }
                    )
                }

                // 2. Clay Color Palettes
                item(key = "color_customizer") {
                    ClayColorPaletteSection(
                        currentSkin = petStatus.bloubSkinColor,
                        onSelectSkin = { skin ->
                            petViewModel.setBloubSkinColor(skin)
                            scope.launch {
                                snackbarHostState.showSnackbar("Applied ${skin.displayName} skin!")
                            }
                        }
                    )
                }

                // Long Term Memory Vault
                memoryVaultSection(memories = memories)
            }
        }

        // Rename Dialog
        if (showRenameDialog) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = { Text(stringResource(R.string.text_rename_companion), color = TextPrimary) },
                text = {
                    OutlinedTextField(
                        value = petNameInput,
                        onValueChange = { petNameInput = it },
                        label = { Text(stringResource(R.string.text_companion_name)) },
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
                        Text(stringResource(R.string.text_save))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text(stringResource(R.string.text_cancel), color = TextSecondary)
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
