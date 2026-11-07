package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Main Menu Screen for "Rage" game.
 * Direct start button, on-screen joystick explanation, slow-mo assist, BGM and SFX toggles, and help guide.
 */
@Composable
fun MainMenuScreen(
    uiState: GameUiState,
    onStartGame: () -> Unit,
    onOpenHelp: () -> Unit,
    onToggleBgm: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleSlowMo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF070914), Color(0xFF05060D), Color(0xFF090B16))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Neon Logo Header - RAGE
            Text(
                text = "⚡ RAGE ⚡",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00F5D4),
                letterSpacing = 4.sp
            )

            Text(
                text = "کابوس غیرقابل پیش‌بینی پشت‌بام",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF2A5F),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Primary Play Button (Direct Entry to Game)
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00F5D4),
                    contentColor = Color(0xFF05060D)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .testTag("menu_play_button")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF05060D), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "شروع بازی",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game Settings & Info Row (Sound, Slow-Mo Assist, Help)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xDD090B16), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x4400F5D4), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info Badge: Joystick Controls
                Text(
                    text = "🕹️ جوی‌استیک لمسی روی صفحه فعال است",
                    color = Color(0xFF00F5D4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slow-Mo Toggle
                    IconButton(
                        onClick = onToggleSlowMo,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (uiState.slowMoEnabled) Color(0x3300F5D4) else Color(0x221E293B),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (uiState.slowMoEnabled) Color(0xFF00F5D4) else Color(0x4494A3B8),
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("menu_slowmo_button")
                    ) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = "حرکت آهسته",
                            tint = if (uiState.slowMoEnabled) Color(0xFF00F5D4) else Color(0xFF94A3B8)
                        )
                    }

                    // BGM Music Loop Toggle
                    IconButton(
                        onClick = onToggleBgm,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (uiState.bgmEnabled) Color(0x3300F5D4) else Color(0x221E293B),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (uiState.bgmEnabled) Color(0xFF00F5D4) else Color(0x4494A3B8),
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("menu_bgm_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.bgmEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "موسیقی پس‌زمینه (BGM)",
                            tint = if (uiState.bgmEnabled) Color(0xFF00F5D4) else Color(0xFFFF2A5F)
                        )
                    }

                    // SFX Sound Effects Toggle
                    IconButton(
                        onClick = onToggleSfx,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (uiState.sfxEnabled) Color(0x33FEE440) else Color(0x221E293B),
                                RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                if (uiState.sfxEnabled) Color(0xFFFEE440) else Color(0x4494A3B8),
                                RoundedCornerShape(10.dp)
                            )
                            .testTag("menu_sfx_toggle")
                    ) {
                        Icon(
                            imageVector = if (uiState.sfxEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "جلوه‌های صوتی (SFX)",
                            tint = if (uiState.sfxEnabled) Color(0xFFFEE440) else Color(0xFFFF2A5F)
                        )
                    }

                    // Help Button
                    IconButton(
                        onClick = onOpenHelp,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0x221E293B), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0x4494A3B8), RoundedCornerShape(10.dp))
                            .testTag("menu_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "راهنما",
                            tint = Color(0xFFFEE440)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stats
            Text(
                text = "💀 مجموع کل سوختن‌ها: ${GameData.toPersianDigits(uiState.totalDeaths)}",
                color = Color(0xAA94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Help & Controls Guide Screen
 */
@Composable
fun HelpControlsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05060D))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xCC090B16), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF00F5D4), RoundedCornerShape(10.dp))
                        .testTag("help_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Color(0xFF00F5D4))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "راهنمای بازی و تله‌های غیرقابل پیش‌بینی",
                    color = Color(0xFF00F5D4),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xF5090B16), RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0x3300F5D4), RoundedCornerShape(14.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🕹️ کنترل‌های لمسی روی صفحه:",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFF00F5D4)
                )
                Text(
                    text = "• جوی‌استیک چپ (Joystick): بالا = پرش / دابل جامپ | پایین = تکل و سر خوردن | راست = شتاب سرعتی",
                    fontSize = 12.sp,
                    color = Color(0xFFF8FAFC)
                )
                Text(
                    text = "• دکمه‌های سمت راست: دکمه بزرگ پرش (JUMP) | دکمه زرد تکل (SLIDE) | دکمه قرمز شتاب (DASH)",
                    fontSize = 12.sp,
                    color = Color(0xFFF8FAFC)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "⚠️ تله‌های شیطانی پشت‌بام:",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFFFF2A5F)
                )
                Text(
                    text = "• لیزرهای آسانسوری: در آخرین لحظه ارتفاعشان تغییر می‌کند! با تکل یا دابل جامپ به موقع فرار کنید.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "• جعبه‌های خائن و دونده: به محض نزدیک شدن تیغ درمی‌آورند یا به سمتتان می‌دوند!",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "• پل‌های ریزشی: زیر پایتان خالی می‌شوند و باید با دابل جامپ از رویشان عبور کنید.",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "• دروازه خروج فراری: وقتی به انتهای مرحله می‌رسید، دروازه فرار می‌کند و لیزر مخفی ظاهر می‌شود!",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
