package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundSynthesizer
import com.example.model.AppLanguage
import com.example.model.ButtonMappingProfile
import com.example.model.CommandCategory
import com.example.model.EmulatorCommand
import com.example.model.Xbox360Button
import kotlinx.coroutines.delay

private val XboxNeonGreen = Color(0xFF107C10)
private val XboxBrightGreen = Color(0xFF2ECC71)
private val XboxDarkSlate = Color(0xFF15191E)
private val XboxCardBg = Color(0xFF1E232A)
private val XboxCardBorder = Color(0xFF333B47)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XboxControllerMappingScreen(
  currentLanguage: AppLanguage,
  initialMappings: Map<Xbox360Button, EmulatorCommand> = ButtonMappingProfile.DefaultLeft4Dead.mappings,
  soundSynthesizer: SoundSynthesizer? = null,
  onSaveMappings: (Map<Xbox360Button, EmulatorCommand>) -> Unit,
  onBack: () -> Unit
) {
  BackHandler { onBack() }

  // Active mappings dictionary
  val currentMappings = remember {
    mutableStateMapOf<Xbox360Button, EmulatorCommand>().apply {
      putAll(initialMappings)
    }
  }

  // Selected button for inspection / remapping
  var selectedButton by remember { mutableStateOf<Xbox360Button>(Xbox360Button.TRIGGER_RIGHT) }
  var showCommandPickerSheet by remember { mutableStateOf(false) }
  var testModeEnabled by remember { mutableStateOf(false) }
  var lastTestedCommand by remember { mutableStateOf<EmulatorCommand?>(null) }
  var lastTestedButton by remember { mutableStateOf<Xbox360Button?>(null) }
  var showResetConfirmation by remember { mutableStateOf(false) }
  var activePresetId by remember { mutableStateOf("preset_default") }

  // Clear tested indicator after delay
  LaunchedEffect(lastTestedCommand) {
    if (lastTestedCommand != null) {
      delay(1200)
      lastTestedCommand = null
      lastTestedButton = null
    }
  }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              Color(0xFF0D1115),
              Color(0xFF13171D),
              Color(0xFF0A0D10)
            )
          )
        )
        .testTag("screen_xbox_mapping")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Header Toolbar
      TopMappingToolbar(
        currentLanguage = currentLanguage,
        testMode = testModeEnabled,
        onToggleTestMode = { testModeEnabled = it },
        onReset = { showResetConfirmation = true },
        onSaveAndExit = {
          onSaveMappings(currentMappings.toMap())
          soundSynthesizer?.playXboxChime()
          onBack()
        },
        onBack = onBack
      )

      // 2. Preset Selection Bar
      PresetChipsRow(
        currentLanguage = currentLanguage,
        activePresetId = activePresetId,
        onSelectPreset = { preset ->
          activePresetId = preset.id
          currentMappings.clear()
          currentMappings.putAll(preset.mappings)
          soundSynthesizer?.vibrate(25, 140)
        }
      )

      // 3. Main Adaptive Content (Landscape: side-by-side; Portrait: stacked)
      BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
          // Large Screen / Landscape: Controller Left, Inspector / List Right
          Row(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Left: Visual Interactive Xbox 360 Controller
            Box(
              modifier =
                Modifier
                  .weight(1.15f)
                  .fillMaxHeight()
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0xFF13171E))
                  .border(1.dp, Color(0xFF28303C), RoundedCornerShape(16.dp))
                  .padding(12.dp),
              contentAlignment = Alignment.Center
            ) {
              InteractiveXbox360ControllerGraphic(
                selectedButton = selectedButton,
                currentMappings = currentMappings,
                testMode = testModeEnabled,
                lastTestedButton = lastTestedButton,
                onButtonTapped = { btn ->
                  selectedButton = btn
                  if (testModeEnabled) {
                    val cmd = currentMappings[btn] ?: EmulatorCommand.UNASSIGNED
                    lastTestedButton = btn
                    lastTestedCommand = cmd
                    soundSynthesizer?.vibrate(35, 180)
                  } else {
                    soundSynthesizer?.vibrate(20, 100)
                  }
                },
                onButtonDoubleTapped = { btn ->
                  selectedButton = btn
                  showCommandPickerSheet = true
                }
              )

              // In-controller active command toast pill
              lastTestedCommand?.let { cmd ->
                Box(
                  modifier =
                    Modifier
                      .align(Alignment.BottomCenter)
                      .padding(bottom = 12.dp)
                      .clip(RoundedCornerShape(20.dp))
                      .background(XboxNeonGreen.copy(alpha = 0.92f))
                      .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Text(cmd.iconGlyph, fontSize = 18.sp)
                    Text(
                      text = "${lastTestedButton?.shortName}: ${cmd.localizedName(currentLanguage)}",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                  }
                }
              }
            }

            // Right: Selected Button Inspector & Command Mappings List
            Column(
              modifier =
                Modifier
                  .weight(0.95f)
                  .fillMaxHeight(),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Active Button Callout Card
              ActiveButtonInspectorCard(
                button = selectedButton,
                currentCommand = currentMappings[selectedButton] ?: EmulatorCommand.UNASSIGNED,
                currentLanguage = currentLanguage,
                testMode = testModeEnabled,
                onChangeCommand = { showCommandPickerSheet = true },
                onTestCommand = {
                  val cmd = currentMappings[selectedButton] ?: EmulatorCommand.UNASSIGNED
                  lastTestedButton = selectedButton
                  lastTestedCommand = cmd
                  soundSynthesizer?.vibrate(40, 200)
                }
              )

              // All Mappings Quick Overview List
              Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = XboxCardBg),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(XboxCardBorder, Color(0xFF1E242B)))),
                shape = RoundedCornerShape(14.dp)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = when (currentLanguage) {
                        AppLanguage.SPANISH -> "TODOS LOS BOTONES"
                        AppLanguage.ENGLISH -> "ALL BUTTON MAPPINGS"
                        AppLanguage.PORTUGUESE -> "TODOS OS BOTÕES"
                      },
                      color = Color(0xFFC7B19C),
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      letterSpacing = 1.sp
                    )
                    Text(
                      text = "${currentMappings.size} asignados",
                      color = XboxBrightGreen,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }

                  LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                  ) {
                    items(Xbox360Button.values()) { btn ->
                      val cmd = currentMappings[btn] ?: EmulatorCommand.UNASSIGNED
                      val isSelected = btn == selectedButton
                      ButtonMappingListItem(
                        button = btn,
                        command = cmd,
                        isSelected = isSelected,
                        currentLanguage = currentLanguage,
                        onClick = {
                          selectedButton = btn
                          soundSynthesizer?.vibrate(15, 80)
                        },
                        onRemap = {
                          selectedButton = btn
                          showCommandPickerSheet = true
                        }
                      )
                    }
                  }
                }
              }
            }
          }
        } else {
          // Portrait: Controller Top (Compact), Inspector and Tabs Bottom
          Column(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Visual Controller Box
            Box(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .height(250.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0xFF13171E))
                  .border(1.dp, Color(0xFF28303C), RoundedCornerShape(16.dp))
                  .padding(8.dp),
              contentAlignment = Alignment.Center
            ) {
              InteractiveXbox360ControllerGraphic(
                selectedButton = selectedButton,
                currentMappings = currentMappings,
                testMode = testModeEnabled,
                lastTestedButton = lastTestedButton,
                onButtonTapped = { btn ->
                  selectedButton = btn
                  if (testModeEnabled) {
                    val cmd = currentMappings[btn] ?: EmulatorCommand.UNASSIGNED
                    lastTestedButton = btn
                    lastTestedCommand = cmd
                    soundSynthesizer?.vibrate(35, 180)
                  } else {
                    soundSynthesizer?.vibrate(20, 100)
                  }
                },
                onButtonDoubleTapped = { btn ->
                  selectedButton = btn
                  showCommandPickerSheet = true
                }
              )

              lastTestedCommand?.let { cmd ->
                Box(
                  modifier =
                    Modifier
                      .align(Alignment.BottomCenter)
                      .padding(bottom = 6.dp)
                      .clip(RoundedCornerShape(16.dp))
                      .background(XboxNeonGreen.copy(alpha = 0.95f))
                      .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(cmd.iconGlyph, fontSize = 14.sp)
                    Text(
                      text = "${lastTestedButton?.shortName}: ${cmd.localizedName(currentLanguage)}",
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp
                    )
                  }
                }
              }
            }

            // Active Inspector Card
            ActiveButtonInspectorCard(
              button = selectedButton,
              currentCommand = currentMappings[selectedButton] ?: EmulatorCommand.UNASSIGNED,
              currentLanguage = currentLanguage,
              testMode = testModeEnabled,
              onChangeCommand = { showCommandPickerSheet = true },
              onTestCommand = {
                val cmd = currentMappings[selectedButton] ?: EmulatorCommand.UNASSIGNED
                lastTestedButton = selectedButton
                lastTestedCommand = cmd
                soundSynthesizer?.vibrate(40, 200)
              }
            )

            // Scrollable list of buttons
            Card(
              modifier = Modifier.weight(1f).fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = XboxCardBg),
              border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(XboxCardBorder, Color(0xFF1E242B)))),
              shape = RoundedCornerShape(14.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = when (currentLanguage) {
                    AppLanguage.SPANISH -> "LISTA DE BOTONES Y ACCIONES"
                    AppLanguage.ENGLISH -> "BUTTON & ACTION LIST"
                    AppLanguage.PORTUGUESE -> "LISTA DE BOTÕES E AÇÕES"
                  },
                  color = Color(0xFFC7B19C),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyColumn(
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.fillMaxSize()
                ) {
                  items(Xbox360Button.values()) { btn ->
                    val cmd = currentMappings[btn] ?: EmulatorCommand.UNASSIGNED
                    val isSelected = btn == selectedButton
                    ButtonMappingListItem(
                      button = btn,
                      command = cmd,
                      isSelected = isSelected,
                      currentLanguage = currentLanguage,
                      onClick = {
                        selectedButton = btn
                        soundSynthesizer?.vibrate(15, 80)
                      },
                      onRemap = {
                        selectedButton = btn
                        showCommandPickerSheet = true
                      }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // 4. Command Picker Bottom Sheet / Dialog
  if (showCommandPickerSheet) {
    CommandPickerModal(
      button = selectedButton,
      currentCommand = currentMappings[selectedButton] ?: EmulatorCommand.UNASSIGNED,
      currentLanguage = currentLanguage,
      onSelectCommand = { cmd ->
        currentMappings[selectedButton] = cmd
        activePresetId = "custom"
        showCommandPickerSheet = false
        soundSynthesizer?.vibrate(30, 160)
      },
      onDismiss = { showCommandPickerSheet = false }
    )
  }

  // 5. Reset Confirmation Dialog
  if (showResetConfirmation) {
    AlertDialog(
      onDismissRequest = { showResetConfirmation = false },
      containerColor = Color(0xFF1A1F26),
      titleColor = Color.White,
      textContentColor = Color(0xFFCCCCCC),
      title = {
        Text(
          when (currentLanguage) {
            AppLanguage.SPANISH -> "¿Restablecer controles?"
            AppLanguage.ENGLISH -> "Reset Controls?"
            AppLanguage.PORTUGUESE -> "Redefinir controles?"
          },
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          when (currentLanguage) {
            AppLanguage.SPANISH -> "Se restaurarán todos los botones al perfil original oficial de Left 4 Dead en Xbox 360."
            AppLanguage.ENGLISH -> "All button mappings will be restored to the official Left 4 Dead Xbox 360 default profile."
            AppLanguage.PORTUGUESE -> "Todos os botões serão redefinidos para o perfil original do Left 4 Dead no Xbox 360."
          }
        )
      },
      confirmButton = {
        Button(
          onClick = {
            currentMappings.clear()
            currentMappings.putAll(ButtonMappingProfile.DefaultLeft4Dead.mappings)
            activePresetId = ButtonMappingProfile.DefaultLeft4Dead.id
            showResetConfirmation = false
            soundSynthesizer?.vibrate(40, 200)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B))
        ) {
          Text(
            when (currentLanguage) {
              AppLanguage.SPANISH -> "Restablecer"
              AppLanguage.ENGLISH -> "Reset"
              AppLanguage.PORTUGUESE -> "Redefinir"
            },
            color = Color.White
          )
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = { showResetConfirmation = false },
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
        ) {
          Text(
            when (currentLanguage) {
              AppLanguage.SPANISH -> "Cancelar"
              AppLanguage.ENGLISH -> "Cancel"
              AppLanguage.PORTUGUESE -> "Cancelar"
            }
          )
        }
      }
    )
  }
}

// ---------------------------------------------------------
// Top Navigation & Action Toolbar
// ---------------------------------------------------------
@Composable
private fun TopMappingToolbar(
  currentLanguage: AppLanguage,
  testMode: Boolean,
  onToggleTestMode: (Boolean) -> Unit,
  onReset: () -> Unit,
  onSaveAndExit: () -> Unit,
  onBack: () -> Unit
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Color(0xFF0F1217))
        .border(1.dp, Color(0xFF212833))
        .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Back + Title
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.size(36.dp).testTag("btn_back_mapping")
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
      }

      Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier =
              Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(XboxNeonGreen),
            contentAlignment = Alignment.Center
          ) {
            Text("X", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
          }
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "MAPEO DE BOTONES XBOX 360"
              AppLanguage.ENGLISH -> "XBOX 360 BUTTON MAPPING"
              AppLanguage.PORTUGUESE -> "MAPEAMENTO DE BOTÕES X360"
            },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
          )
        }
        Text(
          text = when (currentLanguage) {
            AppLanguage.SPANISH -> "Toca cualquier botón del mando para asignar un comando"
            AppLanguage.ENGLISH -> "Tap any button on the controller to assign a command"
            AppLanguage.PORTUGUESE -> "Toque em qualquer botão para atribuir um comando"
          },
          color = Color(0xFF9EABB8),
          fontSize = 10.sp
        )
      }
    }

    // Right Action Buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Test Mode Switch Pill
      Row(
        modifier =
          Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (testMode) Color(0xFF194520) else Color(0xFF222831))
            .border(1.dp, if (testMode) XboxBrightGreen else Color(0xFF384352), RoundedCornerShape(20.dp))
            .clickable { onToggleTestMode(!testMode) }
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
          Icons.Default.FlashOn,
          contentDescription = null,
          tint = if (testMode) XboxBrightGreen else Color(0xFF888888),
          modifier = Modifier.size(14.dp)
        )
        Text(
          text = when (currentLanguage) {
            AppLanguage.SPANISH -> if (testMode) "PROBANDO" else "PROBAR"
            AppLanguage.ENGLISH -> if (testMode) "TESTING" else "TEST"
            AppLanguage.PORTUGUESE -> if (testMode) "TESTANDO" else "TESTAR"
          },
          color = if (testMode) Color.White else Color(0xFFBBBBBB),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }

      // Reset Button
      IconButton(
        onClick = onReset,
        modifier =
          Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFF222831))
      ) {
        Icon(Icons.Default.Refresh, contentDescription = "Restablecer", tint = Color(0xFFE0E0E0), modifier = Modifier.size(16.dp))
      }

      // Save & Apply Button
      Button(
        onClick = onSaveAndExit,
        colors = ButtonDefaults.buttonColors(containerColor = XboxNeonGreen),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.height(34.dp).testTag("btn_save_mapping")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = when (currentLanguage) {
            AppLanguage.SPANISH -> "Guardar"
            AppLanguage.ENGLISH -> "Save"
            AppLanguage.PORTUGUESE -> "Salvar"
          },
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    }
  }
}

