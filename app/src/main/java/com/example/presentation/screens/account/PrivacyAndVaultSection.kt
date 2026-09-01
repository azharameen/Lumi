package com.example.presentation.screens.account
import androidx.compose.ui.res.stringResource
import com.example.R


import com.example.presentation.components.*

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.remote.HardwareAccelerator
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadStatus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.account.LumiPersonaTone
import com.example.domain.account.UserFactItem
import com.example.domain.account.UserProfileData
import com.example.domain.connectors.ConnectorRepository
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiYellow
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.example.core.theme.TextTertiary
import com.example.presentation.viewmodel.LumiViewModel
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

@Composable
fun PrivacyAndVaultSection(
    userProfile: UserProfileData,
    taskCount: Int,
    eventCount: Int,
    memoryCount: Int,
    messageCount: Int,
    onToggleBiometric: (Boolean) -> Unit,
    isOverlayEnabled: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        // Floating Overlay Settings
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(
                        text = stringResource(R.string.text_floating_companion_overlay),
                        color = LumiPink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = LumiPink, modifier = Modifier.size(MaterialTheme.spacing.large))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(id = R.string.text_lumi_floating_pet), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(stringResource(id = R.string.text_keep_lumi_active_on_screen_ove), color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Switch(
                            checked = isOverlayEnabled,
                            onCheckedChange = onToggleOverlay,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianDark,
                                checkedTrackColor = LumiPink,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = SurfaceDarkVariant
                            )
                        )
                    }
                }
            }
        }

        // Security & Biometric Lock
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(
                        text = stringResource(R.string.text_security_biometric_access),
                        color = LumiMint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = LumiMint, modifier = Modifier.size(MaterialTheme.spacing.large))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(stringResource(id = R.string.text_biometric_lock_for_memory_vaul), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(stringResource(id = R.string.text_require_fingerprint_face_to_vi), color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Switch(
                            checked = userProfile.enableBiometricLock,
                            onCheckedChange = onToggleBiometric,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianDark,
                                checkedTrackColor = LumiMint,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = SurfaceDarkVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Firebase App Check Play Integrity Badge
                    Surface(
                        color = SurfaceDarkVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LumiMint.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = LumiMint,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.text_firebase_app_check),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = stringResource(R.string.text_app_check_desc),
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Local SQLite Room Database Telemetry
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(
                        text = stringResource(R.string.text_ondevice_storage_statistics),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.text_all_data_is_securely_stored_locally),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        DbStatPill(label = "Tasks", count = taskCount, color = LumiYellow, modifier = Modifier.weight(1f))
                        DbStatPill(label = "Events", count = eventCount, color = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        DbStatPill(label = "Memories", count = memoryCount, color = LumiPink, modifier = Modifier.weight(1f))
                        DbStatPill(label = "Messages", count = messageCount, color = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Export Data & Backup
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(
                        text = stringResource(R.string.text_data_portability_backup),
                        color = LumiGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Exporting local vault data as JSON...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LumiGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(MaterialTheme.spacing.medium))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(stringResource(id = R.string.text_export_data_vault_json), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    Button(
                        onClick = onResetClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = LumiPink, modifier = Modifier.size(MaterialTheme.spacing.medium))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(stringResource(id = R.string.text_clear_chat_logs_analytics), color = LumiPink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
