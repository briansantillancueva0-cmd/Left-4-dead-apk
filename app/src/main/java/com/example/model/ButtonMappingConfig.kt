package com.example.model

enum class Xbox360Button(
  val id: String,
  val label: String,
  val shortName: String,
  val category: String
) {
  TRIGGER_LEFT("lt", "Gatillo Izquierdo (LT)", "LT", "Gatillos"),
  TRIGGER_RIGHT("rt", "Gatillo Derecho (RT)", "RT", "Gatillos"),
  BUMPER_LEFT("lb", "Botón Superior Izq. (LB)", "LB", "Gatillos"),
  BUMPER_RIGHT("rb", "Botón Superior Der. (RB)", "RB", "Gatillos"),
  BUTTON_Y("btn_y", "Botón Y (Amarillo)", "Y", "Frontales"),
  BUTTON_X("btn_x", "Botón X (Azul)", "X", "Frontales"),
  BUTTON_B("btn_b", "Botón B (Rojo)", "B", "Frontales"),
  BUTTON_A("btn_a", "Botón A (Verde)", "A", "Frontales"),
  STICK_LEFT_CLICK("ls_click", "Palanca Izq. Click (LS / L3)", "LS", "Palancas"),
  STICK_RIGHT_CLICK("rs_click", "Palanca Der. Click (RS / R3)", "RS", "Palancas"),
  DPAD_UP("dpad_up", "Cruceta Arriba (D-Pad ▲)", "▲", "Cruceta D-Pad"),
  DPAD_DOWN("dpad_down", "Cruceta Abajo (D-Pad ▼)", "▼", "Cruceta D-Pad"),
  DPAD_LEFT("dpad_left", "Cruceta Izquierda (D-Pad ◄)", "◄", "Cruceta D-Pad"),
  DPAD_RIGHT("dpad_right", "Cruceta Derecha (D-Pad ►)", "►", "Cruceta D-Pad"),
  BUTTON_BACK("btn_back", "Botón BACK", "BACK", "Sistema"),
  BUTTON_START("btn_start", "Botón START", "START", "Sistema"),
  BUTTON_GUIDE("btn_guide", "Joya Guía Xbox", "GUIDE", "Sistema");

  companion object {
    fun fromId(id: String): Xbox360Button? = values().find { it.id == id }
  }
}

enum class CommandCategory(val labelEs: String, val labelEn: String, val labelPt: String) {
  COMBAT("Combate y Acción", "Combat & Action", "Combate e Ação"),
  EQUIPMENT("Objetos y Curación", "Items & Healing", "Itens e Cura"),
  SYSTEM("Sistema y Emulador", "System & Emulator", "Sistema e Emulador")
}

