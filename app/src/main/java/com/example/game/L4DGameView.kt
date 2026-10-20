package com.example.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundSynthesizer
import com.example.model.AppLanguage
import com.example.model.ResolutionConfig
import com.example.model.XboxAchievement
import com.example.ui.theme.L4DBloodRed
import com.example.ui.theme.L4DGreenHealth
import com.example.ui.theme.L4DRedHealth
import com.example.ui.theme.L4DYellowHealth
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val ShaderModuleNames = listOf(
  "pipeline_volumetric_fog.spv",
  "infected_horde_shadows.spv",
  "flashlight_cone_bloom.spv",
  "blood_splatter_decal.spv",
  "directx_deferred_lighting.spv",
  "tank_muscle_deformation.spv",
  "rain_puddle_reflection.spv",
  "depth_stencil_msaa_resolve.spv",
  "post_process_color_grade.spv"
)

@Composable
fun L4DGameView(
  resolution: ResolutionConfig,
  showFps: Boolean,
  gpuDriverName: String,
  soundSynthesizer: SoundSynthesizer,
  currentLanguage: AppLanguage = AppLanguage.SPANISH,
  shaderCacheManager: ShaderCacheManager? = null,
  onUnlockAchievement: (XboxAchievement) -> Unit,
  onExitGame: () -> Unit,
  modifier: Modifier = Modifier,
  // Control action signals passed from touch overlay
  moveVector: Pair<Float, Float>,
  aimDelta: Pair<Float, Float>,
  fireTrigger: Boolean,
  meleeTrigger: Boolean,
  reloadTrigger: Boolean,
  switchWeaponTrigger: Boolean,
  flashlightTrigger: Boolean
) {
  val context = LocalContext.current
  val cacheManager = remember { shaderCacheManager ?: ShaderCacheManager(context) }
  val scope = rememberCoroutineScope()

  // Current Scene state
  var currentScene by remember { mutableStateOf(GameScene.ALLEYWAYS) }
  var showSceneSelectorModal by remember { mutableStateOf(false) }

  // -------------------------------------------------------------
  // SHADER COMPILATION & DISK CACHE SYSTEM
  // "primera vez que aparece cada cena, el juego compila shaders y puedes jugar un poco. Nas próximas vezes eles já ficam salvas."
  // -------------------------------------------------------------
  var isSceneAlreadySaved by remember(currentScene) {
    mutableStateOf(cacheManager.isSceneCompiled(currentScene.id))
  }
  var compilationState by remember(currentScene) {
    mutableStateOf(
      if (cacheManager.isSceneCompiled(currentScene.id)) {
        ShaderCompilationState.SAVED_CACHE_LOADED
      } else {
        ShaderCompilationState.COMPILING_FIRST_TIME
      }
    )
  }
  var compiledShaderCount by remember(currentScene) {
    mutableIntStateOf(
      if (cacheManager.isSceneCompiled(currentScene.id)) currentScene.totalShaders else 6
    )
  }
  var currentCompilingModuleName by remember(currentScene) {
    mutableStateOf(ShaderModuleNames.first())
  }
  var showCompilationSavedBanner by remember { mutableStateOf(false) }
  var showLoadedFromCacheBanner by remember(currentScene) {
    mutableStateOf(cacheManager.isSceneCompiled(currentScene.id))
  }

  // Realistic FPS fluctuation during compilation
  var dynamicFps by remember { mutableFloatStateOf(60f) }

  // Hide the "Loaded from cache" toast after 3 seconds
  LaunchedEffect(currentScene, isSceneAlreadySaved) {
    if (isSceneAlreadySaved) {
      delay(3200)
      showLoadedFromCacheBanner = false
    }
  }

  // First-time scene compilation Coroutine while player plays!
  LaunchedEffect(currentScene, isSceneAlreadySaved) {
    if (!isSceneAlreadySaved) {
      showLoadedFromCacheBanner = false
      compilationState = ShaderCompilationState.COMPILING_FIRST_TIME
      compiledShaderCount = 6

      // Progressive compilation while user can play freely
      val totalToCompile = currentScene.totalShaders
      while (compiledShaderCount < totalToCompile) {
        // Small interval between shader stage dispatches
        delay(Random.nextLong(220, 480))
        val step = Random.nextInt(2, 6)
        compiledShaderCount = (compiledShaderCount + step).coerceAtMost(totalToCompile)
        currentCompilingModuleName = ShaderModuleNames.random()

        // Minor realistic compilation micro-stutter in FPS meter
        dynamicFps = Random.nextInt(48, 55).toFloat()
        delay(60)
        dynamicFps = 58.5f
      }

      // Compilation complete: SAVE PERMANENTLY TO LOCAL STORAGE!
      cacheManager.markSceneCompiled(currentScene.id, currentScene.totalShaders)
      isSceneAlreadySaved = true
      compilationState = ShaderCompilationState.COMPILATION_COMPLETE_SAVED
      dynamicFps = 60.0f
      showCompilationSavedBanner = true
      soundSynthesizer.vibrate(30, 160)

      // Hide confirmation banner after 4 seconds
      delay(4000)
      showCompilationSavedBanner = false
    } else {
      dynamicFps = 60.0f
    }
  }

  // Game state
  var playerX by remember { mutableFloatStateOf(0f) }
  var playerAngle by remember { mutableFloatStateOf(0f) } // Look angle in radians
  var playerPitch by remember { mutableFloatStateOf(0f) } // Vertical look
  var playerHp by remember { mutableIntStateOf(100) }
  var playerTempHp by remember { mutableIntStateOf(0) }
  var isFlashlightOn by remember { mutableStateOf(true) }
  var isCrouching by remember { mutableStateOf(false) }

  // Weapons
  val weapons = remember {
    listOf(WeaponType.PUMP_SHOTGUN, WeaponType.M16_ASSAULT, WeaponType.DUAL_PISTOLS, WeaponType.MEDKIT)
  }
  var currentWeaponIndex by remember { mutableIntStateOf(0) }
  val currentWeapon = weapons[currentWeaponIndex]
  var currentClip by remember { mutableIntStateOf(currentWeapon.maxClip) }
  var reserveAmmo by remember { mutableIntStateOf(currentWeapon.defaultReserve) }
  var isReloading by remember { mutableStateOf(false) }
  var isShoving by remember { mutableStateOf(false) }

  // Visual effects
  var muzzleFlash by remember { mutableFloatStateOf(0f) }
  var screenShake by remember { mutableFloatStateOf(0f) }
  var bloodAlpha by remember { mutableFloatStateOf(0f) }

  val initialSubtitle = remember(currentLanguage, currentScene) {
    when (currentLanguage) {
      AppLanguage.SPANISH -> "Bill: [${currentScene.nameEs}] ¡Manténganse juntos!"
      AppLanguage.ENGLISH -> "Bill: [${currentScene.nameEn}] Stay together and keep your lights on."
      AppLanguage.PORTUGUESE -> "Bill: [${currentScene.namePt}] Fiquem juntos e lanternas acesas!"
    }
  }
  var subtitleText by remember(currentLanguage, currentScene) { mutableStateOf(initialSubtitle) }

  // Stats
  var score by remember { mutableIntStateOf(0) }
  var killCount by remember { mutableIntStateOf(0) }
  var currentWave by remember { mutableIntStateOf(1) }

  // Survivors team
  val survivors = remember {
    mutableStateListOf(
      SurvivorStatus("Bill", 100, isPlayer = true),
      SurvivorStatus("Zoey", 95),
      SurvivorStatus("Louis", 85),
      SurvivorStatus("Francis", 90)
    )
  }

  // Active zombies
  val zombies = remember { mutableStateListOf<ZombieEntity>() }
  val bloodSplatters = remember { mutableStateListOf<BloodSplatter>() }

  // Spawn initial horde for this scene
  LaunchedEffect(currentScene) {
    zombies.clear()
    when (currentScene) {
      GameScene.ALLEYWAYS -> {
        zombies.add(ZombieEntity(1, ZombieType.COMMON, -1.2f, 8f, 50f))
        zombies.add(ZombieEntity(2, ZombieType.COMMON, 0.8f, 10f, 50f))
        zombies.add(ZombieEntity(3, ZombieType.COMMON, -0.4f, 13f, 50f))
        zombies.add(ZombieEntity(4, ZombieType.BOOMER, 1.5f, 16f, 110f))
      }
      GameScene.SUBWAY -> {
        zombies.add(ZombieEntity(1, ZombieType.COMMON, -0.8f, 6f, 50f))
        zombies.add(ZombieEntity(2, ZombieType.HUNTER, 1.2f, 9f, 150f))
        zombies.add(ZombieEntity(3, ZombieType.COMMON, 0.0f, 12f, 50f))
      }
      GameScene.SEWERS -> {
        zombies.add(ZombieEntity(1, ZombieType.COMMON, -1.5f, 7f, 50f))
        zombies.add(ZombieEntity(2, ZombieType.BOOMER, 0.5f, 11f, 110f))
        zombies.add(ZombieEntity(3, ZombieType.COMMON, 1.4f, 14f, 50f))
      }
      GameScene.HOSPITAL_ROOFTOP -> {
        // Boss fight with Tank!
        zombies.add(ZombieEntity(1, ZombieType.TANK, 0.0f, 12f, 1200f))
        zombies.add(ZombieEntity(2, ZombieType.COMMON, -1.8f, 8f, 50f))
        zombies.add(ZombieEntity(3, ZombieType.COMMON, 1.8f, 9f, 50f))
      }
    }
  }

  // Handle aim delta from touch controls
  LaunchedEffect(aimDelta) {
    if (aimDelta.first != 0f || aimDelta.second != 0f) {
      playerAngle += aimDelta.first * 0.006f
      playerPitch = (playerPitch - aimDelta.second * 0.8f).coerceIn(-120f, 120f)
    }
  }

  // Handle movement
  LaunchedEffect(moveVector) {
    if (moveVector.first != 0f || moveVector.second != 0f) {
      playerX += (moveVector.first * cos(playerAngle) + moveVector.second * sin(playerAngle)) * 0.1f
      playerX = playerX.coerceIn(-3.5f, 3.5f)
    }
  }

  // Handle Flashlight
  LaunchedEffect(flashlightTrigger) {
    if (flashlightTrigger) {
      isFlashlightOn = !isFlashlightOn
      soundSynthesizer.playFlashlight()
    }
  }

  // Handle Melee / Shove
  LaunchedEffect(meleeTrigger) {
    if (meleeTrigger && !isShoving) {
      isShoving = true
      soundSynthesizer.playMelee()
      zombies.forEach { z ->
        if (z.y < 3.5f && abs(z.x - playerX) < 1.8f) {
          z.y += 2.0f
          z.staggerTimer = 1.8f
          z.state = ZombieState.STAGGERED
        }
      }
      delay(300)
      isShoving = false
    }
  }

  // Handle Weapon Switch
  LaunchedEffect(switchWeaponTrigger) {
    if (switchWeaponTrigger && !isReloading) {
      currentWeaponIndex = (currentWeaponIndex + 1) % weapons.size
      val newWeapon = weapons[currentWeaponIndex]
      currentClip = newWeapon.maxClip
      reserveAmmo = newWeapon.defaultReserve
      soundSynthesizer.playReload()
      subtitleText = "Bill: Switching to ${newWeapon.displayName}"
    }
  }

  // Handle Reload
  LaunchedEffect(reloadTrigger) {
    if (reloadTrigger && !isReloading && currentClip < currentWeapon.maxClip && reserveAmmo > 0) {
      isReloading = true
      soundSynthesizer.playReload()
      subtitleText = "Bill: Reloading!"
      delay(1200)
      val needed = currentWeapon.maxClip - currentClip
      val available = minOf(needed, reserveAmmo)
      currentClip += available
      if (currentWeapon != WeaponType.DUAL_PISTOLS) {
        reserveAmmo -= available
      }
      isReloading = false
    }
  }

  // Handle Fire
  LaunchedEffect(fireTrigger) {
    if (fireTrigger && !isReloading && !isShoving) {
      if (currentClip > 0) {
        currentClip--
        muzzleFlash = 1.0f
        screenShake = 6.0f

        when (currentWeapon) {
          WeaponType.PUMP_SHOTGUN -> soundSynthesizer.playShotgun()
          WeaponType.M16_ASSAULT -> soundSynthesizer.playRifle()
          WeaponType.DUAL_PISTOLS -> soundSynthesizer.playPistol()
          WeaponType.MEDKIT -> {
            if (playerHp < 100) {
              playerHp = minOf(100, playerHp + 80)
              soundSynthesizer.playFlashlight()
              subtitleText = "Bill: All patched up!"
            }
          }
        }

        // Raycast hit check on zombies
        val targetZombie = zombies.filter { it.y > 0.5f && it.state != ZombieState.DEAD }
          .minByOrNull { it.y }

        if (targetZombie != null && abs(targetZombie.x - playerX) < 1.2f) {
          targetZombie.hp -= currentWeapon.damage
          soundSynthesizer.playZombieHurt()

          bloodSplatters.add(
            BloodSplatter(
              x = Random.nextFloat() * 0.8f + 0.1f,
              y = Random.nextFloat() * 0.8f + 0.1f,
              radius = Random.nextFloat() * 35f + 15f
            )
          )

          if (targetZombie.hp <= 0) {
            targetZombie.state = ZombieState.DEAD
            killCount++
            score += targetZombie.type.points

            if (killCount == 1) {
              onUnlockAchievement(
                XboxAchievement(
                  id = "first_kill",
                  title = "First Blood",
                  description = "Kill your first Infected.",
                  gamerscore = 10,
                  isUnlocked = true
                )
              )
            } else if (targetZombie.type == ZombieType.TANK) {
              onUnlockAchievement(
                XboxAchievement(
                  id = "tank_buster",
                  title = "Tank Buster",
                  description = "Kill a Tank without taking incapacitating damage.",
                  gamerscore = 30,
                  isUnlocked = true
                )
              )
            }
          }
        }
      } else {
        soundSynthesizer.playFlashlight()
        subtitleText = "Bill: Click! Need ammo!"
      }
    }
  }

  // Game Loop (Zombie movement, decay blood, spawn waves)
  LaunchedEffect(currentScene) {
    while (true) {
      delay(33)

      if (muzzleFlash > 0f) muzzleFlash = maxOf(0f, muzzleFlash - 0.2f)
      if (screenShake > 0f) screenShake = maxOf(0f, screenShake - 0.5f)
      if (bloodAlpha > 0f) bloodAlpha = maxOf(0f, bloodAlpha - 0.03f)

      val toRemove = mutableListOf<ZombieEntity>()
      zombies.forEach { z ->
        if (z.state == ZombieState.DEAD) {
          toRemove.add(z)
        } else if (z.state == ZombieState.STAGGERED) {
          z.staggerTimer -= 0.033f
          if (z.staggerTimer <= 0f) z.state = ZombieState.CHASING
        } else {
          z.y -= z.type.speed * 0.035f
          if (z.x < playerX) z.x += 0.01f else z.x -= 0.01f

          if (z.y <= 0.8f) {
            z.attackCooldown -= 0.033f
            if (z.attackCooldown <= 0f) {
              playerHp = maxOf(0, playerHp - z.type.damagePerHit.toInt())
              bloodAlpha = 0.85f
              soundSynthesizer.vibrate(50, 220)
              z.attackCooldown = 1.0f
              subtitleText = "Zoey: Bill's taking hits! Cover him!"
            }
          }
        }
      }
      zombies.removeAll(toRemove)

      if (zombies.isEmpty()) {
        currentWave++
        soundSynthesizer.playZombieRoar()
        subtitleText = "Francis: Here they come again! Wave $currentWave!"
        val count = minOf(6, 2 + currentWave)
        for (i in 0 until count) {
          val isTank = (currentScene == GameScene.HOSPITAL_ROOFTOP && i == 0)
          val type = if (isTank) ZombieType.TANK else ZombieType.COMMON
          zombies.add(
            ZombieEntity(
              id = Random.nextInt(),
              type = type,
              x = Random.nextFloat() * 4f - 2f,
              y = 9f + i * 2.5f,
              hp = type.maxHp
            )
          )
        }
      }
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black).testTag("l4d_game_view")) {
    val screenW = maxWidth
    val screenH = maxHeight

    // 1. 3D Game Canvas with scene ambiance
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawGameScene(
        scene = currentScene,
        playerX = playerX,
        playerAngle = playerAngle,
        playerPitch = playerPitch,
        isFlashlightOn = isFlashlightOn,
        muzzleFlash = muzzleFlash,
        screenShake = screenShake,
        zombies = zombies,
        resolutionScale = resolution.scale
      )
    }

    // 2. Blood Splatters on Screen
    if (bloodAlpha > 0f) {
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .alpha(bloodAlpha)
            .background(
              Brush.radialGradient(
                listOf(
                  Color.Transparent,
                  L4DBloodRed.copy(alpha = 0.4f),
                  L4DBloodRed.copy(alpha = 0.85f)
                )
              )
            )
      )
    }

    // 3. Dynamic Crosshair
    Box(
      modifier =
        Modifier
          .size(24.dp)
          .align(Alignment.Center),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val crossColor = if (zombies.any { it.y < 5f && abs(it.x - playerX) < 1.0f }) {
          Color.Red
        } else {
          Color.White.copy(alpha = 0.8f)
        }
        val gap = 5.dp.toPx()
        val len = 6.dp.toPx()

        drawLine(crossColor, Offset(center.x, center.y - gap - len), Offset(center.x, center.y - gap), 2f)
        drawLine(crossColor, Offset(center.x, center.y + gap), Offset(center.x, center.y + gap + len), 2f)
        drawLine(crossColor, Offset(center.x - gap - len, center.y), Offset(center.x - gap, center.y), 2f)
        drawLine(crossColor, Offset(center.x + gap, center.y), Offset(center.x + gap + len, center.y), 2f)
      }
    }

    // -------------------------------------------------------------
    // SHADER COMPILATION & DISK CACHE HUD BANNERS
    // -------------------------------------------------------------

    // Top Center: Scene Name & Scene Selector Pill
    Row(
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .padding(top = 14.dp, start = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Scene Title & Switcher Button
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC0E1116))
            .border(1.dp, Color(0xFF333F4D), RoundedCornerShape(8.dp))
            .clickable { showSceneSelectorModal = true }
            .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Layers, contentDescription = null, tint = Color(0xFFE87A4D), modifier = Modifier.size(14.dp))
          Text(
            text = "ESCENA: ${currentScene.localizedName(currentLanguage)}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
          Icon(Icons.Default.SwapHoriz, contentDescription = "Cambiar", tint = Color.LightGray, modifier = Modifier.size(14.dp))
        }
      }
    }

    // A. FIRST TIME COMPILATION IN-GAME HUD (Visible while playing!)
    if (compilationState == ShaderCompilationState.COMPILING_FIRST_TIME && !isSceneAlreadySaved) {
      Box(
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xE6151B22))
            .border(1.5.dp, Color(0xFFE87A4D), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("hud_shader_compilation")
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              Icons.Default.Memory,
              contentDescription = null,
              tint = Color(0xFFE87A4D),
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = when (currentLanguage) {
                AppLanguage.SPANISH -> "⚙️ Compilando shaders (1ª vez en esta escena)..."
                AppLanguage.ENGLISH -> "⚙️ Compiling shaders (1st time in scene)..."
                AppLanguage.PORTUGUESE -> "⚙️ Compilando shaders (1ª vez nesta cena)..."
              },
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
            Text(
              text = "$compiledShaderCount / ${currentScene.totalShaders}",
              color = Color(0xFFFFA07A),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 11.sp
            )
          }

          // Progress Bar
          LinearProgressIndicator(
            progress = { compiledShaderCount.toFloat() / currentScene.totalShaders.toFloat() },
            modifier = Modifier.width(260.dp).height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = Color(0xFFE87A4D),
            trackColor = Color(0xFF333E4D)
          )

          // Module & hint ticker
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "Puedes jugar mientras compila: guardando $currentCompilingModuleName"
              AppLanguage.ENGLISH -> "You can play while compiling: saving $currentCompilingModuleName"
              AppLanguage.PORTUGUESE -> "Você pode jogar enquanto compila: salvando $currentCompilingModuleName"
            },
            color = Color(0xFFA5B2C0),
            fontSize = 9.sp
          )
        }
      }
    }

    // B. COMPILATION COMPLETE & SAVED TO DISK BANNER
    if (showCompilationSavedBanner) {
      Box(
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xF014381C))
            .border(1.5.dp, XboxNeon, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = XboxNeon, modifier = Modifier.size(18.dp))
          Column {
            Text(
              text = when (currentLanguage) {
                AppLanguage.SPANISH -> "💾 ¡Shaders de '${currentScene.localizedName(currentLanguage)}' guardados en disco!"
                AppLanguage.ENGLISH -> "💾 Shaders for '${currentScene.localizedName(currentLanguage)}' saved to local disk!"
                AppLanguage.PORTUGUESE -> "💾 Shaders de '${currentScene.localizedName(currentLanguage)}' salvos no disco!"
              },
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
            Text(
              text = when (currentLanguage) {
                AppLanguage.SPANISH -> "Nas próximas vezes eles já ficam salvos e carregam instantaneamente."
                AppLanguage.ENGLISH -> "Next visits will load instantly from local shader cache."
                AppLanguage.PORTUGUESE -> "Nas próximas vezes eles já ficam salvas e carregam instantaneamente."
              },
              color = Color(0xFFB4E6BE),
              fontSize = 10.sp
            )
          }
        }
      }
    }

    // C. LOADED FROM CACHE BANNER (Subsequent visits!)
    if (showLoadedFromCacheBanner && isSceneAlreadySaved && !showCompilationSavedBanner) {
      Box(
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xDD121B24))
            .border(1.dp, XboxGreen, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text("⚡", fontSize = 14.sp)
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "Shaders guardados en caché local (${currentScene.totalShaders}/${currentScene.totalShaders}) • 0 ms"
              AppLanguage.ENGLISH -> "Loaded from local shader cache (${currentScene.totalShaders}/${currentScene.totalShaders}) • 0 ms"
              AppLanguage.PORTUGUESE -> "Shaders salvos no cache local (${currentScene.totalShaders}/${currentScene.totalShaders}) • 0 ms"
            },
            color = XboxNeon,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }
    }

    // 4. Subtitles (Left 4 Dead callouts)
    Box(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(top = 70.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color.Black.copy(alpha = 0.65f))
          .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
      Text(
        text = subtitleText,
        color = Color(0xFFF39C12),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
    }

    // 5. FPS and Hardware Overlay (if enabled)
    if (showFps) {
      Box(
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .padding(top = 14.dp, end = 14.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .border(1.dp, XboxGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = String.format("%.1f FPS  •  %.1f ms", dynamicFps, 1000f / dynamicFps),
            color = if (dynamicFps < 55f) Color(0xFFFFA07A) else XboxNeon,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
          Text(
            text = "${resolution.label} | ${gpuDriverName.take(16)}",
            color = Color.LightGray,
            fontSize = 9.sp
          )
          Text(
            text = if (isSceneAlreadySaved) "Shader Cache: ACTIVO (Guardado)" else "Shader Cache: COMPILANDO",
            color = if (isSceneAlreadySaved) XboxNeon else Color(0xFFFFA07A),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 6. Authentic Left 4 Dead Survivors Team HUD (Bottom Left)
    Column(
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .padding(start = 16.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      survivors.forEachIndexed { index, s ->
        val hp = if (index == 0) playerHp else s.health
        SurvivorHudItem(survivor = s, currentHp = hp)
      }
    }

    // 7. Weapon & Ammo Counter (Bottom Right)
    Box(
      modifier =
        Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 16.dp, bottom = 16.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color.Black.copy(alpha = 0.75f))
          .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
          .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = currentWeapon.displayName.uppercase(),
          color = Color(0xFFE0E0E0),
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "$currentClip",
            color = if (currentClip < 5) L4DRedHealth else Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp
          )
          Text(
            text = if (currentWeapon == WeaponType.DUAL_PISTOLS) " / ∞" else " / $reserveAmmo",
            color = Color.LightGray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 3.dp, start = 2.dp)
          )
        }
        if (isReloading) {
          val reloadLabel = when (currentLanguage) {
            AppLanguage.SPANISH -> "RECARGANDO..."
            AppLanguage.ENGLISH -> "RELOADING..."
            AppLanguage.PORTUGUESE -> "RECARREGANDO..."
          }
          Text(
            text = reloadLabel,
            color = Color(0xFFF39C12),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }

  // -------------------------------------------------------------
  // Scene Selector & Shader Cache Management Modal
  // -------------------------------------------------------------
  if (showSceneSelectorModal) {
    AlertDialog(
      onDismissRequest = { showSceneSelectorModal = false },
      containerColor = Color(0xFF13171D),
      titleColor = Color.White,
      textContentColor = Color(0xFFCCCCCC),
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "Seleccionar Escena & Shaders"
              AppLanguage.ENGLISH -> "Select Scene & Shaders"
              AppLanguage.PORTUGUESE -> "Selecionar Cena & Shaders"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
          IconButton(onClick = { showSceneSelectorModal = false }) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "La 1ª vez que visitas cada escena se compilan shaders mientras juegas. Las próximas veces ya quedan guardados."
              AppLanguage.ENGLISH -> "The 1st time you visit each scene, shaders compile while you play. Next visits are already saved."
              AppLanguage.PORTUGUESE -> "A 1ª vez que você visita cada cena, os shaders compilam enquanto joga. Nas próximas vezes eles já ficam salvas."
            },
            fontSize = 11.sp,
            color = Color(0xFFA5B2C0)
          )

          GameScene.values().forEach { sc ->
            val isCurrent = sc == currentScene
            val isCompiled = cacheManager.isSceneCompiled(sc.id)

            Card(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .border(
                    1.dp,
                    if (isCurrent) XboxNeon else if (isCompiled) Color(0xFF285E32) else Color(0xFF333F4D),
                    RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    currentScene = sc
                    showSceneSelectorModal = false
                  },
              colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (isCurrent) Color(0xFF1E2833) else Color(0xFF171B21)
              )
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = "Cap. ${sc.chapterNumber}: ${sc.localizedName(currentLanguage)}",
                      color = if (isCurrent) XboxNeon else Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp
                    )
                    if (isCurrent) {
                      Box(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(XboxGreen).padding(horizontal = 4.dp, vertical = 1.dp)
                      ) {
                        Text("ACTIVA", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                      }
                    }
                  }
                  Text(
                    text = sc.localizedDesc(currentLanguage),
                    color = Color(0xFF8E9BA8),
                    fontSize = 9.sp
                  )
                }

                // Shader status badge
                Box(
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (isCompiled) Color(0xFF18381E) else Color(0xFF332018))
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = if (isCompiled) {
                      "✓ ${sc.totalShaders} Shaders (Guardado)"
                    } else {
                      "⚙️ ${sc.totalShaders} Shaders (Sin compilar)"
                    },
                    color = if (isCompiled) XboxNeon else Color(0xFFFFA07A),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }

          // Clear Shader Cache button to re-test
          OutlinedButton(
            onClick = {
              cacheManager.clearCache()
              isSceneAlreadySaved = false
              compilationState = ShaderCompilationState.COMPILING_FIRST_TIME
              showSceneSelectorModal = false
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA07A)),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              when (currentLanguage) {
                AppLanguage.SPANISH -> "Limpiar Caché de Shaders (Probar compilación de nuevo)"
                AppLanguage.ENGLISH -> "Clear Shader Cache (Re-test compilation)"
                AppLanguage.PORTUGUESE -> "Limpar Cache de Shaders (Testar compilação novamente)"
              },
              fontSize = 11.sp
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showSceneSelectorModal = false },
          colors = ButtonDefaults.buttonColors(containerColor = XboxGreen)
        ) {
          Text("OK", color = Color.White)
        }
      }
    )
  }
}