// ---------------------------------------------------------
// Preset Selection Chips Row
// ---------------------------------------------------------
@Composable
private fun PresetChipsRow(
  currentLanguage: AppLanguage,
  activePresetId: String,
  onSelectPreset: (ButtonMappingProfile) -> Unit
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(Color(0xFF13171D))
        .horizontalScroll(rememberScrollState())
        .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = when (currentLanguage) {
        AppLanguage.SPANISH -> "PERFILES:"
        AppLanguage.ENGLISH -> "PROFILES:"
        AppLanguage.PORTUGUESE -> "PERFIS:"
      },
      color = Color(0xFF8C98A4),
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp
    )

    ButtonMappingProfile.AllPresets.forEach { preset ->
      val isSelected = preset.id == activePresetId
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
              if (isSelected) {
                Brush.horizontalGradient(listOf(Color(0xFFE87A4D), Color(0xFFD35400)))
              } else {
                Brush.horizontalGradient(listOf(Color(0xFF1E252E), Color(0xFF1A1F27)))
              }
            )
            .border(
              1.dp,
              if (isSelected) Color(0xFFFFA07A) else Color(0xFF2C3542),
              RoundedCornerShape(14.dp)
            )
            .clickable { onSelectPreset(preset) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Text(
          text = preset.localizedName(currentLanguage),
          color = if (isSelected) Color.White else Color(0xFFC7CBD1),
          fontSize = 11.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
      }
    }

    if (activePresetId == "custom") {
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF334155))
            .border(1.dp, XboxBrightGreen, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Text(
          text = when (currentLanguage) {
            AppLanguage.SPANISH -> "Personalizado *"
            AppLanguage.ENGLISH -> "Custom *"
            AppLanguage.PORTUGUESE -> "Personalizado *"
          },
          color = XboxBrightGreen,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

// ---------------------------------------------------------
// Interactive Graphic of the Xbox 360 Controller
// ---------------------------------------------------------
@Composable
private fun InteractiveXbox360ControllerGraphic(
  selectedButton: Xbox360Button,
  currentMappings: Map<Xbox360Button, EmulatorCommand>,
  testMode: Boolean,
  lastTestedButton: Xbox360Button?,
  onButtonTapped: (Xbox360Button) -> Unit,
  onButtonDoubleTapped: (Xbox360Button) -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition()
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse)
  )

  BoxWithConstraints(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    val containerWidth = maxWidth
    val containerHeight = maxHeight

    // Target aspect ratio of the Xbox 360 controller (~ 1.6 : 1)
    val controllerW = (containerWidth * 0.95f).coerceAtMost(containerHeight * 1.6f)
    val controllerH = controllerW / 1.6f

    Box(
      modifier =
        Modifier
          .size(controllerW, controllerH)
          .align(Alignment.Center)
    ) {
      // 1. Controller Body Background Shape (Canvas vector)
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Outer ergonomic shell of Xbox 360 controller
        val bodyPath = Path().apply {
          // Top shoulder bridge
          moveTo(w * 0.28f, h * 0.20f)
          cubicTo(w * 0.35f, h * 0.17f, w * 0.65f, h * 0.17f, w * 0.72f, h * 0.20f)
          // Right shoulder & bumper contour
          cubicTo(w * 0.82f, h * 0.21f, w * 0.90f, h * 0.27f, w * 0.94f, h * 0.38f)
          // Right grip outside curve down
          cubicTo(w * 0.98f, h * 0.50f, w * 0.96f, h * 0.85f, w * 0.85f, h * 0.96f)
          // Right grip bottom rounded tip
          cubicTo(w * 0.80f, h * 1.00f, w * 0.72f, h * 0.98f, w * 0.70f, h * 0.88f)
          // Inner right grip curve towards center bottom
          cubicTo(w * 0.68f, h * 0.75f, w * 0.60f, h * 0.68f, w * 0.50f, h * 0.69f)
          // Inner left grip curve towards center bottom
          cubicTo(w * 0.40f, h * 0.68f, w * 0.32f, h * 0.75f, w * 0.30f, h * 0.88f)
          // Left grip bottom rounded tip
          cubicTo(w * 0.28f, h * 0.98f, w * 0.20f, h * 1.00f, w * 0.15f, h * 0.96f)
          // Left grip outside curve up
          cubicTo(w * 0.04f, h * 0.85f, w * 0.02f, h * 0.50f, w * 0.06f, h * 0.38f)
          // Left shoulder & bumper contour
          cubicTo(w * 0.10f, h * 0.27f, w * 0.18f, h * 0.21f, w * 0.28f, h * 0.20f)
          close()
        }

        // Draw shadow under controller
        drawPath(
          path = bodyPath,
          color = Color(0xAA000000)
        )

        // Draw main body with matte graphite gradient (Xbox 360 controller)
        drawPath(
          path = bodyPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFF2C323C),
              Color(0xFF232830),
              Color(0xFF191C22)
            ),
            startY = 0f,
            endY = h
          )
        )

        // Draw outer highlight bevel
        drawPath(
          path = bodyPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0x55FFFFFF),
              Color(0x11FFFFFF),
              Color(0x33000000)
            )
          ),
          style = Stroke(width = 3f)
        )

        // Subtle textured side grips
        drawRoundRect(
          color = Color(0x22FFFFFF),
          topLeft = Offset(w * 0.07f, h * 0.46f),
          size = Size(w * 0.06f, h * 0.32f),
          cornerRadius = CornerRadius(16f, 16f)
        )
        drawRoundRect(
          color = Color(0x22FFFFFF),
          topLeft = Offset(w * 0.87f, h * 0.46f),
          size = Size(w * 0.06f, h * 0.32f),
          cornerRadius = CornerRadius(16f, 16f)
        )
      }

      // 2. Interactive Buttons Positioned Onto the Controller

      // --- TOP TRIGGERS & BUMPERS ---
      // Left Trigger (LT)
      ControllerTriggerButton(
        button = Xbox360Button.TRIGGER_LEFT,
        label = "LT",
        command = currentMappings[Xbox360Button.TRIGGER_LEFT],
        isSelected = selectedButton == Xbox360Button.TRIGGER_LEFT,
        isTested = lastTestedButton == Xbox360Button.TRIGGER_LEFT,
        pulseAlpha = pulseAlpha,
        modifier =
          Modifier
            .align(Alignment.TopStart)
            .offset(x = controllerW * 0.16f, y = controllerH * 0.02f),
        onClick = { onButtonTapped(Xbox360Button.TRIGGER_LEFT) }
      )

      // Left Bumper (LB)
      ControllerBumperButton(
        button = Xbox360Button.BUMPER_LEFT,
        label = "LB",
        command = currentMappings[Xbox360Button.BUMPER_LEFT],
        isSelected = selectedButton == Xbox360Button.BUMPER_LEFT,
        isTested = lastTestedButton == Xbox360Button.BUMPER_LEFT,
        pulseAlpha = pulseAlpha,
        modifier =
          Modifier
            .align(Alignment.TopStart)
            .offset(x = controllerW * 0.22f, y = controllerH * 0.12f),
        onClick = { onButtonTapped(Xbox360Button.BUMPER_LEFT) }
      )

      // Right Trigger (RT)
      ControllerTriggerButton(
        button = Xbox360Button.TRIGGER_RIGHT,
        label = "RT",
        command = currentMappings[Xbox360Button.TRIGGER_RIGHT],
        isSelected = selectedButton == Xbox360Button.TRIGGER_RIGHT,
        isTested = lastTestedButton == Xbox360Button.TRIGGER_RIGHT,
        pulseAlpha = pulseAlpha,
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .offset(x = -controllerW * 0.16f, y = controllerH * 0.02f),
        onClick = { onButtonTapped(Xbox360Button.TRIGGER_RIGHT) }
      )

      // Right Bumper (RB)
      ControllerBumperButton(
        button = Xbox360Button.BUMPER_RIGHT,
        label = "RB",
        command = currentMappings[Xbox360Button.BUMPER_RIGHT],
        isSelected = selectedButton == Xbox360Button.BUMPER_RIGHT,
        isTested = lastTestedButton == Xbox360Button.BUMPER_RIGHT,
        pulseAlpha = pulseAlpha,
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .offset(x = -controllerW * 0.22f, y = controllerH * 0.12f),
        onClick = { onButtonTapped(Xbox360Button.BUMPER_RIGHT) }
      )

      // --- LEFT SIDE: LEFT STICK (LS) ---
      ControllerThumbstickButton(
        button = Xbox360Button.STICK_LEFT_CLICK,
        label = "LS",
        subLabel = "Click",
        command = currentMappings[Xbox360Button.STICK_LEFT_CLICK],
        isSelected = selectedButton == Xbox360Button.STICK_LEFT_CLICK,
        isTested = lastTestedButton == Xbox360Button.STICK_LEFT_CLICK,
        pulseAlpha = pulseAlpha,
        size = controllerW * 0.18f,
        modifier =
          Modifier
            .align(Alignment.CenterStart)
            .offset(x = controllerW * 0.16f, y = -controllerH * 0.12f),
        onClick = { onButtonTapped(Xbox360Button.STICK_LEFT_CLICK) }
      )

      // --- LEFT SIDE: D-PAD CROSS ---
      ControllerDpadCluster(
        selectedButton = selectedButton,
        currentMappings = currentMappings,
        lastTestedButton = lastTestedButton,
        pulseAlpha = pulseAlpha,
        size = controllerW * 0.20f,
        modifier =
          Modifier
            .align(Alignment.BottomStart)
            .offset(x = controllerW * 0.26f, y = -controllerH * 0.18f),
        onSelectButton = onButtonTapped
      )

      // --- CENTER CLUSTER: BACK, GUIDE JEWEL, START ---
      // Back Button
      ControllerCenterButton(
        button = Xbox360Button.BUTTON_BACK,
        label = "BACK",
        symbol = "◂◂",
        command = currentMappings[Xbox360Button.BUTTON_BACK],
        isSelected = selectedButton == Xbox360Button.BUTTON_BACK,
        isTested = lastTestedButton == Xbox360Button.BUTTON_BACK,
        pulseAlpha = pulseAlpha,
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(x = -controllerW * 0.12f, y = -controllerH * 0.08f),
        onClick = { onButtonTapped(Xbox360Button.BUTTON_BACK) }
      )

      // Xbox 360 Guide Jewel (Iconic Chrome dome with green quadrants!)
      ControllerGuideJewel(
        isSelected = selectedButton == Xbox360Button.BUTTON_GUIDE,
        isTested = lastTestedButton == Xbox360Button.BUTTON_GUIDE,
        pulseAlpha = pulseAlpha,
        size = controllerW * 0.13f,
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(y = -controllerH * 0.10f),
        onClick = { onButtonTapped(Xbox360Button.BUTTON_GUIDE) }
      )

      // Start Button
      ControllerCenterButton(
        button = Xbox360Button.BUTTON_START,
        label = "START",
        symbol = "▸▸",
        command = currentMappings[Xbox360Button.BUTTON_START],
        isSelected = selectedButton == Xbox360Button.BUTTON_START,
        isTested = lastTestedButton == Xbox360Button.BUTTON_START,
        pulseAlpha = pulseAlpha,
        modifier =
          Modifier
            .align(Alignment.Center)
            .offset(x = controllerW * 0.12f, y = -controllerH * 0.08f),
        onClick = { onButtonTapped(Xbox360Button.BUTTON_START) }
      )

      // --- RIGHT SIDE: ABXY DIAMOND CLUSTER ---
      ControllerAbxyCluster(
        selectedButton = selectedButton,
        currentMappings = currentMappings,
        lastTestedButton = lastTestedButton,
        pulseAlpha = pulseAlpha,
        size = controllerW * 0.22f,
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .offset(x = -controllerW * 0.15f, y = controllerH * 0.22f),
        onSelectButton = onButtonTapped
      )

      // --- RIGHT SIDE: RIGHT STICK (RS) ---
      ControllerThumbstickButton(
        button = Xbox360Button.STICK_RIGHT_CLICK,
        label = "RS",
        subLabel = "Click",
        command = currentMappings[Xbox360Button.STICK_RIGHT_CLICK],
        isSelected = selectedButton == Xbox360Button.STICK_RIGHT_CLICK,
        isTested = lastTestedButton == Xbox360Button.STICK_RIGHT_CLICK,
        pulseAlpha = pulseAlpha,
        size = controllerW * 0.18f,
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .offset(x = -controllerW * 0.26f, y = -controllerH * 0.18f),
        onClick = { onButtonTapped(Xbox360Button.STICK_RIGHT_CLICK) }
      )
    }
  }
}

