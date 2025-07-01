package kz.market.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RawRes
import java.util.concurrent.atomic.AtomicBoolean

object ScannerFeedbackManager {

    private lateinit var soundPool: SoundPool
    private var beepSoundId: Int = 0
    private val isSoundLoaded = AtomicBoolean(false)

    /**
     * Инициализация звукового сигнала.
     * Вызывать один раз, желательно в Application или при старте активити.
     */
    fun init(context: Context, @RawRes beepResId: Int) {
        if (::soundPool.isInitialized) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
            )
            .build()

        beepSoundId = soundPool.load(context, beepResId, 1)
        soundPool.setOnLoadCompleteListener { _, _, success ->
            isSoundLoaded.set(success == 0)
        }
    }

    /**
     * Воспроизведение звукового сигнала.
     * Работает только после init().
     */
    fun playBeep(volume: Float = 1f) {
        if (::soundPool.isInitialized && isSoundLoaded.get()) {
            soundPool.play(beepSoundId, volume, volume, 1, 0, 1f)
        }
    }

    /**
     * Вибрация устройства.
     */
    fun vibrate(context: Context, duration: Long = 100L) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    /**
     * Очистка ресурсов SoundPool.
     * Вызывать при завершении работы с камерой/активити.
     */
    fun release() {
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
    }
}