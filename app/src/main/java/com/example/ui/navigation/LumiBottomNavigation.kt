package com.example.ui.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Modern Material 3 Navigation Bar for navigating Lumi's 3 primary core tabs:
 * Companion (Pet & Sanctuary), Assistant (Gemini AI Brain), and Life Hub (Productivity & Wellness).
 */
@Composable
fun LumiBottomNavigation(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = SurfaceDark,
        contentColor = TextPrimary,
        tonalElevation = 10.dp,
        modifier = modifier.height(76.dp)
    ) {
        NavDestination.allDestinations.forEach { destination ->
            val isSelected = selectedTabIndex == destination.tabIndex
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(destination.tabIndex) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ObsidianDark,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = destination.accentColor,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = destination.accentColor
                ),
                modifier = Modifier.testTag(destination.testTag)
            )
        }
    }
}

