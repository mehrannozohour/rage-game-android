package com.example.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Game HUD, Sarcastic Rage Death Overlay Banner, and Win Celebration Dialog.
 */
@Composable
fun GameHud(
    uiState: GameUiState,
    stageTitle: String,
    onOpenMenu: () -> Unit,
    onRestartStage: () -> Unit,
    onToggleBgm: () -> Unit,
    onToggleSfx: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side: Navigation / Quick Tools
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xCC090B16), RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color(0xFF00F5D4), RoundedCornerShape(10.dp))
                    .testTag("hud_menu_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "منو اصلی", tint = Color(0xFF00F5D4))
            }

            IconButton(
                onClick = onRestartStage,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xCC090B16), RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color(0xFFFEE440), RoundedCornerShape(10.dp))
                    .testTag("hud_restart_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "شروع مجدد", tint = Color(0xFFFEE440))
            }

            // BGM Music Loop Toggle
            IconButton(
                onClick = onToggleBgm,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xCC090B16), RoundedCornerShape(10.dp))
                    .border(1.5.dp, if (uiState.bgmEnabled) Color(0xFF00F5D4) else Color(0xFFFF2A5F), RoundedCornerShape(10.dp))
                    .testTag("hud_bgm_button")
            ) {
                Icon(
                    if (uiState.bgmEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    contentDescription = "موسیقی پس‌زمینه",
                    tint = if (uiState.bgmEnabled) Color(0xFF00F5D4) else Color(0xFFFF2A5F)
                )
            }

            // SFX Sound Effects Toggle
            IconButton(
                onClick = onToggleSfx,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xCC090B16), RoundedCornerShape(10.dp))
                    .border(1.5.dp, if (uiState.sfxEnabled) Color(0xFFFEE440) else Color(0xFFFF2A5F), RoundedCornerShape(10.dp))
                    .testTag("hud_sfx_button")
            ) {
                Icon(
                    if (uiState.sfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "جلوه‌های صوتی",
                    tint = if (uiState.sfxEnabled) Color(0xFFFEE440) else Color(0xFFFF2A5F)
                )
            }
        }

        // Center / Right Side: Minimal Stats Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage Indicator
            Box(
                modifier = Modifier
                    .background(Color(0xDD000000), RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color(0xFF00F5D4), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "مرحله ${GameData.toPersianDigits(uiState.stageIdx + 1)} / ۱۰",
                    color = Color(0xFF00F5D4),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Deaths Counter
            Box(
                modifier = Modifier
                    .background(Color(0xDD000000), RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color(0xFFFF2A5F), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "💀 ${GameData.toPersianDigits(uiState.totalDeaths)}",
                    color = Color(0xFFFF2A5F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Persian Sarcastic Rage Banner shown when player dies
 */
@Composable
fun DeathRageOverlay(
    details: DeathDetails?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (details == null) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x9905060D))
            .clickable { onRetry() }
            .testTag("death_overlay_container"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xF5090B16))
                .border(2.5.dp, Color(0xFFFF2A5F), RoundedCornerShape(18.dp))
                .shadow(24.dp, RoundedCornerShape(18.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0x33FF2A5F), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0x66FF2A5F), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "💀 بار ${GameData.toPersianDigits(details.deathsInStage)} تو این مرحله!",
                        color = Color(0xFFFF758F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(Color(0x33FEE440), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0x66FEE440), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🧠 تخمین IQ: ${GameData.toPersianDigits(details.fakeIQ)}",
                        color = Color(0xFFFEE440),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Emoji
            Text(
                text = details.emoji,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = details.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF2A5F),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = details.desc,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF8FAFC),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Roast Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x664A0413), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0x88FF2A5F), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = details.subRoast,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFEE440),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Retry Button
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2A5F),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("death_retry_button")
            ) {
                Text(
                    text = "⚡ لمس کنید تا دوباره بسوزید!",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Stage Victory Celebration Modal
 */
@Composable
fun StageWinOverlay(
    stageNum: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x8805060D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xF5090B16))
                .border(2.5.dp, Color(0xFF00F5D4), RoundedCornerShape(18.dp))
                .shadow(24.dp, RoundedCornerShape(18.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎉🏆", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "مرحله ${GameData.toPersianDigits(stageNum)} با موفقیت فتح شد!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00F5D4),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "آماده مرحله بعدی با تله‌های شیطانی‌تر باشید...",
                fontSize = 13.sp,
                color = Color(0xFFF8FAFC),
                textAlign = TextAlign.Center
            )
        }
    }
}
