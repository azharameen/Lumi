package com.example.presentation.home.components
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Diamond

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.data.device.BatteryStatus
import com.example.data.device.LocationContext
import com.example.data.device.NetworkStatus
import com.example.data.device.NetworkType
import com.example.domain.account.UserProfileData
import com.example.domain.model.PetStatus

import com.example.domain.model.AuthUser
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun SeamlessRpgPlayerHud(
    petStatus: PetStatus,
    batteryStatus: BatteryStatus,
    networkStatus: NetworkStatus,
    locationContext: LocationContext,
    userProfile: UserProfileData,
    authUser: AuthUser? = null,
    onNavigateToAccount: () -> Unit
) {
    val displayName = authUser?.displayName?.takeIf { it.isNotBlank() } ?: userProfile.userName.ifBlank { "Azhar Ameen" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToAccount() }
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 14.dp)
            .statusBarsPadding() // Ensure content is below status bar
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HudAvatarSection(authUser, displayName, petStatus.level)
            Spacer(modifier = Modifier.width(12.dp))
            HudInfoSection(displayName, petStatus, batteryStatus, networkStatus)
        }
    }
}

@Composable
private fun HudAvatarSection(authUser: AuthUser?, displayName: String, level: Int) {
    val initials = remember(displayName) {
        val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            displayName.length >= 2 -> displayName.take(2).uppercase()
            displayName.isNotEmpty() -> "${displayName.first().uppercaseChar()}N"
            else -> "AA"
        }
    }

    Box(modifier = Modifier.padding(end = 4.dp)) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape)
                .background(Brush.sweepGradient(listOf(LumiCyan, LumiPink, LumiCyan)))
                .padding(2.dp).clip(CircleShape).background(ObsidianDark).padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxSize().clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AvatarBronzeLight, AvatarBronzeDark))),
                contentAlignment = Alignment.Center
            ) {
                if (authUser?.photoUrl != null) {
                    AsyncImage(model = authUser.photoUrl, contentDescription = "Profile", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text(text = initials, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = MaterialTheme.spacing.extraSmall)) {
            SeamlessHexagonLevelBadge(level = level)
        }
    }
}

@Composable
private fun HudInfoSection(displayName: String, petStatus: PetStatus, batteryStatus: BatteryStatus, networkStatus: NetworkStatus) {
    val hpFill = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
    val energyColors = when {
        batteryStatus.levelPercent >= 60 -> listOf(LumiMint, LumiMintBright)
        batteryStatus.levelPercent >= 20 -> listOf(LumiYellow, LumiGoldBright)
        else -> listOf(LumiCoral, LumiCoralDark)
    }

    val xpFill = (petStatus.exp.toFloat() / petStatus.expToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
    
    val (beaconColor, netLabel) = when {
        !networkStatus.isConnected || networkStatus.type == NetworkType.OFFLINE -> LumiCoral to "OFFLINE"
        networkStatus.type == NetworkType.CELLULAR -> LumiYellow to "CELL"
        else -> LumiMint to "WIFI"
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = displayName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            HudCurrencyRow(petStatus.coins, petStatus.gems)
        }

        SciFiProgressBar(label = "HP", labelColor = Color.White.copy(alpha = 0.7f), fillRatio = hpFill, gradient = energyColors, height = 10.dp, trailingText = "${batteryStatus.levelPercent}%", trailingTextColor = Color.White.copy(alpha = 0.9f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            SciFiProgressBar(label = "XP", labelColor = LumiCyan, fillRatio = xpFill, gradient = listOf(LumiCyanDark, LumiCyan), height = MaterialTheme.spacing.small, modifier = Modifier.weight(1f), trailingText = "${petStatus.exp}/${petStatus.expToNextLevel}", trailingTextColor = LumiCyan)
            Spacer(modifier = Modifier.width(6.dp))
            HudBeacon(beaconColor, netLabel)
        }
    }
}

@Composable
private fun HudCurrencyRow(coins: Int, gems: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
        HudCurrencyItem(Icons.Filled.MonetizationOn, LumiGold, "$coins")
        HudCurrencyItem(Icons.Filled.Diamond, LumiCyanLight, "$gems")
    }
}

@Composable
private fun HudCurrencyItem(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HudBeacon(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, modifier = Modifier.width(60.dp)) {
        Box(modifier = Modifier.size(MaterialTheme.spacing.extraSmall).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SciFiProgressBar(
    label: String,
    labelColor: Color,
    fillRatio: Float,
    gradient: List<Color>,
    height: Dp,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    trailingTextColor: Color = Color.Unspecified
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(text = label, color = labelColor, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(20.dp))
        Box(modifier = Modifier.weight(1f).height(height)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val skew = size.height * 0.5f
                val w = size.width
                val h = size.height
                val bgPath = Path().apply { moveTo(skew, 0f); lineTo(w, 0f); lineTo(w - skew, h); lineTo(0f, h); close() }
                drawPath(bgPath, color = Color.White.copy(alpha = 0.1f))
                
                val fillW = (w * fillRatio).coerceAtLeast(0f)
                if (fillW > 0) {
                    val fgPath = Path().apply { moveTo(skew, 0f); lineTo(maxOf(skew, fillW), 0f); lineTo(maxOf(0f, fillW - skew), h); lineTo(0f, h); close() }
                    drawPath(fgPath, brush = Brush.horizontalGradient(gradient))
                }
            }
        }
        if (trailingText != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = trailingText, color = trailingTextColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
        }
    }
}

@Composable
fun SeamlessHexagonLevelBadge(level: Int) {
    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.5f, 0f); lineTo(w, h * 0.25f); lineTo(w, h * 0.75f)
                lineTo(w * 0.5f, h); lineTo(0f, h * 0.75f); lineTo(0f, h * 0.25f); close()
            }
            drawPath(path, color = LumiCyan)
        }
        Text(text = "$level", color = ObsidianDark, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
