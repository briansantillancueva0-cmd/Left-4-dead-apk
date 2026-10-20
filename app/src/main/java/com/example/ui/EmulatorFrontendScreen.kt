package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AntiAliasingConfig
import com.example.model.AppLanguage
import com.example.model.AspectRatioConfig
import com.example.model.GpuDriverItem
import com.example.model.ResolutionConfig
import com.example.model.RomFile
import com.example.model.ScreenScaleConfig
import com.example.model.TouchSettings
import com.example.model.TranslationStrings
import com.example.model.Translations
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeon

@Composable
fun EmulatorFrontendScreen(
  currentLanguage: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  selectedResolution: ResolutionConfig,
  onResolutionChange: (ResolutionConfig) -> Unit,
  selectedAspect: AspectRatioConfig,
  onAspectChange: (AspectRatioConfig) -> Unit,
  selectedAA: AntiAliasingConfig,
  onAAChange: (AntiAliasingConfig) -> Unit,
  selectedScale: ScreenScaleConfig,
  onScaleChange: (ScreenScaleConfig) -> Unit,
  fpsPanelEnabled: Boolean,
  onFpsPanelToggle: (Boolean) -> Unit,
  selectedGpuDriver: GpuDriverItem,
  onGpuDriverChange: (GpuDriverItem) -> Unit,
  touchSettings: TouchSettings,
  onTouchSettingsChange: (TouchSettings) -> Unit,
  currentRom: RomFile,
  onSelectRom: (RomFile) -> Unit,
  isGamepadConnected: Boolean = false,
  connectedGamepadName: String = "",
  hideTouchOnGamepad: Boolean = false,
  onToggleHideTouchOnGamepad: (Boolean) -> Unit = {},
  shaderCacheManager: com.example.game.ShaderCacheManager? = null,
  onOpenButtonMapping: () -> Unit = {},
  onLaunchGame: () -> Unit
) {
  val t = remember(currentLanguage) { Translations.get(currentLanguage) }

  var showRomDialog by remember { mutableStateOf(false) }
  var showDriverDialog by remember { mutableStateOf(false) }
  var showTouchSettingsDialog by remember { mutableStateOf(false) }
  var showBluetoothDialog by remember { mutableStateOf(false) }
  var showShaderCacheDialog by remember { mutableStateOf(false) }
  var showLicenceDialog by remember { mutableStateOf(false) }
  var showLogDialog by remember { mutableStateOf(false) }

  // Yuzu Turnip Drivers Catalog
  val installedDrivers = remember {
    mutableStateListOf<GpuDriverItem>().apply {
      addAll(GpuDriverItem.DefaultYuzuTurnipDrivers)
    }
  }

  // Internal library of ROMs (XBLA and ISOs)
  val internalRoms = remember {
    mutableStateListOf(
      RomFile(
        id = "l4d1",
        title = "Left 4 Dead (USA)",
        path = "/storage/emulated/0/Movies/Left.4.Dead.USA.X360-ZTM",
        fileSize = "4.8 GB",
        format = "ISO",
        isBuiltIn = true,
        description = "Left 4 Dead Xbox 360 ISO oficial con controles táctiles y Bluetooth."
      ),
      RomFile(
        id = "l4d2",
        title = "Left 4 Dead 2 (Survival)",
        path = "/storage/emulated/0/Games/Xbox360/Left.4.Dead.2.USA.X360.ISO",
        fileSize = "6.2 GB",
        format = "ISO",
        isBuiltIn = true,
        description = "Left 4 Dead 2 con modo Supervivencia y nuevos infectados especiales."
      ),
      RomFile(
        id = "l4d_crash",
        title = "L4D: Crash Course DLC",
        path = "/storage/emulated/0/Xbox360/XBLA/58410972/CrashCourse.XBLA",
        fileSize = "980 MB",
        format = "XBLA",
        isBuiltIn = true,
        description = "Campaña oficial Xbox Live Arcade (XBLA) de Left 4 Dead."
      )
    )
  }

  // System SAF File Picker for loading external ISO/XBLA files
  val filePickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
      uri?.let {
        val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "Game.iso"
        val ext = if (fileName.contains('.')) fileName.substringAfterLast('.').uppercase() else "ISO"
        val customRom =
          RomFile(
            id = "custom_${System.currentTimeMillis()}",
            title = fileName.substringBeforeLast('.').replace('.', ' '),
            path = it.path ?: "/storage/emulated/0/Movies/$fileName",
            fileSize = "4.5 GB",
            format = if (ext in listOf("ISO", "XBLA", "XEX")) ext else "ISO",
            isBuiltIn = false,
            description = "Archivo cargado desde el almacenamiento del dispositivo."
          )
        internalRoms.add(customRom)
        onSelectRom(customRom)
      }
    }

  // File Picker to install custom Yuzu / Turnip Driver ZIP
  val driverZipPickerLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
      uri?.let {
        val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "turnip_driver.zip"
        val cleanName = fileName.removeSuffix(".zip").replace('_', ' ')
        val customDriver =
          GpuDriverItem(
            id = "custom_zip_${System.currentTimeMillis()}",
            displayName = cleanName,
            description = "Driver personalizado Yuzu instalado desde $fileName",
            version = "Custom Zip",
            author = "Custom / Mesa",
            vulkanVersion = "Vulkan 1.3",
            isCustom = true
          )
        installedDrivers.add(customDriver)
        onGpuDriverChange(customDriver)
      }
    }

  BoxWithConstraints(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color(0xFF0F0907))
  ) {
    val isLandscape = maxWidth > maxHeight

    // 1. Full-bleed background texture (Matching user uploaded image)
    Image(
      painter = painterResource(id = R.drawable.img_rusty_wall),
      contentDescription = null,
      modifier = Modifier.fillMaxSize().alpha(0.65f),
      contentScale = ContentScale.Crop
    )

    // Dark gradient overlay for visual contrast
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.horizontalGradient(
              listOf(
                Color(0xEE0B0D0F),
                Color(0xCC12161A),
                Color(0xD91B1510)
              )
            )
          )
    )

    // Top Navigation & Actions Bar
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp)
          .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Xbox 360 title & emblem
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier =
            Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(XboxGreen),
          contentAlignment = Alignment.Center
        ) {
          Text("X", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(
          text = "XBOX 360 • LEFT 4 DEAD",
          color = Color(0xFFD4AC0D),
          fontSize = 11.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )
      }

      // Action shortcuts: Language Pills, Bluetooth, Touch, ROMs
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Direct Language Switcher Pills: Español, English, Português
        Row(
          modifier =
            Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x44000000))
              .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
              .padding(2.dp),
          horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          AppLanguage.values().forEach { lang ->
            val isSelected = (lang == currentLanguage)
            Box(
              modifier =
                Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .then(
                    if (isSelected) {
                      Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFE87A4D), Color(0xFFD35400))))
                    } else {
                      Modifier.background(Color.Transparent)
                    }
                  )
                  .clickable { onLanguageChange(lang) }
                  .padding(horizontal = 7.dp, vertical = 3.dp),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Text(lang.flag, fontSize = 11.sp)
                Text(
                  text = lang.displayName,
                  color = if (isSelected) Color.White else Color(0xFFC7B19C),
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }

        // Bluetooth Gamepad Shortcut Button
        Box(
          modifier =
            Modifier
              .testTag("btn_top_bluetooth")
              .clip(RoundedCornerShape(14.dp))
              .background(if (isGamepadConnected) Color(0xFF1B4D24) else Color(0x33FFFFFF))
              .border(
                1.dp,
                if (isGamepadConnected) XboxNeon else Color(0x44D4AC0D),
                RoundedCornerShape(14.dp)
              )
              .clickable { showBluetoothDialog = true }
              .padding(horizontal = 9.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              Icons.Default.SportsEsports,
              contentDescription = null,
              tint = if (isGamepadConnected) XboxNeon else Color(0xFFFFD54F),
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = if (isGamepadConnected) "BT ✓" else t.bluetoothBtn,
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Touch controls dialog button
        Box(
          modifier =
            Modifier
              .testTag("btn_top_touch_settings")
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x33FFFFFF))
              .border(1.dp, Color(0x44D4AC0D), RoundedCornerShape(14.dp))
              .clickable { showTouchSettingsDialog = true }
              .padding(horizontal = 8.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Gamepad, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
            Text(t.touchControlsBtn, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }

        // Interactive Xbox 360 Controller Button Mapping Button
        Box(
          modifier =
            Modifier
              .testTag("btn_top_mapping")
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x33107C10))
              .border(1.dp, XboxNeon, RoundedCornerShape(14.dp))
              .clickable { onOpenButtonMapping() }
              .padding(horizontal = 8.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = XboxNeon, modifier = Modifier.size(14.dp))
            Text(
              when (currentLanguage) {
                AppLanguage.SPANISH -> "Mapeo"
                AppLanguage.ENGLISH -> "Mapping"
                AppLanguage.PORTUGUESE -> "Mapeamento"
              },
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Shader Cache dialog button
        Box(
          modifier =
            Modifier
              .testTag("btn_top_shaders")
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x33FFFFFF))
              .border(1.dp, Color(0xFFE87A4D), RoundedCornerShape(14.dp))
              .clickable { showShaderCacheDialog = true }
              .padding(horizontal = 8.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFFFFA07A), modifier = Modifier.size(14.dp))
            Text(
              when (currentLanguage) {
                AppLanguage.SPANISH -> "Shaders"
                AppLanguage.ENGLISH -> "Shaders"
                AppLanguage.PORTUGUESE -> "Shaders"
              },
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // ROMs library button
        Box(
          modifier =
            Modifier
              .testTag("btn_top_roms")
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0x33FFFFFF))
              .border(1.dp, Color(0x44D4AC0D), RoundedCornerShape(14.dp))
              .clickable { showRomDialog = true }
              .padding(horizontal = 8.dp, vertical = 5.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
            Text("${t.gamesLibraryBtn} (${internalRoms.size})", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Main Layout (Landscape or Portrait)
    if (isLandscape) {
      Row(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(top = 44.dp, start = 14.dp, end = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Left Column: Exact Left 4 Dead Survivors Poster Art + JOGAR + Path + Links
        Box(
          modifier =
            Modifier
              .weight(0.44f)
              .fillMaxHeight(),
          contentAlignment = Alignment.Center
        ) {
          LeftGamePaneFaithful(
            t = t,
            currentRom = currentRom,
            onOpenRomPicker = { showRomDialog = true },
            onLaunchGame = onLaunchGame,
            onOpenLicences = { showLicenceDialog = true },
            onOpenLogs = { showLogDialog = true }
          )
        }

        // Right Column: Configuration Cards (Resolution, Screen, AA, Scale, FPS, Yuzu Driver)
        Box(
          modifier =
            Modifier
              .weight(0.56f)
              .fillMaxHeight()
              .verticalScroll(rememberScrollState())
        ) {
          RightSettingsPaneLocalized(
            t = t,
            selectedResolution = selectedResolution,
            onResolutionChange = onResolutionChange,
            selectedAspect = selectedAspect,
            onAspectChange = onAspectChange,
            selectedAA = selectedAA,
            onAAChange = onAAChange,
            selectedScale = selectedScale,
            onScaleChange = onScaleChange,
            fpsPanelEnabled = fpsPanelEnabled,
            onFpsPanelToggle = onFpsPanelToggle,
            selectedGpuDriver = selectedGpuDriver,
            onOpenGpuDriverDialog = { showDriverDialog = true }
          )
        }
      }
    } else {
      // Portrait scrollable layout
      Column(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 14.dp, end = 14.dp, bottom = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        LeftGamePaneFaithful(
          t = t,
          currentRom = currentRom,
          onOpenRomPicker = { showRomDialog = true },
          onLaunchGame = onLaunchGame,
          onOpenLicences = { showLicenceDialog = true },
          onOpenLogs = { showLogDialog = true }
        )

        RightSettingsPaneLocalized(
          t = t,
          selectedResolution = selectedResolution,
          onResolutionChange = onResolutionChange,
          selectedAspect = selectedAspect,
          onAspectChange = onAspectChange,
          selectedAA = selectedAA,
          onAAChange = onAAChange,
          selectedScale = selectedScale,
          onScaleChange = onScaleChange,
          fpsPanelEnabled = fpsPanelEnabled,
          onFpsPanelToggle = onFpsPanelToggle,
          selectedGpuDriver = selectedGpuDriver,
          onOpenGpuDriverDialog = { showDriverDialog = true }
        )
      }
    }
  }

  // Dialog: Yuzu / Turnip GPU Driver Manager
  if (showDriverDialog) {
    YuzuGpuDriverManagerDialog(
      t = t,
      drivers = installedDrivers,
      currentDriver = selectedGpuDriver,
      onSelect = {
        onGpuDriverChange(it)
        showDriverDialog = false
      },
      onInstallCustomZip = {
        driverZipPickerLauncher.launch(
          arrayOf(
            "application/zip",
            "application/octet-stream",
            "*/*"
          )
        )
        showDriverDialog = false
      },
      onDismiss = { showDriverDialog = false }
    )
  }

  // Dialog: Bluetooth Gamepad Controls
  if (showBluetoothDialog) {
    BluetoothGamepadDialog(
      t = t,
      isConnected = isGamepadConnected,
      deviceName = connectedGamepadName,
      hideTouchOnGamepad = hideTouchOnGamepad,
      onToggleHideTouch = onToggleHideTouchOnGamepad,
      onDismiss = { showBluetoothDialog = false }
    )
  }

  // Dialog: Select ROM (Internal or External SAF)
  if (showRomDialog) {
    RomSelectionDialogLocalized(
      t = t,
      roms = internalRoms,
      currentRom = currentRom,
      onSelect = {
        onSelectRom(it)
        showRomDialog = false
      },
      onLoadExternalFile = {
        filePickerLauncher.launch(
          arrayOf(
            "application/octet-stream",
            "application/x-iso9660-image",
            "*/*"
          )
        )
        showRomDialog = false
      },
      onDismiss = { showRomDialog = false }
    )
  }

  // Dialog: Touch Controls Settings
  if (showTouchSettingsDialog) {
    TouchControlsConfigDialogLocalized(
      t = t,
      settings = touchSettings,
      onSave = {
        onTouchSettingsChange(it)
        showTouchSettingsDialog = false
      },
      onDismiss = { showTouchSettingsDialog = false }
    )
  }

  // Dialog: Licenças
  if (showLicenceDialog) {
    AlertDialog(
      onDismissRequest = { showLicenceDialog = false },
      title = { Text(t.licencesLink, color = Color.White, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "Left 4 Dead © Valve Corporation.\nXbox 360 © Microsoft Corporation.\nMesa Turnip Drivers © Mesa3D / Kimchi.\nEmulador Android con soporte completo para mandos Bluetooth, controladores Yuzu/Turnip, y archivos ISO/XBLA.",
          color = Color.LightGray,
          fontSize = 12.sp
        )
      },
      confirmButton = {
        TextButton(onClick = { showLicenceDialog = false }) {
          Text(t.closeBtn, color = XboxNeon)
        }
      },
      containerColor = Color(0xFF1E2124)
    )
  }

  // Dialog: Enviar log
  if (showLogDialog) {
    AlertDialog(
      onDismissRequest = { showLogDialog = false },
      title = { Text(t.sendLogLink, color = Color.White, fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "[X360-Core] Initializing Virtual Xenon PPC Core (3 cores @ 3.2GHz)...\n[GPU Driver] Active: ${selectedGpuDriver.displayName} (${selectedGpuDriver.vulkanVersion})\n[ROM] Mounted: ${currentRom.path}\n[Bluetooth Input] Connected: $isGamepadConnected ($connectedGamepadName)\n[Vulkan Pipeline] Shaders compiled: 412.",
          color = Color(0xFF52FF00),
          fontSize = 11.sp,
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
      },
      confirmButton = {
        TextButton(onClick = { showLogDialog = false }) {
          Text(t.closeBtn, color = XboxNeon)
        }
      },
      containerColor = Color(0xFF121415)
    )
  }
}

// -------------------------------------------------------------
// Left Pane: Exact reproduction of user's uploaded image
// -------------------------------------------------------------
@Composable
fun LeftGamePaneFaithful(
  t: TranslationStrings,
  currentRom: RomFile,
  onOpenRomPicker: () -> Unit,
  onLaunchGame: () -> Unit,
  onOpenLicences: () -> Unit,
  onOpenLogs: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxWidth()
  ) {
    // 1. Survivors on Rooftop + Wire Fence Art from user's screenshot
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(130.dp)
          .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_survivors_fence),
        contentDescription = "Left 4 Dead Survivors",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 2. Iconic LEFT 4 DEAD Logo with red blood splash
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(60.dp),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_l4d_logo),
        contentDescription = "Left 4 Dead Logo",
        modifier = Modifier.fillMaxHeight(),
        contentScale = ContentScale.Fit
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // 3. JOGAR / JUGAR / PLAY Button (Matching Screenshot exactly!)
    Box(
      modifier =
        Modifier
          .testTag("btn_play_jogar")
          .width(260.dp)
          .height(52.dp)
          .clip(RoundedCornerShape(26.dp))
          .background(
            Brush.horizontalGradient(
              listOf(
                Color(0xFFE87A4D),
                Color(0xFFCE422B),
                Color(0xFFB52B1E)
              )
            )
          )
          .clickable { onLaunchGame() },
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = t.playBtn,
        color = Color(0xFFFFF0E6),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        letterSpacing = 4.sp
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 4. File path: /storage/emulated/0/... • toque para trocar
    Row(
      modifier =
        Modifier
          .clickable { onOpenRomPicker() }
          .padding(horizontal = 8.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${currentRom.path} • ${t.touchToChangePath}",
        color = Color(0xFFBCAAA4),
        fontSize = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 5. Links: Licenças • Carregar ISO/XBLA • Enviar log
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = t.licencesLink,
        color = Color(0xFF8D7B6F),
        fontSize = 10.sp,
        modifier = Modifier.clickable { onOpenLicences() }
      )
      Text(
        text = t.loadIsoXblaLink,
        color = Color(0xFFE59866),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { onOpenRomPicker() }
      )
      Text(
        text = t.sendLogLink,
        color = Color(0xFF8D7B6F),
        fontSize = 10.sp,
        modifier = Modifier.clickable { onOpenLogs() }
      )
    }
  }
}

