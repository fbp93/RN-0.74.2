package com.evernote.neutron.audiomanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap


class AudioRecorderService: Service(), AudioRecorderInterface {
    private val logTag = "AudioRecorderServer"
    private val channelID = "AudioRecorderServiceChannel"
    private val binder: IBinder = AudioRecorderServiceBinder()
    private val audioRecorder: AudioRecorderInterface by lazy {
        AudioRecorder()
    }


    // Service Logic Implementation

    override fun onBind(intent: Intent?): IBinder? {
        // Binder provides a simple way to communicate with the AudioRecorderService.
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationTitle = intent?.getStringExtra("title")
        val notificationText = intent?.getStringExtra("text")
        val notificationID = intent?.getStringExtra("identifier").hashCode()

        if (notificationTitle !== null && notificationText !== null) {
            createNotificationChannel(notificationTitle)
            val notificationIntent = Intent("com.evernote.neutron.TAP_NOTIFICATION")
            notificationIntent.setClassName(applicationContext.packageName, "com.evernote.MainActivity")
            var pendingIntentFlag = 0;
            if(Build.VERSION.SDK_INT > 30 ) {
                pendingIntentFlag = PendingIntent.FLAG_IMMUTABLE
            }
            var pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlag)


            val notification = NotificationCompat.Builder(this, channelID)
                    .setColor(Color.parseColor("#CC453C"))
                    .setColorized(true)
                    .setContentTitle(notificationTitle)
                    .setSmallIcon(R.drawable.vd_app_small_icon)
                    .setContentText(notificationText)
                    .setContentIntent(pendingIntent)
                    .build()

            startForeground(notificationID, notification)
        } else {
            Log.e(logTag, "Missing notification text or title")
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel(name: String) {
        val serviceChannel = NotificationChannel(
                channelID, name,
                NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)

    }

    inner class AudioRecorderServiceBinder : Binder() {
        val service: AudioRecorderService
            get() = this@AudioRecorderService
    }


    // AudioRecorderInterface Logic Implementation

    override val normalizedAmplitude: Float
        get() = audioRecorder.normalizedAmplitude

    override fun record(
            identifier: String,
            reactContext: ReactApplicationContext,
            onDidStart: (info: WritableMap?) -> Unit,
            onDidErrorOccur: (info: WritableMap?) -> Unit,
            onInfo: (what: Int, info: WritableMap?) -> Unit
    ) {
        audioRecorder.record(
                identifier,
                reactContext,
                onDidStart,
                onDidErrorOccur,
                onInfo
        )
    }

    override fun stopRecording(
            reactContext: ReactApplicationContext,
            onDidStop: (info: WritableMap?) -> Unit
    ) {
        audioRecorder.stopRecording(
                reactContext,
                onDidStop
        )
    }

    override fun cancelRecording() {
        audioRecorder.cancelRecording()
    }

    override fun getSessionID(): Int? {
        return audioRecorder.getSessionID()
    }
}