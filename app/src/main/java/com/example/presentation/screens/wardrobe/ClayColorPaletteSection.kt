package com.example.presentation.screens.wardrobe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.domain.model.BloubSkinColor
import com.example.presentation.components.LumiCard

@Composable
fun ClayColorPaletteSection(
    currentSkin: BloubSkinColor,
    onSelectSkin: (BloubSkinColor) -> Unit,
    modifier: Modifier = Modifier
) {
    LumiCard(
        borderColor = LumiPink.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.text_clay_skin_palettes),
            color = LumiPink,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = BloubSkinColor.entries,
                key = { it.name }
            ) { skin ->
                val isSelected = currentSkin == skin ||
                    currentSkin.name.equals(skin.name, ignoreCase = true) ||
                    currentSkin.id.equals(skin.id, ignoreCase = true)
                Surface(
                    onClick = { onSelectSkin(skin) },
                    color = if (isSelected) Color(skin.primaryHex).copy(alpha = 0.25f) else SurfaceDarkVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(1.5.dp, Color(skin.primaryHex)) else null,
                    modifier = Modifier.testTag("skin_${skin.name}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = MaterialTheme.spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(skin.primaryHex),
                                            Color(skin.endHex)
                                        )
                                    ),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = skin.displayName,
                            color = if (isSelected) Color(skin.primaryHex) else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