// ---------------------------------------------------------
// Subcomponents for the Visual Xbox 360 Controller
// ---------------------------------------------------------

@Composable
private fun ControllerTriggerButton(
  button: Xbox360Button,
  label: String,
  command: EmulatorCommand?,
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color(0xFF454E5B)
  val bgBrush = if (isSelected) {
    Brush.verticalGradient(listOf(Color(0xFF285E32), Color(0xFF193B1F)))
  } else {
    Brush.verticalGradient(listOf(Color(0xFF373F4B), Color(0xFF20252D)))
  }

  Column(
    modifier = modifier.clickable { onClick() },
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier =
        Modifier
          .size(width = 54.dp, height = 30.dp)
          .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
          .background(bgBrush)
          .border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
          ),
      contentAlignment = Alignment.Center
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
        command?.let {
          Text(it.iconGlyph, fontSize = 10.sp)
        }
      }
    }
  }
}

@Composable
private fun ControllerBumperButton(
  button: Xbox360Button,
  label: String,
  command: EmulatorCommand?,
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color(0xFF454E5B)
  val bgBrush = if (isSelected) {
    Brush.verticalGradient(listOf(Color(0xFF285E32), Color(0xFF193B1F)))
  } else {
    Brush.verticalGradient(listOf(Color(0xFF424B57), Color(0xFF252B35)))
  }

  Box(
    modifier =
      modifier
        .size(width = 48.dp, height = 24.dp)
        .clip(RoundedCornerShape(6.dp))
        .background(bgBrush)
        .border(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
          shape = RoundedCornerShape(6.dp)
        )
        .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
      command?.let {
        Text(it.iconGlyph, fontSize = 9.sp)
      }
    }
  }
}

