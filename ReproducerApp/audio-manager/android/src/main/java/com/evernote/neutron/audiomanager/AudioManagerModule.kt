package com.evernote.neutron.audiomanager

import android.content.pm.PackageManager.FEATURE_TELEPHONY
import android.media.AudioManager
import android.media.AudioRecordingConfiguration
import android.media.MediaRecorder
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

enum class AudioManagerPlayerEvent(val value: String) {
    DidStart("DidStart"),
    DidRestart("DidRestart"),
    DidPause("DidPause"),
    DidResume("DidResume"),
    DidStop("DidStop"),
    DidFinish("DidFinish"),
    DidErrorOccur("DidErrorOccur"),
}

enum class AudioManagerRecorderEvent(val value: String) {
    DidStart("DidStartRecording"),
    DidStop("DidStopRecording"),
    DidErrorOccur("DidErrorOccurRecording")
}

class AudioManagerModule(private val reactContext: ReactApplicationContext) :
        ReactContextBaseJavaModule(reactContext), Localized {
    private class PhoneCallStateListener(
            val onCallStart: () -> Unit
    ) : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                onCallStart.invoke()
            }
        }
    }

    private class AudioRecordingCallbackImpl(
            val onRecordingConfigChange: () -> Unit
    ) : AudioManager.AudioRecordingCallback() {
        override fun onRecordingConfigChanged(configs: MutableList<AudioRecordingConfiguration>?) {
            super.onRecordingConfigChanged(configs)
            configs?.forEach {
                if (it.clientAudioSessionId == AudioRecorderFacade.getSessionID() && it.isClientSilenced) {
                    onRecordingConfigChange.invoke()
                }
            }
        }
    }

    private val TAG = "ENAudioManager"

    private val audioRecordingCallbackResponder by lazy { AudioRecordingCallbackImpl { this.stopRecording() } }
    private val phoneStateListener by lazy { PhoneCallStateListener { this.stopRecording() }}

    private val audioPlayer by lazy {
        AudioPlayer(reactContext)
    }

    override fun getName(): String {
        return "AudioManager"
    }

    @ReactMethod
    override fun setLocalizedTexts(texts: ReadableMap) {
        AudioRecorderFacade.setLocalizedTexts(texts)
    }

    @ReactMethod
    fun play(resourceLocalPath: String) {
        // Stop current
        audioPlayer.onPlayerDidStart = { this.onPlayerDidStart() }
        audioPlayer.onPlayerDidRestart = { this.onPlayerDidRestart() }
        audioPlayer.onPlayerDidPause = { this.onPlayerDidPause() }
        audioPlayer.onPlayerDidResume = { this.onPlayerDidResume() }
        audioPlayer.onPlayerDidStop = { this.onPlayerDidStop() }
        audioPlayer.onPlayerDidFinish = { this.onPlayerDidFinish() }
        audioPlayer.onPlayerDidErrorOccur = { this.onPlayerDidErrorOccur() }
        audioPlayer.play(resourceLocalPath)
    }

    @ReactMethod
    fun togglePlayPause() {
        audioPlayer.togglePlayPause()
    }

    @ReactMethod
    fun stop() {
        audioPlayer.stop()
    }

    @ReactMethod
    fun seekToNormalizedPlaybackPoint(normalizedPlaybackPoint: Double) {
        audioPlayer.seekToNormalizedPlaybackPoint(normalizedPlaybackPoint)
    }

    @ReactMethod
    fun getPlaybackInfo(callback: Callback) {
        audioPlayer.getPlaybackInfo(callback)
    }

    @ReactMethod
    fun getPlaybackDuration(callback: Callback) {
        audioPlayer.getPlaybackDuration(callback)
    }

    @ReactMethod
    fun releasePlayer() {
        audioPlayer.release()
    }

    @ReactMethod
    fun record(identifier: String) {
        AudioRecorderFacade.record(
                identifier,
                reactContext,
                { info: WritableMap? -> onRecorderDidStart(info) },
                { info: WritableMap? -> onRecorderDidErrorOccur(info) },
                { what: Int, info: WritableMap? -> onRecorderInfo(what, info) }
        )
    }

    @ReactMethod
    fun stopRecording() {
        stopListeningForRecordingInterruptions()
        AudioRecorderFacade.stopRecording(
                reactContext
        ) { info -> onRecorderDidStop(info) }
    }

    @ReactMethod
    fun cancelRecording() {
        stopListeningForRecordingInterruptions()
        AudioRecorderFacade.cancelRecording()
    }

    private fun onPlayerDidStart() {
        sendEvent(AudioManagerPlayerEvent.DidStart.value)
    }

    private fun onPlayerDidRestart() {
        sendEvent(AudioManagerPlayerEvent.DidRestart.value)
    }

    private fun onPlayerDidPause() {
        sendEvent(AudioManagerPlayerEvent.DidPause.value)
    }

    private fun onPlayerDidResume() {
        sendEvent(AudioManagerPlayerEvent.DidResume.value)
    }

    private fun onPlayerDidStop() {
        sendEvent(AudioManagerPlayerEvent.DidStop.value)
    }

    private fun onPlayerDidFinish() {
        sendEvent(AudioManagerPlayerEvent.DidFinish.value)
    }

    private fun onPlayerDidErrorOccur() {
        sendEvent(AudioManagerPlayerEvent.DidErrorOccur.value)
    }

    private fun onRecorderDidStart(info: WritableMap?) {
        startListeningForRecordingInterruptions()
        sendEvent(AudioManagerRecorderEvent.DidStart.value, info)
    }

    private fun onRecorderDidStop(info: WritableMap?) {
        sendEvent(AudioManagerRecorderEvent.DidStop.value, info)
    }

    private fun onRecorderDidErrorOccur(info: WritableMap?) {
        stopListeningForRecordingInterruptions()
        sendEvent(AudioManagerRecorderEvent.DidErrorOccur.value, info)
    }

    private fun onRecorderInfo(what: Int, info: WritableMap?) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            // TODO: send UI event
        }
    }

    private fun sendEvent(
            eventName: String,
            params: WritableMap? = null
    ) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
    }

    private fun startListeningForRecordingInterruptions() {
        Log.i(TAG, "Starting listening for recording interruptions.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val audioManagerService = reactContext.getSystemService(AudioManager::class.java)
            audioManagerService.registerAudioRecordingCallback(audioRecordingCallbackResponder, null)
        } else if (reactContext.packageManager.hasSystemFeature(FEATURE_TELEPHONY)) {
            val telephonyManager = reactContext.getSystemService(TelephonyManager::class.java) as TelephonyManager
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    private fun stopListeningForRecordingInterruptions() {
        Log.i(TAG, "Stopping listening for recording interruptions.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val audioManagerService = reactContext.getSystemService(AudioManager::class.java)
            audioManagerService.unregisterAudioRecordingCallback(audioRecordingCallbackResponder)
        } else if (reactContext.packageManager.hasSystemFeature(FEATURE_TELEPHONY)) {
            val telephonyManager = reactContext.getSystemService(TelephonyManager::class.java) as TelephonyManager
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }
}

