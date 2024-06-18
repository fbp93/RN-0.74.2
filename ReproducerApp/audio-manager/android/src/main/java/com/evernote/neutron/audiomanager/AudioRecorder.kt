package com.evernote.neutron.audiomanager

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.sqrt

class AudioRecorder: AudioRecorderInterface, MediaRecorder.OnErrorListener,
        MediaRecorder.OnInfoListener,
        AudioManager.OnAudioFocusChangeListener {
    private val TAG = "ENAudioRecorder"
    private val amplitudeHistorySize = 200 // Arbitrary

    /*
        This normalization is done by https://en.wikipedia.org/wiki/Standard_score , which
        is a measure of how above a sample is above the average. We choose this
        normalization because is independent of the unknown boundaries of the quantity
        measured by the microphone, in other words, we do not know the min or max possible
        values measured in this platform, so we compare the sample against the average of
        a collection of samples.
     */
    override val normalizedAmplitude: Float
        get() {
            val amplitudeDB = lastAmplitude
            val averageDB = amplitudeHistory.average().toFloat()
            val standardDeviationDB = amplitudeHistoryStandardDeviation()

            val standardScore = (amplitudeDB - averageDB) / standardDeviationDB
            return min(standardScore, 1F)
        }

    private val amplitudeHistory by lazy {
        FloatArray(amplitudeHistorySize)
    }

    private var amplitudeSampleTimer: Timer? = null

    private var amplitudeSampleCount = 0
    private var lastAmplitude = 0F
    private var mediaRecorder: MediaRecorder? = null
    private var mediaRecordingFile: File? = null
    private var isRecording = false
    private var recordingIdentifier: String? = null
    private var onErrorOccur: ((info: WritableMap?) -> Unit)? = null
    private var onInfo: ((what: Int, info: WritableMap?) -> Unit)? = null

    override fun record(
            identifier: String,
            reactContext: ReactApplicationContext,
            onDidStart: (info: WritableMap?) -> Unit,
            onDidErrorOccur: (info: WritableMap?) -> Unit,
            onInfo: (what: Int, info: WritableMap?) -> Unit
    ) {
        if (requestAudioPlayingFocus(reactContext) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        this.onErrorOccur = onDidErrorOccur
        this.onInfo = onInfo
        mediaRecorder?.let {
            if (isRecording) {
                it.release()
                mediaRecorder = null
                isRecording = false
                // TODO: Notify UI about restart recording
            }
        }

        val permission = reactContext.checkSelfPermission(
                android.Manifest.permission.RECORD_AUDIO
        )

        if (permission == PackageManager.PERMISSION_GRANTED) {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.let {
                try {
                    it.setOnErrorListener(this)
                    it.setOnInfoListener(this)
                    it.setAudioSource(MediaRecorder.AudioSource.MIC)
                    it.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
                    it.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    it.setAudioEncodingBitRate(128000)
                    it.setAudioSamplingRate(44100)

                    mediaRecordingFile = newRecordingFile("aac", reactContext)
                    it.setOutputFile(mediaRecordingFile!!.absolutePath)
                    it.setMaxFileSize(0)
                    it.prepare()
                    it.start()

                    this.startAmplitudeSampling()
                    isRecording = true
                    recordingIdentifier = identifier
                    val info = Arguments.createMap()
                    info.putString("identifier", recordingIdentifier)
                    onDidStart(info)

                } catch (e: Exception) {
                    Log.e(TAG,"Failed to record" + e.localizedMessage)
                    isRecording = false
                    val errorInfo = Arguments.createMap()
                    errorInfo.putString("identifier", recordingIdentifier)
                    errorInfo.putInt("what", 0)
                    errorInfo.putInt("extra", 0)
                    onErrorOccur?.invoke(errorInfo) // @TODO Define error constants
                }
            }
        } else {
            // @TODO Define error constants, audio-manager will not handle permission request.
            isRecording = false
            val errorInfo = Arguments.createMap()
            errorInfo.putString("identifier", recordingIdentifier)
            errorInfo.putInt("what", 0)
            errorInfo.putInt("extra", 0)
            onErrorOccur?.invoke(errorInfo) // @TODO Define error constants
        }
    }

    override fun stopRecording(
            reactContext: ReactApplicationContext,
            onDidStop: (info: WritableMap?) -> Unit
    ) {
        if (!isRecording) {
            return
        }

        stopAmplitudeSampling()

        mediaRecorder?.let {
            it.stop()
            it.release()
            isRecording = false

            val info = Arguments.createMap()
            val outputFilePath = mediaRecordingFile?.absolutePath
            outputFilePath?.run {
                val index = this.lastIndexOf("/")
                val filename = this.substring(index + 1, this.length)

                info.putString("identifier", recordingIdentifier)
                info.putString("path", this)
                info.putString("mime", "audio/aac")
                info.putString("filename", filename)
                mediaRecordingFile = null
            }
            onDidStop(info)
        }
    }

    override fun cancelRecording() {
        if (!isRecording) {
            return
        }

        stopAmplitudeSampling()

        mediaRecorder?.let {
            it.stop()
            it.release()
            isRecording = false
            val outputFilePath = mediaRecordingFile?.absolutePath
            val file = File(outputFilePath)
            file.delete()
        }
    }

    override fun getSessionID(): Int? {
        return mediaRecorder?.activeRecordingConfiguration?.clientAudioSessionId
    }

    override fun onError(mr: MediaRecorder?, what: Int, extra: Int) {
        stopAmplitudeSampling()
        mediaRecordingFile = null
        isRecording = false
        val info = Arguments.createMap()
        info.putString("identifier", recordingIdentifier)
        info.putInt("what", what)
        info.putInt("extra", extra)
        onErrorOccur?.invoke(info)
    }

    override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
        val info = Arguments.createMap()
        info.putString("identifier", recordingIdentifier)
        info.putInt("what", what)
        info.putInt("extra", extra)
        onInfo?.invoke(what, info)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // TODO: Keep recording?
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // TODO: Keep recording?

            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
                // TODO: Actually low volume

            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
                // TODO: Actually rise volume
            }
        }
    }

    private fun newRecordingFile(
            extension: String,
            reactContext: ReactApplicationContext
    ): File {
        val voiceRecordsCachePath = getVoiceCachePath(reactContext)
        val voiceRecordsCacheDir = File(voiceRecordsCachePath)
        if (!voiceRecordsCacheDir.exists()) {
            voiceRecordsCacheDir.mkdir()
        }
        var fullPath = voiceRecordsCacheDir.absolutePath
        fullPath += File.separator
        fullPath += SimpleDateFormat("MM.dd.yyyy_kkmma").format(Date())
        fullPath += ".$extension"
        return File(fullPath)
    }

    private fun getVoiceCachePath(reactContext: ReactApplicationContext): String {
        val cachePath = reactContext.cacheDir.absolutePath
        return cachePath + File.separator +
                "voiceRecords" + File.separator
    }

    private fun startAmplitudeSampling() {
        stopAmplitudeSampling()
        amplitudeSampleTimer = Timer()
        amplitudeSampleTimer?.let {
            it.scheduleAtFixedRate(timerTask {
                sampleAmplitude()
            }, 0, 1)
        }
    }

    private fun stopAmplitudeSampling() {
        amplitudeSampleTimer?.let {
            it.cancel()
            amplitudeSampleTimer = null
        }
    }

    private fun sampleAmplitude() {
        val amplitude = mediaRecorder?.maxAmplitude
        amplitude?.let {
            if (amplitude != 0) {
                val index = amplitudeSampleCount % amplitudeHistorySize
                amplitudeHistory[index] = dBFromAmplitude(it)
                lastAmplitude = dBFromAmplitude(it)
                amplitudeSampleCount += 1
            }
        }
    }

    private fun dBFromAmplitude(amplitude: Int): Float {
        return 20 * log10(amplitude.toDouble()).toFloat()
    }

    private fun amplitudeHistoryStandardDeviation(): Float {
        val average = amplitudeHistory.average().toFloat()
        val sum = amplitudeHistory.reduce { accumulator: Float, current: Float ->
            accumulator + (current - average) * (current - average)
        }
        val squareDiffAverage = sum / amplitudeHistory.count()
        return sqrt(squareDiffAverage)
    }

    private fun requestAudioPlayingFocus(context: ReactApplicationContext): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        )
    }
}