@Composable
private fun ControllerThumbstickButton(
  button: Xbox360Button,
  label: String,
  subLabel: String,
  command: EmulatorCommand?,
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  size: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color(0xFF38404B)

  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(Brush.radialGradient(listOf(Color(0xFF363C46), Color(0xFF1C2026))))
        .border(
          width = if (isSelected) 2.5.dp else 1.5.dp,
          color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
          shape = CircleShape
        )
        .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    // Inner thumb depression
    Box(
      modifier =
        Modifier
          .size(size * 0.72f)
          .clip(CircleShape)
          .background(Brush.radialGradient(listOf(Color(0xFF15181E), Color(0xFF2A2E37))))
          .border(1.dp, Color(0x33FFFFFF), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        command?.let {
          Text(it.iconGlyph, fontSize = 12.sp)
        }
        Text(label, color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
      }
    }
  }
}

@Composable
private fun ControllerDpadCluster(
  selectedButton: Xbox360Button,
  currentMappings: Map<Xbox360Button, EmulatorCommand>,
  lastTestedButton: Xbox360Button?,
  pulseAlpha: Float,
  size: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
  onSelectButton: (Xbox360Button) -> Unit
) {
  val armSize = size * 0.36f

  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(Color(0xFF181C22))
        .border(1.5.dp, Color(0xFF313844), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    // Up arm
    DpadArmButton(
      button = Xbox360Button.DPAD_UP,
      symbol = "▲",
      isSelected = selectedButton == Xbox360Button.DPAD_UP,
      isTested = lastTestedButton == Xbox360Button.DPAD_UP,
      pulseAlpha = pulseAlpha,
      modifier = Modifier.align(Alignment.TopCenter).offset(y = 2.dp).size(armSize),
      onClick = { onSelectButton(Xbox360Button.DPAD_UP) }
    )

    // Down arm
    DpadArmButton(
      button = Xbox360Button.DPAD_DOWN,
      symbol = "▼",
      isSelected = selectedButton == Xbox360Button.DPAD_DOWN,
      isTested = lastTestedButton == Xbox360Button.DPAD_DOWN,
      pulseAlpha = pulseAlpha,
      modifier = Modifier.align(Alignment.BottomCenter).offset(y = -2.dp).size(armSize),
      onClick = { onSelectButton(Xbox360Button.DPAD_DOWN) }
    )

    // Left arm
    DpadArmButton(
      button = Xbox360Button.DPAD_LEFT,
      symbol = "◄",
      isSelected = selectedButton == Xbox360Button.DPAD_LEFT,
      isTested = lastTestedButton == Xbox360Button.DPAD_LEFT,
      pulseAlpha = pulseAlpha,
      modifier = Modifier.align(Alignment.CenterStart).offset(x = 2.dp).size(armSize),
      onClick = { onSelectButton(Xbox360Button.DPAD_LEFT) }
    )

    // Right arm
    DpadArmButton(
      button = Xbox360Button.DPAD_RIGHT,
      symbol = "►",
      isSelected = selectedButton == Xbox360Button.DPAD_RIGHT,
      isTested = lastTestedButton == Xbox360Button.DPAD_RIGHT,
      pulseAlpha = pulseAlpha,
      modifier = Modifier.align(Alignment.CenterEnd).offset(x = -2.dp).size(armSize),
      onClick = { onSelectButton(Xbox360Button.DPAD_RIGHT) }
    )

    // Center pivot
    Box(
      modifier =
        Modifier
          .size(armSize * 0.7f)
          .clip(CircleShape)
          .background(Color(0xFF2B323D))
    )
  }
}