// -------------------------------------------------------------
// Right Settings Pane: Localized Cards matching user screenshot
// -------------------------------------------------------------
@Composable
fun RightSettingsPaneLocalized(
  t: TranslationStrings,
  selectedResolution: ResolutionConfig,
  onResolutionChange: (ResolutionConfig) -> Unit,
  selectedAspect: AspectRatioConfig,
  onAspectChange: (AspectRatioConfig) -> Unit,
  selectedAA: AntiAliasingConfig,
  onAAChange: (AntiAliasingConfig) -> Unit,
  selectedScale: ScreenScaleConfig,
  onScaleChange: (ScreenScaleConfig) -> Unit,
  fpsPanelEnabled: Boolean,
  onFpsPanelToggle: (Boolean) -> Unit,
  selectedGpuDriver: GpuDriverItem,
  onOpenGpuDriverDialog: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Card 1: Resolução (360p, 540p, 720p, 1080p)
    SettingsCard(title = t.resolutionTitle) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val subs = listOf(t.resolution360Sub, t.resolution540Sub, t.resolution720Sub, t.resolution1080Sub)
        ResolutionConfig.values().forEachIndexed { index, res ->
          val isSelected = (res == selectedResolution)
          SegmentedPill(
            title = res.label,
            subtitle = subs.getOrElse(index) { res.subtitle },
            isSelected = isSelected,
            onClick = { onResolutionChange(res) },
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Row with Card 2 (Tela) and Card 3 (Antisserrilhado)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Tela (16:9, Esticar)
      SettingsCard(
        title = t.screenTitle,
        modifier = Modifier.weight(0.48f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AspectRatioConfig.values().forEach { asp ->
            val label = if (asp == AspectRatioConfig.RATIO_16_9) t.screenAspect16_9 else t.screenAspectStretch
            SegmentedPill(
              title = label,
              subtitle = null,
              isSelected = (asp == selectedAspect),
              onClick = { onAspectChange(asp) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Antisserrilhado (Off, FXAA, FXAA+)
      SettingsCard(
        title = t.aaTitle,
        modifier = Modifier.weight(0.52f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AntiAliasingConfig.values().forEach { aa ->
            SegmentedPill(
              title = aa.label,
              subtitle = null,
              isSelected = (aa == selectedAA),
              onClick = { onAAChange(aa) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Row with Card 4 (Escala na tela) and Card 5 (Painel de FPS)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Escala na tela (Normal, FSR, CAS)
      SettingsCard(
        title = t.scaleTitle,
        modifier = Modifier.weight(0.54f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ScreenScaleConfig.values().forEach { sc ->
            SegmentedPill(
              title = sc.label,
              subtitle = null,
              isSelected = (sc == selectedScale),
              onClick = { onScaleChange(sc) },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // Painel de FPS (Switch)
      SettingsCard(
        title = t.fpsPanelTitle,
        modifier = Modifier.weight(0.46f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().height(42.dp),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Switch(
            checked = fpsPanelEnabled,
            onCheckedChange = onFpsPanelToggle,
            colors =
              SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFDEBD0),
                checkedTrackColor = Color(0xFFD35400),
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = Color(0xFF2E241E)
              )
          )
        }
      }
    }

    // Card 6: Driver da GPU (Yuzu Turnip) + Trocar button
    SettingsCard(title = t.gpuDriverTitle) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (selectedGpuDriver.isSystem) t.gpuDriverDefault else selectedGpuDriver.displayName,
            color = Color(0xFFE8D7C8),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "${selectedGpuDriver.author} • ${selectedGpuDriver.vulkanVersion}",
            color = Color(0xFFFFCC80),
            fontSize = 10.sp
          )
        }

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color(0xFFE5A663))
              .clickable { onOpenGpuDriverDialog() }
              .padding(horizontal = 18.dp, vertical = 9.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = t.gpuDriverChangeBtn,
            color = Color(0xFF2C1605),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }

    // Footnote warning (Exact text from screenshot localized!)
    Text(
      text = t.footnoteWarning,
      color = Color(0xFF9E8E7F),
      fontSize = 10.sp,
      lineHeight = 14.sp,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
  }
}

// -------------------------------------------------------------
// Dialog: Yuzu / Turnip GPU Driver Manager
// -------------------------------------------------------------
@Composable
fun YuzuGpuDriverManagerDialog(
  t: TranslationStrings,
  drivers: List<GpuDriverItem>,
  currentDriver: GpuDriverItem,
  onSelect: (GpuDriverItem) -> Unit,
  onInstallCustomZip: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFFFFD54F))
          Text(t.gpuDriverManagerTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().height(360.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Selecciona un driver Mesa Turnip optimizado o instala un archivo .ZIP descargado:",
          color = Color.LightGray,
          fontSize = 11.sp
        )

        drivers.forEach { driver ->
          val isSelected = (driver.id == currentDriver.id)
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF382319) else Color(0xFF1B1411))
                .border(
                  1.dp,
                  if (isSelected) Color(0xFFE87A4D) else Color(0xFF33221A),
                  RoundedCornerShape(10.dp)
                )
                .clickable { onSelect(driver) }
                .padding(10.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = isSelected,
                onClick = { onSelect(driver) },
                colors =
                  RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFE87A4D),
                    unselectedColor = Color.Gray
                  )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = driver.displayName,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Text(
                  text = driver.description,
                  color = Color(0xFFC7B19C),
                  fontSize = 10.sp
                )
                Text(
                  text = "${driver.author} • ${driver.vulkanVersion}",
                  color = Color(0xFFFFD54F),
                  fontSize = 9.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Button to install custom driver ZIP
        Button(
          onClick = onInstallCustomZip,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Color(0xFFE5A663),
              contentColor = Color(0xFF2C1605)
            ),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(t.installDriverZipBtn, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(t.closeBtn, color = Color(0xFFFFCC80))
      }
    },
    containerColor = Color(0xFF140E0B)
  )
}

// -------------------------------------------------------------
// Dialog: Bluetooth Gamepad Controls
// -------------------------------------------------------------
@Composable
fun BluetoothGamepadDialog(
  t: TranslationStrings,
  isConnected: Boolean,
  deviceName: String,
  hideTouchOnGamepad: Boolean,
  onToggleHideTouch: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = XboxNeon)
        Text(t.bluetoothTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Status Card
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isConnected) Color(0xFF193D1E) else Color(0xFF2B2018))
              .border(1.dp, if (isConnected) XboxNeon else Color(0xFF553D2A), RoundedCornerShape(10.dp))
              .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier.size(12.dp).clip(CircleShape).background(if (isConnected) XboxNeon else Color.Gray)
          )
          Column {
            Text(
              text = if (isConnected) "${t.bluetoothConnected}: $deviceName" else t.bluetoothNone,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = if (isConnected) "Mapeo Xbox 360 / XInput activo" else "Enciende tu mando en modo vinculación Bluetooth",
              color = Color.LightGray,
              fontSize = 10.sp
            )
          }
        }

        // Auto-hide touch overlay toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(t.hideTouchOnGamepad, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(1f))
          Switch(
            checked = hideTouchOnGamepad,
            onCheckedChange = onToggleHideTouch,
            colors = SwitchDefaults.colors(checkedThumbColor = XboxNeon, checkedTrackColor = XboxGreen)
          )
        }

        // Mapping legend
        Text(
          text = "Mapeo de botones Xbox 360:",
          color = Color(0xFFFFD54F),
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
        Text(
          text = "• Gatillo RT (R2): Disparar\n• Gatillo LT (L2): Golpe / Melee\n• Botón RB (R1): Cambiar arma\n• Botón LB (L1): Linterna\n• Botón A: Saltar  |  Botón B: Agachar\n• Botón X: Recargar  |  Botón Y: Cambiar arma\n• Stick Izquierdo (LS): Moverse\n• Stick Derecho (RS): Apuntar y girar cámara\n• Botón Guía / Start: Abrir Guía Xbox",
          color = Color.LightGray,
          fontSize = 10.sp,
          lineHeight = 15.sp
        )

        // Pair hint
        Text(
          text = t.bluetoothPairHint,
          color = Color(0xFF9E8E7F),
          fontSize = 10.sp
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(t.closeBtn, color = Color(0xFFFFCC80))
      }
    },
    containerColor = Color(0xFF140E0B)
  )
}

