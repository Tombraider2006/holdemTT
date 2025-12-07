package com.holdem.poker.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Менеджер звуков для игры
 */
class SoundManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)
    
    // Настройки звука
    var soundsEnabled: Boolean
        get() = prefs.getBoolean("sounds_enabled", true)
        set(value) = prefs.edit().putBoolean("sounds_enabled", value).apply()
    
    var cardSoundsEnabled: Boolean
        get() = prefs.getBoolean("card_sounds_enabled", true)
        set(value) = prefs.edit().putBoolean("card_sounds_enabled", value).apply()
    
    var chipSoundsEnabled: Boolean
        get() = prefs.getBoolean("chip_sounds_enabled", true)
        set(value) = prefs.edit().putBoolean("chip_sounds_enabled", value).apply()
    
    var winLoseSoundsEnabled: Boolean
        get() = prefs.getBoolean("win_lose_sounds_enabled", true)
        set(value) = prefs.edit().putBoolean("win_lose_sounds_enabled", value).apply()
    
    private var currentMediaPlayer: MediaPlayer? = null
    
    /**
     * Воспроизводит звук раздачи карт
     */
    fun playCardDealSound() {
        if (!soundsEnabled || !cardSoundsEnabled) return
        playSystemSound(android.media.ToneGenerator.TONE_PROP_BEEP)
    }
    
    /**
     * Воспроизводит звук фишек при ставке
     */
    fun playChipSound() {
        if (!soundsEnabled || !chipSoundsEnabled) return
        playSystemSound(android.media.ToneGenerator.TONE_PROP_BEEP2)
    }
    
    /**
     * Воспроизводит звук победы
     */
    fun playWinSound() {
        if (!soundsEnabled || !winLoseSoundsEnabled) return
        playSystemSound(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
    }
    
    /**
     * Воспроизводит звук поражения
     */
    fun playLoseSound() {
        if (!soundsEnabled || !winLoseSoundsEnabled) return
        playSystemSound(android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD)
    }
    
    /**
     * Воспроизводит системный звук через ToneGenerator
     */
    private fun playSystemSound(toneType: Int) {
        try {
            val toneGenerator = android.media.ToneGenerator(
                android.media.AudioManager.STREAM_MUSIC,
                100
            )
            toneGenerator.startTone(toneType, 200)
            // Освобождаем ресурсы после небольшой задержки
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                toneGenerator.release()
            }, 300)
        } catch (e: Exception) {
            // Игнорируем ошибки воспроизведения звука
        }
    }
    
    /**
     * Освобождает ресурсы
     */
    fun release() {
        currentMediaPlayer?.release()
        currentMediaPlayer = null
    }
}

/**
 * Получает SoundManager из контекста
 */
@Composable
fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current
    return remember { SoundManager(context) }
}

