package com.example.game

/**
 * Game state, player entity, hunter entity, particle system, and difficulty models.
 */

enum class GameScreen {
    MENU,
    PLAYING,
    HELP
}

enum class GameDifficulty(
    val title: String,
    val subtitle: String,
    val speedMultiplier: Float,
    val trapFrequencyMultiplier: Float, // Higher = more extra surprise traps generated
    val triggerDistanceMultiplier: Float, // Triggers earlier or faster
    val hunterAggression: Float,
    val themeColorHex: Long
) {
    NORMAL(
        title = "عادی (Normal)",
        subtitle = "سرعت متعادل و تله‌های غافلگیرکننده استاندارد",
        speedMultiplier = 1.0f,
        trapFrequencyMultiplier = 1.0f,
        triggerDistanceMultiplier = 1.0f,
        hunterAggression = 1.0f,
        themeColorHex = 0xFF00F5D4
    ),
    HARD(
        title = "سخت (Hard)",
        subtitle = "سرعت بالا (+۱۵٪) و افزایش تله‌های لیزری و متحرک",
        speedMultiplier = 1.15f,
        trapFrequencyMultiplier = 1.35f,
        triggerDistanceMultiplier = 1.15f,
        hunterAggression = 1.25f,
        themeColorHex = 0xFFFEE440
    ),
    RAGE(
        title = "خشم جنون‌آمیز (Rage)",
        subtitle = "حداکثر سرعت (+۳۰٪)، تله‌های متوالی زنجیره‌ای و مامور پرسرعت!",
        speedMultiplier = 1.30f,
        trapFrequencyMultiplier = 1.70f,
        triggerDistanceMultiplier = 1.28f,
        hunterAggression = 1.60f,
        themeColorHex = 0xFFFF2A5F
    )
}

enum class PlayerPose {
    RUN,
    JUMP,
    SLIDE,
    VAULT,
    DASH
}

data class RunnerState(
    var x: Float = 64f,
    var y: Float = 170f,
    var vx: Float = 2.4f,
    var baseSpeed: Float = 2.4f,
    var vy: Float = 0f,
    var onGround: Boolean = true,
    var jumpsLeft: Int = 2,
    var pose: PlayerPose = PlayerPose.RUN,
    var slideTimer: Float = 0f,
    var vaultTimer: Float = 0f,
    var dashTimer: Float = 0f,
    var dashCooldown: Float = 0f,
    var stumbleTimer: Float = 0f,
    var frame: Float = 0f
)

data class HunterState(
    var x: Float = 12f,
    var y: Float = 170f,
    var frame: Float = 0f
)

data class ExitGateState(
    var x: Float = 1380f,
    val y: Float = 170f,
    val canFake: Boolean = false,
    var faked: Boolean = false
)

enum class ParticleType {
    PIXEL_SPARK,   // Normal square pixel burst
    TRAIL_SMOKE,   // Fading soft dust / smoke behind boots
    NEON_GLOW,     // Speed dash / boost glow streak
    GLASS_SHARD,   // Tumbling glass fragment
    ELEC_ZAP,      // Jagged electric spark for shock/laser
    IMPACT_BURST   // Explosive shockwave ring
}

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val color: Long,
    var life: Float,
    val maxLife: Float = life,
    val type: ParticleType = ParticleType.PIXEL_SPARK,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    val gravity: Float = 0.18f,
    val drag: Float = 0.98f
)

data class DeathDetails(
    val emoji: String,
    val title: String,
    val desc: String,
    val subRoast: String,
    val fakeIQ: String,
    val deathsInStage: Int
)

data class CityBuilding(
    val x: Float,
    val w: Float,
    val h: Float,
    val antenna: Boolean,
    val neonColor: Long?
)