// -------------------------------------------------------------
// Dialog: Localized ROMs selection dialog
// -------------------------------------------------------------
@Composable
fun RomSelectionDialogLocalized(
  t: TranslationStrings,
  roms: List<RomFile>,
  currentRom: RomFile,
  onSelect: (RomFile) -> Unit,
  onLoadExternalFile: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFFFFD54F))
          Text(t.romDialogTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().height(360.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = t.romDialogDesc,
          color = Color.LightGray,
          fontSize = 11.sp
        )

        roms.forEach { rom ->
          val isCurrent = (rom.id == currentRom.id)
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isCurrent) Color(0xFF3E271D) else Color(0xFF1F1714))
                .border(
                  1.dp,
                  if (isCurrent) Color(0xFFE87A4D) else Color(0xFF3A2922),
                  RoundedCornerShape(10.dp)
                )
                .clickable { onSelect(rom) }
                .padding(12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier =
                  Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (rom.format == "ISO") Color(0xFF1B4F72) else Color(0xFF196F3D)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = rom.format,
                  color = Color.White,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = rom.title,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = rom.path,
                  color = Color.Gray,
                  fontSize = 10.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${rom.fileSize} • ${if (rom.isBuiltIn) "ROM" else "Externo"}",
                  color = Color(0xFFFFCC80),
                  fontSize = 10.sp
                )
              }

              if (isCurrent) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFE87A4D))
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
          onClick = onLoadExternalFile,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Color(0xFFE5A663),
              contentColor = Color(0xFF2C1605)
            ),
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.fillMaxWidth().height(46.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(t.romLoadExternalBtn, fontWeight = FontWeight.Bold)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(t.closeBtn, color = Color(0xFFFFCC80))
      }
    },
    containerColor = Color(0xFF17110E)
  )
}