@Composable
private fun DpadArmButton(
  button: Xbox360Button,
  symbol: String,
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color.Transparent
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(4.dp))
        .background(if (isSelected) Color(0xFF234F2B) else Color(0xFF2C333E))
        .border(
          width = if (isSelected) 2.dp else 0.dp,
          color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
          shape = RoundedCornerShape(4.dp)
        )
        .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = symbol,
      color = if (isSelected) Color.White else Color(0xFFBBBBBB),
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun ControllerAbxyCluster(
  selectedButton: Xbox360Button,
  currentMappings: Map<Xbox360Button, EmulatorCommand>,
  lastTestedButton: Xbox360Button?,
  pulseAlpha: Float,
  size: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
  onSelectButton: (Xbox360Button) -> Unit
) {
  val btnSize = size * 0.38f

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    // Y (Top, Yellow)
    AbxyFaceButton(
      button = Xbox360Button.BUTTON_Y,
      label = "Y",
      color = Color(0xFFF1C40F),
      isSelected = selectedButton == Xbox360Button.BUTTON_Y,
      isTested = lastTestedButton == Xbox360Button.BUTTON_Y,
      pulseAlpha = pulseAlpha,
      size = btnSize,
      modifier = Modifier.align(Alignment.TopCenter),
      onClick = { onSelectButton(Xbox360Button.BUTTON_Y) }
    )

    // X (Left, Blue)
    AbxyFaceButton(
      button = Xbox360Button.BUTTON_X,
      label = "X",
      color = Color(0xFF2980B9),
      isSelected = selectedButton == Xbox360Button.BUTTON_X,
      isTested = lastTestedButton == Xbox360Button.BUTTON_X,
      pulseAlpha = pulseAlpha,
      size = btnSize,
      modifier = Modifier.align(Alignment.CenterStart),
      onClick = { onSelectButton(Xbox360Button.BUTTON_X) }
    )

    // B (Right, Red)
    AbxyFaceButton(
      button = Xbox360Button.BUTTON_B,
      label = "B",
      color = Color(0xFFC0392B),
      isSelected = selectedButton == Xbox360Button.BUTTON_B,
      isTested = lastTestedButton == Xbox360Button.BUTTON_B,
      pulseAlpha = pulseAlpha,
      size = btnSize,
      modifier = Modifier.align(Alignment.CenterEnd),
      onClick = { onSelectButton(Xbox360Button.BUTTON_B) }
    )

    // A (Bottom, Green)
    AbxyFaceButton(
      button = Xbox360Button.BUTTON_A,
      label = "A",
      color = Color(0xFF27AE60),
      isSelected = selectedButton == Xbox360Button.BUTTON_A,
      isTested = lastTestedButton == Xbox360Button.BUTTON_A,
      pulseAlpha = pulseAlpha,
      size = btnSize,
      modifier = Modifier.align(Alignment.BottomCenter),
      onClick = { onSelectButton(Xbox360Button.BUTTON_A) }
    )
  }
}

