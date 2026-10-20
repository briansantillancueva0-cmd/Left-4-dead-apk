package com.example.game

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.model.AppLanguage

enum class GameScene(
  val id: String,
  val nameEs: String,
  val nameEn: String,
  val namePt: String,
  val chapterNumber: Int,
  val totalShaders: Int,
  val ambientDescriptionEs: String,
  val ambientDescriptionEn: String,
  val ambientDescriptionPt: String,
  val skyColor: Long,
  val groundColor: Long
) {
  ALLEYWAYS(
    id = "scene_alleyways",
    nameEs = "Los Callejones (Fairfield)",
    nameEn = "The Alleyways (Fairfield)",
    namePt = "Os Becos (Fairfield)",
    chapterNumber = 1,
    totalShaders = 64,
    ambientDescriptionEs = "Niebla nocturna densa, farolas titilantes y charcos de lluvia.",
    ambientDescriptionEn = "Dense night fog, flickering lampposts and rainy puddles.",
    ambientDescriptionPt = "Nevoeiro denso noturno, postes piscando e poças de chuva.",
    skyColor = 0xFF0E131A,
    groundColor = 0xFF14191F
  ),
  SUBWAY(
    id = "scene_subway",
    nameEs = "El Metro Subterráneo",
    nameEn = "The Subway Tunnels",
    namePt = "O Metrô Subterrâneo",
    chapterNumber = 2,
    totalShaders = 82,
    ambientDescriptionEs = "Vagones volcados, generador diesel y luces de emergencia rojas.",
    ambientDescriptionEn = "Overturned train cars, diesel generator and red emergency beacons.",
    ambientDescriptionPt = "Vagões capotados, gerador a diesel e luzes vermelhas de emergência.",
    skyColor = 0xFF120B0B,
    groundColor = 0xFF1A1212
  ),
  SEWERS(
    id = "scene_sewers",
    nameEs = "Cloacas y Drenajes",
    nameEn = "Sewers & Drainage",
    namePt = "Esgotos e Drenagem",
    chapterNumber = 3,
    totalShaders = 76,
    ambientDescriptionEs = "Tuberías oxidadas, agua estancada y eco de gritos de infectados.",
    ambientDescriptionEn = "Rusty pipes, murky water and echoing screams of infected.",
    ambientDescriptionPt = "Tubulações enferrujadas, água turva e eco de infectados.",
    skyColor = 0xFF0A130F,
    groundColor = 0xFF0F1A15
  ),
  HOSPITAL_ROOFTOP(
    id = "scene_hospital",
    nameEs = "Azotea del Hospital Mercy",
    nameEn = "Mercy Hospital Rooftop",
    namePt = "Terraço do Hospital Mercy",
    chapterNumber = 4,
    totalShaders = 108,
    ambientDescriptionEs = "Helipuerto de rescate, lluvia torrencial y asedio final con el Tank.",
    ambientDescriptionEn = "Rescue helipad, torrential rain and climactic Tank battle.",
    ambientDescriptionPt = "Heliponto de resgate, chuva forte e batalha épica com o Tank.",
    skyColor = 0xFF14101A,
    groundColor = 0xFF1F1926
  );

  fun localizedName(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SPANISH -> nameEs
      AppLanguage.ENGLISH -> nameEn
      AppLanguage.PORTUGUESE -> namePt
    }

  fun localizedDesc(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SPANISH -> ambientDescriptionEs
      AppLanguage.ENGLISH -> ambientDescriptionEn
      AppLanguage.PORTUGUESE -> ambientDescriptionPt
    }
}

enum class ShaderCompilationState {
  CHECKING_CACHE,
  SAVED_CACHE_LOADED,
  COMPILING_FIRST_TIME,
  COMPILATION_COMPLETE_SAVED
}

data class SceneShaderStatus(
  val scene: GameScene,
  val isSavedInCache: Boolean,
  val cachedShaderCount: Int
)

class ShaderCacheManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("x360_vulkan_shader_cache", Context.MODE_PRIVATE)

  // In-memory reactive state for Compose
  val sceneSavedStatus = mutableStateMapOf<String, Boolean>()
  var totalCachedShadersCount by mutableIntStateOf(0)
    private set

  init {
    refreshCacheStatus()
  }

  fun refreshCacheStatus() {
    var total = 0
    GameScene.values().forEach { scene ->
      val isSaved = prefs.getBoolean("cache_saved_${scene.id}", false)
      sceneSavedStatus[scene.id] = isSaved
      if (isSaved) {
        total += prefs.getInt("shader_count_${scene.id}", scene.totalShaders)
      }
    }
    totalCachedShadersCount = total
  }

  fun isSceneCompiled(sceneId: String): Boolean {
    return prefs.getBoolean("cache_saved_$sceneId", false)
  }

  fun getCompiledShaderCount(sceneId: String): Int {
    return prefs.getInt("shader_count_$sceneId", 0)
  }

  fun markSceneCompiled(sceneId: String, shaderCount: Int) {
    prefs.edit()
      .putBoolean("cache_saved_$sceneId", true)
      .putInt("shader_count_$sceneId", shaderCount)
      .putLong("compiled_at_$sceneId", System.currentTimeMillis())
      .apply()

    sceneSavedStatus[sceneId] = true
    refreshCacheStatus()
  }

  fun clearCache() {
    prefs.edit().clear().apply()
    GameScene.values().forEach { scene ->
      sceneSavedStatus[scene.id] = false
    }
    totalCachedShadersCount = 0
  }

  fun getAllSceneStatuses(): List<SceneShaderStatus> {
    return GameScene.values().map { scene ->
      val saved = prefs.getBoolean("cache_saved_${scene.id}", false)
      val count = if (saved) prefs.getInt("shader_count_${scene.id}", scene.totalShaders) else 0
      SceneShaderStatus(scene, saved, count)
    }
  }

  fun getCacheSizeFormatted(): String {
    val bytes = totalCachedShadersCount * 42000L // ~42 KB per compiled SPIR-V Vulkan pipeline
    return if (bytes < 1024 * 1024) {
      "${bytes / 1024} KB"
    } else {
      String.format("%.1f MB", bytes.toDouble() / (1024 * 1024))
    }
  }
}
