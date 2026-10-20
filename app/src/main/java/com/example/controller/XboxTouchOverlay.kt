package com.example.controller

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TouchSettings
import com.example.model.TranslationStrings
import com.example.model.Translations
import com.example.ui.theme.ButtonA
import com.example.ui.theme.ButtonB
import com.example.ui.theme.ButtonX
import com.example.ui.theme.ButtonY
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxNeon
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun XboxTouchOverlay(
  settings: TouchSettings,
  onSettingsChanged: ((TouchSettings) -> Unit)? = null,
  translation: TranslationStrings = Translations.Spanish,
  onMove: (dx: Float, dy: Float) -> Unit,
  onAim: (dx: Float, dy: Float) -> Unit,
  onFireDown: () -> Unit,
  onFireUp: () -> Unit,
  onMelee: () -> Unit,
  onReload: () -> Unit,
  onSwitchWeapon: () -> Unit,
  onFlashlightToggle: () -> Unit,
  onCrouchToggle: () -> Unit,
  onJump: () -> Unit,
  onDpadAction: ((String) -> Unit)? = null,
  onOpenGuide: () -> Unit,
  onStartButton: () -> Unit,
  onBackButton: () -> Unit,
  modifier: Modifier = Modifier
) {
  val baseAlpha = settings.opacity
  val scaleFactor = settings.buttonScale

  // In-Game Quick Touch Customization Sheet state
  var showQuickCustomizer by remember { mutableStateOf(false) }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWide = maxWidth > 600.dp

    // -------------------------------------------------------------
    // TOP BAR: Triggers (LT/RT), Bumpers (LB/RB), Back, Guide, Start, Quick Settings
    // -------------------------------------------------------------
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 6.dp)
          .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left Shoulder: LT & LB
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        XboxTriggerButton(
          label = "LT",
          sublabel = translation.ltLabel,
          color = Color(0xFF4A5568),
          alpha = baseAlpha,
          onClick = onMelee,
          testTag = "trigger_lt"
        )
        XboxTriggerButton(
          label = "LB",
          sublabel = translation.lbLabel,
          color = Color(0xFF333E48),
          alpha = baseAlpha,
          onClick = onFlashlightToggle,
          testTag = "bumper_lb"
        )
      }

      // Center Group: Back, Guide Jewel, Start, and Quick Customizer pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        XboxSmallMenuButton(label = "BACK", alpha = baseAlpha, onClick = onBackButton)

        // Iconic Glowing Xbox 360 Guide Jewel
        XboxGuideJewel(alpha = baseAlpha, onClick = onOpenGuide)

        XboxSmallMenuButton(label = "START", alpha = baseAlpha, onClick = onStartButton)

        // In-Game Quick Opacity Customizer Button
        if (onSettingsChanged != null) {
          Box(
            modifier =
              Modifier
                .testTag("btn_quick_opacity_customizer")
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x66000000))
                .border(1.dp, Color(0x66E87A4D), RoundedCornerShape(12.dp))
                .clickable(
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  onClick = { showQuickCustomizer = !showQuickCustomizer }
                )
                .padding(horizontal = 7.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                Icons.Default.Tune,
                contentDescription = null,
                tint = Color(0xFFFFCC80),
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "${(settings.opacity * 100).toInt()}%",
                color = Color(0xFFFFE0B2),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Right Shoulder: RB & RT
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        XboxTriggerButton(
          label = "RB",
          sublabel = translation.rbLabel,
          color = Color(0xFF333E48),
          alpha = baseAlpha,
          onClick = onSwitchWeapon,
          testTag = "bumper_rb"
        )
        XboxTriggerButton(
          label = "RT",
          sublabel = translation.rtLabel,
          color = Color(0xFFC0392B),
          alpha = baseAlpha,
          onPressDown = onFireDown,
          onPressUp = onFireUp,
          testTag = "trigger_rt"
        )
      }
    }

    // -------------------------------------------------------------
    // LEFT SIDE: Left Analog Stick (LS) & Xbox 360 D-Pad Cross
    // -------------------------------------------------------------
    Box(
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .padding(start = 16.dp, bottom = 14.dp)
          .scale(scaleFactor)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Left Analog Thumbstick
        XboxVirtualStick(
          label = translation.moveStickLabel,
          alpha = baseAlpha,
          onVectorChanged = { dx, dy -> onMove(dx, dy) }
        )

        // Xbox 360 D-Pad (8-Way Cross)
        if (settings.showDpad) {
          XboxDpadCross(
            alpha = baseAlpha,
            onUp = {
              onFlashlightToggle()
              onDpadAction?.invoke("UP")
            },
            onDown = {
              onReload()
              onDpadAction?.invoke("DOWN")
            },
            onLeft = {
              onSwitchWeapon()
              onDpadAction?.invoke("LEFT")
            },
            onRight = {
              onMelee()
              onDpadAction?.invoke("RIGHT")
            }
          )
        }
      }
    }

    // -------------------------------------------------------------
    // RIGHT SIDE: ABXY Diamond & Right Analog Thumbstick (RS)
    // -------------------------------------------------------------
    Box(
      modifier =
        Modifier
          .align(Alignment.BottomEnd)
          .padding(end = 16.dp, bottom = 14.dp)
          .scale(scaleFactor)
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // ABXY Diamond Cluster
        XboxAbxyDiamond(
          alpha = baseAlpha,
          translation = translation,
          onA = onJump,
          onB = onCrouchToggle,
          onX = onReload,
          onY = onSwitchWeapon
        )

        // Right Thumbstick (RS) for Look / Aim & RS Click (Melee)
        if (settings.showRightStick) {
          XboxRightStick(
            alpha = baseAlpha,
            onVectorChanged = { dx, dy ->
              onAim(dx * 18f * settings.sensitivity, dy * 18f * settings.sensitivity)
            },
            onStickClick = onMelee
          )
        }
      }
    }

    // -------------------------------------------------------------
    // CENTER-SCREEN AIM TOUCH ZONE (Swipe to look around)
    // -------------------------------------------------------------
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(top = 70.dp, bottom = 120.dp, start = 160.dp, end = 160.dp)
          .pointerInput(settings.sensitivity) {
            detectDragGestures { change, dragAmount ->
              change.consume()
              onAim(dragAmount.x * settings.sensitivity, dragAmount.y * settings.sensitivity)
            }
          }
    )

    // -------------------------------------------------------------
    // IN-GAME QUICK TOUCH OPACITY & LAYOUT CUSTOMIZER SHEET
    // -------------------------------------------------------------
    AnimatedVisibility(
      visible = showQuickCustomizer && onSettingsChanged != null,
      enter = slideInVertically { it / 2 } + fadeIn(),
      exit = slideOutVertically { it / 2 } + fadeOut(),
      modifier = Modifier.align(Alignment.Center)
    ) {
      onSettingsChanged?.let { updateSettings ->
        QuickTouchCustomizerDialog(
          settings = settings,
          translation = translation,
          onUpdate = updateSettings,
          onClose = { showQuickCustomizer = false }
        )
      }
    }
  }
}

