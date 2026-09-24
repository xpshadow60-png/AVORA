package com.example.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.StudyRepository
import com.example.data.local.StudySessionEntity
import com.example.focus.audio.AmbientPlaybackState
import com.example.focus.audio.AmbientSoundService
import com.example.focus.audio.AmbientSoundType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FocusTimerState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val currentSubject: String = "General Study",
    val focusScore: Int = 100,
    val activeAmbientSound: String = "RAIN", // Default to soothing Rain
    val ambientVolume: Float = 0.7f,
    val isAmbientSoundPlaying: Boolean = false
)

class FocusModeManager(
    private val context: Context,
    private val repository: StudyRepository? = null
) {

    private val _timerState = MutableStateFlow(FocusTimerState())
    val timerState: StateFlow<FocusTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Observe AmbientSoundService playback state to keep FocusTimerState in sync
        scope.launch {
            AmbientSoundService.servicePlaybackState.collect { playback ->
                _timerState.value = _timerState.value.copy(
                    isAmbientSoundPlaying = playback.isPlaying,
                    ambientVolume = playback.volume
                )
            }
        }
    }

    fun startSession(durationMinutes: Int, subject: String) {
        timerJob?.cancel()
        val totalSecs = durationMinutes * 60
        val soundType = AmbientSoundType.fromId(_timerState.value.activeAmbientSound)
        
        _timerState.value = _timerState.value.copy(
            isActive = true,
            isPaused = false,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            currentSubject = subject,
            focusScore = 100
        )

        // Automatically start selected ambient soundscape if not OFF
        if (soundType != AmbientSoundType.OFF) {
            AmbientSoundService.playSound(context, soundType, _timerState.value.ambientVolume)
        }

        runTimer()
    }

    fun pauseSession() {
        if (_timerState.value.isActive && !_timerState.value.isPaused) {
            timerJob?.cancel()
            _timerState.value = _timerState.value.copy(isPaused = true)
            AmbientSoundService.pauseSound(context)
        }
    }

    fun resumeSession() {
        if (_timerState.value.isActive && _timerState.value.isPaused) {
            _timerState.value = _timerState.value.copy(isPaused = false)
            val soundType = AmbientSoundType.fromId(_timerState.value.activeAmbientSound)
            if (soundType != AmbientSoundType.OFF) {
                AmbientSoundService.resumeSound(context)
            }
            runTimer()
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isActive = false,
            isPaused = false
        )
        AmbientSoundService.stopSound(context)
    }

    private fun runTimer() {
        timerJob = scope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isActive && !_timerState.value.isPaused) {
                delay(1000L)
                val currentSecs = _timerState.value.remainingSeconds - 1
                if (currentSecs <= 0) {
                    val subject = _timerState.value.currentSubject
                    val durationMins = _timerState.value.totalSeconds / 60
                    
                    AmbientSoundService.stopSound(context)
                    playCompletionTone()
                    showCompletionNotification(subject)

                    // Log completed session to database
                    repository?.let { repo ->
                        launch(Dispatchers.IO) {
                            try {
                                repo.logStudySession(
                                    StudySessionEntity(
                                        subject = subject,
                                        durationMinutes = durationMins,
                                        focusScore = _timerState.value.focusScore,
                                        distractionsBlockedCount = 0,
                                        completedAt = System.currentTimeMillis()
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("FocusModeManager", "Failed to save completed study session to database", e)
                            }
                        }
                    }

                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = 0,
                        isActive = false,
                        isPaused = false
                    )
                } else {
                    _timerState.value = _timerState.value.copy(remainingSeconds = currentSecs)
                }
            }
        }
    }

    fun setAmbientSound(soundTypeId: String) {
        val soundType = AmbientSoundType.fromId(soundTypeId)
        _timerState.value = _timerState.value.copy(activeAmbientSound = soundType.id)

        if (_timerState.value.isActive && !_timerState.value.isPaused) {
            if (soundType == AmbientSoundType.OFF) {
                AmbientSoundService.stopSound(context)
            } else {
                AmbientSoundService.playSound(context, soundType, _timerState.value.ambientVolume)
            }
        } else if (!_timerState.value.isActive && soundType == AmbientSoundType.OFF) {
            AmbientSoundService.stopSound(context)
        }
    }

    fun toggleAmbientSoundscape(soundTypeId: String) {
        val soundType = AmbientSoundType.fromId(soundTypeId)
        if (_timerState.value.activeAmbientSound.equals(soundType.id, ignoreCase = true) && _timerState.value.isAmbientSoundPlaying) {
            setAmbientSound(AmbientSoundType.OFF.id)
        } else {
            setAmbientSound(soundType.id)
            if (!_timerState.value.isActive) {
                // Allow previewing ambient sound even before timer starts
                AmbientSoundService.playSound(context, soundType, _timerState.value.ambientVolume)
            }
        }
    }

    fun setAmbientVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _timerState.value = _timerState.value.copy(ambientVolume = clamped)
        AmbientSoundService.setVolume(context, clamped)
    }

    private fun playCompletionTone() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 80)
            toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            scope.launch(Dispatchers.Default) {
                delay(600L)
                try {
                    toneGen.release()
                } catch (e: Exception) {
                    Log.w("FocusModeManager", "Failed to release ToneGenerator", e)
                }
            }
        } catch (e: Exception) {
            Log.w("FocusModeManager", "Failed to play completion tone", e)
        }
    }

    private fun showCompletionNotification(subject: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channelId = "study_timer_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Study Session Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts triggered when a focused study session completes"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🎉 Study Session Complete!")
                .setContentText("Great job staying focused on $subject! Take a well-deserved break now.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Great job staying focused on $subject! Take a well-deserved break now. Your focus stats have been saved to Analytics.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .build()

            notificationManager.notify(1001, notification)
        } catch (e: Exception) {
            Log.e("FocusModeManager", "Failed to show completion notification", e)
        }
    }
}

