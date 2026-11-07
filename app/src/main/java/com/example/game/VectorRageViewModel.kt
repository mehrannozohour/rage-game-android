package com.example.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

data class GameUiState(
    val currentScreen: GameScreen = GameScreen.MENU,
    val stageIdx: Int = 0,
    val isDead: Boolean = false,
    val isStageWon: Boolean = false,
    val totalDeaths: Int = 0,
    val stageDeaths: Int = 0,
    val ragePercent: Int = 10,
    val slowMoEnabled: Boolean = false,
    val bgmEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val currentDeathDetails: DeathDetails? = null,
    val laughBubble: String = "HAHA!"
)

class VectorRageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val audioPlayer = ChiptuneAudioPlayer()

    // Internal game simulation state
    val stages = GameData.getStages()
    var runner = RunnerState()
    var hunter = HunterState()
    var currentFloors: List<StageFloor> = emptyList()
    var currentTraps: MutableList<StageTrap> = mutableListOf()
    var exitGate = ExitGateState()
    val particles: MutableList<GameParticle> = mutableListOf()
    val scarfPixels: MutableList<Pair<Float, Float>> = mutableListOf()
    val bgSkyscrapers: MutableList<List<CityBuilding>> = mutableListOf()

    var camX: Float = 0f
    var shake: Float = 0f
    var tick: Long = 0
    var deathTimer: Float = 0f
    private var lastTauntTitle: String = ""

    init {
        initCityBuildings()
        loadStage(0)
    }

    private fun initCityBuildings() {
        bgSkyscrapers.clear()
        val rnd = Random(42)
        for (layer in 0..1) {
            val list = mutableListOf<CityBuilding>()
            var cx = 0f
            while (cx < 1600f) {
                val bw = 26f + rnd.nextInt(36)
                val bh = 55f + rnd.nextInt(85) + layer * 20f
                val antenna = rnd.nextFloat() < 0.4f
                val neon = if (rnd.nextFloat() < 0.35f) {
                    if (rnd.nextBoolean()) 0xFF00F5D4 else 0xFFFF2A5F
                } else null
                list.add(CityBuilding(cx, bw, bh, antenna, neon))
                cx += bw + 6f + rnd.nextInt(12)
            }
            bgSkyscrapers.add(list)
        }
    }

    fun navigateTo(screen: GameScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        if (screen == GameScreen.PLAYING) {
            audioPlayer.onGameStart()
            if (_uiState.value.isDead) {
                restartCurrentStage()
            }
        } else {
            audioPlayer.onGameStop()
        }
    }

    fun toggleBgm() {
        val next = !_uiState.value.bgmEnabled
        audioPlayer.isBgmEnabled = next
        _uiState.update { it.copy(bgmEnabled = next) }
    }

    fun toggleSfx() {
        val next = !_uiState.value.sfxEnabled
        audioPlayer.isSfxEnabled = next
        _uiState.update { it.copy(sfxEnabled = next) }
    }

    fun toggleSlowMo() {
        _uiState.update { it.copy(slowMoEnabled = !it.slowMoEnabled) }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.onGameStop()
    }

    fun restartCurrentStage() {
        loadStage(_uiState.value.stageIdx, resetStageDeaths = false)
    }

    fun loadStage(idx: Int, resetStageDeaths: Boolean = false) {
        val stg = stages[idx]
        currentFloors = stg.floors
        currentTraps = stg.traps.map { it.copy(
            triggered = false,
            broken = false,
            currentMode = it.mode,
            dropY = 0f,
            alertTimer = 0f
        ) }.toMutableList()

        exitGate = ExitGateState(
            x = stg.length,
            y = stg.floors.last().y,
            canFake = stg.fakeExit,
            faked = false
        )

        runner = RunnerState(
            x = 64f,
            y = stg.floors.first().y,
            vx = stg.speed,
            baseSpeed = stg.speed,
            vy = 0f,
            onGround = true,
            jumpsLeft = 2,
            pose = PlayerPose.RUN,
            slideTimer = 0f,
            vaultTimer = 0f,
            dashTimer = 0f,
            dashCooldown = 0f,
            stumbleTimer = 0f,
            frame = 0f
        )

        hunter = HunterState(
            x = 12f,
            y = stg.floors.first().y,
            frame = 0f
        )

        scarfPixels.clear()
        particles.clear()
        camX = runner.x - 85f
        shake = 0f
        deathTimer = 0f

        _uiState.update {
            it.copy(
                stageIdx = idx,
                isDead = false,
                isStageWon = false,
                currentDeathDetails = null,
                stageDeaths = if (resetStageDeaths) 0 else it.stageDeaths
            )
        }
    }

    // --- ENHANCED RETRO PARTICLE SYSTEM EMITTERS ---

    fun spawnBurst(
        x: Float,
        y: Float,
        color: Long,
        count: Int = 16,
        type: ParticleType = ParticleType.PIXEL_SPARK,
        minSpeed: Float = 0.8f,
        maxSpeed: Float = 3.2f,
        gravity: Float = 0.18f
    ) {
        val rnd = Random
        for (i in 0 until count) {
            val angle = rnd.nextFloat() * 2f * Math.PI.toFloat()
            val speed = minSpeed + rnd.nextFloat() * (maxSpeed - minSpeed)
            val life = 16f + rnd.nextInt(18)
            particles.add(
                GameParticle(
                    x = x,
                    y = y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 0.8f,
                    size = if (rnd.nextBoolean()) 2f else 3f,
                    color = color,
                    life = life,
                    maxLife = life,
                    type = type,
                    rotation = rnd.nextFloat() * 360f,
                    vRot = (rnd.nextFloat() - 0.5f) * 22f,
                    gravity = gravity,
                    drag = 0.96f
                )
            )
        }
    }

    /**
     * Emits trail dust/smoke behind running or sliding feet
     */
    fun emitFootprintSmoke(x: Float, y: Float, count: Int = 2, color: Long = 0xAA64748B) {
        val rnd = Random
        for (i in 0 until count) {
            val life = 14f + rnd.nextInt(10)
            particles.add(
                GameParticle(
                    x = x - rnd.nextFloat() * 5f,
                    y = y - 1f - rnd.nextFloat() * 2f,
                    vx = -runner.vx * 0.35f + (rnd.nextFloat() - 0.5f) * 0.5f,
                    vy = -0.3f - rnd.nextFloat() * 0.6f,
                    size = 2.5f + rnd.nextFloat() * 2f,
                    color = color,
                    life = life,
                    maxLife = life,
                    type = ParticleType.TRAIL_SMOKE,
                    gravity = -0.02f, // floats upwards slightly
                    drag = 0.94f
                )
            )
        }
    }

    /**
     * Emits bright neon electric friction sparks for high-speed slides and dashes
     */
    fun emitFrictionSparks(x: Float, y: Float, count: Int = 3, color: Long = 0xFFFEE440) {
        val rnd = Random
        for (i in 0 until count) {
            val life = 10f + rnd.nextInt(8)
            particles.add(
                GameParticle(
                    x = x + (rnd.nextFloat() - 0.5f) * 8f,
                    y = y - rnd.nextFloat() * 2f,
                    vx = -runner.vx * 0.7f - rnd.nextFloat() * 1.5f,
                    vy = -0.8f - rnd.nextFloat() * 1.8f,
                    size = if (rnd.nextBoolean()) 2f else 3f,
                    color = if (rnd.nextBoolean()) color else 0xFF00F5D4,
                    life = life,
                    maxLife = life,
                    type = ParticleType.ELEC_ZAP,
                    gravity = 0.15f,
                    drag = 0.92f
                )
            )
        }
    }

    /**
     * Emits neon speed streaks behind player during Dash and Boost pads
     */
    fun emitDashGlowStreak(x: Float, y: Float, color: Long = 0xFF00F5D4) {
        val rnd = Random
        val life = 12f + rnd.nextInt(6)
        particles.add(
            GameParticle(
                x = x - 6f,
                y = y - 12f + (rnd.nextFloat() - 0.5f) * 10f,
                vx = -runner.vx * 0.25f,
                vy = (rnd.nextFloat() - 0.5f) * 0.4f,
                size = 3.5f + rnd.nextFloat() * 2.5f,
                color = color,
                life = life,
                maxLife = life,
                type = ParticleType.NEON_GLOW,
                gravity = 0f,
                drag = 0.96f
            )
        )
    }

    /**
     * Emits shattering glass polygon/shards on window smash
     */
    fun emitGlassShards(x: Float, y: Float, count: Int = 22) {
        val rnd = Random
        for (i in 0 until count) {
            val angle = rnd.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 1.2f + rnd.nextFloat() * 4.2f
            val life = 24f + rnd.nextInt(18)
            particles.add(
                GameParticle(
                    x = x + (rnd.nextFloat() - 0.5f) * 6f,
                    y = y - rnd.nextFloat() * 36f,
                    vx = cos(angle) * speed + runner.vx * 0.5f,
                    vy = sin(angle) * speed - 1.2f,
                    size = 3f + rnd.nextInt(4),
                    color = if (rnd.nextFloat() < 0.6f) 0xFF90E0EF else 0xFFFFFFFF,
                    life = life,
                    maxLife = life,
                    type = ParticleType.GLASS_SHARD,
                    rotation = rnd.nextFloat() * 360f,
                    vRot = (rnd.nextFloat() - 0.5f) * 35f,
                    gravity = 0.22f,
                    drag = 0.98f
                )
            )
        }
    }

    /**
     * Emits shockwave and spark bursts upon jump or landing
     */
    fun emitJumpImpact(x: Float, y: Float, color: Long = 0xFF00BBF9) {
        val rnd = Random
        // Shock ring
        particles.add(
            GameParticle(
                x = x,
                y = y,
                vx = 0f,
                vy = 0f,
                size = 4f,
                color = color,
                life = 14f,
                maxLife = 14f,
                type = ParticleType.IMPACT_BURST,
                gravity = 0f
            )
        )
        // Horizontal puff dust
        for (dir in listOf(-1f, 1f)) {
            for (i in 0..2) {
                val life = 12f + rnd.nextInt(8)
                particles.add(
                    GameParticle(
                        x = x,
                        y = y - 1f,
                        vx = dir * (1.2f + rnd.nextFloat() * 1.5f),
                        vy = -0.4f - rnd.nextFloat() * 0.6f,
                        size = 2f + rnd.nextFloat() * 2f,
                        color = 0xAA64748B,
                        life = life,
                        maxLife = life,
                        type = ParticleType.TRAIL_SMOKE,
                        gravity = 0.05f
                    )
                )
            }
        }
    }

    fun doJump() {
        if (_uiState.value.isDead) {
            restartCurrentStage()
            return
        }
        if (_uiState.value.currentScreen != GameScreen.PLAYING) return

        // Check Kong Vault over normal vault_box
        val nearBox = currentTraps.firstOrNull {
            it.type == TrapType.VAULT_BOX &&
                    (it.x - runner.x) in -6f..32f &&
                    abs(runner.y - it.y) < 14f
        }

        if (nearBox != null && runner.onGround) {
            runner.pose = PlayerPose.VAULT
            runner.vaultTimer = 14f
            runner.vy = -4.6f
            runner.vx = runner.baseSpeed + 1.1f
            runner.onGround = false
            runner.jumpsLeft = 1
            audioPlayer.playVault()
            spawnBurst(runner.x, runner.y - 8f, 0xFF00F5D4, 12, ParticleType.NEON_GLOW, minSpeed = 1f, maxSpeed = 3.5f)
            emitJumpImpact(runner.x, runner.y, 0xFF00F5D4)
            return
        }

        if (runner.onGround) {
            runner.vy = -5.4f
            runner.onGround = false
            runner.jumpsLeft = 1
            runner.pose = PlayerPose.JUMP
            runner.slideTimer = 0f
            audioPlayer.playJump()
            emitJumpImpact(runner.x, runner.y, 0xFF00BBF9)
            spawnBurst(runner.x, runner.y, 0xFF00BBF9, 10, ParticleType.PIXEL_SPARK)
        } else if (runner.jumpsLeft > 0) {
            runner.vy = -4.9f
            runner.jumpsLeft = 0
            runner.pose = PlayerPose.JUMP
            runner.slideTimer = 0f
            audioPlayer.playDoubleJump()
            emitJumpImpact(runner.x, runner.y - 6f, 0xFF00F5D4)
            spawnBurst(runner.x, runner.y - 6f, 0xFF00F5D4, 14, ParticleType.NEON_GLOW, minSpeed = 1.5f, maxSpeed = 4f)
        }
    }

    fun doSlide() {
        if (_uiState.value.isDead) {
            restartCurrentStage()
            return
        }
        if (_uiState.value.currentScreen != GameScreen.PLAYING) return

        if (!runner.onGround) {
            // Fast fall slam to dodge sudden high obstacles
            runner.vy = max(runner.vy, 6.2f)
            runner.slideTimer = 26f
            runner.pose = PlayerPose.SLIDE
            audioPlayer.playSlide()
            emitFrictionSparks(runner.x, runner.y - 4f, count = 4, color = 0xFFFF2A5F)
        } else {
            runner.slideTimer = 28f
            runner.pose = PlayerPose.SLIDE
            audioPlayer.playSlide()
            emitFrictionSparks(runner.x + 8f, runner.y, count = 6, color = 0xFFFEE440)
            emitFootprintSmoke(runner.x - 6f, runner.y, count = 4, color = 0xAA64748B)
        }
    }

    fun doDash() {
        if (_uiState.value.isDead) {
            restartCurrentStage()
            return
        }
        if (_uiState.value.currentScreen != GameScreen.PLAYING || runner.dashCooldown > 0f) return

        runner.dashTimer = 14f
        runner.dashCooldown = 48f
        runner.vx = runner.baseSpeed + 2.4f
        runner.vy = min(runner.vy, 0f)
        runner.pose = PlayerPose.DASH
        audioPlayer.playDash()
        spawnBurst(runner.x, runner.y - 10f, 0xFFFEE440, 16, ParticleType.NEON_GLOW, minSpeed = 2f, maxSpeed = 4.5f)
        emitJumpImpact(runner.x, runner.y - 8f, 0xFFFF2A5F)
    }

    private fun killPlayer(trapReason: String) {
        if (_uiState.value.isDead || _uiState.value.isStageWon) return

        shake = 9f
        deathTimer = 45f
        runner.vy = -3.8f
        runner.vx = -1.4f

        audioPlayer.playDeath()

        // Sarcastic violent retro explosion with mixed particles
        spawnBurst(runner.x, runner.y - 12f, 0xFF00F5D4, 24, ParticleType.NEON_GLOW, minSpeed = 2f, maxSpeed = 5.5f)
        spawnBurst(runner.x, runner.y - 12f, 0xFFFF2A5F, 22, ParticleType.ELEC_ZAP, minSpeed = 1.5f, maxSpeed = 4.8f)
        spawnBurst(runner.x, runner.y - 12f, 0xFFFEE440, 18, ParticleType.PIXEL_SPARK, minSpeed = 1f, maxSpeed = 4.0f)
        particles.add(
            GameParticle(
                x = runner.x,
                y = runner.y - 12f,
                vx = 0f,
                vy = 0f,
                size = 8f,
                color = 0xFFFF2A5F,
                life = 20f,
                maxLife = 20f,
                type = ParticleType.IMPACT_BURST
            )
        )

        val nextTotalDeaths = _uiState.value.totalDeaths + 1
        val nextStageDeaths = _uiState.value.stageDeaths + 1
        val nextRage = min(100, _uiState.value.ragePercent + 12)

        val pool = GameData.TAUNTS[trapReason] ?: GameData.TAUNTS["elevator_laser"]!!
        var pick = pool[Random.nextInt(pool.size)]
        if (pool.size > 1 && pick.title == lastTauntTitle) {
            val idx = (pool.indexOf(pick) + 1) % pool.size
            pick = pool[idx]
        }
        lastTauntTitle = pick.title

        val roastIdx = min(GameData.STREAK_ROASTS.size - 1, (nextStageDeaths - 1) % GameData.STREAK_ROASTS.size)
        val subRoast = GameData.STREAK_ROASTS[roastIdx]
        val fakeIQ = max(-50f, (5.0f - nextTotalDeaths * 0.7f))
        val fakeIqFormatted = String.format("%.1f", fakeIQ)
        val laugh = GameData.PIXEL_LAUGH_BUBBLES[nextTotalDeaths % GameData.PIXEL_LAUGH_BUBBLES.size]

        val details = DeathDetails(
            emoji = pick.emoji,
            title = pick.title,
            desc = pick.desc,
            subRoast = subRoast,
            fakeIQ = fakeIqFormatted,
            deathsInStage = nextStageDeaths
        )

        _uiState.update {
            it.copy(
                isDead = true,
                totalDeaths = nextTotalDeaths,
                stageDeaths = nextStageDeaths,
                ragePercent = nextRage,
                currentDeathDetails = details,
                laughBubble = laugh
            )
        }
    }

    private fun handleStageWin() {
        if (_uiState.value.isStageWon) return
        _uiState.update { it.copy(isStageWon = true) }
        audioPlayer.playWin()

        // Celebration confetti & fireworks
        spawnBurst(exitGate.x, exitGate.y - 20f, 0xFF00F5D4, 30, ParticleType.NEON_GLOW, minSpeed = 2f, maxSpeed = 6f)
        spawnBurst(exitGate.x, exitGate.y - 20f, 0xFFFEE440, 26, ParticleType.PIXEL_SPARK, minSpeed = 1.5f, maxSpeed = 5f)
        spawnBurst(exitGate.x, exitGate.y - 20f, 0xFFFF2A5F, 24, ParticleType.ELEC_ZAP, minSpeed = 1f, maxSpeed = 4f)

        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            val currentIdx = _uiState.value.stageIdx
            val nextIdx = (currentIdx + 1) % stages.size
            loadStage(nextIdx, resetStageDeaths = true)
        }
    }

    private fun getGroundAt(wx: Float): Float? {
        for (t in currentTraps) {
            if (t.type == TrapType.SUDDEN_BRIDGE && wx >= t.x && wx <= t.x + t.w) {
                if (t.dropY > 18f) return null
                return t.y + t.dropY
            }
        }
        for (f in currentFloors) {
            if (wx >= f.x && wx <= f.x + f.w) {
                return f.y
            }
        }
        return null
    }

    fun updateGameStep() {
        tick++
        val dt = if (_uiState.value.slowMoEnabled) 0.58f else 1.0f

        // Physics update on all active particles (gravity, drag, rotation, fade)
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += p.gravity * dt
            p.vx *= p.drag
            p.vy *= p.drag
            p.rotation += p.vRot * dt
            p.life -= dt
            if (p.life <= 0f) pIter.remove()
        }

        if (_uiState.value.isDead) {
            if (deathTimer > 0f) deathTimer -= dt
            if (runner.y < 216f - 18f) {
                runner.x += runner.vx * 0.5f
                runner.y += runner.vy * 0.6f
                runner.vy += 0.25f
            }
            return
        }

        if (_uiState.value.currentScreen != GameScreen.PLAYING || _uiState.value.isStageWon) return

        // --- Continuous Movement Trail Particle Emitters ---
        if (runner.onGround) {
            if (runner.pose == PlayerPose.SLIDE) {
                // Intense friction sparks and smoke when sliding
                if (tick % 2L == 0L) {
                    emitFrictionSparks(runner.x + 8f, runner.y, count = 2, color = 0xFFFEE440)
                    emitFootprintSmoke(runner.x - 4f, runner.y, count = 1, color = 0x8864748B)
                }
            } else if (runner.pose == PlayerPose.DASH) {
                // Cyber dash neon trail
                emitDashGlowStreak(runner.x, runner.y, 0xFFFF2A5F)
                if (tick % 2L == 0L) {
                    emitFootprintSmoke(runner.x - 5f, runner.y, count = 2, color = 0xBB00F5D4)
                }
            } else {
                // Running dust puff every few strides
                if (tick % 4L == 0L) {
                    emitFootprintSmoke(runner.x - 5f, runner.y, count = 1, color = 0x6664748B)
                }
            }
        } else {
            // Mid-air trails during Dash or fast-fall
            if (runner.pose == PlayerPose.DASH) {
                emitDashGlowStreak(runner.x, runner.y, 0xFF00F5D4)
            } else if (runner.pose == PlayerPose.SLIDE) {
                // Fast-fall vertical speed streaks
                if (tick % 2L == 0L) {
                    emitFrictionSparks(runner.x, runner.y - 10f, count = 2, color = 0xFFFF2A5F)
                }
            }
        }

        // Timers
        if (runner.dashCooldown > 0f) runner.dashCooldown -= dt
        if (runner.dashTimer > 0f) {
            runner.dashTimer -= dt
        } else {
            runner.vx = runner.vx + (runner.baseSpeed - runner.vx) * 0.1f * dt
        }

        if (runner.stumbleTimer > 0f) {
            runner.stumbleTimer -= dt
            runner.vx = runner.baseSpeed * 0.45f
            // Emit stumbling sweat / danger sparks
            if (tick % 3L == 0L) {
                emitFrictionSparks(runner.x, runner.y - 18f, count = 2, color = 0xFFFF2A5F)
            }
        }

        if (runner.slideTimer > 0f) {
            runner.slideTimer -= dt
            if (runner.slideTimer <= 0f && runner.onGround) runner.pose = PlayerPose.RUN
        }

        if (runner.vaultTimer > 0f) {
            runner.vaultTimer -= dt
            if (runner.vaultTimer <= 0f && !runner.onGround) runner.pose = PlayerPose.JUMP
        }

        val prevY = runner.y
        runner.x += runner.vx * dt
        runner.y += runner.vy * dt
        runner.vy += 0.28f * dt

        val gy = getGroundAt(runner.x)
        if (gy != null && prevY <= gy + 7f && runner.y >= gy && runner.vy >= 0f) {
            runner.y = gy
            runner.vy = 0f
            if (!runner.onGround) {
                // Player just landed on the rooftop!
                runner.onGround = true
                runner.jumpsLeft = 2
                emitJumpImpact(runner.x, runner.y, 0xFF00BBF9)
                if (runner.slideTimer <= 0f) runner.pose = PlayerPose.RUN
            }
        } else if (gy == null || runner.y < gy) {
            runner.onGround = false
        }

        if (runner.y > 216f + 16f) {
            killPlayer("sudden_gap")
            return
        }

        // Scarf
        runner.frame += runner.vx * 0.12f * dt
        scarfPixels.add(0, Pair(runner.x - 3f, runner.y - (if (runner.pose == PlayerPose.SLIDE) 8f else 19f)))
        if (scarfPixels.size > 10) scarfPixels.removeAt(scarfPixels.size - 1)

        // Hunter Agent AI
        val targetDist = if (runner.stumbleTimer > 0f) 14f else 56f
        hunter.x = hunter.x + (runner.x - targetDist - hunter.x) * (if (runner.stumbleTimer > 0f) 0.09f else 0.045f) * dt
        val hgy = getGroundAt(hunter.x)
        hunter.y = if (hgy != null) hunter.y + (hgy - hunter.y) * 0.3f else runner.y
        hunter.frame += runner.vx * 0.12f * dt

        // Hunter siren sparks
        if (tick % 6L == 0L) {
            emitFrictionSparks(hunter.x + 8f, hunter.y - 18f, count = 1, color = 0xFFFF2A5F)
        }

        if (hunter.x >= runner.x - 9f) {
            killPlayer("hunter")
            return
        }

        // Runner Hitbox
        val rH = if (runner.pose == PlayerPose.SLIDE) 10f else 22f
        val rTop = runner.y - rH

        // Traps
        for (t in currentTraps) {
            if (t.alertTimer > 0f) t.alertTimer -= dt
            val dist = t.x - runner.x

            // 1. Elevator laser
            if (t.type == TrapType.ELEVATOR_LASER && !t.triggered && dist < t.triggerDist && dist > 10f) {
                t.triggered = true
                t.currentMode = t.endMode
                t.alertTimer = 28f
                audioPlayer.playAmbush()
                spawnBurst(t.x, t.y - 24f, 0xFFFF2A5F, 12, ParticleType.ELEC_ZAP, minSpeed = 1f, maxSpeed = 3.5f)
            }

            // 2. Spike Mimic
            if (t.type == TrapType.SPIKE_MIMIC_BOX && !t.triggered && dist < t.triggerDist && dist > 5f) {
                t.triggered = true
                t.alertTimer = 28f
                audioPlayer.playAmbush()
                spawnBurst(t.x + 10f, t.y - 22f, 0xFFFEE440, 10, ParticleType.PIXEL_SPARK, minSpeed = 1.2f, maxSpeed = 3f)
            }

            // 3. Walking Box
            if (t.type == TrapType.WALKING_BOX) {
                if (!t.triggered && dist < t.triggerDist) {
                    t.triggered = true
                    t.alertTimer = 28f
                    audioPlayer.playAmbush()
                    spawnBurst(t.x, t.y - 4f, 0xFFFF2A5F, 8, ParticleType.PIXEL_SPARK)
                }
                if (t.triggered && runner.x < t.x + 40f) {
                    t.x += t.vx * dt
                    if (tick % 5L == 0L) {
                        emitFootprintSmoke(t.x + 10f, t.y, count = 1, color = 0x88FF2A5F)
                    }
                }
            }

            // 4. Sudden Bridge
            if (t.type == TrapType.SUDDEN_BRIDGE) {
                if (!t.triggered && dist < t.triggerDist) {
                    t.triggered = true
                    t.alertTimer = 28f
                    audioPlayer.playAmbush()
                    spawnBurst(t.x + t.w / 2f, t.y + 4f, 0xFFFEE440, 12, ParticleType.PIXEL_SPARK)
                }
                if (t.triggered) {
                    t.dropY += 1.65f * dt
                    if (tick % 4L == 0L) {
                        emitFootprintSmoke(t.x + Random.nextFloat() * t.w, t.y + t.dropY, count = 1, color = 0xAA64748B)
                    }
                }
            }

            // 5. Crusher
            if (t.type == TrapType.CRUSHER) {
                if (!t.triggered && dist < t.triggerDist) {
                    t.triggered = true
                    t.alertTimer = 28f
                    audioPlayer.playAmbush()
                }
                if (t.triggered && t.dropY < 34f) {
                    t.dropY = min(34f, t.dropY + 2.8f * dt)
                    // Impact dust when crusher slams down
                    if (t.dropY >= 33.5f) {
                        emitJumpImpact(t.x + t.w / 2f, t.y, 0xFFFF2A5F)
                        spawnBurst(t.x + t.w / 2f, t.y, 0xFFFEE440, 10, ParticleType.ELEC_ZAP)
                    }
                }
            }

            // 6. Fake Glass
            if (t.type == TrapType.FAKE_GLASS && !t.triggered && dist < t.triggerDist) {
                t.triggered = true
                t.alertTimer = 28f
                audioPlayer.playAmbush()
                spawnBurst(t.x, t.y - 16f, 0xFF94A3B8, 12, ParticleType.PIXEL_SPARK)
            }

            // --- Collisions & Trap Interactions ---
            if (t.type == TrapType.VAULT_BOX) {
                if (abs(runner.x - (t.x + t.w / 2f)) < t.w / 2f + 5f && runner.y > t.y - t.h + 4f) {
                    if (runner.vy < 0f || runner.pose == PlayerPose.VAULT || runner.pose == PlayerPose.DASH) {
                        runner.y = t.y - t.h
                        runner.vy = -3.8f
                        runner.pose = PlayerPose.VAULT
                        audioPlayer.playVault()
                        // Vault contact spark
                        spawnBurst(runner.x, t.y - t.h, 0xFF00F5D4, 10, ParticleType.NEON_GLOW)
                    } else {
                        // Stumble on box: impact sparks & heavy dust
                        runner.x = t.x - 6f
                        runner.vy = -2.2f
                        runner.stumbleTimer = 32f
                        shake = 4f
                        emitJumpImpact(runner.x, runner.y, 0xFFFF2A5F)
                        spawnBurst(runner.x, runner.y - 10f, 0xFFFEE440, 8, ParticleType.ELEC_ZAP)
                    }
                }
            } else if (t.type == TrapType.SPIKE_MIMIC_BOX || t.type == TrapType.WALKING_BOX) {
                if (abs(runner.x - (t.x + t.w / 2f)) < t.w / 2f + 4f && runner.y > t.y - t.h - 5f) {
                    killPlayer("mimic_crate")
                    return
                }
            } else if (t.type == TrapType.GLASS && !t.broken) {
                if (abs(runner.x - t.x) < 10f && runner.y >= t.y - t.h) {
                    t.broken = true
                    audioPlayer.playGlass()
                    emitGlassShards(t.x, t.y, count = 28)
                    shake = 3f
                }
            } else if (t.type == TrapType.FAKE_GLASS) {
                if (abs(runner.x - (t.x + t.w / 2f)) < t.w / 2f + 4f && runner.y > t.y - t.h + 2f) {
                    // Solid steel collision!
                    spawnBurst(runner.x, runner.y - 15f, 0xFF94A3B8, 14, ParticleType.PIXEL_SPARK)
                    killPlayer("mimic_crate")
                    return
                }
            } else if (t.type == TrapType.BOOST_PAD) {
                if (!t.triggered && abs(runner.x - (t.x + t.w / 2f)) < t.w / 2f && abs(runner.y - t.y) < 8f) {
                    t.triggered = true
                    runner.vx = runner.baseSpeed + 2.6f
                    runner.dashTimer = 18f
                    audioPlayer.playDash()
                    // Intense turbo particle eruption
                    spawnBurst(runner.x, runner.y - 4f, 0xFF00F5D4, 18, ParticleType.NEON_GLOW, minSpeed = 2.5f, maxSpeed = 5.5f)
                    spawnBurst(runner.x, runner.y - 4f, 0xFFFEE440, 14, ParticleType.ELEC_ZAP, minSpeed = 2f, maxSpeed = 4.5f)
                    emitJumpImpact(runner.x, runner.y, 0xFF00F5D4)
                }
            } else if (t.type == TrapType.OVERHANG) {
                if (runner.x + 4f > t.x && runner.x - 4f < t.x + t.w) {
                    val bottomEdge = t.y - 14f
                    val topEdge = t.y - t.h - 14f
                    if (rTop < bottomEdge && runner.y > topEdge) {
                        spawnBurst(runner.x, rTop, 0xFFFF2A5F, 16, ParticleType.ELEC_ZAP)
                        killPlayer("spike_wall")
                        return
                    }
                }
            } else if (t.type == TrapType.CRUSHER) {
                if (runner.x + 4f > t.x && runner.x - 4f < t.x + t.w) {
                    val crusherBottom = (t.y - 52f) + t.dropY
                    if (rTop < crusherBottom) {
                        spawnBurst(runner.x, crusherBottom, 0xFFFF2A5F, 20, ParticleType.ELEC_ZAP)
                        killPlayer("crusher")
                        return
                    }
                }
            } else if (t.type == TrapType.LASER || t.type == TrapType.ELEVATOR_LASER) {
                val mode = if (t.type == TrapType.LASER) t.mode else t.currentMode
                if (abs(runner.x - t.x) < 6f) {
                    if (mode == "low") {
                        if (runner.y > t.y - 22f) {
                            spawnBurst(runner.x, runner.y - 10f, 0xFFFF2A5F, 20, ParticleType.ELEC_ZAP)
                            killPlayer("elevator_laser")
                            return
                        }
                    } else {
                        if (rTop < t.y - 13f && runner.y > t.y - 45f) {
                            spawnBurst(runner.x, t.y - 30f, 0xFFFF2A5F, 20, ParticleType.ELEC_ZAP)
                            killPlayer("elevator_laser")
                            return
                        }
                    }
                }
            }
        }

        // Fake Exit Gate Troll Check
        if (exitGate.canFake && !exitGate.faked && (exitGate.x - runner.x) < 55f) {
            exitGate.faked = true
            exitGate.x += 115f
            currentTraps.add(
                StageTrap(
                    type = TrapType.LASER,
                    x = runner.x + 48f,
                    y = exitGate.y,
                    mode = "low",
                    alertTimer = 30f
                )
            )
            audioPlayer.playAmbush()
            spawnBurst(exitGate.x - 115f, exitGate.y - 20f, 0xFF00F5D4, 20, ParticleType.NEON_GLOW)
            spawnBurst(exitGate.x - 115f, exitGate.y - 20f, 0xFFFF2A5F, 16, ParticleType.ELEC_ZAP)
        }

        // Win check
        if (runner.x >= exitGate.x) {
            handleStageWin()
        }

        // Smooth Camera Follow
        camX += (runner.x - 90f - camX) * 0.22f
    }
}
