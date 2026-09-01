package com.example.presentation.home.components

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

/**
 * Seamless Floating RPG Player HUD Banner:
 * - Left: Solid warm brown Avatar touching the dual progress bars directly.
 * - Center: Dual attached progress bars (HP Bar on top, XP Bar directly attached below).
 * - Top line: User Name + Hexagonal Level Badge.
 * - Bottom line: Location telemetry & Network link status.
 */
@Composable
fun SeamlessRpgPlayerHud(
    petStatus: PetStatus,
    batteryStatus: BatteryStatus,
    networkStatus: NetworkStatus,
    locationContext: LocationContext,
    userProfile: UserProfileData,
    onNavigateToAccount: () -> Unit
) {
    val displayName = userProfile.userName.ifBlank { "Azhar Ameen" }

    // Initials calculation
    val initials = remember(displayName) {
        val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            displayName.length >= 2 -> displayName.take(2).uppercase()
            displayName.isNotEmpty() -> "${displayName.first().uppercaseChar()}N"
            else -> "AA"
        }
    }

    val hpFillRatio = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
    val energyGradient = when {
        batteryStatus.levelPercent >= 60 -> listOf(LumiMint, LumiMintBright)
        batteryStatus.levelPercent >= 20 -> listOf(LumiYellow, LumiGoldBright)
        else -> listOf(LumiCoral, LumiCoralDark)
    }

    val xpRatio = (petStatus.exp.toFloat() / petStatus.expToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
    val xpGradient = listOf(LumiCyanDark, LumiCyan)

    val beaconColor = when {
        !networkStatus.isConnected || networkStatus.type == NetworkType.OFFLINE -> LumiCoral
        networkStatus.type == NetworkType.CELLULAR -> LumiYellow
        else -> LumiMint
    }

    val netLabel = when (networkStatus.type) {
        NetworkType.WIFI -> "WIFI"
        NetworkType.CELLULAR -> "CELL"
        NetworkType.ETHERNET -> "ETH"
        NetworkType.OFFLINE -> "OFFLINE"
    }

    // Floating header bar with top-down gradient
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianDark.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .clickable { onNavigateToAccount() }
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Avatar with overlapping Level Badge
            Box(
                modifier = Modifier.padding(end = 12.dp)
            ) {
                // Outer Tech Ring
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(LumiCyan, LumiPink, LumiCyan)))
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(ObsidianDark)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AvatarBronzeLight, AvatarBronzeDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Level Badge overlapping at bottom right
                Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = MaterialTheme.spacing.extraSmall)) {
                    SeamlessHexagonLevelBadge(level = petStatus.level)
                }
            }

            // RIGHT: Info & Bars
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Row: Name + Resources + Telemetry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name
                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Currencies
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        // Coins
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${petStatus.coins}",
                                color = LumiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Gems
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💎", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${petStatus.gems}",
                                color = LumiCyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // HP Bar
                SciFiProgressBar(
                    label = "HP",
                    labelColor = Color.White.copy(alpha = 0.7f),
                    fillRatio = hpFillRatio,
                    gradient = energyGradient,
                    height = 10.dp,
                    trailingText = "${batteryStatus.levelPercent}%",
                    trailingTextColor = Color.White.copy(alpha = 0.9f)
                )

                // XP Bar & Telemetry
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SciFiProgressBar(
                        label = "XP",
                        labelColor = LumiCyan,
                        fillRatio = xpRatio,
                        gradient = xpGradient,
                        height = MaterialTheme.spacing.small,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.width(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(MaterialTheme.spacing.extraSmall)
                                .clip(CircleShape)
                                .background(beaconColor)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = netLabel,
                            color = beaconColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
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
        Text(
            text = label,
            color = labelColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(20.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val skew = size.height * 0.5f
                val w = size.width
                val h = size.height
                
                val bgPath = Path().apply {
                    moveTo(skew, 0f)
                    lineTo(w, 0f)
                    lineTo(w - skew, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(bgPath, color = Color.White.copy(alpha = 0.1f))
                
                val fillW = (w * fillRatio).coerceAtLeast(0f)
                if (fillW > 0) {
                    val fgPath = Path().apply {
                        moveTo(skew, 0f)
                        lineTo(maxOf(skew, fillW), 0f)
                        lineTo(maxOf(0f, fillW - skew), h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(fgPath, brush = Brush.horizontalGradient(gradient))
                }
            }
        }
        if (trailingText != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = trailingText,
                color = trailingTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Sleek Hexagonal Level Badge
 */
@Composable
fun SeamlessHexagonLevelBadge(level: Int) {
    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.25f)
                lineTo(w, h * 0.75f)
                lineTo(w * 0.5f, h)
                lineTo(0f, h * 0.75f)
                lineTo(0f, h * 0.25f)
                close()
            }
            drawPath(path, color = LumiCyan)
        }
        Text(
            text = "$level",
            color = ObsidianDark,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}
