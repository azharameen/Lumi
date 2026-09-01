package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.NavDestination
import com.example.core.theme.*
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

/**
 * Floating Holographic Glassmorphic Bottom Navigation Dock.
 * Unifies all 5 core destinations:
 * - 0: Companion (Cyan)
 * - 1: Assistant (Violet)
 * - 2: Productivity / Life Hub (Gold)
 * - 3: Wellness (Pink)
 * - 4: Account (Mint)
 */
@Composable
fun LumiNavigationBar(
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics(),
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    pendingTasksCount: Int = 0
) {
    val destinations = NavDestination.allDestinations

    Surface(
        color = SurfaceGlass,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(
            1.2.dp,
            Brush.verticalGradient(
                listOf(
                    SurfaceHighlight.copy(alpha = 0.9f),
                    SurfaceDarkVariant.copy(alpha = 0.4f)
                )
            )
        ),
        shadowElevation = MaterialTheme.spacing.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 10.dp)
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val isSelected = selectedTab == destination.tabIndex
                LumiNavigationItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { 
                        haptics.performTick()
                        onTabSelected(destination.tabIndex) 
                    },
                    badgeCount = if (destination == NavDestination.LifeHub) pendingTasksCount else 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LumiNavigationItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val activeColor = destination.accentColor
    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "NavBgColor"
    )

    val animatedIconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "NavIconScale"
    )

    val animatedIndicatorHeight by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "NavIndicatorHeight"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(animatedBgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag(destination.testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Glow Halo behind active icon
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.spacing.extraLarge)
                            .background(
                                Brush.radialGradient(
                                    listOf(activeColor.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                }

                Icon(
                    imageVector = destination.icon,
                    contentDescription = destination.title,
                    tint = if (isSelected) activeColor else TextSecondary,
                    modifier = Modifier
                        .size(22.dp)
                        .scale(animatedIconScale)
                )

                // Mini Badge for Pending Tasks on Productivity tab
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 10.dp, y = (-8).dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(LumiYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 9) "9+" else "$badgeCount",
                            color = ObsidianDark,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = destination.title,
                color = if (isSelected) activeColor else TextTertiary,
                fontSize = if (isSelected) 10.5.sp else 9.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Active Underline Indicator
            Box(
                modifier = Modifier
                    .width(MaterialTheme.spacing.medium)
                    .height(animatedIndicatorHeight)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else Color.Transparent)
            )
        }
    }
}
