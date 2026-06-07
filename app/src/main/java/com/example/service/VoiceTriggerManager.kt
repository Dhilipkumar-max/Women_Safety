package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceTriggerManager(private val context: Context, private val onTriggerDetected: (String) -> Unit) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _voiceStatus = MutableStateFlow("Tap Start to activate Voice Detection Dashboard")
    val voiceStatus: StateFlow<String> = _voiceStatus.asStateFlow()

    private val triggerKeywords = listOf("help me", "emergency", "save me", "help")

    init {
        // Delayed/lazy initialization implemented inside startListening() to prevent unauthorized AppOps logs on startup
    }

    private fun initSpeechRecognizer() {
        if (speechRecognizer != null) return
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            _voiceStatus.value = "RECORD_AUDIO permission check failed."
            return
        }
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }

                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _voiceStatus.value = "Listening active... Speak now!"
                    }

                    override fun onBeginningOfSpeech() {
                        _voiceStatus.value = "Analyzing audio stream..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // RMS audio power level changes, can be used for visualizers
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _voiceStatus.value = "Processing command..."
                    }

                    override fun onError(error: Int) {
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission RECORD_AUDIO missing"
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No command detected. Retrying..."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Service occupied. Re-initializing..."
                            SpeechRecognizer.ERROR_SERVER -> "Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech active. Retrying..."
                            else -> "Status idle (Code $error)"
                        }
                        _voiceStatus.value = errorMsg
                        Log.w("VoiceTrigger", "Speech recognizer error: $errorMsg")
                        
                        // Automatically restart listener if it timed out or didn't find match, to maintain continuous safety listening
                        if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || 
                            error == SpeechRecognizer.ERROR_NO_MATCH || 
                            error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                            restartListeningSilently()
                        } else {
                            _isListening.value = false
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val speechText = matches[0]
                            _lastRecognizedText.value = speechText
                            _voiceStatus.value = "Recognized: \"$speechText\""
                            checkTriggers(speechText)
                        }
                        // Continuous loop pattern to keep listening for safety triggers!
                        restartListeningSilently()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val partialText = matches[0]
                            _lastRecognizedText.value = partialText
                            checkTriggers(partialText)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            } else {
                _voiceStatus.value = "Speech recognition is not available on this device configuration."
            }
        } catch (e: Exception) {
            _voiceStatus.value = "Error initializing listener: ${e.message}"
        }
    }

    private fun checkTriggers(text: String) {
        val normalized = text.lowercase(Locale.getDefault())
        for (keyword in triggerKeywords) {
            if (normalized.contains(keyword)) {
                onTriggerDetected(keyword)
                _voiceStatus.value = "🚨 VOOCAL TRIGGER DETECTED: \"$keyword\"!"
                break
            }
        }
    }

    fun startListening() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            _voiceStatus.value = "Waiting for RECORD_AUDIO permission..."
            Log.w("VoiceTrigger", "Cannot start listening: RECORD_AUDIO permission not granted")
            return
        }
        initSpeechRecognizer()
        if (speechRecognizer == null) {
            _voiceStatus.value = "API not ready. Retrying connection..."
            return
        }
        try {
            speechRecognizer?.startListening(recognizerIntent)
            _isListening.value = true
        } catch (e: Exception) {
            _voiceStatus.value = "Failed starting service: ${e.message}"
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            val wasListening = _isListening.value
            _isListening.value = false
            if (wasListening) {
                speechRecognizer?.cancel()
            }
            speechRecognizer?.destroy()
            speechRecognizer = null
            _voiceStatus.value = "Voice trigger system paused"
        } catch (e: Exception) {
            Log.e("VoiceTrigger", "Error stopping: ${e.message}")
        }
    }

    private fun restartListeningSilently() {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            _isListening.value = false
            return
        }
        if (!_isListening.value) return
        initSpeechRecognizer()
        val recognizer = speechRecognizer
        if (recognizer != null) {
            try {
                recognizer.cancel()
                recognizer.startListening(recognizerIntent)
            } catch (e: Exception) {
                Log.e("VoiceTrigger", "Error restarting listener silently: ${e.message}")
            }
        }
    }

    fun simulatePhrase(phrase: String) {
        _lastRecognizedText.value = phrase
        _voiceStatus.value = "Simulating test voice input: \"$phrase\""
        checkTriggers(phrase)
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("VoiceTrigger", "Error destroying speech recognizer: ${e.message}")
        }
    }
}
