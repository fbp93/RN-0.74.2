package com.evernote.neutron.audiomanager

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import java.util.*
import kotlin.concurrent.timerTask

class AudioVisualizerAnimatedView(private val reactContext: ThemedReactContext) : View(reactContext) {
    private val fps = 30

    private var timer: Timer? = null
    private var frameCount: Int = 0
    private var normalizedAmplitude: Float = 0F

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

        val rect = Rect()
        getLocalVisibleRect(rect)
        canvas?.apply {
            val drawer = AudioVisualizerDrawer(
                    canvas,
                    fps,
                    frameCount,
                    AudioRecorderFacade.normalizedAmplitude
            )
            drawer.draw()
        }
    }

    fun startUpdates() {
        stopUpdates()
        timer = Timer()
        timer?.let {
            it.scheduleAtFixedRate(timerTask {
                updateFrame()
            }, 0, fps.toLong())
        }
    }

    fun stopUpdates() {
        timer?.let { it.cancel() }
    }

    private fun updateFrame() {
        frameCount += 1
        reactContext.currentActivity?.runOnUiThread {
            this.invalidate()
        }
    }
}

class AudioVisualizerManager(reactContext: ReactApplicationContext) : SimpleViewManager<AudioVisualizerAnimatedView>() {
    private val startUpdatesID = 0
    private val stopUpdatesID = 1

    private var audioVisualizerAnimatedView: AudioVisualizerAnimatedView? = null

    override fun createViewInstance(reactContext: ThemedReactContext): AudioVisualizerAnimatedView {
        audioVisualizerAnimatedView = AudioVisualizerAnimatedView(reactContext)
        return audioVisualizerAnimatedView!!
    }

    override fun getName(): String {
        return "AudioVisualizer"
    }

    override fun receiveCommand(root: AudioVisualizerAnimatedView, commandId: Int, args: ReadableArray?) {
        super.receiveCommand(root, commandId, args)

        when (commandId) {
            startUpdatesID -> audioVisualizerAnimatedView?.startUpdates()
            stopUpdatesID -> audioVisualizerAnimatedView?.stopUpdates()
        }
    }

    override fun getCommandsMap(): MutableMap<String, Int> {
        return MapBuilder.of(
                "startUpdates",
                startUpdatesID,
                "stopUpdates",
                stopUpdatesID)
    }
}