// -------------------------------------------------------------
// Component: In-game Quick Touch Customizer Popup
// -------------------------------------------------------------
@Composable
private fun QuickTouchCustomizerDialog(
  settings: TouchSettings,
  translation: TranslationStrings,
  onUpdate: (TouchSettings) -> Unit,
  onClose: () -> Unit
) {
  Box(
    modifier =
      Modifier
        .testTag("dialog_quick_touch_customizer")
        .width(340.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color(0xF014171A))
        .border(1.5.dp, Color(0xFFE87A4D), RoundedCornerShape(16.dp))
        .padding(16.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFFFCC80), modifier = Modifier.size(18.dp))
          Text(
            text = translation.touchQuickSettings,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
        Icon(
          Icons.Default.Close,
          contentDescription = "Close",
          tint = Color.LightGray,
          modifier = Modifier.size(20.dp).clickable { onClose() }
        )
      }

      // Live Opacity Slider (10% to 100%)
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(translation.touchOpacity, color = Color.LightGray, fontSize = 11.sp)
          Text("${(settings.opacity * 100).toInt()}%", color = Color(0xFFFFCC80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = settings.opacity,
          onValueChange = { onUpdate(settings.copy(opacity = it)) },
          valueRange = 0.15f..1.0f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE87A4D),
            activeTrackColor = Color(0xFFE87A4D),
            inactiveTrackColor = Color(0x44FFFFFF)
          )
        )
      }

      // Button Scale Slider (0.7x to 1.4x)
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(translation.touchScale, color = Color.LightGray, fontSize = 11.sp)
          Text(String.format("%.1fx", settings.buttonScale), color = Color(0xFFFFCC80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = settings.buttonScale,
          onValueChange = { onUpdate(settings.copy(buttonScale = it)) },
          valueRange = 0.7f..1.4f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE87A4D),
            activeTrackColor = Color(0xFFE87A4D),
            inactiveTrackColor = Color(0x44FFFFFF)
          )
        )
      }

      // Sensitivity Slider
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(translation.touchSensitivity, color = Color.LightGray, fontSize = 11.sp)
          Text(String.format("%.1fx", settings.sensitivity), color = Color(0xFFFFCC80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
          value = settings.sensitivity,
          onValueChange = { onUpdate(settings.copy(sensitivity = it)) },
          valueRange = 0.5f..2.5f,
          colors = SliderDefaults.colors(
            thumbColor = Color(0xFFE87A4D),
            activeTrackColor = Color(0xFFE87A4D),
            inactiveTrackColor = Color(0x44FFFFFF)
          )
        )
      }

      // Toggle D-Pad
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(translation.touchShowDpad, color = Color.LightGray, fontSize = 11.sp)
        Switch(
          checked = settings.showDpad,
          onCheckedChange = { onUpdate(settings.copy(showDpad = it)) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFFFFCC80),
            checkedTrackColor = Color(0xFFD35400)
          )
        )
      }

      // Toggle Right Stick (RS)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(translation.touchShowRightStick, color = Color.LightGray, fontSize = 11.sp)
        Switch(
          checked = settings.showRightStick,
          onCheckedChange = { onUpdate(settings.copy(showRightStick = it)) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFFFFCC80),
            checkedTrackColor = Color(0xFFD35400)
          )
        )
      }

      // Toggle Haptics
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(translation.touchHaptics, color = Color.LightGray, fontSize = 11.sp)
        Switch(
          checked = settings.hapticsEnabled,
          onCheckedChange = { onUpdate(settings.copy(hapticsEnabled = it)) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFFFFCC80),
            checkedTrackColor = Color(0xFFD35400)
          )
        )
      }

      // Done Button
      Button(
        onClick = onClose,
        modifier = Modifier.fillMaxWidth().height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE87A4D)),
        shape = RoundedCornerShape(19.dp)
      ) {
        Text(translation.closeBtn, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}

// -------------------------------------------------------------
// Component: Xbox 360 D-Pad Cross (8-Way Directional Pad)
// -------------------------------------------------------------
@Composable
fun XboxDpadCross(
  alpha: Float,
  onUp: () -> Unit,
  onDown: () -> Unit,
  onLeft: () -> Unit,
  onRight: () -> Unit
) {
  Box(
    modifier =
      Modifier
        .testTag("xbox_dpad_cross")
        .size(96.dp)
        .alpha(alpha),
    contentAlignment = Alignment.Center
  ) {
    // Outer circular disc base (Xbox 360 Transforming D-Pad base)
    Canvas(modifier = Modifier.size(96.dp)) {
      drawCircle(
        brush =
          Brush.radialGradient(
            listOf(
              Color(0xFF2C3238),
              Color(0xFF181C20),
              Color(0xFF0C0E10)
            )
          ),
        radius = size.width / 2
      )
      drawCircle(
        color = Color(0x33FFFFFF),
        radius = size.width / 2,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
      )
    }

    // Up arm
    Box(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .size(width = 30.dp, height = 34.dp)
          .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
          .background(Color(0xFF333A42))
          .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onUp
          ),
      contentAlignment = Alignment.Center
    ) {
      Text("▲", color = Color(0xFFBDC3C7), fontSize = 10.sp)
    }

    // Down arm
    Box(
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .size(width = 30.dp, height = 34.dp)
          .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
          .background(Color(0xFF333A42))
          .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onDown
          ),
      contentAlignment = Alignment.Center
    ) {
      Text("▼", color = Color(0xFFBDC3C7), fontSize = 10.sp)
    }

    // Left arm
    Box(
      modifier =
        Modifier
          .align(Alignment.CenterStart)
          .size(width = 34.dp, height = 30.dp)
          .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
          .background(Color(0xFF333A42))
          .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onLeft
          ),
      contentAlignment = Alignment.Center
    ) {
      Text("◀", color = Color(0xFFBDC3C7), fontSize = 10.sp)
    }

    // Right arm
    Box(
      modifier =
        Modifier
          .align(Alignment.CenterEnd)
          .size(width = 34.dp, height = 30.dp)
          .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
          .background(Color(0xFF333A42))
          .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onRight
          ),
      contentAlignment = Alignment.Center
    ) {
      Text("▶", color = Color(0xFFBDC3C7), fontSize = 10.sp)
    }

    // Center concave hub
    Box(
      modifier =
        Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(Color(0xFF1E2328))
          .border(1.dp, Color(0x33FFFFFF), CircleShape)
    )
  }
}

