package com.example.ui.overlay.components

import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Tactical Drag Handle Pill positioned under the floating Lumi avatar to easily reposition the overlay.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OverlayPetDragHandle(
    isDragging: Boolean,
    onDragStart: (Float, Float) -> Unit,
    onDragMove: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDragging) LumiCyan.copy(alpha = 0.35f) else ObsidianDark.copy(alpha = 0.88f),
        border = BorderStroke(
            1.dp,
            if (isDragging) LumiCyan else LumiCyan.copy(alpha = 0.35f)
        ),
        shadowElevation = if (isDragging) 8.dp else 3.dp,
        modifier = modifier
            .padding(top = 1.dp)
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        onDragStart(motionEvent.rawX, motionEvent.rawY)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        onDragMove(motionEvent.rawX, motionEvent.rawY)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        onDragEnd()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = "Reposition Handle",
                tint = if (isDragging) LumiCyan else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (isDragging) "Moving" else "Drag",
                color = if (isDragging) TextPrimary else TextSecondary,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Top Grab Bar positioned at the header of the expanded Companion Hub card.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OverlayCardDragBar(
    onDragStart: (Float, Float) -> Unit,
    onDragMove: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        onDragStart(motionEvent.rawX, motionEvent.rawY)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        onDragMove(motionEvent.rawX, motionEvent.rawY)
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        onDragEnd()
                        true
                    }
                    else -> false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceHighlight.copy(alpha = 0.8f))
                .padding(horizontal = 14.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LumiCyan.copy(alpha = 0.7f))
            )
        }
    }
}

