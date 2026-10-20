package com.example.controller

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

class GamepadInputManager(private val context: Context) : InputManager.InputDeviceListener {

  var isGamepadConnected by mutableStateOf(false)
  var connectedGamepadName by mutableStateOf("Ningún control conectado")

  // Real-time button states
  var moveStickX by mutableStateOf(0f)
  var moveStickY by mutableStateOf(0f)
  var lookStickX by mutableStateOf(0f)
  var lookStickY by mutableStateOf(0f)

  // Triggers & actions callbacks
  var onFireDown: (() -> Unit)? = null
  var onFireUp: (() -> Unit)? = null
  var onMelee: (() -> Unit)? = null
  var onReload: (() -> Unit)? = null
  var onSwitchWeapon: (() -> Unit)? = null
  var onFlashlight: (() -> Unit)? = null
  var onJump: (() -> Unit)? = null
  var onCrouch: (() -> Unit)? = null
  var onGuide: (() -> Unit)? = null

  private val inputManager: InputManager? by lazy {
    context.getSystemService(Context.INPUT_SERVICE) as? InputManager
  }

  init {
    inputManager?.registerInputDeviceListener(this, null)
    checkConnectedGamepads()
  }

  fun checkConnectedGamepads() {
    val deviceIds = InputDevice.getDeviceIds()
    var foundGamepad = false
    var name = "Ningún control conectado"

    for (id in deviceIds) {
      val device = InputDevice.getDevice(id) ?: continue
      val sources = device.sources
      val isGamepad = (sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
        (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK

      if (isGamepad && !device.isVirtual) {
        foundGamepad = true
        name = device.name
        break
      }
    }

    isGamepadConnected = foundGamepad
    connectedGamepadName = if (foundGamepad) name else "Ningún control conectado"
  }

  override fun onInputDeviceAdded(deviceId: Int) {
    checkConnectedGamepads()
  }

  override fun onInputDeviceRemoved(deviceId: Int) {
    checkConnectedGamepads()
  }

  override fun onInputDeviceChanged(deviceId: Int) {
    checkConnectedGamepads()
  }

  // Handle Hardware Buttons (A, B, X, Y, Triggers, Bumpers, D-Pad, Start, Guide)
  fun handleKeyEvent(event: KeyEvent): Boolean {
    val isDown = event.action == KeyEvent.ACTION_DOWN

    // Check if this event comes from a gamepad
    val isFromGamepad = (event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
      (event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
      KeyEvent.isGamepadButton(event.keyCode)

    if (!isFromGamepad && event.keyCode !in listOf(
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_L1,
        KeyEvent.KEYCODE_BUTTON_R1,
        KeyEvent.KEYCODE_BUTTON_L2,
        KeyEvent.KEYCODE_BUTTON_R2,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_MODE
      )
    ) {
      return false
    }

    when (event.keyCode) {
      KeyEvent.KEYCODE_BUTTON_R2 -> {
        if (isDown) onFireDown?.invoke() else onFireUp?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_L2 -> {
        if (isDown) onMelee?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_R1 -> {
        if (isDown) onSwitchWeapon?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_L1 -> {
        if (isDown) onFlashlight?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_X -> {
        if (isDown) onReload?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_Y -> {
        if (isDown) onSwitchWeapon?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_A -> {
        if (isDown) onJump?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_B -> {
        if (isDown) onCrouch?.invoke()
        return true
      }
      KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_MODE, KeyEvent.KEYCODE_BUTTON_SELECT -> {
        if (isDown) onGuide?.invoke()
        return true
      }
      KeyEvent.KEYCODE_DPAD_UP -> {
        if (isDown) moveStickY = -1.0f else moveStickY = 0f
        return true
      }
      KeyEvent.KEYCODE_DPAD_DOWN -> {
        if (isDown) moveStickY = 1.0f else moveStickY = 0f
        return true
      }
      KeyEvent.KEYCODE_DPAD_LEFT -> {
        if (isDown) moveStickX = -1.0f else moveStickX = 0f
        return true
      }
      KeyEvent.KEYCODE_DPAD_RIGHT -> {
        if (isDown) moveStickX = 1.0f else moveStickX = 0f
        return true
      }
    }
    return false
  }

  // Handle Analog Joysticks & Triggers (Left stick, Right stick, L2/R2 analog pressure)
  fun handleMotionEvent(event: MotionEvent): Boolean {
    if ((event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
      // Left Analog Stick (Movement)
      val lx = getCenteredAxis(event, MotionEvent.AXIS_X)
      val ly = getCenteredAxis(event, MotionEvent.AXIS_Y)
      moveStickX = lx
      moveStickY = ly

      // Right Analog Stick (Aim / Look)
      var rx = getCenteredAxis(event, MotionEvent.AXIS_Z)
      var ry = getCenteredAxis(event, MotionEvent.AXIS_RZ)
      if (rx == 0f && ry == 0f) {
        rx = getCenteredAxis(event, MotionEvent.AXIS_RX)
        ry = getCenteredAxis(event, MotionEvent.AXIS_RY)
      }
      lookStickX = rx
      lookStickY = ry

      // Analog Triggers (LT & RT)
      val rt = event.getAxisValue(MotionEvent.AXIS_RTRIGGER)
      val gas = event.getAxisValue(MotionEvent.AXIS_GAS)
      if (rt > 0.4f || gas > 0.4f) {
        onFireDown?.invoke()
      }

      val lt = event.getAxisValue(MotionEvent.AXIS_LTRIGGER)
      val brake = event.getAxisValue(MotionEvent.AXIS_BRAKE)
      if (lt > 0.4f || brake > 0.4f) {
        onMelee?.invoke()
      }

      // D-Pad Hat Axis
      val hatX = event.getAxisValue(MotionEvent.AXIS_HAT_X)
      val hatY = event.getAxisValue(MotionEvent.AXIS_HAT_Y)
      if (hatX != 0f) moveStickX = hatX
      if (hatY != 0f) moveStickY = hatY

      return true
    }
    return false
  }

  private fun getCenteredAxis(event: MotionEvent, axis: Int, historyPos: Int = -1): Float {
    val value = if (historyPos < 0) event.getAxisValue(axis) else event.getHistoricalAxisValue(axis, historyPos)
    val deadZone = 0.18f
    return if (abs(value) > deadZone) value else 0.0f
  }

  fun cleanup() {
    inputManager?.unregisterInputDeviceListener(this)
  }
}
