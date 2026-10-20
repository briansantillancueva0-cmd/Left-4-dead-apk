package com.example.model

enum class ResolutionConfig(val label: String, val subtitle: String, val scale: Float) {
  P360("360p", "+FPS", 0.5f),
  P540("540p", "leve", 0.75f),
  P720("720p", "padrão", 1.0f),
  P1080("1080p", "nítido", 1.5f)
}

enum class AspectRatioConfig(val label: String) {
  RATIO_16_9("16:9"),
  STRETCH("Esticar")
}

enum class AntiAliasingConfig(val label: String) {
  OFF("Off"),
  FXAA("FXAA"),
  FXAA_PLUS("FXAA+")
}

enum class ScreenScaleConfig(val label: String) {
  NORMAL("Normal"),
  FSR("FSR"),
  CAS("CAS")
}

data class GpuDriverItem(
  val id: String,
  val displayName: String,
  val description: String,
  val version: String,
  val author: String,
  val vulkanVersion: String,
  val isCustom: Boolean = false,
  val isSystem: Boolean = false
) {
  companion object {
    val SystemDefault =
      GpuDriverItem(
        id = "sys_default",
        displayName = "Sistema (padrão do celular)",
        description = "Driver proprietário instalado pelo fabricante do dispositivo",
        version = "Padrão SO",
        author = "Fabricante / Qualcomm",
        vulkanVersion = "Vulkan 1.1 / 1.3",
        isSystem = true
      )

    val DefaultYuzuTurnipDrivers =
      listOf(
        SystemDefault,
        GpuDriverItem(
          id = "turnip_24_3_0",
          displayName = "Mesa Turnip v24.3.0 - Rev 18",
          description = "Driver open-source Mesa para Adreno 6xx / 7xx. Maior compatibilidade e estabilidade",
          version = "v24.3.0 R18",
          author = "Kimchi / Mesa3D",
          vulkanVersion = "Vulkan 1.3.284"
        ),
        GpuDriverItem(
          id = "turnip_24_2_0",
          displayName = "Mesa Turnip v24.2.0 - A7xx Fixes",
          description = "Otimizado para Snapdragon 8 Gen 1, 8+ Gen 1 e 8 Gen 2 (Adreno 730/740)",
          version = "v24.2.0",
          author = "Kimchi / Mesa3D",
          vulkanVersion = "Vulkan 1.3.279"
        ),
        GpuDriverItem(
          id = "adreno_762",
          displayName = "Qualcomm Adreno Proprietary v762.0",
          description = "Driver oficial Qualcomm compilado para Snapdragon 8 Gen 2 e Gen 3",
          version = "v762.0",
          author = "Qualcomm",
          vulkanVersion = "Vulkan 1.3.276"
        ),
        GpuDriverItem(
          id = "adreno_744",
          displayName = "Qualcomm Adreno Proprietary v744.19",
          description = "Estabilidade máxima para Snapdragon 865, 870 e 888 (Adreno 650/660)",
          version = "v744.19",
          author = "Qualcomm",
          vulkanVersion = "Vulkan 1.3.268"
        ),
        GpuDriverItem(
          id = "adreno_676",
          displayName = "Qualcomm Adreno v676.53 Legacy",
          description = "Recomendado para dispositivos intermediários com GPU Adreno 618 / 620 / 642L",
          version = "v676.53",
          author = "Qualcomm",
          vulkanVersion = "Vulkan 1.1.128"
        )
      )
  }
}

// Deprecated enum alias for backwards compatibility
enum class GpuDriverConfig(val displayName: String, val description: String) {
  SYSTEM("Sistema (padrão do celular)", "Driver Vulkan/OpenGL padrão do SO"),
  TURNIP("Mesa Turnip v24.3 (Adreno Vulkan)", "Driver open-source otimizado para Snapdragon"),
  ADRENO_PROP("Adreno Proprietary v744.19", "Driver oficial Qualcomm Adreno"),
  VULKAN_DIRECT("Vulkan 1.3 Direct Pipeline", "Renderização direta de baixo overhead")
}

data class RomFile(
  val id: String,
  val title: String,
  val path: String,
  val fileSize: String,
  val format: String, // "ISO", "XBLA", "XEX"
  val isBuiltIn: Boolean = false,
  val region: String = "USA/NTSC",
  val description: String = ""
)

data class TouchSettings(
  val opacity: Float = 0.75f,
  val buttonScale: Float = 1.0f,
  val sensitivity: Float = 1.0f,
  val hapticsEnabled: Boolean = true,
  val showDpad: Boolean = true,
  val showRightStick: Boolean = true,
  val invertY: Boolean = false
)

data class XboxAchievement(
  val id: String,
  val title: String,
  val description: String,
  val gamerscore: Int,
  val icon: String = "🏆",
  var isUnlocked: Boolean = false
)
