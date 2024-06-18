package com.evernote.neutron.audiomanager

import android.os.Build
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap

object AudioRecorderFacade: AudioRecorderInterface, Localized {

    private val platformAudioRecorder: AudioRecorderInterface by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) AudioRecorderClient() else AudioRecorder()
    }

    override val normalizedAmplitude: Float
        get() {
            return platformAudioRecorder.normalizedAmplitude
        }

    override fun setLocalizedTexts(texts: ReadableMap) {
        if (platformAudioRecorder is Localized) {
            (platformAudioRecorder as Localized).setLocalizedTexts(texts)
        }
    }

    override fun record(
        identifier: String,
        reactContext: ReactApplicationContext,
        onDidStart: (info: WritableMap?) -> Unit,
        onDidErrorOccur: (info: WritableMap?) -> Unit,
        onInfo: (what: Int, info: WritableMap?) -> Unit
    ) {
        platformAudioRecorder.record(identifier, reactContext, onDidStart, onDidErrorOccur, onInfo)
    }

    override fun stopRecording(
            reactContext: ReactApplicationContext,
            onDidStop: (info: WritableMap?) -> Unit
    ) {
        platformAudioRecorder.stopRecording(reactContext,onDidStop)
    }

    override fun cancelRecording() {
        platformAudioRecorder.cancelRecording()
    }

    override fun getSessionID(): Int? {
        return platformAudioRecorder.getSessionID()
    }
}
