package com.example.presentation.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*

@Composable
fun NameAndGoalStep(
    initialName: String,
    initialGoal: String,
    onNext: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var goal by remember(initialGoal) { mutableStateOf(initialGoal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = stringResource(R.string.text_final_touches),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.text_tell_lumi_a_bit_about_yourself),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.text_your_name), color = TextSecondary) },
            textStyle = MaterialTheme.typography.titleMedium.copy(color = TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LumiCyan,
                unfocusedBorderColor = SurfaceHighlight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text(stringResource(id = R.string.text_primary_focus_goal), color = TextSecondary) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LumiCyan,
                unfocusedBorderColor = SurfaceHighlight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onNext(name, goal) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.text_scan_hardware_models), style = MaterialTheme.typography.titleMedium, color = ObsidianDark, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ObsidianDark)
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}
