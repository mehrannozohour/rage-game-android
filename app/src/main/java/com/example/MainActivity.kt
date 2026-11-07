package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.DeathRageOverlay
import com.example.game.GameHud
import com.example.game.GameScreen
import com.example.game.HelpControlsScreen
import com.example.game.MainMenuScreen
import com.example.game.OnScreenGameControls
import com.example.game.PixelGameCanvas
import com.example.game.StageWinOverlay
import com.example.game.VectorRageViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: VectorRageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RageApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RageApp(viewModel: VectorRageViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 60FPS Game Loop Engine using Compose Choreographer frame clock
    LaunchedEffect(uiState.currentScreen) {
        while (true) {
            withFrameNanos { _ ->
                if (uiState.currentScreen == GameScreen.PLAYING) {
                    viewModel.updateGameStep()
                }
            }
        }
    }

    // Handle Android system back button
    BackHandler(enabled = uiState.currentScreen != GameScreen.MENU) {
        when (uiState.currentScreen) {
            GameScreen.PLAYING -> viewModel.navigateTo(GameScreen.MENU)
            GameScreen.HELP -> viewModel.navigateTo(GameScreen.MENU)
            GameScreen.MENU -> { /* Exit app handled by system */ }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            when (uiState.currentScreen) {
                GameScreen.MENU -> {
                    MainMenuScreen(
                        uiState = uiState,
                        onStartGame = { viewModel.navigateTo(GameScreen.PLAYING) },
                        onOpenHelp = { viewModel.navigateTo(GameScreen.HELP) },
                        onToggleBgm = { viewModel.toggleBgm() },
                        onToggleSfx = { viewModel.toggleSfx() },
                        onToggleSlowMo = { viewModel.toggleSlowMo() }
                    )
                }

                GameScreen.HELP -> {
                    HelpControlsScreen(
                        onBack = { viewModel.navigateTo(GameScreen.MENU) }
                    )
                }

                GameScreen.PLAYING -> {
                    // Full Screen Pixel Engine Viewport with Direct Tap Fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(uiState.isDead) {
                                detectTapGestures(
                                    onTap = { offset ->
                                        if (uiState.isDead) {
                                            viewModel.restartCurrentStage()
                                        } else {
                                            // Tap upper 60% = Jump, Lower 40% = Slide
                                            if (offset.y > size.height * 0.6f) {
                                                viewModel.doSlide()
                                            } else {
                                                viewModel.doJump()
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        // 1. Pixel Art Canvas
                        PixelGameCanvas(
                            viewModel = viewModel,
                            uiState = uiState
                        )

                        // 2. Minimal Top Game HUD
                        GameHud(
                            uiState = uiState,
                            stageTitle = viewModel.stages[uiState.stageIdx].title,
                            onOpenMenu = { viewModel.navigateTo(GameScreen.MENU) },
                            onRestartStage = { viewModel.restartCurrentStage() },
                            onToggleBgm = { viewModel.toggleBgm() },
                            onToggleSfx = { viewModel.toggleSfx() }
                        )

                        // 3. Permanent Tactile On-Screen Joystick & Action Buttons
                        if (!uiState.isDead) {
                            OnScreenGameControls(
                                onJump = { viewModel.doJump() },
                                onSlide = { viewModel.doSlide() },
                                onDash = { viewModel.doDash() }
                            )
                        }

                        // 4. Sarcastic Rage Death Overlay
                        AnimatedVisibility(
                            visible = uiState.isDead,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            DeathRageOverlay(
                                details = uiState.currentDeathDetails,
                                survivalTime = uiState.currentStageTime,
                                onRetry = { viewModel.restartCurrentStage() }
                            )
                        }

                        // 5. Stage Clear Celebration Overlay
                        AnimatedVisibility(
                            visible = uiState.isStageWon,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            StageWinOverlay(
                                stageNum = uiState.stageIdx + 1,
                                timeTaken = uiState.lastCompletedTime ?: uiState.currentStageTime,
                                bestTime = uiState.personalBestTime,
                                isNewRecord = uiState.isNewRecord
                            )
                        }
                    }
                }
            }
        }
    }
}
