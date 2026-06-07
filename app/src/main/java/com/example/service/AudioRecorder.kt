package com.example.service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    var currentFile: File? = null

    fun startRecording(): File? {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e("AudioRecorder", "Cannot start recording: RECORD_AUDIO permission not granted")
            return null
        }
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Evidence_LiveAmbient_$timeStamp.mp3"
            val file = File(context.cacheDir, fileName)
            currentFile = file

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            Log.d("AudioRecorder", "Recording started: ${file.absolutePath}")
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording: ${e.message}")
            return null
        }
    }

    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            Log.d("AudioRecorder", "Recording stopped: ${currentFile?.absolutePath}")
            return currentFile
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to stop recording: ${e.message}")
            mediaRecorder = null
            return null
        }
    }
}
