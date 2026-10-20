package com.example.game

enum class WeaponType(
  val displayName: String,
  val maxClip: Int,
  val defaultReserve: Int,
  val damage: Float,
  val isAutomatic: Boolean,
  val pellets: Int = 1,
  val spread: Float = 0.05f
) {
  DUAL_PISTOLS("Dual 9mm Pistols", 30, 999, 35f, false, 1, 0.04f),
  PUMP_SHOTGUN("Pump Shotgun", 8, 72, 140f, false, 8, 0.15f),
  M16_ASSAULT("M16 Assault Rifle", 50, 360, 48f, true, 1, 0.06f),
  MEDKIT("First Aid Kit", 1, 1, 0f, false, 1, 0f)
}

enum class ZombieType(
  val displayName: String,
  val maxHp: Float,
  val speed: Float,
  val damagePerHit: Float,
  val points: Int,
  val scale: Float
) {
  COMMON("Infected", 50f, 1.8f, 6f, 10, 1.0f),
  BOOMER("Boomer", 110f, 1.1f, 15f, 50, 1.4f),
  HUNTER("Hunter", 150f, 2.6f, 20f, 75, 1.1f),
  TANK("THE TANK", 1200f, 1.5f, 35f, 300, 2.0f)
}

data class ZombieEntity(
  val id: Int,
  var type: ZombieType,
  var x: Float,
  var y: Float,
  var hp: Float,
  var state: ZombieState = ZombieState.CHASING,
  var staggerTimer: Float = 0f,
  var attackCooldown: Float = 0f
)

enum class ZombieState {
  IDLE,
  CHASING,
  ATTACKING,
  STAGGERED,
  DEAD
}

data class SurvivorStatus(
  val name: String,
  var health: Int,
  val maxHealth: Int = 100,
  val isPlayer: Boolean = false,
  var statusMessage: String = "OK"
)

data class BloodSplatter(
  val x: Float,
  val y: Float,
  val radius: Float,
  var alpha: Float = 1.0f
)

data class Particle(
  var x: Float,
  var y: Float,
  var vx: Float,
  var vy: Float,
  var life: Float,
  val maxLife: Float,
  val color: Long
)