// -------------------------------------------------------------
// Dialog: Touch Controls Settings Localized
// -------------------------------------------------------------
@Composable
fun TouchControlsConfigDialogLocalized(
  t: TranslationStrings,
  settings: TouchSettings,
  onSave: (TouchSettings) -> Unit,
  onDismiss: () -> Unit
) {
  var opacity by remember { mutableStateOf(settings.opacity) }
  var scale by remember { mutableStateOf(settings.buttonScale) }
  var sensitivity by remember { mutableStateOf(settings.sensitivity) }
  var haptics by remember { mutableStateOf(settings.hapticsEnabled) }
  var showDpad by remember { mutableStateOf(settings.showDpad) }
  var showRightStick by remember { mutableStateOf(settings.showRightStick) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(t.touchSettingsTitle, color = Color.White, fontWeight = FontWeight.Bold)
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Live Button Opacity Preview
        Box(
          modifier =
            Modifier
              .fillMaxWidth()
              .height(56.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF0D1117))
              .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
              .padding(horizontal = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().alpha(opacity),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF107C10)),
              contentAlignment = Alignment.Center
            ) { Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Box(
              modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFC0392B)),
              contentAlignment = Alignment.Center
            ) { Text("B", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Box(
              modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF2980B9)),
              contentAlignment = Alignment.Center
            ) { Text("X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Box(
              modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF39C12)),
              contentAlignment = Alignment.Center
            ) { Text("Y", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Box(
              modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFF333A42)),
              contentAlignment = Alignment.Center
            ) { Text("LS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp) }
          }
        }

        // Opacity
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(t.touchOpacity, color = Color.LightGray, fontSize = 12.sp)
            Text("${(opacity * 100).toInt()}%", color = Color(0xFFFFCC80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = opacity,
            onValueChange = { opacity = it },
            valueRange = 0.15f..1.0f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFFE87A4D), activeTrackColor = Color(0xFFE87A4D))
          )
        }

        // Button Scale
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(t.touchScale, color = Color.LightGray, fontSize = 12.sp)
            Text(String.format("%.1fx", scale), color = Color(0xFFFFCC80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Slider(
            value = scale,
            onValueChange = { scale = it },
            valueRange = 0.7f..1.4f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFFE87A4D), activeTrackColor = Color(0xFFE87A4D))
          )
        }

        // Sensitivity
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(t.touchSensitivity, color = Color.LightGray, fontSize = 12.sp)
            Text(String.format("%.1fx", sensitivity), color = Color(0xFFFFCC80), fontSize = 12.sp)
          }
          Slider(
            value = sensitivity,
            onValueChange = { sensitivity = it },
            valueRange = 0.5f..2.5f,
            colors = SliderDefaults.colors(thumbColor = Color(0xFFE87A4D), activeTrackColor = Color(0xFFE87A4D))
          )
        }

        // Show D-Pad
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(t.touchShowDpad, color = Color.LightGray, fontSize = 12.sp)
          Switch(
            checked = showDpad,
            onCheckedChange = { showDpad = it },
            colors =
              SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFCC80),
                checkedTrackColor = Color(0xFFD35400)
              )
          )
        }

        // Show Right Stick
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(t.touchShowRightStick, color = Color.LightGray, fontSize = 12.sp)
          Switch(
            checked = showRightStick,
            onCheckedChange = { showRightStick = it },
            colors =
              SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFCC80),
                checkedTrackColor = Color(0xFFD35400)
              )
          )
        }

        // Vibration
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(t.touchHaptics, color = Color.LightGray, fontSize = 12.sp)
          Switch(
            checked = haptics,
            onCheckedChange = { haptics = it },
            colors =
              SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFCC80),
                checkedTrackColor = Color(0xFFD35400)
              )
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            settings.copy(
              opacity = opacity,
              buttonScale = scale,
              sensitivity = sensitivity,
              hapticsEnabled = haptics,
              showDpad = showDpad,
              showRightStick = showRightStick
            )
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE87A4D))
      ) {
        Text(t.saveBtn, color = Color.White)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(t.cancelBtn, color = Color.Gray)
      }
    },
    containerColor = Color(0xFF140E0B)
  )
}

