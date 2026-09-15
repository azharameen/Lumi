package com.example.presentation.screens.wardrobe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.domain.model.LumiRemoteConfig
import com.example.domain.model.PetAccessory
import com.example.domain.model.PetStatus
import com.example.presentation.components.LumiCard

@Composable
fun AccessoryShopSection(
    petStatus: PetStatus,
    unlockedAccessories: Set<String>,
    remoteConfig: LumiRemoteConfig,
    onEquipAccessory: (PetAccessory, Boolean, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!remoteConfig.enablePetAccessories) return

    LumiCard(
        borderColor = LumiGold.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
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
                    unlockedAccessories.contains(acc.id.uppercase(java.util.Locale.ROOT)) ||
                    unlockedAccessories.contains(acc.name.uppercase(java.util.Locale.ROOT))
                val isEquipped = petStatus.activeAccessory.equals(acc.id, ignoreCase = true) ||
                    petStatus.activeAccessory.equals(acc.name, ignoreCase = true)

                Surface(
                    onClick = {
                        onEquipAccessory(acc, isUnlocked, isEquipped)
                    },
                    color = when {
                        isEquipped -> LumiGold.copy(alpha = 0.25f)
                        isUnlocked -> SurfaceDarkVariant
                        else -> SurfaceDark
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = if (isEquipped) BorderStroke(1.5.dp, LumiGold) else null,
                    modifier = Modifier
                        .width(130.dp)
                        .testTag("accessory_${acc.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = acc.iconEmoji, style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = acc.displayName,
                            color = if (isEquipped) LumiGold else TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
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
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            isUnlocked -> {
                                Text(
                                    text = stringResource(R.string.text_equip),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
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
                                                style = MaterialTheme.typography.labelSmall,
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
                                                style = MaterialTheme.typography.labelSmall,
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
