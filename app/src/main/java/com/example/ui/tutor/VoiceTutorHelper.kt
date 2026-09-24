package com.example.ui.tutor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

class VoiceTutorHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            _errorMessage.value = "TTS Initialization error: ${e.localizedMessage}"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                tts?.setSpeechRate(1.0f)
                tts?.setPitch(1.05f) // Friendly enthusiastic pitch for learning tutor
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = VoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _voiceState.value = VoiceState.IDLE
                    }

                    override fun onError(utteranceId: String?) {
                        _voiceState.value = VoiceState.IDLE
                    }
                })
            }
        }
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isTtsReady || tts == null) {
            _errorMessage.value = "Voice synthesizer is warming up..."
            return
        }

        // Clean out markdown characters for natural speech
        val spokenScript = text
            .replace(Regex("#+\\s*"), "")
            .replace(Regex("\\*\\*|\\*"), "")
            .replace(Regex("`{1,3}[^`]*`{1,3}"), "code block")
            .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
            .trim()

        _voiceState.value = VoiceState.SPEAKING
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "AvoraTutorUtterance")
        tts?.speak(spokenScript, TextToSpeech.QUEUE_FLUSH, params, "AvoraTutorUtterance")
    }

    fun stopSpeaking() {
        tts?.stop()
        _voiceState.value = VoiceState.IDLE
    }

    fun startListening(onResult: (String) -> Unit) {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                stopSpeaking()
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Avora anything...")
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceState.value = VoiceState.LISTENING
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _voiceState.value = VoiceState.PROCESSING
                    }

                    override fun onError(error: Int) {
                        _voiceState.value = VoiceState.IDLE
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error for voice recognition"
                            SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that, please try again"
                            else -> "Voice input error ($error)"
                        }
                        _errorMessage.value = msg
                    }

                    override fun onResults(results: Bundle?) {
                        _voiceState.value = VoiceState.IDLE
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        if (text.isNotBlank()) {
                            _spokenText.value = text
                            onResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        _spokenText.value = matches?.firstOrNull() ?: ""
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer?.startListening(intent)
            } else {
                _errorMessage.value = "Speech recognition is not available on this device"
            }
        } catch (e: Exception) {
            _voiceState.value = VoiceState.IDLE
            _errorMessage.value = e.localizedMessage
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _voiceState.value = VoiceState.IDLE
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
