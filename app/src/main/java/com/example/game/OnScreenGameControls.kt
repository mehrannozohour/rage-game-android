package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance, zero-lag touch controls for "Rage".
 * Uses instant pointerInput tap detectors rather than nested Button composition to ensure immediate response without frame drops.
 */
@Composable
fun OnScreenGameControls(
    onJump: () -> Unit,
    onSlide: () -> Unit,
    onDash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left Side: Virtual Analog Joystick
        VirtualJoystick(
            onJump = onJump,
            onSlide = onSlide,
            onDash = onDash,
            modifier = Modifier.testTag("virtual_joystick")
        )

        // Right Side: Quick Action Floating Buttons (Jump, Slide / Fall, Dash)
        ActionButtonsPad(
            onJump = onJump,
            onSlide = onSlide,
            onDash = onDash,
            modifier = Modifier.testTag("action_buttons_pad")
        )
    }
}

@Composable
fun VirtualJoystick(
    onJump: () -> Unit,
    onSlide: () -> Unit,
    onDash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val maxRadiusPx = 100f
    var thumbOffsetX by remember { mutableFloatStateOf(0f) }
    var thumbOffsetY by remember { mutableFloatStateOf(0f) }
    var hasTriggeredDirection by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(130.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xCC090B16), Color(0x9905060D))
                )
            )
            .border(2.dp, Color(0xFF00F5D4), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val dx = offset.x - size.width / 2f
                        val dy = offset.y - size.height / 2f
                        val dist = sqrt(dx * dx + dy * dy)
                        val angle = atan2(dy, dx)
                        val clampedDist = dist.coerceAtMost(maxRadiusPx)
                        thumbOffsetX = cos(angle) * clampedDist
                        thumbOffsetY = sin(angle) * clampedDist
                        hasTriggeredDirection = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newX = thumbOffsetX + dragAmount.x
                        val newY = thumbOffsetY + dragAmount.y
                        val dist = sqrt(newX * newX + newY * newY)
                        val angle = atan2(newY, newX)
                        val clampedDist = dist.coerceAtMost(maxRadiusPx)
                        thumbOffsetX = cos(angle) * clampedDist
                        thumbOffsetY = sin(angle) * clampedDist

                        if (dist > 38f && !hasTriggeredDirection) {
                            if (newY < -30f) {
                                onJump()
                                hasTriggeredDirection = true
                            } else if (newY > 30f) {
                                onSlide()
                                hasTriggeredDirection = true
                            } else if (newX > 30f) {
                                onDash()
                                hasTriggeredDirection = true
                            }
                        } else if (dist < 20f) {
                            hasTriggeredDirection = false
                        }
                    },
                    onDragEnd = {
                        thumbOffsetX = 0f
                        thumbOffsetY = 0f
                        hasTriggeredDirection = false
                    },
                    onDragCancel = {
                        thumbOffsetX = 0f
                        thumbOffsetY = 0f
                        hasTriggeredDirection = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Cross guide lines
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(56.dp)
                .background(Color(0x3300F5D4))
        )
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(2.dp)
                .background(Color(0x3300F5D4))
        )

        // Direction indicators
        Text(
            text = "▲",
            color = Color(0x8800F5D4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
        Text(
            text = "▼",
            color = Color(0x8800F5D4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
        Text(
            text = "▶",
            color = Color(0x8800F5D4),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
        )

        // Joystick Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetX.roundToInt(), thumbOffsetY.roundToInt()) }
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00F5D4), Color(0xFF00BBF9))
                    )
                )
                .border(2.dp, Color.White, CircleShape)
                .shadow(6.dp, CircleShape)
        )
    }
}

@Composable
fun ActionButtonsPad(
    onJump: () -> Unit,
    onSlide: () -> Unit,
    onDash: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Dash Button
        GameFastActionButton(
            title = "DASH",
            subTitle = "شتاب",
            color = Color(0xFFFF2A5F),
            sizeDp = 62,
            onClick = onDash,
            testTag = "action_dash_button"
        )

        // Slide Button
        GameFastActionButton(
            title = "SLIDE",
            subTitle = "تکل",
            color = Color(0xFFFEE440),
            sizeDp = 68,
            onClick = onSlide,
            testTag = "action_slide_button"
        )

        // Jump Button (Primary Large)
        GameFastActionButton(
            title = "JUMP",
            subTitle = "پرش",
            color = Color(0xFF00F5D4),
            sizeDp = 80,
            onClick = onJump,
            testTag = "action_jump_button"
        )
    }
}

/**
 * Instant Zero-Lag Touch Button Component.
 * Responds immediately on pointer down without gesture conflict or UI thread delay.
 */
@Composable
fun GameFastActionButton(
    title: String,
    subTitle: String,
    color: Color,
    sizeDp: Int,
    onClick: () -> Unit,
    testTag: String
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = if (isPressed) listOf(color, Color(0xFF090B16))
                    else listOf(Color(0xDD090B16), Color(0xBB05060D))
                )
            )
            .border(2.5.dp, color, CircleShape)
            .shadow(if (isPressed) 10.dp else 4.dp, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onClick()
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = title,
                fontSize = (sizeDp * 0.23f).sp,
                fontWeight = FontWeight.Black,
                color = if (isPressed) Color.White else color,
                textAlign = TextAlign.Center
            )
            Text(
                text = subTitle,
                fontSize = (sizeDp * 0.16f).sp,
                fontWeight = FontWeight.Bold,
                color = if (isPressed) Color.White else Color(0xFFE2E8F0),
                textAlign = TextAlign.Center
            )
        }
    }
}
