package com.example.presentation.components
import androidx.compose.ui.res.stringResource
import com.example.R


import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraVisionDialog(
    onDismiss: () -> Unit,
    onImageCaptured: (Bitmap, String) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = ObsidianDark,
            shape = RoundedCornerShape(24.dp)
        ) {
            if (cameraPermissionState.status.isGranted) {
                CameraPreviewContent(
                    onDismiss = onDismiss,
                    onCapture = onImageCaptured
                )
            } else {
                CameraPermissionRequestView(
                    onRequest = { cameraPermissionState.launchPermissionRequest() },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionRequestView(
    onRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(LumiViolet.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = stringResource(id = R.string.desc_camera_permission),
                tint = LumiCyan,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.text_let_lumi_see_your_world),
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.text_lumi_uses_camera_vision_to_examine),
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequest,
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan, contentColor = ObsidianDark),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(id = R.string.text_grant_camera_access), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary)
        ) {
            Text(stringResource(id = R.string.text_cancel))
        }
    }
}

@Composable
private fun CameraPreviewContent(
    onDismiss: () -> Unit,
    onCapture: (Bitmap, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    var selectedPromptPreset by remember { mutableStateOf("Analyze what you see and give empathetic advice") }

    val presets = listOf(
        "Analyze what you see and give empathetic advice",
        "Check my workspace and give focus tips",
        "Inspect this meal and give wellness nutrition feedback",
        "Help explain these study notes or document"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = ObsidianDark.copy(alpha = 0.75f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LumiCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(id = R.string.text_lumi_multimodal_vision), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .background(ObsidianDark.copy(alpha = 0.75f), CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.desc_close), tint = Color.White)
            }
        }

        // Viewfinder Center Overlay with Cyber Corner Brackets
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.Center)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val cornerLength = 28.dp.toPx()
                val w = size.width
                val h = size.height

                // Top-Left corner
                drawLine(LumiCyan, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
                drawLine(LumiCyan, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)

                // Top-Right corner
                drawLine(LumiCyan, Offset(w, 0f), Offset(w - cornerLength, 0f), strokeWidth)
                drawLine(LumiCyan, Offset(w, 0f), Offset(w, cornerLength), strokeWidth)

                // Bottom-Left corner
                drawLine(LumiCyan, Offset(0f, h), Offset(cornerLength, h), strokeWidth)
                drawLine(LumiCyan, Offset(0f, h), Offset(0f, h - cornerLength), strokeWidth)

                // Bottom-Right corner
                drawLine(LumiCyan, Offset(w, h), Offset(w - cornerLength, h), strokeWidth)
                drawLine(LumiCyan, Offset(w, h), Offset(w, h - cornerLength), strokeWidth)
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(ObsidianDark.copy(alpha = 0.85f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Preset prompt selector
            Text(
                text = stringResource(R.string.text_what_should_lumi_analyze),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                presets.take(3).forEach { prompt ->
                    val isSelected = selectedPromptPreset == prompt
                    val label = when {
                        prompt.contains("workspace") -> "Workspace"
                        prompt.contains("meal") -> "Nutrition"
                        prompt.contains("notes") -> "Study Notes"
                        else -> "General"
                    }
                    Surface(
                        color = if (isSelected) LumiCyan.copy(alpha = 0.25f) else SurfaceDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedPromptPreset = prompt }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) LumiCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capture button
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(LumiCyan)
                    .clickable {
                        imageCapture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val bitmap = imageProxyToBitmap(image)
                                    image.close()
                                    onCapture(bitmap, selectedPromptPreset)
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    // Fallback sample bitmap
                                    val dummy = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
                                    onCapture(dummy, selectedPromptPreset)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(id = R.string.desc_capture),
                    tint = ObsidianDark,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val planeProxy = image.planes[0]
    val buffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val rotation = image.imageInfo.rotationDegrees
    return if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}
