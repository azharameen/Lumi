package com.example.presentation.screens.lifehub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (title: String, desc: String, startOffset: Int, duration: Int, location: String, color: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startOffsetHours by remember { mutableIntStateOf(1) }
    var durationHours by remember { mutableIntStateOf(1) }
    var location by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#00E5FF") }

    val colors = listOf("#00E5FF", "#FF4081", "#FFD700", "#00E676", "#7C4DFF")

    LumiDialog(
        onDismissRequest = onDismiss,
        title = "Schedule New Event",
        subtitle = "Synchronized to Lumi's autonomous calendar",
        icon = Icons.Default.CalendarMonth,
        accentColor = LumiCyan,
        confirmButtonText = "Schedule & Alarm",
        isConfirmEnabled = title.isNotBlank(),
        onConfirm = {
            if (title.isNotBlank()) {
                onAddEvent(title, desc, startOffsetHours, durationHours, location, selectedColor)
            }
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.text_event_title), color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(stringResource(id = R.string.text_location_link_optional), color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Starts in: +${startOffsetHours}h", color = TextSecondary, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (startOffsetHours > 0) startOffsetHours-- },
                        modifier = Modifier.size(MaterialTheme.spacing.extraLarge).background(SurfaceHighlight, CircleShape)
                    ) {
                        Text(text = "−", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    IconButton(
                        onClick = { startOffsetHours++ },
                        modifier = Modifier.size(MaterialTheme.spacing.extraLarge).background(LumiCyan, CircleShape)
                    ) {
                        Text(text = "+", color = ObsidianDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(text = stringResource(R.string.text_event_color_accent), color = TextSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                colors.forEach { hex ->
                    val isSelected = selectedColor == hex
                    val col = when (hex) {
                        "#00E5FF" -> LumiCyan
                        "#FF4081" -> LumiPink
                        "#FFD700" -> LumiGold
                        "#00E676" -> LumiGreen
                        else -> LumiViolet
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(col, CircleShape)
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = ObsidianDark,
                                modifier = Modifier.size(MaterialTheme.spacing.medium)
                            )
                        }
                    }
                }
            }
        }
    }
}
