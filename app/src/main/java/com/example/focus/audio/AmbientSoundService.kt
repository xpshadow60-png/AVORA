package com.example.focus.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AmbientSoundService : Service() {

    companion object {
        const val ACTION_PLAY = "com.example.focus.audio.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.focus.audio.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.focus.audio.ACTION_RESUME"
        const val ACTION_STOP = "com.example.focus.audio.ACTION_STOP"
        const val ACTION_SET_VOLUME = "com.example.focus.audio.ACTION_SET_VOLUME"
        const val ACTION_TOGGLE = "com.example.focus.audio.ACTION_TOGGLE"

        const val EXTRA_SOUND_ID = "extra_sound_id"
        const val EXTRA_VOLUME = "extra_volume"

        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "ambient_sound_channel"

        private val _servicePlaybackState = MutableStateFlow(AmbientPlaybackState())
        val servicePlaybackState: StateFlow<AmbientPlaybackState> = _servicePlaybackState.asStateFlow()

        fun playSound(context: Context, soundType: AmbientSoundType, volume: Float = 0.7f) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_SOUND_ID, soundType.id)
                putExtra(EXTRA_VOLUME, volume)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    context.startForegroundService(intent)
                } catch (_: Exception) {
                    context.startService(intent)
                }
            } else {
                context.startService(intent)
            }
        }

        fun pauseSound(context: Context) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeSound(context: Context) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stopSound(context: Context) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setVolume(context: Context, volume: Float) {
            val intent = Intent(context, AmbientSoundService::class.java).apply {
                action = ACTION_SET_VOLUME
                putExtra(EXTRA_VOLUME, volume)
            }
            context.startService(intent)
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): AmbientSoundService = this@AmbientSoundService
    }

    private val binder = LocalBinder()
    private val audioEngine = AmbientAudioEngine()
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val soundId = intent.getStringExtra(EXTRA_SOUND_ID) ?: AmbientSoundType.RAIN.id
                val volume = intent.getFloatExtra(EXTRA_VOLUME, _servicePlaybackState.value.volume)
                val soundType = AmbientSoundType.fromId(soundId)
                handlePlay(soundType, volume)
            }
            ACTION_PAUSE -> {
                handlePause()
            }
            ACTION_RESUME -> {
                handleResume()
            }
            ACTION_STOP -> {
                handleStop()
            }
            ACTION_SET_VOLUME -> {
                val volume = intent.getFloatExtra(EXTRA_VOLUME, 0.7f)
                handleSetVolume(volume)
            }
            ACTION_TOGGLE -> {
                if (_servicePlaybackState.value.isPlaying) {
                    handlePause()
                } else if (_servicePlaybackState.value.soundType != AmbientSoundType.OFF) {
                    handleResume()
                } else {
                    handlePlay(AmbientSoundType.RAIN, _servicePlaybackState.value.volume)
                }
            }
        }
        return START_STICKY
    }

    private fun handlePlay(soundType: AmbientSoundType, volume: Float) {
        if (soundType == AmbientSoundType.OFF) {
            handleStop()
            return
        }

        audioEngine.startSound(soundType, volume)
        _servicePlaybackState.value = AmbientPlaybackState(
            soundType = soundType,
            isPlaying = true,
            volume = volume
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(soundType, isPlaying = true),
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, buildNotification(soundType, isPlaying = true))
            }
        } catch (e: Exception) {
            android.util.Log.w("AmbientSoundService", "startForeground failed safely: ${e.message}")
        }
    }

    private fun handlePause() {
        audioEngine.pause()
        _servicePlaybackState.value = _servicePlaybackState.value.copy(isPlaying = false)
        updateNotification(buildNotification(_servicePlaybackState.value.soundType, isPlaying = false))
    }

    private fun handleResume() {
        if (_servicePlaybackState.value.soundType != AmbientSoundType.OFF) {
            audioEngine.resume()
            _servicePlaybackState.value = _servicePlaybackState.value.copy(isPlaying = true)
            updateNotification(buildNotification(_servicePlaybackState.value.soundType, isPlaying = true))
        }
    }

    private fun handleStop() {
        audioEngine.stop()
        _servicePlaybackState.value = AmbientPlaybackState(
            soundType = AmbientSoundType.OFF,
            isPlaying = false,
            volume = _servicePlaybackState.value.volume
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleSetVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        audioEngine.setVolume(clamped)
        _servicePlaybackState.value = _servicePlaybackState.value.copy(volume = clamped)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ambient Focus Soundscapes",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controls for background ambient soundscapes during study focus sessions"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(soundType: AmbientSoundType, isPlaying: Boolean): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingContentIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AmbientSoundService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_RESUME
        }
        val pendingToggleIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AmbientSoundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStopIntent = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("🎧 Focus Sound: ${soundType.displayName}")
            .setContentText(if (isPlaying) "Playing background ambiance for concentration" else "Ambiance paused")
            .setContentIntent(pendingContentIntent)
            .setOngoing(isPlaying)
            .addAction(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Resume",
                pendingToggleIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                pendingStopIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(notification: Notification) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
    }
}
