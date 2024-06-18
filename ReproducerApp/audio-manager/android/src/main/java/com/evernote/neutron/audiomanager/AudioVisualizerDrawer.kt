package com.evernote.neutron.audiomanager

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.*

class AudioVisualizerDrawer constructor(
        canvas: Canvas,
        fps: Int,
        frameCount: Int,
        normalizedAmplitude: Float
) {

    private val canvas = canvas
    private val fps = fps
    private val frameCount = frameCount
    private val normalizedAmplitude = normalizedAmplitude

    private val minAmplitude = 0.1F

    // Following ratios are based on constants arbitrarily defined for iOS
    // we use this ratios to create draw waves independently of screen's density
    // and resolution.
    private val strokeSpaceRatio = 2F / 6F
    private val barAspectRatio = 2F / 34F
    private val barMaximumHeightRatio = 34F / 40F
    private val barMinimumHeightRatio = 3F / 40F
    private val barSpeedRatio = 40F / 375F
    private val waveSpeedRatio = 80F / 375F
    private val waveLengthRatio = 120F / 375F

    private val barMaxHeight: Float
        get() {
            return canvas.height * barMaximumHeightRatio
        }

    private val barWidth: Float
        get() {
            return barMaxHeight * barAspectRatio
        }

    private val spaceWidth: Float
        get() {
            return barWidth / strokeSpaceRatio
        }

    private val waveSpeed: Float
        get() {
            return canvas.width * waveSpeedRatio
        }

    private val barSpeed: Float
        get() {
            return canvas.width * barSpeedRatio
        }

    private val waveLength: Float
        get() {
            return canvas.width * waveLengthRatio
        }

    private val paint: Paint
        get() {
            val aPaint = Paint()
            aPaint.color = Color.parseColor("#ED8682")
            aPaint.style = Paint.Style.STROKE
            aPaint.strokeWidth = barWidth
            return aPaint
        }

    fun draw() {
        val centerY = canvas.height / 2
        val width = canvas.width

        val numberOfBars = numberOfBarsFor(width)
        for (index in 0 until numberOfBars) {
            val x = xFor(index)
            val startY = centerY - waveFormFor(x) / 2
            val endY = centerY + waveFormFor(x) / 2
            canvas.drawLine(x, startY, x, endY, paint)
        }
    }

    private fun numberOfBarsFor(width: Int): Int {
        if ((barWidth + spaceWidth).toInt() > 0) {
            return width / (barWidth + spaceWidth).toInt()
        }
        return 0
    }

    private fun xFor(index: Int): Float {
        if (canvas.width > 0) {
            val pixelDisplacementPerFrame = (barSpeed / fps)
            val framesPerLoop = ((barWidth + spaceWidth) / pixelDisplacementPerFrame).toInt()
            if (framesPerLoop > 0) {
                val pixelDisplacement = pixelDisplacementPerFrame * (frameCount % framesPerLoop.toInt())
                return index * (barWidth + spaceWidth) - pixelDisplacement
            }
        }
        return 0F
    }

    private fun waveFormFor(x: Float): Float {
        val waveNumber = 2 * PI / waveLength
        val angularFrequency = waveNumber * waveSpeed
        val normalizedRecordedAmplitude = max(normalizedAmplitude, minAmplitude)
        val amplitude = (barMaximumHeightRatio - barMinimumHeightRatio) * canvas.height * normalizedRecordedAmplitude
        val time = frameCount.toFloat() / fps.toFloat()
        return amplitude * abs(sin(waveNumber * x + angularFrequency * time)).toFloat()
    }
}