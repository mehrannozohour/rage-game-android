package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Permanent On-Screen Tactile Joystick + Action Buttons for "Rage" mobile gameplay.
 * The virtual joystick is permanently on-screen without requiring selection.
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
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Left Side: Permanent Tactile Analog Joystick
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
    val maxRadiusPx = 110f
    var thumbOffsetX by remember { mutableFloatStateOf(0f) }
    var thumbOffsetY by remember { mutableFloatStateOf(0f) }
    var hasTriggeredDirection by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(136.dp)
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

                        // Trigger actions based on stick deflection
                        if (dist > 45f && !hasTriggeredDirection) {
                            if (newY < -35f) {
                                onJump()
                                hasTriggeredDirection = true
                            } else if (newY > 35f) {
                                onSlide()
                                hasTriggeredDirection = true
                            } else if (newX > 35f) {
                                onDash()
                                hasTriggeredDirection = true
                            }
                        } else if (dist < 25f) {
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
                .height(60.dp)
                .background(Color(0x3300F5D4))
        )
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(2.dp)
                .background(Color(0x3300F5D4))
        )

        // Direction indicators
        Text(
            text = "▲",
            color = Color(0x8800F5D4),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        )
        Text(
            text = "▼",
            color = Color(0x8800F5D4),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
        Text(
            text = "▶",
            color = Color(0x8800F5D4),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
        )

        // Joystick Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetX.roundToInt(), thumbOffsetY.roundToInt()) }
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00F5D4), Color(0xFF00BBF9))
                    )
                )
                .border(2.dp, Color.White, CircleShape)
                .shadow(8.dp, CircleShape)
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
        GameActionButton(
            title = "شتاب",
            subTitle = "DASH",
            color = Color(0xFFFF2A5F),
            sizeDp = 58,
            onClick = onDash,
            testTag = "action_dash_button"
        )

        // Slide Button
        GameActionButton(
            title = "تکل",
            subTitle = "SLIDE",
            color = Color(0xFFFEE440),
            sizeDp = 64,
            onClick = onSlide,
            testTag = "action_slide_button"
        )

        // Jump Button (Primary Large)
        GameActionButton(
            title = "پرش",
            subTitle = "JUMP",
            color = Color(0xFF00F5D4),
            sizeDp = 76,
            onClick = onJump,
            testTag = "action_jump_button"
        )
    }
}

@Composable
fun GameActionButton(
    title: String,
    subTitle: String,
    color: Color,
    sizeDp: Int,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick()
        }
    }

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
            .shadow(if (isPressed) 12.dp else 4.dp, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onClick() },
                    onDrag = { change, _ -> change.consume() }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            interactionSource = interactionSource,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = if (isPressed) Color(0xFF05060D) else color
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = (sizeDp * 0.22f).sp,
                    fontWeight = FontWeight.Black,
                    color = if (isPressed) Color.White else color
                )
                Text(
                    text = subTitle,
                    fontSize = (sizeDp * 0.14f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed) Color.White else Color(0xBBF8FAFC)
                )
            }
        }
    }
}
