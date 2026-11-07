package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.sin

/**
 * Custom Compose Canvas rendering true 16-bit pixel retro arcade graphics
 * Virtual coordinate system: 384 x 216
 */
@Composable
fun PixelGameCanvas(
    viewModel: VectorRageViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val virtualW = 384f
        val virtualH = 216f
        val scale = minOf(size.width / virtualW, size.height / virtualH)
        val offsetX = (size.width - virtualW * scale) / 2f
        val offsetY = (size.height - virtualH * scale) / 2f

        val shakeOffset = if (viewModel.shake > 0.2f) {
            Offset(
                (Math.random().toFloat() - 0.5f) * viewModel.shake * scale,
                (Math.random().toFloat() - 0.5f) * viewModel.shake * scale
            )
        } else Offset.Zero
        if (viewModel.shake > 0.2f) {
            viewModel.shake *= 0.82f
        }

        fun toScreen(vx: Float, vy: Float): Offset {
            return Offset(
                offsetX + (vx - viewModel.camX) * scale + shakeOffset.x,
                offsetY + vy * scale + shakeOffset.y
            )
        }

        fun drawPRect(vx: Float, vy: Float, vw: Float, vh: Float, color: Color) {
            val start = toScreen(vx, vy)
            drawRect(
                color = color,
                topLeft = start,
                size = Size(vw * scale, vh * scale)
            )
        }

        // Draw letterbox background
        drawRect(color = Color(0xFF05060D), size = size)

        // 1. Banded 16-bit Night Sky
        val skyTop = offsetY + shakeOffset.y
        drawRect(Color(0xFF070914), topLeft = Offset(offsetX, skyTop), size = Size(virtualW * scale, 55f * scale))
        drawRect(Color(0xFF0D1326), topLeft = Offset(offsetX, skyTop + 55f * scale), size = Size(virtualW * scale, 55f * scale))
        drawRect(Color(0xFF141E38), topLeft = Offset(offsetX, skyTop + 110f * scale), size = Size(virtualW * scale, 60f * scale))
        drawRect(Color(0xFF1C294A), topLeft = Offset(offsetX, skyTop + 170f * scale), size = Size(virtualW * scale, 46f * scale))

        // Cyberpunk Moon
        drawPRect(viewModel.camX + 310f, 22f, 18f, 18f, Color(0xFFFEF08A))
        drawPRect(viewModel.camX + 314f, 19f, 10f, 24f, Color(0xFFFEF08A))
        drawPRect(viewModel.camX + 307f, 25f, 24f, 12f, Color(0xFFFEF08A))

        // 2. Parallax Skyscrapers
        val layerColors = listOf(Color(0xFF090D1A), Color(0xFF050811))
        val speeds = listOf(0.15f, 0.35f)
        for (l in 0..1) {
            val buildings = viewModel.bgSkyscrapers.getOrNull(l) ?: continue
            for (b in buildings) {
                var bx = ((b.x - viewModel.camX * speeds[l]) % 1100f)
                if (bx < -60f) bx += 1100f
                val by = virtualH - b.h
                drawPRect(viewModel.camX + bx, by, b.w, b.h, layerColors[l])

                if (b.antenna) {
                    drawPRect(viewModel.camX + bx + (b.w / 2f), by - 12f, 2f, 12f, layerColors[l])
                    if ((viewModel.tick % 30) < 15) {
                        drawPRect(viewModel.camX + bx + (b.w / 2f), by - 14f, 2f, 2f, Color(0xFFFF2A5F))
                    }
                }
                b.neonColor?.let { neon ->
                    drawPRect(viewModel.camX + bx + 3f, by + 10f, b.w - 6f, 3f, Color(neon))
                }
            }
        }

        // 3. Rooftops (Floors)
        for (f in viewModel.currentFloors) {
            val screenX = f.x - viewModel.camX
            if (screenX + f.w < -20f || screenX > virtualW + 20f) continue
            drawPRect(f.x, f.y, f.w, virtualH - f.y + 20f, Color(0xFF04060C))
            // Top Roof Trim Neon Cyan
            drawPRect(f.x, f.y, f.w, 2f, Color(0xFF00BBF9))
            var gx = f.x + 8f
            while (gx < f.x + f.w - 8f) {
                drawPRect(gx, f.y + 5f, 8f, 3f, Color(0xFF0F172A))
                gx += 16f
            }
        }

        // 4. Traps & Obstacles
        for (t in viewModel.currentTraps) {
            val screenX = t.x - viewModel.camX
            if (screenX < -80f || screenX > virtualW + 80f) continue

            when (t.type) {
                TrapType.VAULT_BOX -> {
                    drawPRect(t.x, t.y - t.h, t.w, t.h, Color(0xFF090D1A))
                    drawPRect(t.x, t.y - t.h, t.w, 2f, Color(0xFF00F5D4))
                    drawPRect(t.x, t.y - 2f, t.w, 2f, Color(0xFF00F5D4))
                    drawPRect(t.x, t.y - t.h, 2f, t.h, Color(0xFF00F5D4))
                    drawPRect(t.x + t.w - 2f, t.y - t.h, 2f, t.h, Color(0xFF00F5D4))
                    drawPRect(t.x + 6f, t.y - t.h + 6f, t.w - 12f, t.h - 12f, Color(0xFF00BBF9))
                }
                TrapType.SPIKE_MIMIC_BOX, TrapType.WALKING_BOX -> {
                    val borderCol = if (t.triggered) Color(0xFFFF2A5F) else Color(0xFF00F5D4)
                    drawPRect(t.x, t.y - t.h, t.w, t.h, Color(0xFF090D1A))
                    drawPRect(t.x, t.y - t.h, t.w, 2f, borderCol)
                    drawPRect(t.x, t.y - t.h, 2f, t.h, borderCol)
                    drawPRect(t.x + t.w - 2f, t.y - t.h, 2f, t.h, borderCol)

                    if (t.triggered) {
                        drawPRect(t.x + 4f, t.y - t.h + 5f, 4f, 3f, Color(0xFFFEE440))
                        drawPRect(t.x + t.w - 8f, t.y - t.h + 5f, 4f, 3f, Color(0xFFFEE440))
                        drawPRect(t.x + 5f, t.y - t.h + 6f, 2f, 2f, Color(0xFFFF2A5F))
                        drawPRect(t.x + t.w - 7f, t.y - t.h + 6f, 2f, 2f, Color(0xFFFF2A5F))
                        drawPRect(t.x + 5f, t.y - 7f, t.w - 10f, 4f, Color(0xFF05060D))
                        drawPRect(t.x + 6f, t.y - 7f, 2f, 2f, Color.White)
                        drawPRect(t.x + t.w - 8f, t.y - 7f, 2f, 2f, Color.White)

                        // Spikes
                        var sx = t.x + 2f
                        while (sx < t.x + t.w - 3f) {
                            drawPRect(sx, t.y - t.h - 5f, 3f, 5f, Color(0xFFFF2A5F))
                            drawPRect(sx + 1f, t.y - t.h - 7f, 1f, 2f, Color(0xFFFEE440))
                            sx += 5f
                        }
                        if (t.type == TrapType.WALKING_BOX) {
                            val legOff = if ((viewModel.tick % 6) < 3) 2f else 0f
                            drawPRect(t.x + 4f, t.y, 3f, 4f - legOff, Color(0xFFFF2A5F))
                            drawPRect(t.x + t.w - 7f, t.y, 3f, 2f + legOff, Color(0xFFFF2A5F))
                        }
                    } else {
                        drawPRect(t.x + 6f, t.y - t.h + 6f, t.w - 12f, t.h - 12f, Color(0xFF00BBF9))
                    }
                }
                TrapType.GLASS -> {
                    if (!t.broken) {
                        drawPRect(t.x, t.y - t.h, t.w, t.h, Color(0x5590E0EF))
                        drawPRect(t.x, t.y - t.h, 2f, t.h, Color(0xFF90E0EF))
                        drawPRect(t.x + 2f, t.y - t.h + 8f, 2f, 10f, Color.White)
                    }
                }
                TrapType.FAKE_GLASS -> {
                    if (!t.triggered) {
                        drawPRect(t.x, t.y - t.h, 8f, t.h, Color(0x5590E0EF))
                        drawPRect(t.x, t.y - t.h, 2f, t.h, Color(0xFF90E0EF))
                    } else {
                        drawPRect(t.x, t.y - t.h, t.w, t.h, Color(0xFF334155))
                        drawPRect(t.x, t.y - t.h, t.w, 2f, Color(0xFFF8FAFC))
                        drawPRect(t.x + 2f, t.y - t.h + 5f, t.w - 4f, 3f, Color(0xFFFF2A5F))
                    }
                }
                TrapType.BOOST_PAD -> {
                    drawPRect(t.x, t.y - 3f, t.w, 3f, Color(0xFF00F5D4))
                    drawPRect(t.x + 4f, t.y - 5f, t.w - 8f, 2f, Color(0xFFFEE440))
                }
                TrapType.SUDDEN_BRIDGE -> {
                    val by = t.y + t.dropY
                    drawPRect(t.x, by, t.w, 8f, Color(0xFF1E293B))
                    var hx = t.x
                    while (hx < t.x + t.w) {
                        drawPRect(hx, by, 4f, 3f, Color(0xFFFEE440))
                        hx += 8f
                    }
                }
                TrapType.OVERHANG -> {
                    val topY = t.y - t.h - 14f
                    drawPRect(t.x, topY, t.w, t.h, Color(0xFF05070E))
                    drawPRect(t.x, topY + t.h - 2f, t.w, 2f, Color(0xFFFF2A5F))
                    drawPRect(t.x + 3f, t.y - 14f, 2f, 14f, Color(0xFF1E293B))
                    drawPRect(t.x + t.w - 5f, t.y - 14f, 2f, 14f, Color(0xFF1E293B))
                }
                TrapType.CRUSHER -> {
                    val cy = (t.y - 52f) + t.dropY - 26f
                    drawPRect(t.x + (t.w / 2f) - 2f, 0f, 4f, cy, Color(0xFF1E293B))
                    drawPRect(t.x, cy, t.w, 26f, Color(0xFF090D1A))
                    drawPRect(t.x, cy + 24f, t.w, 2f, Color(0xFFFF2A5F))
                    var sx = t.x + 3f
                    while (sx < t.x + t.w - 3f) {
                        drawPRect(sx, cy + 26f, 3f, 4f, Color(0xFFFEE440))
                        sx += 6f
                    }
                }
                TrapType.LASER, TrapType.ELEVATOR_LASER -> {
                    val mode = if (t.type == TrapType.LASER) t.mode else t.currentMode
                    drawPRect(t.x - 2f, t.y - 46f, 4f, 46f, Color(0xFF1E293B))
                    val droneBob = sin((viewModel.tick * 0.2f).toDouble()).toFloat() * 2f
                    drawPRect(t.x - 6f, t.y - 52f + droneBob, 12f, 6f, Color(0xFF334155))
                    drawPRect(t.x - 2f, t.y - 50f + droneBob, 4f, 3f, Color(0xFFFF2A5F))

                    val beamCol = if ((viewModel.tick % 4) < 2) Color(0xFFFF2A5F) else Color(0xFFFF758F)
                    if (mode == "low") {
                        drawPRect(t.x - 1f, t.y - 22f, 3f, 20f, beamCol)
                        drawPRect(t.x, t.y - 22f, 1f, 20f, Color.White)
                    } else {
                        drawPRect(t.x - 1f, t.y - 44f, 3f, 30f, beamCol)
                        drawPRect(t.x, t.y - 44f, 1f, 30f, Color.White)
                    }
                }
            }

            // Exclamation Box
            if (t.alertTimer > 0f) {
                drawPRect(t.x - 1f, t.y - 64f, 10f, 14f, Color(0xFFFF2A5F))
                drawPRect(t.x, t.y - 63f, 8f, 12f, Color(0xFF090B16))
                drawPRect(t.x + 3f, t.y - 61f, 2f, 6f, Color(0xFFFEE440))
                drawPRect(t.x + 3f, t.y - 53f, 2f, 2f, Color(0xFFFEE440))
            }
        }

        // 5. Exit Freedom Gate
        val ex = viewModel.exitGate.x
        drawPRect(ex, viewModel.exitGate.y - 44f, 18f, 44f, Color(0x4400F5D4))
        drawPRect(ex, viewModel.exitGate.y - 44f, 18f, 3f, Color(0xFF00F5D4))
        drawPRect(ex, viewModel.exitGate.y - 44f, 3f, 44f, Color(0xFF00F5D4))
        drawPRect(ex + 15f, viewModel.exitGate.y - 44f, 3f, 44f, Color(0xFF00F5D4))

        // 6. Draw Hunter Character
        val hx = viewModel.hunter.x
        val hy = viewModel.hunter.y
        val hFrame = (viewModel.hunter.frame.toInt() % 6)
        val hBob = if (hFrame == 1 || hFrame == 4) -2f else if (hFrame == 2 || hFrame == 5) -1f else 0f
        val hTorsoY = hy - 18f + hBob
        val hHeadY = hy - 27f + hBob

        // Shadow
        drawPRect(hx - 6f, hy - 1f, 13f, 2f, Color(0x88000000))
        // Legs
        val stride = listOf(
            Pair(Pair(-5f, -8f), Pair(3f, -8f)),
            Pair(Pair(-3f, -9f), Pair(2f, -7f)),
            Pair(Pair(0f, -8f), Pair(-1f, -8f)),
            Pair(Pair(3f, -8f), Pair(-5f, -8f)),
            Pair(Pair(2f, -7f), Pair(-3f, -9f)),
            Pair(Pair(-1f, -8f), Pair(0f, -8f))
        )[hFrame]
        drawPRect(hx + stride.first.first, hy + stride.first.second, 4f, 6f, Color(0xFF090D16))
        drawPRect(hx + stride.first.first, hy + stride.first.second + 5f, 5f, 3f, Color(0xFFDC2626))
        drawPRect(hx + stride.second.first, hy + stride.second.second, 4f, 6f, Color(0xFF1E293B))
        drawPRect(hx + stride.second.first, hy + stride.second.second + 5f, 5f, 3f, Color(0xFFDC2626))
        // Torso
        drawPRect(hx - 4f, hTorsoY, 9f, 10f, Color(0xFF0F172A))
        drawPRect(hx - 3f, hTorsoY + 1f, 7f, 8f, Color(0xFF1E293B))
        drawPRect(hx + 1f, hTorsoY + 2f, 2f, 6f, Color(0xFFFF2A5F))
        drawPRect(hx - 2f, hTorsoY + 2f, 2f, 2f, Color(0xFFFEE440))
        // Siren
        val sirenCol = if ((viewModel.tick % 8) < 4) Color(0xFFFF2A5F) else Color(0xFF00BBF9)
        drawPRect(hx - 3f, hTorsoY - 1f, 3f, 2f, sirenCol)
        // Head
        drawPRect(hx - 4f, hHeadY, 9f, 9f, Color(0xFF090D16))
        drawPRect(hx - 3f, hHeadY + 1f, 7f, 7f, Color(0xFF1E293B))
        drawPRect(hx, hHeadY + 3f, 5f, 3f, Color(0xFFFF2A5F))
        drawPRect(hx + 2f, hHeadY + 4f, 2f, 1f, Color.White)
        // Stun baton arm
        drawPRect(hx + 2f, hTorsoY + 2f, 5f, 3f, Color(0xFF0F172A))
        drawPRect(hx + 7f, hTorsoY - 2f, 3f, 7f, Color(0xFF1E293B))
        val batonSpark = if ((viewModel.tick % 4) < 2) Color(0xFFFEE440) else Color(0xFF00F5D4)
        drawPRect(hx + 7f, hTorsoY - 4f, 4f, 3f, batonSpark)

        // 7. Draw Hero Character
        val rx = viewModel.runner.x
        val ry = viewModel.runner.y

        if (uiState.isDead) {
            // Skeleton Ragdoll
            val isShock = viewModel.deathTimer > 12f && ((viewModel.tick % 4) < 2)
            val boneCol = if (isShock) Color.White else Color(0xFF00F5D4)
            val outCol = if (isShock) Color(0xFFFF2A5F) else Color(0xFF0F172A)

            drawPRect(rx - 11f, ry - 16f, 22f, 14f, outCol)
            drawPRect(rx - 9f, ry - 14f, 7f, 7f, boneCol)
            drawPRect(rx - 8f, ry - 12f, 2f, 2f, Color(0xFF090B16))
            drawPRect(rx - 4f, ry - 12f, 2f, 2f, Color(0xFF090B16))
            drawPRect(rx - 1f, ry - 12f, 8f, 5f, boneCol)
            drawPRect(rx + 1f, ry - 12f, 1f, 5f, outCol)
            drawPRect(rx + 4f, ry - 12f, 1f, 5f, outCol)
            drawPRect(rx - 7f, ry - 6f, 4f, 3f, boneCol)
            drawPRect(rx + 7f, ry - 14f, 4f, 3f, boneCol)
        } else {
            // Scarf trail
            for (i in viewModel.scarfPixels.indices) {
                val sp = viewModel.scarfPixels[i]
                val waveY = sin((viewModel.tick * 0.3f + i * 0.7f).toDouble()).toFloat() * 1.5f
                val col = if (i % 2 == 0) Color(0xFFFF2A5F) else Color(0xFFBE123C)
                drawPRect(sp.first - i * 1.6f, sp.second + waveY, 4f, 3f, col)
            }

            if (viewModel.runner.pose == PlayerPose.SLIDE) {
                // Slide Pose
                drawPRect(rx - 10f, ry - 2f, 22f, 2f, Color(0x88000000))
                if ((viewModel.tick % 2).toInt() == 0) {
                    drawPRect(rx + 10f, ry - 3f, 3f, 2f, Color(0xFFFEE440))
                    drawPRect(rx - 8f, ry - 2f, 2f, 2f, Color(0xFF00F5D4))
                }
                drawPRect(rx + 2f, ry - 6f, 9f, 4f, Color(0xFF0F172A))
                drawPRect(rx + 10f, ry - 7f, 4f, 5f, Color(0xFFFF2A5F))
                drawPRect(rx + 11f, ry - 5f, 3f, 1f, Color.White)
                drawPRect(rx - 7f, ry - 10f, 11f, 6f, Color(0xFF1E293B))
                drawPRect(rx - 5f, ry - 9f, 7f, 2f, Color(0xFF00BBF9))
                drawPRect(rx - 11f, ry - 17f, 8f, 7f, Color(0xFF1E293B))
                drawPRect(rx - 9f, ry - 16f, 6f, 5f, Color(0xFFFBD38D))
                drawPRect(rx - 6f, ry - 15f, 5f, 2f, Color(0xFF00F5D4))
            } else {
                // Upright Run / Jump / Vault
                val rFrame = (viewModel.runner.frame.toInt() % 6)
                val rBob = if (!viewModel.runner.onGround) 0f else if (rFrame == 1 || rFrame == 4) -2f else if (rFrame == 2 || rFrame == 5) -1f else 0f
                val rHeadY = ry - 27f + rBob
                val rTorsoY = ry - 18f + rBob

                if (viewModel.runner.onGround) {
                    drawPRect(rx - 6f, ry - 1f, 13f, 2f, Color(0x88000000))
                }

                // Back Arm
                val armPhase = if (rFrame < 3) 1f else -1f
                drawPRect(rx - 2f + armPhase * 3f, rTorsoY + 2f, 3f, 6f, Color(0xFF1E293B))
                drawPRect(rx - 2f + armPhase * 4f, rTorsoY + 7f, 3f, 2f, Color(0xFF00F5D4))

                // Legs
                if (!viewModel.runner.onGround) {
                    if (viewModel.runner.pose == PlayerPose.VAULT) {
                        drawPRect(rx - 8f, ry - 13f, 6f, 4f, Color(0xFF0F172A))
                        drawPRect(rx - 10f, ry - 13f, 3f, 4f, Color(0xFFFF2A5F))
                        drawPRect(rx - 5f, ry - 9f, 6f, 4f, Color(0xFF1E293B))
                        drawPRect(rx - 7f, ry - 9f, 3f, 4f, Color(0xFFFF2A5F))
                    } else {
                        drawPRect(rx - 6f, ry - 10f, 4f, 6f, Color(0xFF0F172A))
                        drawPRect(rx - 7f, ry - 5f, 5f, 3f, Color(0xFFFF2A5F))
                        drawPRect(rx + 2f, ry - 12f, 4f, 6f, Color(0xFF1E293B))
                        drawPRect(rx + 3f, ry - 7f, 5f, 3f, Color(0xFFFF2A5F))
                    }
                } else {
                    val rStride = listOf(
                        Pair(Pair(-5f, -8f), Pair(3f, -8f)),
                        Pair(Pair(-3f, -9f), Pair(2f, -7f)),
                        Pair(Pair(0f, -8f), Pair(-1f, -8f)),
                        Pair(Pair(3f, -8f), Pair(-5f, -8f)),
                        Pair(Pair(2f, -7f), Pair(-3f, -9f)),
                        Pair(Pair(-1f, -8f), Pair(0f, -8f))
                    )[rFrame]
                    drawPRect(rx + rStride.first.first, ry + rStride.first.second, 4f, 6f, Color(0xFF090D16))
                    drawPRect(rx + rStride.first.first, ry + rStride.first.second + 5f, 5f, 3f, Color(0xFFFF2A5F))
                    drawPRect(rx + rStride.second.first, ry + rStride.second.second, 4f, 6f, Color(0xFF1E293B))
                    drawPRect(rx + rStride.second.first, ry + rStride.second.second + 5f, 5f, 3f, Color(0xFFFF2A5F))
                    drawPRect(rx + rStride.second.first + 1f, ry + rStride.second.second + 6f, 3f, 1f, Color.White)
                }

                // Torso
                drawPRect(rx - 4f, rTorsoY, 9f, 10f, Color(0xFF090D16))
                drawPRect(rx - 3f, rTorsoY + 1f, 7f, 8f, Color(0xFF1E293B))
                drawPRect(rx + 2f, rTorsoY + 1f, 2f, 7f, Color(0xFF00BBF9))
                drawPRect(rx - 3f, rTorsoY + 8f, 7f, 1f, Color(0xFF090D16))

                // Head
                drawPRect(rx - 4f, rHeadY, 9f, 9f, Color(0xFF090D16))
                drawPRect(rx - 5f, rHeadY, 3f, 2f, Color(0xFF090D16))
                drawPRect(rx - 3f, rHeadY + 2f, 7f, 6f, Color(0xFFFBD38D))
                drawPRect(rx - 4f, rHeadY + 2f, 9f, 2f, Color(0xFFFF2A5F))
                drawPRect(rx, rHeadY + 3f, 5f, 3f, Color(0xFF00F5D4))
                drawPRect(rx + 2f, rHeadY + 3f, 2f, 1f, Color.White)

                // Front Arm
                drawPRect(rx - 1f - armPhase * 3f, rTorsoY + 2f, 3f, 5f, Color(0xFF1E293B))
                drawPRect(rx - 1f - armPhase * 3f, rTorsoY + 6f, 3f, 2f, Color(0xFF00F5D4))
            }
        }

        // Laugh bubble on death
        if (uiState.isDead) {
            val bubbleBob = sin((viewModel.tick * 0.35f).toDouble()).toFloat() * 2f
            val bhy = hy - 38f + bubbleBob
            drawPRect(hx - 14f, bhy - 11f, 36f, 14f, Color(0xFFFEE440))
            drawPRect(hx - 13f, bhy - 10f, 34f, 12f, Color(0xFF090B16))
            drawPRect(hx - 2f, bhy + 3f, 3f, 3f, Color(0xFFFEE440))

            // Text on Native Canvas
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#FF2A5F")
                    textSize = 10f * scale
                    isFakeBoldText = true
                    typeface = android.graphics.Typeface.MONOSPACE
                }
                val bubbleScreen = toScreen(hx - 11f, bhy - 1f)
                drawText(uiState.laughBubble, bubbleScreen.x, bubbleScreen.y, paint)
            }
        }

        // 8. Advanced Retro Arcade Particles
        for (p in viewModel.particles) {
            val progress = (p.life / p.maxLife).coerceIn(0f, 1f)
            val baseColor = Color(p.color)
            val alphaColor = baseColor.copy(alpha = (baseColor.alpha * progress).coerceIn(0f, 1f))

            when (p.type) {
                ParticleType.PIXEL_SPARK -> {
                    // Crisp decaying square spark
                    drawPRect(p.x - p.size / 2f, p.y - p.size / 2f, p.size, p.size, alphaColor)
                }

                ParticleType.TRAIL_SMOKE -> {
                    // Soft fading dust puff expanding slightly as it ages
                    val expand = (1f - progress) * 2.5f
                    val s = p.size + expand
                    drawPRect(p.x - s / 2f, p.y - s / 2f, s, s, alphaColor)
                    // Inner lighter core for 16-bit smoke shading
                    if (progress > 0.4f) {
                        drawPRect(p.x - 1f, p.y - 1f, 2f, 2f, Color.White.copy(alpha = alphaColor.alpha * 0.7f))
                    }
                }

                ParticleType.NEON_GLOW -> {
                    // Speed streaks & neon bursts with multi-layer pixel glow
                    val glowSize = p.size * (1f + (1f - progress) * 0.8f)
                    // Outer aura
                    drawPRect(
                        p.x - glowSize / 2f,
                        p.y - glowSize / 2f,
                        glowSize,
                        glowSize,
                        alphaColor.copy(alpha = alphaColor.alpha * 0.45f)
                    )
                    // Core bright pixel
                    drawPRect(p.x - p.size / 2f, p.y - p.size / 2f, p.size, p.size, alphaColor)
                    drawPRect(p.x - 1f, p.y - 1f, 2f, 2f, Color.White.copy(alpha = alphaColor.alpha))
                }

                ParticleType.GLASS_SHARD -> {
                    // Rotating pixel diamond / triangle shard
                    val rad = Math.toRadians(p.rotation.toDouble())
                    val cosR = kotlin.math.cos(rad).toFloat()
                    val sinR = kotlin.math.sin(rad).toFloat()
                    val s = p.size
                    val center = toScreen(p.x, p.y)

                    // Draw rotated 4-corner polygon shard via native path or pixel cross
                    val p1 = Offset(center.x - (s * cosR) * scale, center.y - (s * sinR) * scale)
                    val p2 = Offset(center.x + (s * cosR) * scale, center.y + (s * sinR) * scale)
                    val p3 = Offset(center.x - (s * 0.5f * sinR) * scale, center.y + (s * 0.5f * cosR) * scale)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        close()
                    }
                    drawPath(path, color = alphaColor)
                    drawPath(path, color = Color.White.copy(alpha = alphaColor.alpha * 0.8f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f * scale))
                }

                ParticleType.ELEC_ZAP -> {
                    // Jagged lightning / shock spark
                    val len = p.size * 2f
                    val zapDx = if ((viewModel.tick + p.hashCode()) % 2L == 0L) len else -len
                    val start = toScreen(p.x, p.y)
                    val mid = toScreen(p.x + zapDx * 0.5f, p.y - p.size)
                    val end = toScreen(p.x + zapDx, p.y)
                    drawLine(alphaColor, start, mid, strokeWidth = 1.5f * scale)
                    drawLine(Color.White.copy(alpha = alphaColor.alpha), mid, end, strokeWidth = 1.2f * scale)
                }

                ParticleType.IMPACT_BURST -> {
                    // Expanding shockwave impact ring
                    val ringRadius = (1f - progress) * 22f * scale
                    val center = toScreen(p.x, p.y)
                    if (ringRadius > 1f) {
                        drawCircle(
                            color = alphaColor,
                            radius = ringRadius,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * scale)
                        )
                    }
                }
            }
        }
    }
}
