package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

class SoundSynthesizer(private val context: Context) {
  private val scope = CoroutineScope(Dispatchers.Default)

  private val vibrator: Vibrator? by lazy {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager =
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }

  fun vibrate(durationMs: Long = 40, amplitude: Int = 180) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(
          VibrationEffect.createOneShot(
            durationMs,
            amplitude.coerceIn(1, 255)
          )
        )
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(durationMs)
      }
    } catch (_: Exception) {}
  }

  fun playPistol() {
    vibrate(25, 140)
    scope.launch {
      playToneWithNoise(freqStart = 800f, freqEnd = 120f, durationMs = 120, noiseAmount = 0.5f)
    }
  }

  fun playShotgun() {
    vibrate(60, 250)
    scope.launch {
      playToneWithNoise(freqStart = 320f, freqEnd = 45f, durationMs = 280, noiseAmount = 0.85f)
    }
  }

  fun playRifle() {
    vibrate(30, 180)
    scope.launch {
      playToneWithNoise(freqStart = 650f, freqEnd = 110f, durationMs = 100, noiseAmount = 0.65f)
    }
  }

  fun playReload() {
    scope.launch {
      playTone(freq = 900f, durationMs = 50)
      kotlinx.coroutines.delay(120)
      playTone(freq = 1200f, durationMs = 60)
    }
  }

  fun playMelee() {
    vibrate(45, 200)
    scope.launch {
      playToneWithNoise(freqStart = 240f, freqEnd = 70f, durationMs = 180, noiseAmount = 0.7f)
    }
  }

  fun playZombieHurt() {
    scope.launch {
      playToneWithNoise(freqStart = 210f, freqEnd = 130f, durationMs = 220, noiseAmount = 0.6f)
    }
  }

  fun playZombieRoar() {
    scope.launch {
      playToneWithNoise(freqStart = 160f, freqEnd = 240f, durationMs = 380, noiseAmount = 0.75f)
    }
  }

  fun playFlashlight() {
    scope.launch {
      playTone(freq = 1800f, durationMs = 30)
    }
  }

  fun playAchievementChime() {
    vibrate(70, 220)
    scope.launch {
      // Iconic Xbox 360 achievement tone (high harmonious double chime)
      playTone(freq = 1046.50f, durationMs = 140) // C6
      kotlinx.coroutines.delay(40)
      playTone(freq = 1567.98f, durationMs = 280) // G6
    }
  }

  fun playXboxChime() {
    scope.launch {
      playTone(freq = 440f, durationMs = 90)
      playTone(freq = 660f, durationMs = 120)
      playTone(freq = 880f, durationMs = 220)
    }
  }

  private fun playTone(freq: Float, durationMs: Int) {
    try {
      val sampleRate = 22050
      val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
      val buffer = ShortArray(numSamples)
      for (i in 0 until numSamples) {
        val time = i.toDouble() / sampleRate
        val envelope = 1.0 - (i.toDouble() / numSamples)
        val value = sin(2.0 * Math.PI * freq * time) * envelope
        buffer[i] = (value * Short.MAX_VALUE * 0.7).toInt().toShort()
      }
      playShortBuffer(buffer, sampleRate)
    } catch (_: Exception) {}
  }

  private fun playToneWithNoise(
    freqStart: Float,
    freqEnd: Float,
    durationMs: Int,
    noiseAmount: Float
  ) {
    try {
      val sampleRate = 22050
      val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
      val buffer = ShortArray(numSamples)
      var currentFreq = freqStart
      val freqStep = (freqEnd - freqStart) / numSamples

      for (i in 0 until numSamples) {
        val time = i.toDouble() / sampleRate
        val envelope = (1.0 - (i.toDouble() / numSamples))
        val tone = sin(2.0 * Math.PI * currentFreq * time)
        val noise = (Random.nextFloat() * 2f - 1f) * noiseAmount
        val mixed = (tone * (1f - noiseAmount) + noise) * envelope
        buffer[i] = (mixed * Short.MAX_VALUE * 0.8f).toInt().coerceIn(
          Short.MIN_VALUE.toInt(),
          Short.MAX_VALUE.toInt()
        ).toShort()
        currentFreq += freqStep
      }
      playShortBuffer(buffer, sampleRate)
    } catch (_: Exception) {}
  }

  private fun playShortBuffer(buffer: ShortArray, sampleRate: Int) {
    try {
      val track =
        AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(buffer.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

      track.write(buffer, 0, buffer.size)
      track.play()
      track.setNotificationMarkerPosition(buffer.size)
      track.setPlaybackPositionUpdateListener(
        object : AudioTrack.OnPlaybackPositionUpdateListener {
          override fun onMarkerReached(t: AudioTrack?) {
            try {
              t?.release()
            } catch (_: Exception) {}
          }
          override fun onPeriodicNotification(t: AudioTrack?) {}
        }
      )
    } catch (_: Exception) {}
  }
}
