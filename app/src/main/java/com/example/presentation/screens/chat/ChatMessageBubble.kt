package com.example.presentation.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.data.local.entity.ChatMessageEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    onCopyMessage: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onImageClick: (String) -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var isLiked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            val isGemmaOnDevice = message.content.contains("[Gemma On-Device]") || message.petEmotion == "OFFLINE"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 5.dp, start = MaterialTheme.spacing.extraSmall)
            ) {
                Surface(
                    color = LumiCyan.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "✨", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stringResource(R.string.text_lumi),
                    color = LumiCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = LumiCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = message.petEmotion,
                        color = LumiCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = if (isGemmaOnDevice) LumiGreen.copy(alpha = 0.15f) else LumiViolet.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isGemmaOnDevice) "⚡ Gemma Local" else "☁️ Gemini 2.5",
                        color = if (isGemmaOnDevice) LumiGreen else LumiViolet,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Message Surface
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else SurfaceDarkVariant,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            border = if (isUser) null else BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.6f)),
            shadowElevation = 4.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                if (message.imageBase64OrUri != null) {
                    Surface(
                        color = ObsidianDark.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onImageClick(message.imageBase64OrUri) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (isUser) ObsidianDark else LumiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📷 Vision Attachment",
                                color = if (isUser) ObsidianDark else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.content,
                    color = if (isUser) ObsidianDark else TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                if (message.toolUsedName != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LumiGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LumiGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Tool Executed: ${message.toolUsedName}",
                                    color = LumiGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!message.toolResultJson.isNullOrBlank()) {
                                    Text(
                                        text = message.toolResultJson,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar & Timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                color = TextTertiary,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.text_copy_message),
                tint = TextTertiary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable {
                        haptics.performSuccess()
                        onCopyMessage(message.content)
                    }
            )

            if (!isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(R.string.desc_read_aloud),
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            haptics.performTick()
                            onSpeakMessage(message.content)
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) LumiPink else TextTertiary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            isLiked = !isLiked
                            haptics.performTick()
                        }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.text_delete_message),
                tint = TextTertiary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable {
                        haptics.performTick()
                        onDeleteMessage(message.id)
                    }
            )
        }
    }
}