@Composable
private fun AbxyFaceButton(
  button: Xbox360Button,
  label: String,
  color: Color,
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  size: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color(0x66FFFFFF)

  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(
              color.copy(alpha = 0.95f),
              color.copy(alpha = 0.70f),
              Color(0xFF1B1E24)
            )
          )
        )
        .border(
          width = if (isSelected) 2.5.dp else 1.dp,
          color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
          shape = CircleShape
        )
        .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = Color.White,
      fontWeight = FontWeight.Black,
      fontSize = 12.sp,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun ControllerCenterButton(
  button: Xbox360Button,
  label: String,
  symbol: String,
  command: EmulatorCommand?,
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color(0xFF3F4856)

  Column(
    modifier = modifier.clickable { onClick() },
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier =
        Modifier
          .size(width = 28.dp, height = 18.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(if (isSelected) Color(0xFF214E29) else Color(0xFF282F3A))
          .border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
            shape = RoundedCornerShape(8.dp)
          ),
      contentAlignment = Alignment.Center
    ) {
      Text(symbol, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
    Text(label, color = Color(0xFFAAAAAA), fontSize = 8.sp, fontWeight = FontWeight.Bold)
  }
}

@Composable
private fun ControllerGuideJewel(
  isSelected: Boolean,
  isTested: Boolean,
  pulseAlpha: Float,
  size: androidx.compose.ui.unit.Dp,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val borderTint = if (isSelected) XboxBrightGreen else if (isTested) Color.Yellow else Color(0xFF888888)

  Box(
    modifier =
      modifier
        .size(size)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(
              Color(0xFFE8ECEF),
              Color(0xFFB5BAC0),
              Color(0xFF5A6069)
            )
          )
        )
        .border(
          width = if (isSelected) 3.dp else 1.5.dp,
          color = if (isSelected) borderTint.copy(alpha = pulseAlpha) else borderTint,
          shape = CircleShape
        )
        .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    // 4 Ring of Light quadrant indicators (Player 1 top-left illuminated)
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height
      val pad = w * 0.14f

      // Player 1 quadrant (Top-Left arc active green)
      drawArc(
        color = XboxBrightGreen,
        startAngle = 185f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        style = Stroke(width = w * 0.12f)
      )

      // Other quadrants dim
      val dimColor = Color(0x33000000)
      drawArc(
        color = dimColor,
        startAngle = 275f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        style = Stroke(width = w * 0.10f)
      )
      drawArc(
        color = dimColor,
        startAngle = 5f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        style = Stroke(width = w * 0.10f)
      )
      drawArc(
        color = dimColor,
        startAngle = 95f,
        sweepAngle = 70f,
        useCenter = false,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        style = Stroke(width = w * 0.10f)
      )
    }

    // Chrome X center emblem
    Text(
      text = "X",
      color = Color(0xFF1E242B),
      fontSize = (size.value * 0.42f).sp,
      fontWeight = FontWeight.Black
    )
  }
}

