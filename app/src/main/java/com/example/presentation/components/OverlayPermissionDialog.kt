package com.example.presentation.components
import com.example.R

import androidx.compose.ui.res.stringResource

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.LumiCyan
import com.example.core.theme.TextSecondary

@Composable
fun OverlayPermissionDialog(
    onDismiss: () -> Unit,
    onGranted: () -> Unit
) {
    val context = LocalContext.current

    LumiDialog(
        onDismissRequest = onDismiss,
        title = "Floating Companion Mode",
        subtitle = "Allow Lumi to live & roam over your screen",
        icon = Icons.Default.Visibility,
        accentColor = LumiCyan,
        confirmButtonText = "Grant Permission",
        onConfirm = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            } else {
                onGranted()
            }
        },
        onDismiss = onDismiss
    ) {
        Column {
            Text(
                text = stringResource(R.string.text_allow_lumi_to_float_over_other),
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "• Tap 'Grant Permission'\n• Toggle 'Allow display over other apps'\n• Return here to activate floating companion!",
                color = LumiCyan,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
