package com.example.presentation.screens.lifehub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.LumiDialog
import com.example.core.theme.*

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, desc: String, category: String, priority: String, estimated: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("Medium") }
    var estimatedMinutes by remember { mutableIntStateOf(25) }

    val categories = listOf("Work", "Personal", "Health", "Study", "Creative")
    val priorities = listOf("High", "Medium", "Low")

    LumiDialog(
        onDismissRequest = onDismiss,
        title = "Add Action Task",
        subtitle = "Earn +15 XP upon completion",
        icon = Icons.Default.TaskAlt,
        accentColor = LumiGold,
        confirmButtonText = "Add Task",
        isConfirmEnabled = title.isNotBlank(),
        onConfirm = {
            if (title.isNotBlank()) {
                onAddTask(title, "", category, priority, estimatedMinutes)
            }
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.text_task_description), color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiGold,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "Category", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    Surface(
                        color = if (isSelected) LumiGold else SurfaceDarkVariant,
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

            Text(text = "Priority Level", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priorities.forEach { prio ->
                    val isSelected = priority == prio
                    val prioColor = when (prio) {
                        "High" -> LumiCoral
                        "Medium" -> LumiGold
                        else -> LumiMint
                    }
                    Surface(
                        color = if (isSelected) prioColor else SurfaceDarkVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable { priority = prio }
                    ) {
                        Text(
                            text = prio,
                            color = if (isSelected) ObsidianDark else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