// -------------------------------------------------------------
// 3D Corridor and Zombie Drawing Function with Scene Ambiance
// -------------------------------------------------------------
private fun DrawScope.drawGameScene(
  scene: GameScene,
  playerX: Float,
  playerAngle: Float,
  playerPitch: Float,
  isFlashlightOn: Boolean,
  muzzleFlash: Float,
  screenShake: Float,
  zombies: List<ZombieEntity>,
  resolutionScale: Float
) {
  val w = size.width
  val h = size.height
  val horizon = h / 2f + playerPitch + (sin(screenShake.toDouble()) * 8f).toFloat()

  // Scene colors
  val skyTopColor = Color(scene.skyColor)
  val groundBaseColor = Color(scene.groundColor)

  // 1. Ceiling / Sky
  drawRect(
    brush =
      Brush.verticalGradient(
        listOf(skyTopColor, Color(0xFF090C10)),
        startY = 0f,
        endY = horizon
      ),
    topLeft = Offset(0f, 0f),
    size = Size(w, horizon)
  )

  // 2. Floor
  drawRect(
    brush =
      Brush.verticalGradient(
        listOf(groundBaseColor, Color(0xFF07090C)),
        startY = horizon,
        endY = h
      ),
    topLeft = Offset(0f, horizon),
    size = Size(w, h - horizon)
  )

  // Corridor walls (Left & Right 3D perspective trapezoids)
  val wallColor1 = when (scene) {
    GameScene.ALLEYWAYS -> Color(0xFF241A14) // Dark brick red
    GameScene.SUBWAY -> Color(0xFF1E282E) // Subway tile metallic
    GameScene.SEWERS -> Color(0xFF15261C) // Murky sewer moss
    GameScene.HOSPITAL_ROOFTOP -> Color(0xFF1C222C) // Hospital concrete
  }
  val wallColor2 = Color(0xFF0B0E12)

  val leftWallPath = Path().apply {
    moveTo(0f, 0f)
    lineTo(w * 0.22f, horizon * 0.7f)
    lineTo(w * 0.22f, horizon + (h - horizon) * 0.3f)
    lineTo(0f, h)
    close()
  }
  drawPath(
    path = leftWallPath,
    brush = Brush.horizontalGradient(listOf(wallColor1, wallColor2))
  )

  val rightWallPath = Path().apply {
    moveTo(w, 0f)
    lineTo(w * 0.78f, horizon * 0.7f)
    lineTo(w * 0.78f, horizon + (h - horizon) * 0.3f)
    lineTo(w, h)
    close()
  }
  drawPath(
    path = rightWallPath,
    brush = Brush.horizontalGradient(listOf(wallColor2, wallColor1))
  )

  // Safe room door at the end of corridor
  val doorWidth = w * 0.18f
  val doorHeight = h * 0.28f
  val doorLeft = (w - doorWidth) / 2f
  val doorTop = horizon - doorHeight * 0.6f

  drawRect(
    color = Color(0xFF781818),
    topLeft = Offset(doorLeft, doorTop),
    size = Size(doorWidth, doorHeight)
  )
  drawRect(
    color = Color.White.copy(alpha = 0.7f),
    topLeft = Offset(doorLeft + doorWidth * 0.1f, doorTop + doorHeight * 0.1f),
    size = Size(doorWidth * 0.8f, doorHeight * 0.15f)
  )

  // 3. Flashlight Beam illumination
  if (isFlashlightOn) {
    drawCircle(
      brush =
        Brush.radialGradient(
          listOf(
            Color(0xFFFFFBE6).copy(alpha = 0.38f),
            Color(0xFFFFF3B8).copy(alpha = 0.18f),
            Color.Transparent
          ),
          center = Offset(w / 2f, horizon),
          radius = w * 0.45f
        ),
      radius = w * 0.45f,
      center = Offset(w / 2f, horizon)
    )
  }

  // 4. Draw Zombies sorted by distance (furthest to closest)
  zombies.sortedByDescending { it.y }.forEach { zombie ->
    if (zombie.y > 0.5f) {
      val distance = zombie.y
      val screenX = (w / 2f) + (zombie.x - playerX) * (w / distance) * 0.7f
      val spriteHeight = (h / distance) * 0.8f * zombie.type.scale
      val spriteWidth = spriteHeight * 0.55f
      val screenY = horizon + (h - horizon) * 0.1f - spriteHeight * 0.6f

      val zombieColor = when (zombie.type) {
        ZombieType.TANK -> Color(0xFF6B2B1B)
        ZombieType.BOOMER -> Color(0xFF4A5528)
        ZombieType.HUNTER -> Color(0xFF1F2B38)
        ZombieType.COMMON -> Color(0xFF384034)
      }

      drawOval(
        color = zombieColor,
        topLeft = Offset(screenX - spriteWidth / 2f, screenY),
        size = Size(spriteWidth, spriteHeight)
      )

      val eyeY = screenY + spriteHeight * 0.18f
      val eyeSpacing = spriteWidth * 0.16f
      drawCircle(
        color = if (zombie.type == ZombieType.TANK) Color.Red else Color(0xFFE5FF66),
        radius = spriteWidth * 0.08f,
        center = Offset(screenX - eyeSpacing, eyeY)
      )
      drawCircle(
        color = if (zombie.type == ZombieType.TANK) Color.Red else Color(0xFFE5FF66),
        radius = spriteWidth * 0.08f,
        center = Offset(screenX + eyeSpacing, eyeY)
      )

      // Tank Health Bar
      if (zombie.type == ZombieType.TANK) {
        val barW = spriteWidth * 1.4f
        val barH = 7.dp.toPx()
        val barX = screenX - barW / 2f
        val barY = screenY - 14.dp.toPx()

        drawRect(
          color = Color.Black.copy(alpha = 0.8f),
          topLeft = Offset(barX, barY),
          size = Size(barW, barH)
        )
        val hpFrac = (zombie.hp / zombie.type.maxHp).coerceIn(0f, 1f)
        drawRect(
          color = Color.Red,
          topLeft = Offset(barX, barY),
          size = Size(barW * hpFrac, barH)
        )
      }
    }
  }

  // 5. Muzzle Flash Lighting
  if (muzzleFlash > 0f) {
    drawCircle(
      color = Color(0xFFFFD54F).copy(alpha = muzzleFlash * 0.45f),
      radius = w * 0.5f,
      center = Offset(w * 0.7f, h * 0.8f)
    )
  }
}
