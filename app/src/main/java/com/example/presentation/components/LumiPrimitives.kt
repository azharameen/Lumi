package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

/**
 * Standard Lumi Glassmorphic Card Surface.
 * Consistent background, rounded corners (18dp default), and sleek glowing border.
 */
@Composable
fun LumiCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceDark.copy(alpha = 0.90f),
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    shadowElevation: Dp = MaterialTheme.spacing.extraSmall,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val appliedBorder = when {
        borderBrush != null -> BorderStroke(borderWidth, borderBrush)
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        else -> BorderStroke(borderWidth, SurfaceHighlight.copy(alpha = 0.6f))
    }

    Surface(
        color = backgroundColor,
        shape = shape,
        border = appliedBorder,
        shadowElevation = shadowElevation,
        modifier = modifier.then(
            if (onClick != null) Modifier.clip(shape).clickable { onClick() } else Modifier
        )
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium),
            content = content
        )
    }
}

/**
 * Unified Section Header with icon, title, optional subtitle, and trailing action button.
 */
@Composable
fun LumiSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (icon != null) {
                Surface(
                    color = accentColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (actionText != null && onActionClick != null) {
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onActionClick() }
            ) {
                Text(
                    text = actionText,
                    color = accentColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Reusable Status/Category Badge (e.g., Priority, Tags, Task Category).
 */
@Composable
fun LumiBadge(
    text: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    textColor: Color = accentColor,
    isPill: Boolean = false
) {
    val shape = if (isPill) CircleShape else RoundedCornerShape(MaterialTheme.spacing.small)
    Surface(
        color = accentColor.copy(alpha = 0.18f),
        shape = shape,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 3.5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
            }
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            )
        }
    }
}

/**
 * KPI Stat Tile for displaying high-level metrics (Score, Stats, Days).
 */
@Composable
fun LumiStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subValue: String? = null
) {
    LumiCard(
        modifier = modifier,
        borderColor = accentColor.copy(alpha = 0.3f),
        backgroundColor = SurfaceDark.copy(alpha = 0.90f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Surface(
                color = accentColor.copy(alpha = 0.18f),
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )

        if (subValue != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Standardized Empty State banner with halo icon, title, and subtitle.
 */
@Composable
fun LumiEmptyState(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "EmptyHalo")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloPulse"
    )

    LumiCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = SurfaceDarkVariant.copy(alpha = 0.5f),
        borderColor = SurfaceHighlight.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(accentColor.copy(alpha = 0.15f * pulse), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )

            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                LumiGradientButton(
                    text = actionButtonText,
                    onClick = onActionClick,
                    accentColor = accentColor,
                    modifier = Modifier.height(40.dp)
                )
            }
        }
    }
}

/**
 * Interactive Gradient Button with tactile pill shape and glowing state.
 */
@Composable
fun LumiGradientButton(
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics(),
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = { haptics.performSuccess(); onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = accentColor,
            disabledContainerColor = SurfaceHighlight
        ),
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = ObsidianDark,
                    modifier = Modifier.size(MaterialTheme.spacing.medium)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = ObsidianDark,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Smooth Rounded Multi-Stop Gradient Progress Bar.
 */
@Composable
fun LumiProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = SurfaceHighlight,
    height: Dp = MaterialTheme.spacing.small
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clampedProgress)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            barColor.copy(alpha = 0.8f),
                            barColor,
                            Color.White.copy(alpha = 0.85f)
                        )
                    )
                )
        )
    }
}
