package com.example.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TouchSettings
import com.example.model.XboxAchievement
import com.example.ui.theme.ButtonA
import com.example.ui.theme.ButtonB
import com.example.ui.theme.XboxBladeGray
import com.example.ui.theme.XboxGreen
import com.example.ui.theme.XboxGuideGreen
import com.example.ui.theme.XboxNeon

import com.example.model.TranslationStrings
import com.example.model.Translations

@Composable
fun Xbox360GuideOverlay(
  isOpen: Boolean,
  translation: TranslationStrings = Translations.Portuguese,
  onClose: () -> Unit,
  onExitToMenu: () -> Unit,
  achievements: List<XboxAchievement>,
  touchSettings: TouchSettings,
  onTouchSettingsChanged: (TouchSettings) -> Unit,
  onOpenButtonMapping: () -> Unit = {}
) {
  var activeTab by remember { mutableStateOf("menu") } // "menu", "achievements", "controls"

  AnimatedVisibility(
    visible = isOpen,
    enter = fadeIn() + slideInHorizontally { -it },
    exit = fadeOut() + slideOutHorizontally { -it }
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.7f))
          .clickable { onClose() }
    ) {
      // Guide Panel (Left side classic Blade slide-out)
      Column(
        modifier =
          Modifier
            .fillMaxHeight()
            .width(340.dp)
            .background(
              Brush.horizontalGradient(
                listOf(
                  Color(0xFF14191E),
                  Color(0xFF1E262E)
                )
              )
            )
            .border(width = 1.5.dp, color = XboxGreen.copy(alpha = 0.6f))
            .clickable(enabled = false) {}
            .padding(16.dp)
      ) {
        // Guide Header with Glowing Xbox Emblem
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
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(XboxGreen),
              contentAlignment = Alignment.Center
            ) {
              Text(text = "X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Column {
              Text(
                text = translation.guideTitle,
                color = XboxNeon,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
              )
              Text(
                text = "Left 4 Dead (X360)",
                color = Color.LightGray,
                fontSize = 11.sp
              )
            }
          }

          IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_guide")) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Content based on tab
        when (activeTab) {
          "menu" -> {
            GuideMainMenu(
              translation = translation,
              onResume = onClose,
              onOpenAchievements = { activeTab = "achievements" },
              onOpenControls = { activeTab = "controls" },
              onExitToMenu = onExitToMenu
            )
          }
          "achievements" -> {
            GuideAchievementsMenu(
              achievements = achievements,
              onBack = { activeTab = "menu" }
            )
          }
          "controls" -> {
            GuideControlsMenu(
              touchSettings = touchSettings,
              onChanged = onTouchSettingsChanged,
              onOpenButtonMapping = onOpenButtonMapping,
              onBack = { activeTab = "menu" }
            )
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Xbox Quick Help (A Seleccionar, B Volver)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier.size(16.dp).clip(CircleShape).background(ButtonA),
              contentAlignment = Alignment.Center
            ) {
              Text("A", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text("Seleccionar", color = Color.LightGray, fontSize = 11.sp)
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Box(
              modifier = Modifier.size(16.dp).clip(CircleShape).background(ButtonB),
              contentAlignment = Alignment.Center
            ) {
              Text("B", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text("Volver / Reanudar", color = Color.LightGray, fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun GuideMainMenu(
  translation: TranslationStrings,
  onResume: () -> Unit,
  onOpenAchievements: () -> Unit,
  onOpenControls: () -> Unit,
  onExitToMenu: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    GuideOptionButton(
      icon = Icons.Default.PlayArrow,
      title = translation.guideResume,
      subtitle = "Left 4 Dead",
      onClick = onResume,
      testTag = "guide_btn_resume"
    )

    GuideOptionButton(
      icon = Icons.Default.EmojiEvents,
      title = translation.guideAchievements,
      subtitle = "Gamerscore",
      onClick = onOpenAchievements,
      testTag = "guide_btn_achievements"
    )

    GuideOptionButton(
      icon = Icons.Default.VideogameAsset,
      title = translation.guideControls,
      subtitle = translation.touchOpacity,
      onClick = onOpenControls,
      testTag = "guide_btn_controls"
    )

    GuideOptionButton(
      icon = Icons.Default.Refresh,
      title = translation.guideExit,
      subtitle = translation.romDialogTitle,
      onClick = onExitToMenu,
      testTag = "guide_btn_exit"
    )
  }
}

@Composable
private fun GuideOptionButton(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String
) {
  Box(
    modifier =
      Modifier
        .testTag(testTag)
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color(0xFF26303A))
        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
        .clickable { onClick() }
        .padding(horizontal = 12.dp, vertical = 10.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(XboxGreen.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = XboxNeon, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = subtitle, color = Color.Gray, fontSize = 10.sp)
      }
    }
  }
}

@Composable
private fun GuideAchievementsMenu(
  achievements: List<XboxAchievement>,
  onBack: () -> Unit
) {
  val totalG = achievements.filter { it.isUnlocked }.sumOf { it.gamerscore }
  val maxG = achievements.sumOf { it.gamerscore }

  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "LOGROS (${achievements.count { it.isUnlocked }}/${achievements.size})",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
      Text(
        text = "${totalG}G / ${maxG}G",
        color = XboxNeon,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      modifier = Modifier.height(300.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      items(achievements) { a ->
        Row(
          modifier =
            Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(if (a.isUnlocked) Color(0xFF1E3824) else Color(0xFF1A1F24))
              .border(
                1.dp,
                if (a.isUnlocked) XboxGreen else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(6.dp)
              )
              .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = a.icon, fontSize = 20.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = a.title,
              color = if (a.isUnlocked) Color.White else Color.Gray,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
            Text(
              text = a.description,
              color = if (a.isUnlocked) Color.LightGray else Color.DarkGray,
              fontSize = 10.sp
            )
          }
          Text(
            text = "${a.gamerscore}G",
            color = if (a.isUnlocked) XboxNeon else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun GuideControlsMenu(
  touchSettings: TouchSettings,
  onChanged: (TouchSettings) -> Unit,
  onOpenButtonMapping: () -> Unit = {},
  onBack: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Text(
      text = "CONFIGURACIÓN TÁCTIL",
      color = Color.White,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp
    )

    // Opacity
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Opacidad de botones", color = Color.LightGray, fontSize = 11.sp)
        Text("${(touchSettings.opacity * 100).toInt()}%", color = XboxNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
      Slider(
        value = touchSettings.opacity,
        onValueChange = { onChanged(touchSettings.copy(opacity = it)) },
        valueRange = 0.15f..1.0f,
        colors = SliderDefaults.colors(thumbColor = XboxGreen, activeTrackColor = XboxNeon)
      )
    }

    // Button Size / Scale
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Tamaño de botones", color = Color.LightGray, fontSize = 11.sp)
        Text(String.format("%.1fx", touchSettings.buttonScale), color = XboxNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
      Slider(
        value = touchSettings.buttonScale,
        onValueChange = { onChanged(touchSettings.copy(buttonScale = it)) },
        valueRange = 0.7f..1.4f,
        colors = SliderDefaults.colors(thumbColor = XboxGreen, activeTrackColor = XboxNeon)
      )
    }

    // Sensitivity
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text("Sensibilidad de mira", color = Color.LightGray, fontSize = 11.sp)
        Text(String.format("%.1fx", touchSettings.sensitivity), color = XboxNeon, fontSize = 11.sp)
      }
      Slider(
        value = touchSettings.sensitivity,
        onValueChange = { onChanged(touchSettings.copy(sensitivity = it)) },
        valueRange = 0.5f..2.5f,
        colors = SliderDefaults.colors(thumbColor = XboxGreen, activeTrackColor = XboxNeon)
      )
    }

    // Show D-Pad
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Mostrar cruceta D-Pad", color = Color.LightGray, fontSize = 11.sp)
      Switch(
        checked = touchSettings.showDpad,
        onCheckedChange = { onChanged(touchSettings.copy(showDpad = it)) },
        colors = SwitchDefaults.colors(checkedThumbColor = XboxGreen, checkedTrackColor = XboxNeon)
      )
    }

    // Show Right Stick
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Mostrar palanca derecha (RS)", color = Color.LightGray, fontSize = 11.sp)
      Switch(
        checked = touchSettings.showRightStick,
        onCheckedChange = { onChanged(touchSettings.copy(showRightStick = it)) },
        colors = SwitchDefaults.colors(checkedThumbColor = XboxGreen, checkedTrackColor = XboxNeon)
      )
    }

    // Vibration / Haptics Toggle
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Vibración háptica (Rumble)", color = Color.LightGray, fontSize = 11.sp)
      Switch(
        checked = touchSettings.hapticsEnabled,
        onCheckedChange = { onChanged(touchSettings.copy(hapticsEnabled = it)) },
        colors = SwitchDefaults.colors(checkedThumbColor = XboxGreen, checkedTrackColor = XboxNeon)
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Interactive Button Mapping Entry
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF223628))
          .border(1.dp, XboxNeon, RoundedCornerShape(8.dp))
          .clickable { onOpenButtonMapping() }
          .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = XboxNeon, modifier = Modifier.size(18.dp))
        Column {
          Text("Mapeo Visual de Botones", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text("Personalizar botones en control Xbox 360", color = Color(0xFFC7CBD1), fontSize = 10.sp)
        }
      }
    }
  }
}

@Composable
fun XboxAchievementToast(
  achievement: XboxAchievement?,
  translation: TranslationStrings = Translations.Portuguese,
  onDismiss: () -> Unit
) {
  AnimatedVisibility(
    visible = achievement != null,
    enter = slideInVertically { -it } + fadeIn(),
    exit = slideOutVertically { -it } + fadeOut()
  ) {
    if (achievement != null) {
      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, start = 32.dp, end = 32.dp),
        contentAlignment = Alignment.TopCenter
      ) {
        Row(
          modifier =
            Modifier
              .clip(RoundedCornerShape(24.dp))
              .background(
                Brush.horizontalGradient(
                  listOf(
                    Color(0xFF1B231B),
                    Color(0xFF104A15),
                    Color(0xFF1B231B)
                  )
                )
              )
              .border(1.5.dp, XboxNeon, RoundedCornerShape(24.dp))
              .padding(horizontal = 18.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Xbox Badge Jewel Icon
          Box(
            modifier =
              Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(XboxNeon),
            contentAlignment = Alignment.Center
          ) {
            Text("🏆", fontSize = 16.sp)
          }

          Column {
            Text(
              text = translation.achievementUnlocked,
              color = XboxNeon,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
            Text(
              text = "${achievement.title} - ${achievement.gamerscore}G",
              color = Color.White,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp
            )
          }
        }
      }
    }
  }
}
