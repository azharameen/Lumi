package com.example.presentation.screens.wellness

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Psychology
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
import com.example.data.local.entity.PetMemoryEntity
import java.util.Locale

@Composable
fun BiometricMemoryVaultCard(
    isUnlocked: Boolean,
    vaultAuthError: String?,
    memories: List<PetMemoryEntity>,
    onUnlock: () -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.desc_vault_security),
                        tint = if (isUnlocked) LumiGreen else LumiGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = stringResource(R.string.text_biometric_memory_vault),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isUnlocked) {
                    Button(
                        onClick = onLock,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight, contentColor = TextSecondary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = MaterialTheme.spacing.extraSmall),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(stringResource(id = R.string.text_lock), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            if (!isUnlocked) {
                Text(
                    text = stringResource(R.string.text_lumis_longterm_memory_bank_and_confidential),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onUnlock,
                    colors = ButtonDefaults.buttonColors(containerColor = LumiGold, contentColor = ObsidianDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("unlock_biometric_vault_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = stringResource(id = R.string.text_unlock_with_fingerprint_pin),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                vaultAuthError?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = err, color = LumiPink, fontSize = 11.sp)
                }
            } else {
                // Unlocked State: Show Lumi's Learned Memory Bank
                Text(
                    text = "Unlocked: Lumi's Persistent Memory Bank (${memories.size} items stored)",
                    color = LumiGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (memories.isEmpty()) {
                    Text(
                        text = stringResource(R.string.text_lumi_hasnt_learned_memories_yet_chat),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    memories.forEach { memory ->
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.spacing.extraSmall)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                Column {
                                    Text(
                                        text = memory.category.uppercase(Locale.ROOT),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = memory.memoryText,
                                        color = TextPrimary,
                                        fontSize = 12.sp
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
