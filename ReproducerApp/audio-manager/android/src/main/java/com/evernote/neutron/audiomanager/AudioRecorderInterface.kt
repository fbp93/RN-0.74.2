package com.evernote.neutron.audiomanager

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap

interface AudioRecorderInterface {

    val normalizedAmplitude: Float

    fun record(
            identifier: String,
            reactContext: ReactApplicationContext,
            onDidStart: (info: WritableMap?) -> Unit,
            onDidErrorOccur: (info: WritableMap?) -> Unit,
            onInfo: (what: Int, info: WritableMap?) -> Unit
    )

    fun stopRecording(
            reactContext: ReactApplicationContext,
            onDidStop: (info: WritableMap?) -> Unit
    )

    fun cancelRecording()

    fun getSessionID(): Int?
}