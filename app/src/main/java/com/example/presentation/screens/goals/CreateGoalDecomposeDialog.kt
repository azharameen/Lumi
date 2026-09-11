package com.example.presentation.screens.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.presentation.components.LumiDialog

@Composable
fun CreateGoalDecomposeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, targetDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Productivity") }
    var targetDate by remember { mutableStateOf("") }

    val categories = listOf("Productivity", "Engineering", "Health", "Learning", "Creative")

    LumiDialog(
        onDismissRequest = onDismiss,
        title = "Decompose Goal Plan",
        subtitle = "Lumi's agentic swarm will construct phases and assign tools",
        icon = Icons.Default.AutoAwesome,
        accentColor = LumiCyan,
        confirmButtonText = "Decompose with Lumi",
        isConfirmEnabled = title.isNotBlank(),
        onConfirm = {
            if (title.isNotBlank()) {
                onConfirm(title, description, selectedCategory, targetDate)
            }
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.text_goal_title_e_g_launch_mobile_a), color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_goal_title")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(id = R.string.text_key_outcomes_notes_optional), color = TextSecondary) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.text_category_domain),
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) LumiCyan else SurfaceDarkVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) ObsidianDark else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                        )
                    }
                }
            }
        }
    }
}