// -------------------------------------------------------------
// Component: Xbox Right Analog Stick (RS) for Look / Aim
// -------------------------------------------------------------
@Composable
fun XboxRightStick(
  alpha: Float,
  onVectorChanged: (Float, Float) -> Unit,
  onStickClick: () -> Unit
) {
  var thumbX by remember { mutableFloatStateOf(0f) }
  var thumbY by remember { mutableFloatStateOf(0f) }
  val maxRadius = 45f

  val animatedThumbX by animateFloatAsState(targetValue = thumbX, label = "rsThumbX")
  val animatedThumbY by animateFloatAsState(targetValue = thumbY, label = "rsThumbY")

  Box(
    modifier =
      Modifier
        .testTag("xbox_right_stick")
        .size(96.dp)
        .alpha(alpha)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(
              Color(0xFF282C34).copy(alpha = 0.7f),
              Color(0xFF131518).copy(alpha = 0.85f)
            )
          )
        )
        .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              val dx = offset.x - size.width / 2
              val dy = offset.y - size.height / 2
              val dist = sqrt(dx * dx + dy * dy)
              val clampedDist = min(dist, maxRadius)
              val angle = Math.atan2(dy.toDouble(), dx.toDouble())
              thumbX = (clampedDist * cos(angle)).toFloat()
              thumbY = (clampedDist * sin(angle)).toFloat()
              onVectorChanged(thumbX / maxRadius, thumbY / maxRadius)
            },
            onDragEnd = {
              thumbX = 0f
              thumbY = 0f
              onVectorChanged(0f, 0f)
            },
            onDragCancel = {
              thumbX = 0f
              thumbY = 0f
              onVectorChanged(0f, 0f)
            },
            onDrag = { change, _ ->
              change.consume()
              val dx = change.position.x - size.width / 2
              val dy = change.position.y - size.height / 2
              val dist = sqrt(dx * dx + dy * dy)
              val clampedDist = min(dist, maxRadius)
              val angle = Math.atan2(dy.toDouble(), dx.toDouble())
              thumbX = (clampedDist * cos(angle)).toFloat()
              thumbY = (clampedDist * sin(angle)).toFloat()
              onVectorChanged(thumbX / maxRadius, thumbY / maxRadius)
            }
          )
        },
    contentAlignment = Alignment.Center
  ) {
    // Stick Knob
    Box(
      modifier =
        Modifier
          .offset { IntOffset(animatedThumbX.toInt(), animatedThumbY.toInt()) }
          .size(46.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(
                Color(0xFF555B66),
                Color(0xFF2E323A),
                Color(0xFF16181B)
              )
            )
          )
          .border(1.5.dp, Color(0xFFE87A4D).copy(alpha = 0.6f), CircleShape)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onStickClick
          ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "RS",
        color = Color.White.copy(alpha = 0.85f),
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
fun XboxTriggerButton(
  label: String,
  sublabel: String,
  color: Color,
  alpha: Float,
  onClick: (() -> Unit)? = null,
  onPressDown: (() -> Unit)? = null,
  onPressUp: (() -> Unit)? = null,
  testTag: String = "xbox_trigger"
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier =
      Modifier
        .testTag(testTag)
        .size(width = 62.dp, height = 42.dp)
        .alpha(alpha)
        .clip(RoundedCornerShape(8.dp))
        .background(
          brush =
            Brush.verticalGradient(
              listOf(
                if (isPressed) color.copy(alpha = 0.95f) else color.copy(alpha = 0.55f),
                Color(0xFF14171A).copy(alpha = 0.85f)
              )
            )
        )
        .border(
          width = 1.dp,
          color = if (isPressed) XboxNeon else Color.White.copy(alpha = 0.3f),
          shape = RoundedCornerShape(8.dp)
        )
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = {
              isPressed = true
              onPressDown?.invoke()
              onClick?.invoke()
            },
            onDragEnd = {
              isPressed = false
              onPressUp?.invoke()
            },
            onDragCancel = {
              isPressed = false
              onPressUp?.invoke()
            },
            onDrag = { _, _ -> }
          )
        },
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )
      Text(
        text = sublabel,
        color = Color(0xFFD0D7DE),
        fontSize = 8.sp
      )
    }
  }
}