// ---------------------------------------------------------
// Selected Button Inspector Card
// ---------------------------------------------------------
@Composable
private fun ActiveButtonInspectorCard(
  button: Xbox360Button,
  currentCommand: EmulatorCommand,
  currentLanguage: AppLanguage,
  testMode: Boolean,
  onChangeCommand: () -> Unit,
  onTestCommand: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("card_inspector"),
    colors = CardDefaults.cardColors(containerColor = XboxCardBg),
    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(XboxNeonGreen.copy(alpha = 0.8f), XboxCardBorder))),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier =
              Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A323D))
                .border(1.dp, XboxBrightGreen, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(button.shortName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
          }

          Column {
            Text(
              text = button.label,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
            Text(
              text = "Categoría: ${button.category}",
              color = Color(0xFF8E9BA8),
              fontSize = 10.sp
            )
          }
        }

        // Action Buttons: Change or Test
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onTestCommand,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(Color(0xFF455A64), Color(0xFF263238)))),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp)
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = XboxBrightGreen, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = when (currentLanguage) {
                AppLanguage.SPANISH -> "Probar"
                AppLanguage.ENGLISH -> "Test"
                AppLanguage.PORTUGUESE -> "Testar"
              },
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Button(
            onClick = onChangeCommand,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE87A4D)),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            modifier = Modifier.height(32.dp).testTag("btn_change_command")
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = when (currentLanguage) {
                AppLanguage.SPANISH -> "Cambiar"
                AppLanguage.ENGLISH -> "Remap"
                AppLanguage.PORTUGUESE -> "Mudar"
              },
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      HorizontalDivider(color = Color(0xFF2A323E), modifier = Modifier.padding(vertical = 8.dp))

      // Current mapped command preview
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF15191F))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(currentCommand.iconGlyph, fontSize = 24.sp)

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = currentCommand.localizedName(currentLanguage),
            color = XboxBrightGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
          Text(
            text = currentCommand.localizedDesc(currentLanguage),
            color = Color(0xFFB5BDC6),
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

// ---------------------------------------------------------
// Button Mapping List Item
// ---------------------------------------------------------
@Composable
private fun ButtonMappingListItem(
  button: Xbox360Button,
  command: EmulatorCommand,
  isSelected: Boolean,
  currentLanguage: AppLanguage,
  onClick: () -> Unit,
  onRemap: () -> Unit
) {
  val bg = if (isSelected) Color(0xFF242C37) else Color(0xFF171B21)
  val border = if (isSelected) XboxBrightGreen else Color(0xFF272F3B)

  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(bg)
        .border(1.dp, border, RoundedCornerShape(10.dp))
        .clickable { onClick() }
        .padding(horizontal = 10.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Left: Button Badge + Name
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      Box(
        modifier =
          Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Color(0xFF2A323F)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = button.shortName,
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 10.sp
        )
      }

      Column {
        Text(
          text = button.label,
          color = Color.White,
          fontWeight = FontWeight.SemiBold,
          fontSize = 11.sp
        )
        Text(
          text = button.category,
          color = Color(0xFF7D8B99),
          fontSize = 9.sp
        )
      }
    }

    // Right: Mapped Command Tag
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier =
          Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E252F))
            .border(1.dp, Color(0xFF333D4C), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(command.iconGlyph, fontSize = 11.sp)
          Text(
            text = command.localizedName(currentLanguage),
            color = XboxBrightGreen,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      IconButton(
        onClick = onRemap,
        modifier = Modifier.size(28.dp)
      ) {
        Icon(Icons.Default.Edit, contentDescription = "Remapear", tint = Color(0xFFE87A4D), modifier = Modifier.size(14.dp))
      }
    }
  }
}

