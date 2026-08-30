import re

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

# Fix the call site
old_call = """                    4 -> SystemPreferencesSection(
                        userProfile = userProfile,
                        onToggleBiometric = { isEnabled ->
                            viewModel.updateUserProfile(userProfile.copy(enableBiometricLock = isEnabled))
                        },
                        onResetClicked = { showResetDialog = true }
                    )"""

new_call = """                    4 -> SystemPreferencesSection(
                        userProfile = userProfile,
                        viewModel = viewModel,
                        uiState = uiState,
                        onToggleBiometric = { isEnabled ->
                            viewModel.updateUserProfile(userProfile.copy(enableBiometricLock = isEnabled))
                        },
                        onResetClicked = { showResetDialog = true }
                    )"""
content = content.replace(old_call, new_call)

# Fix the function signature
old_sig = """private fun SystemPreferencesSection(
    userProfile: UserProfileData,
    onToggleBiometric: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {"""

new_sig = """import android.provider.Settings
import android.os.Build
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.service.PetOverlayService

@Composable
private fun SystemPreferencesSection(
    userProfile: UserProfileData,
    viewModel: LumiViewModel,
    uiState: com.example.ui.viewmodel.UiState,
    onToggleBiometric: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {"""
content = content.replace(old_sig, new_sig)

# Add the overlay toggle item
old_item = """        // Security & Biometric Lock
        item {"""

new_item = """        // Screen Pet Overlay Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Floating Pet Overlay",
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
                            Icon(Icons.Default.PictureInPictureAlt, contentDescription = null, tint = LumiPink, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Always-on Companion", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Let Lumi float over other apps on your screen", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        Switch(
                            checked = uiState.isOverlayEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        viewModel.setShowOverlayPermission(true)
                                    } else {
                                        viewModel.setOverlayEnabled(true)
                                        val serviceIntent = Intent(context, PetOverlayService::class.java)
                                        ContextCompat.startForegroundService(context, serviceIntent)
                                    }
                                } else {
                                    viewModel.setOverlayEnabled(false)
                                    val serviceIntent = Intent(context, PetOverlayService::class.java)
                                    context.stopService(serviceIntent)
                                }
                            },
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
        item {"""
content = content.replace(old_item, new_item)

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)