@Composable
fun XboxSmallMenuButton(
  label: String,
  alpha: Float,
  onClick: () -> Unit
) {
  Box(
    modifier =
      Modifier
        .testTag("btn_${label.lowercase()}")
        .size(width = 46.dp, height = 26.dp)
        .alpha(alpha)
        .clip(RoundedCornerShape(13.dp))
        .background(Color(0xFF22262A).copy(alpha = 0.75f))
        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(13.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick
        ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = Color.LightGray,
      fontSize = 8.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun XboxGuideJewel(
  alpha: Float,
  onClick: () -> Unit
) {
  Box(
    modifier =
      Modifier
        .testTag("xbox_guide_jewel")
        .size(44.dp)
        .alpha(alpha)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(
              Color(0xFFE0E5EC),
              Color(0xFF8A939E),
              Color(0xFF1E2124)
            )
          )
        )
        .border(2.dp, XboxGreen, CircleShape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick
        ),
    contentAlignment = Alignment.Center
  ) {
    // 4 Glowing green quadrants (Xbox 360 Ring of Light)
    Canvas(modifier = Modifier.size(32.dp)) {
      val center = Offset(size.width / 2, size.height / 2)
      val radius = size.width / 2.2f

      drawCircle(
        color = XboxNeon.copy(alpha = 0.85f),
        radius = radius,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
      )

      val strokeW = 3.5.dp.toPx()
      drawLine(
        color = Color(0xFF101214),
        start = Offset(center.x - 6.dp.toPx(), center.y - 6.dp.toPx()),
        end = Offset(center.x + 6.dp.toPx(), center.y + 6.dp.toPx()),
        strokeWidth = strokeW
      )
      drawLine(
        color = Color(0xFF101214),
        start = Offset(center.x + 6.dp.toPx(), center.y - 6.dp.toPx()),
        end = Offset(center.x - 6.dp.toPx(), center.y + 6.dp.toPx()),
        strokeWidth = strokeW
      )
    }
  }
}

@Composable
fun XboxVirtualStick(
  label: String,
  alpha: Float,
  onVectorChanged: (Float, Float) -> Unit
) {
  var thumbX by remember { mutableFloatStateOf(0f) }
  var thumbY by remember { mutableFloatStateOf(0f) }
  val maxRadius = 55f

  val animatedThumbX by animateFloatAsState(targetValue = thumbX, label = "thumbX")
  val animatedThumbY by animateFloatAsState(targetValue = thumbY, label = "thumbY")

  Box(
    modifier =
      Modifier
        .testTag("xbox_virtual_stick")
        .size(124.dp)
        .alpha(alpha)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(
              Color(0xFF282C34).copy(alpha = 0.7f),
              Color(0xFF131518).copy(alpha = 0.85f)
            )
          )
        )
        .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
        .pointerInput(Unit) {
          detectDragGestures(
            onDragStart = { offset ->
              val dx = offset.x - size.width / 2
              val dy = offset.y - size.height / 2
              val dist = sqrt(dx * dx + dy * dy)
              val clampedDist = min(dist, maxRadius)
              val angle = Math.atan2(dy.toDouble(), dx.toDouble())
              thumbX = (clampedDist * cos(angle)).toFloat()
              thumbY = (clampedDist * sin(angle)).toFloat()
              onVectorChanged(thumbX / maxRadius, thumbY / maxRadius)
            },
            onDragEnd = {
              thumbX = 0f
              thumbY = 0f
              onVectorChanged(0f, 0f)
            },
            onDragCancel = {
              thumbX = 0f
              thumbY = 0f
              onVectorChanged(0f, 0f)
            },
            onDrag = { change, _ ->
              change.consume()
              val dx = change.position.x - size.width / 2
              val dy = change.position.y - size.height / 2
              val dist = sqrt(dx * dx + dy * dy)
              val clampedDist = min(dist, maxRadius)
              val angle = Math.atan2(dy.toDouble(), dx.toDouble())
              thumbX = (clampedDist * cos(angle)).toFloat()
              thumbY = (clampedDist * sin(angle)).toFloat()
              onVectorChanged(thumbX / maxRadius, thumbY / maxRadius)
            }
          )
        },
    contentAlignment = Alignment.Center
  ) {
    // Stick Knob
    Box(
      modifier =
        Modifier
          .offset { IntOffset(animatedThumbX.toInt(), animatedThumbY.toInt()) }
          .size(52.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              listOf(
                Color(0xFF555B66),
                Color(0xFF2E323A),
                Color(0xFF16181B)
              )
            )
          )
          .border(2.dp, XboxNeon.copy(alpha = 0.5f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "LS",
        color = Color.White.copy(alpha = 0.85f),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun XboxAbxyDiamond(
  alpha: Float,
  translation: TranslationStrings = Translations.Spanish,
  onA: () -> Unit,
  onB: () -> Unit,
  onX: () -> Unit,
  onY: () -> Unit
) {
  Box(
    modifier =
      Modifier
        .testTag("xbox_abxy_diamond")
        .size(136.dp)
        .alpha(alpha),
    contentAlignment = Alignment.Center
  ) {
    // Y (Top - Yellow)
    Box(modifier = Modifier.align(Alignment.TopCenter)) {
      XboxColorButton(
        letter = "Y",
        sublabel = translation.btnYLabel,
        btnColor = ButtonY,
        onClick = onY,
        testTag = "btn_y"
      )
    }

    // X (Left - Blue)
    Box(modifier = Modifier.align(Alignment.CenterStart)) {
      XboxColorButton(
        letter = "X",
        sublabel = translation.btnXLabel,
        btnColor = ButtonX,
        onClick = onX,
        testTag = "btn_x"
      )
    }

    // B (Right - Red)
    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
      XboxColorButton(
        letter = "B",
        sublabel = translation.btnBLabel,
        btnColor = ButtonB,
        onClick = onB,
        testTag = "btn_b"
      )
    }

    // A (Bottom - Green)
    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
      XboxColorButton(
        letter = "A",
        sublabel = translation.btnALabel,
        btnColor = ButtonA,
        onClick = onA,
        testTag = "btn_a"
      )
    }
  }
}

@Composable
fun XboxColorButton(
  letter: String,
  sublabel: String,
  btnColor: Color,
  onClick: () -> Unit,
  testTag: String
) {
  var isPressed by remember { mutableStateOf(false) }

  Box(
    modifier =
      Modifier
        .testTag(testTag)
        .size(44.dp)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(
              btnColor.copy(alpha = if (isPressed) 1.0f else 0.85f),
              btnColor.copy(alpha = 0.6f),
              Color(0xFF15181C)
            )
          )
        )
        .border(
          width = 1.5.dp,
          color = Color.White.copy(alpha = 0.45f),
          shape = CircleShape
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick
        ),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = letter,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp
      )
      Text(
        text = sublabel,
        color = Color(0xFFEDEDED),
        fontSize = 7.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}
