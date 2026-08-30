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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Modern Material 3 Navigation Bar for navigating Lumi's 5 core tabs:
 * Companion, Assistant, Productivity, Wellness, and Account.
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
        modifier = modifier.height(72.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

