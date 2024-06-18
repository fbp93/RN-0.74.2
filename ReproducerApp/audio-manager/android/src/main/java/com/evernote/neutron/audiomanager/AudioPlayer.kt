package com.evernote.neutron.audiomanager

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactContext

class AudioPlayer(val context: ReactContext) : MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {
    var onPlayerDidStart: (() -> Unit)? = null
    var onPlayerDidRestart: (() -> Unit)? = null
    var onPlayerDidPause: (() -> Unit)? = null
    var onPlayerDidResume: (() -> Unit)? = null
    var onPlayerDidStop: (() -> Unit)? = null
    var onPlayerDidFinish: (() -> Unit)? = null
    var onPlayerDidErrorOccur: (() -> Unit)? = null
    var isPaused = false
        private set

    private var mediaPlayer: MediaPlayer? = null

    fun play(resourceLocalPath: String) {
        // Request Focus
        if (requestAudioPlayingFocus() != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        // Stop current
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.release()
                mediaPlayer = null
                onPlayerDidRestart?.invoke()
            }
        }

        mediaPlayer = MediaPlayer()
        mediaPlayer?.let {
            it.setDataSource(resourceLocalPath)
            it.setOnPreparedListener(this)
            it.setOnCompletionListener(this)
            it.setOnErrorListener(this)
            it.prepareAsync()
        }
    }

    fun togglePlayPause() {
        if (isPaused) {
            resume()
        } else {
            pause()
        }
    }

    fun pause() {
        isPaused = true
        mediaPlayer?.let {
            it.pause()
            onPlayerDidPause?.invoke()
        }
    }

    fun resume() {
        // Request Focus
        if (requestAudioPlayingFocus() != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return
        }

        isPaused = false
        mediaPlayer?.let {
            it.start()
            onPlayerDidResume?.invoke()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            it.stop()
            it.release()
            mediaPlayer = null
            isPaused = false
            onPlayerDidStop?.invoke()
        }
    }

    fun seekToNormalizedPlaybackPoint(normalizedPlaybackPoint: Double) {
        mediaPlayer?.let {
            val duration = it.duration
            val playbackPointMillis = (duration * normalizedPlaybackPoint).toInt()
            it.seekTo(playbackPointMillis)
        }
    }

    fun getPlaybackInfo(callback: Callback) {
        if (mediaPlayer == null) {
            callback.invoke("Player is not available.")
            return
        }

        mediaPlayer?.let {
            val playbackPoint = it.currentPosition.toDouble() / it.duration.toDouble()
            val playbackTimeInterval = it.currentPosition.toDouble() / 1000.0
            callback.invoke(null, playbackPoint, playbackTimeInterval)
        }
    }

    fun getPlaybackDuration(callback: Callback) {
        if (mediaPlayer == null) {
            callback.invoke("Player is not available.")
            return
        }

        mediaPlayer?.let {
            val duration = it.duration.toDouble() / 1000.0
            callback.invoke(null, duration)
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer?.let {
            it.start()
            isPaused = false
            onPlayerDidStart?.invoke()
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        isPaused = false
        onPlayerDidFinish?.invoke()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        onPlayerDidErrorOccur?.invoke()
        return true
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Pause playback
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
                // TODO: Actually low volume
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
                // TODO: Actually rise volume
                if (isPaused) {
                    resume()
                }
            }
        }
    }

    private fun requestAudioPlayingFocus(): Int {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        )
    }
}