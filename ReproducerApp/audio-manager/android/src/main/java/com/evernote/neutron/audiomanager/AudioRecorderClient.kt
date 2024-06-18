package com.evernote.neutron.audiomanager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap

class AudioRecorderClient: AudioRecorderInterface, Localized, ServiceConnection {
    private class RecordParams(
            val identifier: String,
            val reactContext: ReactApplicationContext,
            val onDidStart: (info: WritableMap?) -> Unit,
            val onDidErrorOccur: (info: WritableMap?) -> Unit,
            val onInfo: (what: Int, info: WritableMap?) -> Unit
    )

    private val logTag = "AudioRecorderClient"
    private var recordParams: RecordParams? = null
    private var audioRecorderService: AudioRecorderService? = null
    private var localizedRecorderNotificationTitle: String? = null
    private var localizedRecorderNotificationText: String? = null

    override val normalizedAmplitude: Float
        get() = audioRecorderService?.normalizedAmplitude ?: 0f

    override fun setLocalizedTexts(texts: ReadableMap) {
        localizedRecorderNotificationTitle = texts.getString("recorderNotificationTitle")
        localizedRecorderNotificationText = texts.getString("recorderNotificationText")
    }

    override fun record(
            identifier: String,
            reactContext: ReactApplicationContext,
            onDidStart: (info: WritableMap?) -> Unit,
            onDidErrorOccur: (info: WritableMap?) -> Unit,
            onInfo: (what: Int, info: WritableMap?) -> Unit
    ) {
        if (recordParams == null && audioRecorderService == null) {
            //Store parameters for record call after binding
            recordParams = RecordParams(identifier, reactContext, onDidStart, onDidErrorOccur, onInfo)

            //Start and bind service
            startAudioRecorderService(reactContext, identifier)
        } else {
            Log.e(logTag, "Cannot start record while service is already recording")
        }
    }

    private fun startAudioRecorderService(reactContext: ReactApplicationContext, identifier: String) {
        val serviceIntent = Intent(reactContext, AudioRecorderService::class.java)
        serviceIntent.putExtra("title", localizedRecorderNotificationTitle)
        serviceIntent.putExtra("text", localizedRecorderNotificationText)
        serviceIntent.putExtra("identifier", identifier)
        ContextCompat.startForegroundService(reactContext, serviceIntent)
        reactContext.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE)
    }

    override fun stopRecording(
            reactContext: ReactApplicationContext,
            onDidStop: (info: WritableMap?) -> Unit
    ) {
        if (audioRecorderService != null) {
            // Stop service
            stopAudioRecorderService(reactContext, onDidStop)
        } else {
            Log.e(logTag, "Cannot stop record while service is already stopping")
        }
    }

    private fun stopAudioRecorderService(
            reactContext: ReactApplicationContext,
            onDidStop: (info: WritableMap?) -> Unit
    ) {
        if (audioRecorderService != null) {
            val serviceIntent = Intent(reactContext, AudioRecorderService::class.java)
            audioRecorderService!!.stopRecording(reactContext, onDidStop)
            reactContext.unbindService(this)
            reactContext.stopService(serviceIntent)
            recordParams = null
            audioRecorderService = null
        } else {
            Log.e(logTag, "Cannot stop service without reference, or not started")
        }
    }

    override fun cancelRecording() {
        if (audioRecorderService != null) {
            audioRecorderService!!.cancelRecording()
            recordParams?.let {
                val serviceIntent = Intent(it.reactContext, AudioRecorderService::class.java)
                it.reactContext.unbindService(this)
                it.reactContext.stopService(serviceIntent)
            }
            recordParams = null
            audioRecorderService = null
        } else {
            Log.e(logTag, "Cannot cancel service without reference, or not started")
        }
    }

    override fun getSessionID(): Int? {
        return audioRecorderService?.getSessionID()
    }

    override fun onServiceDisconnected(componentName: ComponentName?) {
        // This should never been called since is called when the process
        // providing the service gets terminated. Since the service and
        // client for AudioRecorder lives in the same process, this
        // should never happen.
        if (audioRecorderService != null) {
            Log.e(logTag, "Audio recording service was disconnected")
            audioRecorderService = null
            recordParams = null
        }
    }

    override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
        val audioRecorderServiceBinder = binder as? AudioRecorderService.AudioRecorderServiceBinder
        audioRecorderService = audioRecorderServiceBinder?.service

        recordParams?.let {
            audioRecorderService?.record(
                    it.identifier,
                    it.reactContext,
                    it.onDidStart,
                    it.onDidErrorOccur,
                    it.onInfo
            )
        }
    }
}