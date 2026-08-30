package com.example.presentation.screens.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.LumiDialog
import com.example.core.theme.*

@Composable
fun AddFactDialog(
    onDismiss: () -> Unit,
    onAddFact: (category: String, factText: String, isPinned: Boolean) -> Unit
) {
    var category by remember { mutableStateOf("Work & Code") }
    var factText by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    val categories = listOf("Work & Code", "Preferences", "Health & Routines", "Routines", "Personal")

    LumiDialog(
        onDismissRequest = onDismiss,
        title = "Teach Lumi a Fact",
        subtitle = "Lumi remembers this context across conversations & tools",
        icon = Icons.Default.Psychology,
        accentColor = LumiCyan,
        confirmButtonText = "Save Memory",
        isConfirmEnabled = factText.isNotBlank(),
        onConfirm = {
            if (factText.isNotBlank()) {
                onAddFact(category, factText, isPinned)
            }
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Category selector
            Text(stringResource(id = R.string.text_category), color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    Surface(
                        color = if (isSelected) LumiCyan else SurfaceDarkVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { category = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) ObsidianDark else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = factText,
                onValueChange = { factText = it },
                label = { Text(stringResource(id = R.string.text_what_should_lumi_know_about_yo), color = TextSecondary) },
                placeholder = { Text(stringResource(id = R.string.text_e_g_i_prefer_vegetarian_meals_), color = TextTertiary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 4
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isPinned = !isPinned }
            ) {
                Icon(
                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                    contentDescription = null,
                    tint = if (isPinned) LumiGold else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(id = R.string.text_pin_as_high_priority_core_cont), color = TextPrimary, fontSize = 12.sp)
            }
        }
    }
}