// -------------------------------------------------------------
// Component: Reusable Dark Translucent Glass Card
// -------------------------------------------------------------
@Composable
fun SettingsCard(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xFF1B1411).copy(alpha = 0.8f))
        .border(1.dp, Color(0xFF422F26).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Column {
      Text(
        text = title,
        color = Color(0xFFF3E2D2),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      content()
    }
  }
}

// -------------------------------------------------------------
// Component: Segmented Orange Pill Selector
// -------------------------------------------------------------
@Composable
fun SegmentedPill(
  title: String,
  subtitle: String?,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier =
      modifier
        .height(44.dp)
        .clip(RoundedCornerShape(22.dp))
        .background(
          if (isSelected) {
            Brush.horizontalGradient(
              listOf(Color(0xFFE87A4D), Color(0xFFD35400))
            )
          } else {
            Brush.horizontalGradient(
              listOf(Color(0xFF241A16), Color(0xFF2A1F1B))
            )
          }
        )
        .border(
          width = 1.dp,
          color = if (isSelected) Color(0xFFFFCC80) else Color(0xFF3E2D26),
          shape = RoundedCornerShape(22.dp)
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick
        ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = title,
        color = if (isSelected) Color.White else Color(0xFFD6C5B8),
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.sp
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          color = if (isSelected) Color(0xFFFFF0E0) else Color(0xFF8D7B6F),
          fontSize = 9.sp
        )
      }
    }
  }
}