enum class EmulatorCommand(
  val id: String,
  val nameEs: String,
  val nameEn: String,
  val namePt: String,
  val descEs: String,
  val descEn: String,
  val descPt: String,
  val category: CommandCategory,
  val iconGlyph: String
) {
  SHOOT(
    "cmd_shoot",
    "Disparar (Ataque Principal)",
    "Shoot (Primary Attack)",
    "Atirar (Ataque Principal)",
    "Dispara el arma actual de fuego o golpea",
    "Fires the currently equipped firearm or swings",
    "Dispara a arma de fogo equipada ou golpeia",
    CommandCategory.COMBAT,
    "💥"
  ),
  MELEE(
    "cmd_melee",
    "Golpe Cuerpo a Cuerpo (Empujón)",
    "Melee Shove",
    "Golpe Corpo a Corpo",
    "Empuja a los infectados para ganar distancia",
    "Shoves back infected to create space and stun",
    "Empurra os infectados para abrir espaço",
    CommandCategory.COMBAT,
    "👊"
  ),
  RELOAD(
    "cmd_reload",
    "Recargar Arma",
    "Reload Weapon",
    "Recarregar Arma",
    "Recarga el cargador de la munición",
    "Reloads weapon magazine with reserve ammo",
    "Recarrega o carregador da arma",
    CommandCategory.COMBAT,
    "🔄"
  ),
  JUMP(
    "cmd_jump",
    "Saltar",
    "Jump",
    "Pular",
    "Salta obstáculos y vallas de contención",
    "Jumps over debris, fences and gaps",
    "Pula detritos, cercas e obstáculos",
    CommandCategory.COMBAT,
    "🦘"
  ),
  CROUCH(
    "cmd_crouch",
    "Agacharse",
    "Crouch / Duck",
    "Agachar",
    "Mejora la precisión del arma y reduce silueta",
    "Improves weapon accuracy and lowers profile",
    "Melhora a precisão da arma e reduz silhueta",
    CommandCategory.COMBAT,
    "🧎"
  ),
  FLASHLIGHT(
    "cmd_flashlight",
    "Alternar Linterna",
    "Toggle Flashlight",
    "Alternar Lanterna",
    "Enciende o apaga la linterna frontal",
    "Turns the survivor shoulder flashlight on or off",
    "Liga ou desliga a lanterna de ombro",
    CommandCategory.COMBAT,
    "🔦"
  ),
  USE_REVIVE(
    "cmd_use",
    "Interactuar / Revivir Aliado",
    "Use / Revive Survivor",
    "Interagir / Reviver",
    "Abre puertas, rescata aliados caídos y activa palancas",
    "Opens safe room doors, revives fallen teammates",
    "Abre portas, revive aliados e ativa mecanismos",
    CommandCategory.COMBAT,
    "🤝"
  ),
  SWITCH_WEAPON(
    "cmd_switch_weapon",
    "Siguiente Arma / Cambiar",
    "Next Weapon / Cycle",
    "Próxima Arma / Trocar",
    "Alterna entre arma principal, secundaria y cuerpo a cuerpo",
    "Cycles between primary, secondary and pistols",
    "Alterna entre arma primária e secundária",
    CommandCategory.COMBAT,
    "🔁"
  ),
  PREV_WEAPON(
    "cmd_prev_weapon",
    "Arma Anterior",
    "Previous Weapon",
    "Arma Anterior",
    "Vuelve a la selección de arma anterior",
    "Selects the previous weapon slot",
    "Seleciona o slot de arma anterior",
    CommandCategory.COMBAT,
    "⏮️"
  ),
  MEDKIT(
    "cmd_medkit",
    "Usar Botiquín de Primeros Auxilios",
    "Use First Aid Kit",
    "Usar Kit de Primeiros Socorros",
    "Restaura hasta un 80% de salud permanente",
    "Restores up to 80% permanent survivor health",
    "Restaura até 80% de vida permanente",
    CommandCategory.EQUIPMENT,
    "🩹"
  ),
  PILLS(
    "cmd_pills",
    "Píldoras de Dolor / Adrenalina",
    "Pain Pills / Adrenaline",
    "Pílulas de Dor / Adrenalina",
    "Otorga 50 puntos de salud temporal de absorción",
    "Grants temporary absorption buffer health",
    "Concede vida temporária para fuga rápida",
    CommandCategory.EQUIPMENT,
    "💊"
  ),
  GRENADE(
    "cmd_grenade",
    "Lanzar Bomba Casera / Molotov",
    "Throw Pipe Bomb / Molotov",
    "Lançar Bomba Caseira / Molotov",
    "Atrae la horda de infectados con un pitido o incendia",
    "Emits beeping to distract horde or covers area in fire",
    "Atrai horda com bip ou cobre área com fogo",
    CommandCategory.EQUIPMENT,
    "💣"
  ),
  PAUSE(
    "cmd_pause",
    "Pausa / Menú de Campaña",
    "Pause / Campaign Menu",
    "Pausar / Menu de Campanha",
    "Pausa el juego y muestra opciones del capítulo",
    "Pauses game and shows chapter details",
    "Pausa o jogo e mostra opções da campanha",
    CommandCategory.SYSTEM,
    "⏸️"
  ),
  XBOX_GUIDE(
    "cmd_guide",
    "Guía Xbox 360",
    "Xbox 360 Guide Overlay",
    "Guia Xbox 360",
    "Abre el menú flotante oficial de la consola Xbox 360",
    "Opens the signature sliding Xbox 360 blade Guide",
    "Abre o painel clássico da lâmina Xbox 360",
    CommandCategory.SYSTEM,
    "🎮"
  ),
  QUICK_SAVE(
    "cmd_quick_save",
    "Guardado Rápido (Save State)",
    "Quick Save State",
    "Salvamento Rápido (Save State)",
    "Crea un punto de guardado instantáneo en la memoria",
    "Captures an instant memory snapshot",
    "Captura um ponto imediato na memória",
    CommandCategory.SYSTEM,
    "💾"
  ),
  QUICK_LOAD(
    "cmd_quick_load",
    "Carga Rápida (Load State)",
    "Quick Load State",
    "Carregamento Rápido (Load State)",
    "Restaura el último punto de guardado instantáneo",
    "Restores the latest captured state snapshot",
    "Restaura o último estado salvo",
    CommandCategory.SYSTEM,
    "📂"
  ),
  TOGGLE_FPS(
    "cmd_toggle_fps",
    "Mostrar / Ocultar FPS & Estadísticas",
    "Toggle FPS & Performance Stats",
    "Alternar FPS e Estatísticas",
    "Muestra tasa de refresco, GPU, CPU y memoria en tiempo real",
    "Toggles real-time framerate and GPU diagnostic HUD",
    "Mostra taxa de quadros e consumo da GPU",
    CommandCategory.SYSTEM,
    "📊"
  ),
  TURBO(
    "cmd_turbo",
    "Modo Turbo (Acelerar 2x)",
    "Turbo Speed (Fast Forward 2x)",
    "Modo Turbo (Acelerar 2x)",
    "Acelera las cinemáticas y tiempos de carga",
    "Fast forwards cutscenes and loading transitions",
    "Acelera cutscenes e telas de carregamento",
    CommandCategory.SYSTEM,
    "⚡"
  ),
  SCREENSHOT(
    "cmd_screenshot",
    "Captura de Pantalla HD",
    "Take Screenshot HD",
    "Captura de Tela HD",
    "Guarda la imagen actual en la galería de fotos",
    "Saves current frame to Android media gallery",
    "Salva o frame atual na galeria de fotos",
    CommandCategory.SYSTEM,
    "📸"
  ),
  UNASSIGNED(
    "cmd_unassigned",
    "Sin Asignar (Desactivado)",
    "Unassigned (Disabled)",
    "Não Atribuído (Desativado)",
    "El botón no ejecutará ninguna acción en el emulador",
    "Button will perform no action in emulator",
    "O botão não executará nenhuma ação",
    CommandCategory.SYSTEM,
    "🚫"
  );

  fun localizedName(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SPANISH -> nameEs
      AppLanguage.ENGLISH -> nameEn
      AppLanguage.PORTUGUESE -> namePt
    }

  fun localizedDesc(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SPANISH -> descEs
      AppLanguage.ENGLISH -> descEn
      AppLanguage.PORTUGUESE -> descPt
    }
}

