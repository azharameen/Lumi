package com.example.presentation.screens.wardrobe

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.domain.model.BloubShape
import com.example.presentation.components.LumiCard

@Composable
fun MorphingShapeSection(
    currentShape: BloubShape,
    onSelectShape: (BloubShape) -> Unit,
    modifier: Modifier = Modifier
) {
    LumiCard(
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.text_3d_morphing_shape),
            color = MaterialTheme.colorScheme.primary,
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
                items = BloubShape.entries,
                key = { it.name }
            ) { shape ->
                val isSelected = currentShape == shape ||
                    currentShape.name.equals(shape.name, ignoreCase = true) ||
                    currentShape.id.equals(shape.id, ignoreCase = true)
                Surface(
                    onClick = { onSelectShape(shape) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceDarkVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.testTag("shape_${shape.name}")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = shape.iconEmoji, style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = shape.displayName,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else TextPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