// ---------------------------------------------------------
// Command Picker Modal (Categorized)
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandPickerModal(
  button: Xbox360Button,
  currentCommand: EmulatorCommand,
  currentLanguage: AppLanguage,
  onSelectCommand: (EmulatorCommand) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedCategory by remember { mutableStateOf<CommandCategory?>(null) }
  var searchQuery by remember { mutableStateOf("") }

  val filteredCommands = remember(selectedCategory, searchQuery, currentLanguage) {
    EmulatorCommand.values().filter { cmd ->
      val matchesCat = selectedCategory == null || cmd.category == selectedCategory
      val matchesSearch = searchQuery.isBlank() ||
        cmd.localizedName(currentLanguage).contains(searchQuery, ignoreCase = true) ||
        cmd.localizedDesc(currentLanguage).contains(searchQuery, ignoreCase = true)
      matchesCat && matchesSearch
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF14181E),
    contentColor = Color.White,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.85f)
          .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "Asignar comando a: ${button.label}"
              AppLanguage.ENGLISH -> "Assign command to: ${button.label}"
              AppLanguage.PORTUGUESE -> "Atribuir comando a: ${button.label}"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
          )
          Text(
            text = when (currentLanguage) {
              AppLanguage.SPANISH -> "Selecciona la acción del emulador que ejecutará este botón"
              AppLanguage.ENGLISH -> "Choose which emulator action this button will perform"
              AppLanguage.PORTUGUESE -> "Escolha qual ação do emulador este botão executará"
            },
            color = Color(0xFF9EABB8),
            fontSize = 11.sp
          )
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Search Box
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            when (currentLanguage) {
              AppLanguage.SPANISH -> "Buscar comando (ej. Disparar, Curar, Linterna)..."
              AppLanguage.ENGLISH -> "Search command (e.g. Shoot, Heal, Flashlight)..."
              AppLanguage.PORTUGUESE -> "Buscar comando (ex. Atirar, Curar, Lanterna)..."
            },
            color = Color(0xFF7E8B99),
            fontSize = 12.sp
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF7E8B99))
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF1C222B),
          unfocusedContainerColor = Color(0xFF181D24),
          focusedBorderColor = XboxBrightGreen,
          unfocusedBorderColor = Color(0xFF2C3542),
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Category Filter Chips
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val allLabel = when (currentLanguage) {
          AppLanguage.SPANISH -> "Todos"
          AppLanguage.ENGLISH -> "All"
          AppLanguage.PORTUGUESE -> "Todos"
        }
        val isAll = selectedCategory == null

        Box(
          modifier =
            Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(if (isAll) XboxNeonGreen else Color(0xFF212833))
              .clickable { selectedCategory = null }
              .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(allLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        CommandCategory.values().forEach { cat ->
          val isCat = selectedCategory == cat
          val catLabel = when (currentLanguage) {
            AppLanguage.SPANISH -> cat.labelEs
            AppLanguage.ENGLISH -> cat.labelEn
            AppLanguage.PORTUGUESE -> cat.labelPt
          }
          Box(
            modifier =
              Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(if (isCat) XboxNeonGreen else Color(0xFF212833))
                .clickable { selectedCategory = cat }
                .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(catLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Command List
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filteredCommands) { cmd ->
          val isSelected = cmd == currentCommand
          Card(
            modifier =
              Modifier
                .fillMaxWidth()
                .clickable { onSelectCommand(cmd) },
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) Color(0xFF1E3823) else Color(0xFF1C222B)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
              brush = Brush.horizontalGradient(
                if (isSelected) listOf(XboxBrightGreen, XboxNeonGreen) else listOf(Color(0xFF2B3542), Color(0xFF232A35))
              )
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(cmd.iconGlyph, fontSize = 24.sp)

              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = cmd.localizedName(currentLanguage),
                    color = if (isSelected) XboxBrightGreen else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  if (isSelected) {
                    Box(
                      modifier =
                        Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(XboxNeonGreen)
                          .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                      Text("ACTUAL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                  }
                }
                Text(
                  text = cmd.localizedDesc(currentLanguage),
                  color = Color(0xFFA5B2BF),
                  fontSize = 11.sp
                )
              }

              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = XboxBrightGreen)
              }
            }
          }
        }
      }
    }
  }
}