data class ButtonMappingProfile(
  val id: String,
  val nameEs: String,
  val nameEn: String,
  val namePt: String,
  val descriptionEs: String,
  val descriptionEn: String,
  val descriptionPt: String,
  val mappings: Map<Xbox360Button, EmulatorCommand>
) {
  fun localizedName(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SPANISH -> nameEs
      AppLanguage.ENGLISH -> nameEn
      AppLanguage.PORTUGUESE -> namePt
    }

  fun localizedDesc(lang: AppLanguage): String =
    when (lang) {
      AppLanguage.SPANISH -> descriptionEs
      AppLanguage.ENGLISH -> descriptionEn
      AppLanguage.PORTUGUESE -> descriptionPt
    }

  companion object {
    val DefaultLeft4Dead =
      ButtonMappingProfile(
        id = "preset_default",
        nameEs = "Oficial Left 4 Dead 360",
        nameEn = "Official Left 4 Dead 360",
        namePt = "Oficial Left 4 Dead 360",
        descriptionEs = "Configuración original por defecto de Left 4 Dead en Xbox 360.",
        descriptionEn = "Original default Left 4 Dead button layout on Xbox 360.",
        descriptionPt = "Configuração clássica de fábrica do Left 4 Dead no Xbox 360.",
        mappings =
          mapOf(
            Xbox360Button.TRIGGER_RIGHT to EmulatorCommand.SHOOT,
            Xbox360Button.TRIGGER_LEFT to EmulatorCommand.MELEE,
            Xbox360Button.BUMPER_LEFT to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.BUMPER_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_A to EmulatorCommand.JUMP,
            Xbox360Button.BUTTON_B to EmulatorCommand.CROUCH,
            Xbox360Button.BUTTON_X to EmulatorCommand.RELOAD,
            Xbox360Button.BUTTON_Y to EmulatorCommand.SWITCH_WEAPON,
            Xbox360Button.STICK_LEFT_CLICK to EmulatorCommand.USE_REVIVE,
            Xbox360Button.STICK_RIGHT_CLICK to EmulatorCommand.MELEE,
            Xbox360Button.DPAD_UP to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.DPAD_DOWN to EmulatorCommand.MEDKIT,
            Xbox360Button.DPAD_LEFT to EmulatorCommand.PILLS,
            Xbox360Button.DPAD_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_BACK to EmulatorCommand.TOGGLE_FPS,
            Xbox360Button.BUTTON_START to EmulatorCommand.PAUSE,
            Xbox360Button.BUTTON_GUIDE to EmulatorCommand.XBOX_GUIDE
          )
      )

    val TacticalProfile =
      ButtonMappingProfile(
        id = "preset_tactical",
        nameEs = "Táctico (Modern Shooter)",
        nameEn = "Tactical (Modern Shooter)",
        namePt = "Tático (Tiro Moderno)",
        descriptionEs = "Agacharse en la palanca RS y golpe cuerpo a cuerpo en el botón B.",
        descriptionEn = "Crouch on RS click and melee shove mapped to B button.",
        descriptionPt = "Agachar no clique do RS e golpe corpo a corpo no botão B.",
        mappings =
          mapOf(
            Xbox360Button.TRIGGER_RIGHT to EmulatorCommand.SHOOT,
            Xbox360Button.TRIGGER_LEFT to EmulatorCommand.MELEE,
            Xbox360Button.BUMPER_LEFT to EmulatorCommand.USE_REVIVE,
            Xbox360Button.BUMPER_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_A to EmulatorCommand.JUMP,
            Xbox360Button.BUTTON_B to EmulatorCommand.MELEE,
            Xbox360Button.BUTTON_X to EmulatorCommand.RELOAD,
            Xbox360Button.BUTTON_Y to EmulatorCommand.SWITCH_WEAPON,
            Xbox360Button.STICK_LEFT_CLICK to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.STICK_RIGHT_CLICK to EmulatorCommand.CROUCH,
            Xbox360Button.DPAD_UP to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.DPAD_DOWN to EmulatorCommand.MEDKIT,
            Xbox360Button.DPAD_LEFT to EmulatorCommand.PILLS,
            Xbox360Button.DPAD_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_BACK to EmulatorCommand.TOGGLE_FPS,
            Xbox360Button.BUTTON_START to EmulatorCommand.PAUSE,
            Xbox360Button.BUTTON_GUIDE to EmulatorCommand.XBOX_GUIDE
          )
      )

    val BumperJumperProfile =
      ButtonMappingProfile(
        id = "preset_bumper_jumper",
        nameEs = "Bumper Jumper (Salto en LB)",
        nameEn = "Bumper Jumper (Air Mobility)",
        namePt = "Bumper Jumper (Salto no LB)",
        descriptionEs = "Salto asignado a LB para disparar y saltar simultáneamente sin soltar la palanca.",
        descriptionEn = "Jump on LB allows aiming and jumping simultaneously without lifting thumb.",
        descriptionPt = "Salto no LB para mirar e pular simultaneamente sem tirar o polegar.",
        mappings =
          mapOf(
            Xbox360Button.TRIGGER_RIGHT to EmulatorCommand.SHOOT,
            Xbox360Button.TRIGGER_LEFT to EmulatorCommand.MELEE,
            Xbox360Button.BUMPER_LEFT to EmulatorCommand.JUMP,
            Xbox360Button.BUMPER_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_A to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.BUTTON_B to EmulatorCommand.CROUCH,
            Xbox360Button.BUTTON_X to EmulatorCommand.RELOAD,
            Xbox360Button.BUTTON_Y to EmulatorCommand.SWITCH_WEAPON,
            Xbox360Button.STICK_LEFT_CLICK to EmulatorCommand.USE_REVIVE,
            Xbox360Button.STICK_RIGHT_CLICK to EmulatorCommand.MELEE,
            Xbox360Button.DPAD_UP to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.DPAD_DOWN to EmulatorCommand.MEDKIT,
            Xbox360Button.DPAD_LEFT to EmulatorCommand.PILLS,
            Xbox360Button.DPAD_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_BACK to EmulatorCommand.TOGGLE_FPS,
            Xbox360Button.BUTTON_START to EmulatorCommand.PAUSE,
            Xbox360Button.BUTTON_GUIDE to EmulatorCommand.XBOX_GUIDE
          )
      )

    val SouthpawProfile =
      ButtonMappingProfile(
        id = "preset_southpaw",
        nameEs = "Zurdo (Southpaw)",
        nameEn = "Southpaw (Lefty)",
        namePt = "Canhoto (Southpaw)",
        descriptionEs = "Gatillos invertidos: LT dispara y RT realiza golpe cuerpo a cuerpo.",
        descriptionEn = "Inverted triggers: LT shoots and RT executes melee shove.",
        descriptionPt = "Gatilhos invertidos: LT atira e RT executa o golpe corpo a corpo.",
        mappings =
          mapOf(
            Xbox360Button.TRIGGER_LEFT to EmulatorCommand.SHOOT,
            Xbox360Button.TRIGGER_RIGHT to EmulatorCommand.MELEE,
            Xbox360Button.BUMPER_LEFT to EmulatorCommand.GRENADE,
            Xbox360Button.BUMPER_RIGHT to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.BUTTON_A to EmulatorCommand.JUMP,
            Xbox360Button.BUTTON_B to EmulatorCommand.CROUCH,
            Xbox360Button.BUTTON_X to EmulatorCommand.RELOAD,
            Xbox360Button.BUTTON_Y to EmulatorCommand.SWITCH_WEAPON,
            Xbox360Button.STICK_LEFT_CLICK to EmulatorCommand.USE_REVIVE,
            Xbox360Button.STICK_RIGHT_CLICK to EmulatorCommand.MELEE,
            Xbox360Button.DPAD_UP to EmulatorCommand.FLASHLIGHT,
            Xbox360Button.DPAD_DOWN to EmulatorCommand.MEDKIT,
            Xbox360Button.DPAD_LEFT to EmulatorCommand.PILLS,
            Xbox360Button.DPAD_RIGHT to EmulatorCommand.GRENADE,
            Xbox360Button.BUTTON_BACK to EmulatorCommand.TOGGLE_FPS,
            Xbox360Button.BUTTON_START to EmulatorCommand.PAUSE,
            Xbox360Button.BUTTON_GUIDE to EmulatorCommand.XBOX_GUIDE
          )
      )

    val AllPresets = listOf(DefaultLeft4Dead, TacticalProfile, BumperJumperProfile, SouthpawProfile)
  }
}
