package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.audio.SoundSynthesizer
import com.example.controller.GamepadInputManager
import com.example.controller.XboxTouchOverlay
import com.example.game.L4DGameView
import com.example.guide.Xbox360GuideOverlay
import com.example.guide.XboxAchievementToast
import com.example.model.AntiAliasingConfig
import com.example.model.AppLanguage
import com.example.model.AspectRatioConfig
import com.example.model.GpuDriverItem
import com.example.model.ResolutionConfig
import com.example.model.RomFile
import com.example.model.ScreenScaleConfig
import com.example.model.TouchSettings
import com.example.model.Translations
import com.example.model.XboxAchievement
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppScreen {
  FRONTEND,
  IN_GAME
}

@Composable
fun MainAppScreen(gamepadManager: GamepadInputManager? = null) {
  val context = LocalContext.current
  val soundSynthesizer = remember { SoundSynthesizer(context) }
  val scope = rememberCoroutineScope()

  // App navigation state
  var currentScreen by remember { mutableStateOf(AppScreen.FRONTEND) }

  // App Language: Default to Spanish as requested ("Agregar idiomas spañol e ingles")
  var currentLanguage by remember { mutableStateOf(AppLanguage.SPANISH) }
  val translation = remember(currentLanguage) { Translations.get(currentLanguage) }

  // Emulator configuration
  var selectedResolution by remember { mutableStateOf(ResolutionConfig.P540) }
  var selectedAspect by remember { mutableStateOf(AspectRatioConfig.RATIO_16_9) }
  var selectedAA by remember { mutableStateOf(AntiAliasingConfig.FXAA) }
  var selectedScale by remember { mutableStateOf(ScreenScaleConfig.NORMAL) }
  var fpsPanelEnabled by remember { mutableStateOf(true) }
  var selectedGpuDriver by remember { mutableStateOf(GpuDriverItem.DefaultYuzuTurnipDrivers[1]) } // Turnip v24.3.0
  var touchSettings by remember { mutableStateOf(TouchSettings()) }

  // Bluetooth gamepad settings
  var hideTouchWhenGamepadConnected by remember { mutableStateOf(false) }

  // Active ROM
  var currentRom by remember {
    mutableStateOf(
      RomFile(
        id = "l4d1",
        title = "Left 4 Dead (USA)",
        path = "/storage/emulated/0/Movies/Left.4.Dead.USA.X360-ZTM",
        fileSize = "4.8 GB",
        format = "ISO",
        isBuiltIn = true,
        description = "Left 4 Dead Xbox 360 ISO oficial con controles táctiles y Bluetooth."
      )
    )
  }

  // Guide Menu overlay state
  var isGuideOpen by remember { mutableStateOf(false) }

  // Achievements
  val achievements = remember {
    mutableStateListOf(
      XboxAchievement("first_kill", "First Blood", "Kill your first Infected.", 10),
      XboxAchievement("stand_tall", "Stand Tall", "Survive a horde attack and eliminate 10 zombies.", 20),
      XboxAchievement("tank_buster", "Tank Buster", "Kill a Tank without taking incapacitating damage.", 30),
      XboxAchievement("mercy_killer", "Mercy Killer", "Survive the No Mercy campaign.", 50),
      XboxAchievement("witch_hunter", "Witch Hunter", "Kill a Witch without disturbing her.", 25)
    )
  }
  var activeToastAchievement by remember { mutableStateOf<XboxAchievement?>(null) }

  // In-Game Touch & Bluetooth Input States
  var moveVector by remember { mutableStateOf(Pair(0f, 0f)) }
  var aimDelta by remember { mutableStateOf(Pair(0f, 0f)) }
  var fireTrigger by remember { mutableStateOf(false) }
  var meleeTrigger by remember { mutableStateOf(false) }
  var reloadTrigger by remember { mutableStateOf(false) }
  var switchWeaponTrigger by remember { mutableStateOf(false) }
  var flashlightTrigger by remember { mutableStateOf(false) }

  // Hook up Bluetooth Gamepad listeners
  LaunchedEffect(gamepadManager) {
    gamepadManager?.let { gp ->
      gp.onFireDown = {
        fireTrigger = true
        if (touchSettings.hapticsEnabled) soundSynthesizer.vibrate(30, 200)
      }
      gp.onFireUp = { fireTrigger = false }
      gp.onMelee = {
        meleeTrigger = true
        scope.launch { delay(100); meleeTrigger = false }
      }
      gp.onReload = {
        reloadTrigger = true
        scope.launch { delay(100); reloadTrigger = false }
      }
      gp.onSwitchWeapon = {
        switchWeaponTrigger = true
        scope.launch { delay(100); switchWeaponTrigger = false }
      }
      gp.onFlashlight = {
        flashlightTrigger = true
        scope.launch { delay(100); flashlightTrigger = false }
      }
      gp.onJump = { soundSynthesizer.vibrate(25, 120) }
      gp.onCrouch = { soundSynthesizer.vibrate(20, 100) }
      gp.onGuide = { isGuideOpen = !isGuideOpen }
    }
  }

  // Handle Bluetooth Gamepad Analog Movement
  LaunchedEffect(gamepadManager?.moveStickX, gamepadManager?.moveStickY) {
    gamepadManager?.let { gp ->
      if (gp.moveStickX != 0f || gp.moveStickY != 0f) {
        moveVector = Pair(gp.moveStickX, gp.moveStickY)
      }
    }
  }

  // Handle Bluetooth Gamepad Analog Look / Aim
  LaunchedEffect(gamepadManager?.lookStickX, gamepadManager?.lookStickY) {
    gamepadManager?.let { gp ->
      if (gp.lookStickX != 0f || gp.lookStickY != 0f) {
        aimDelta = Pair(gp.lookStickX * 14f * touchSettings.sensitivity, gp.lookStickY * 14f * touchSettings.sensitivity)
      }
    }
  }

  // Back handler
  BackHandler {
    if (isGuideOpen) {
      isGuideOpen = false
    } else if (currentScreen == AppScreen.IN_GAME) {
      isGuideOpen = true
    }
  }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black)
  ) {
    when (currentScreen) {
      AppScreen.FRONTEND -> {
        EmulatorFrontendScreen(
          currentLanguage = currentLanguage,
          onLanguageChange = { currentLanguage = it },
          selectedResolution = selectedResolution,
          onResolutionChange = { selectedResolution = it },
          selectedAspect = selectedAspect,
          onAspectChange = { selectedAspect = it },
          selectedAA = selectedAA,
          onAAChange = { selectedAA = it },
          selectedScale = selectedScale,
          onScaleChange = { selectedScale = it },
          fpsPanelEnabled = fpsPanelEnabled,
          onFpsPanelToggle = { fpsPanelEnabled = it },
          selectedGpuDriver = selectedGpuDriver,
          onGpuDriverChange = { selectedGpuDriver = it },
          touchSettings = touchSettings,
          onTouchSettingsChange = { touchSettings = it },
          currentRom = currentRom,
          onSelectRom = { currentRom = it },
          isGamepadConnected = gamepadManager?.isGamepadConnected ?: false,
          connectedGamepadName = gamepadManager?.connectedGamepadName ?: "",
          hideTouchOnGamepad = hideTouchWhenGamepadConnected,
          onToggleHideTouchOnGamepad = { hideTouchWhenGamepadConnected = it },
          onLaunchGame = {
            soundSynthesizer.playXboxChime()
            currentScreen = AppScreen.IN_GAME
          }
        )
      }

      AppScreen.IN_GAME -> {
        // 1. Playable Left 4 Dead Game View
        L4DGameView(
          resolution = selectedResolution,
          showFps = fpsPanelEnabled,
          gpuDriverName = selectedGpuDriver.displayName,
          soundSynthesizer = soundSynthesizer,
          currentLanguage = currentLanguage,
          onUnlockAchievement = { ach ->
            val existing = achievements.find { it.id == ach.id }
            if (existing != null && !existing.isUnlocked) {
              existing.isUnlocked = true
              activeToastAchievement = existing
              soundSynthesizer.playAchievementChime()
              scope.launch {
                delay(3500)
                activeToastAchievement = null
              }
            }
          },
          onExitGame = {
            currentScreen = AppScreen.FRONTEND
          },
          moveVector = moveVector,
          aimDelta = aimDelta,
          fireTrigger = fireTrigger,
          meleeTrigger = meleeTrigger,
          reloadTrigger = reloadTrigger,
          switchWeaponTrigger = switchWeaponTrigger,
          flashlightTrigger = flashlightTrigger
        )

        // 2. Xbox 360 Touch Controls Overlay (Hidden if user connected Bluetooth gamepad and opted to hide)
        val shouldShowTouchOverlay = !(hideTouchWhenGamepadConnected && (gamepadManager?.isGamepadConnected == true))
        if (shouldShowTouchOverlay) {
          XboxTouchOverlay(
            settings = touchSettings,
            onSettingsChanged = { touchSettings = it },
            translation = translation,
            onMove = { dx, dy -> moveVector = Pair(dx, dy) },
            onAim = { dx, dy -> aimDelta = Pair(dx, dy) },
            onFireDown = {
              fireTrigger = true
              if (touchSettings.hapticsEnabled) soundSynthesizer.vibrate(30, 200)
            },
            onFireUp = {
              fireTrigger = false
            },
            onMelee = {
              meleeTrigger = true
              scope.launch {
                delay(100)
                meleeTrigger = false
              }
            },
            onReload = {
              reloadTrigger = true
              scope.launch {
                delay(100)
                reloadTrigger = false
              }
            },
            onSwitchWeapon = {
              switchWeaponTrigger = true
              scope.launch {
                delay(100)
                switchWeaponTrigger = false
              }
            },
            onFlashlightToggle = {
              flashlightTrigger = true
              scope.launch {
                delay(100)
                flashlightTrigger = false
              }
            },
            onCrouchToggle = {
              soundSynthesizer.vibrate(20, 100)
            },
            onJump = {
              soundSynthesizer.vibrate(25, 120)
            },
            onOpenGuide = {
              soundSynthesizer.playXboxChime()
              isGuideOpen = true
            },
            onStartButton = {
              isGuideOpen = true
            },
            onBackButton = {
              isGuideOpen = true
            }
          )
        }

        // 3. Xbox Achievement Toast Popup
        XboxAchievementToast(
          achievement = activeToastAchievement,
          translation = translation,
          onDismiss = { activeToastAchievement = null }
        )

        // 4. Xbox 360 Guide Overlay (Slides in when Xbox Guide button or Start is pressed)
        Xbox360GuideOverlay(
          isOpen = isGuideOpen,
          translation = translation,
          onClose = { isGuideOpen = false },
          onExitToMenu = {
            isGuideOpen = false
            currentScreen = AppScreen.FRONTEND
          },
          achievements = achievements,
          touchSettings = touchSettings,
          onTouchSettingsChanged = { touchSettings = it }
        )
      }
    }
  }
}
