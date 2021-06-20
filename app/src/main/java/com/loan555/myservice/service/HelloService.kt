package com.loan555.myservice.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.loan555.myservice.CHANNEL_ID
import com.loan555.myservice.R
import com.loan555.myservice.adapter.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Process

const val tagTest = "tesss"

const val THREAD_PRIORITY_BACKGROUND = 10

class HelloService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    // Handler that receives messages from the thread
    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {
                // bat tin hieu: khi thread chay het thi cung tat service
                for (i in 1..10) {
                    Log.e(tagTest, "thread off service here...........$i")
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                // Restore interrupt status.
                Thread.currentThread().interrupt()
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
//            stopSelf(msg.arg1)
            stopForeground(true)
        }
    }

    override fun onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread("ServiceStartArguments", THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show()

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        serviceHandler?.obtainMessage()?.also { msg ->
            msg.arg1 = startId // mỗi lần nhấn lại sinh 1 id khác
            serviceHandler?.sendMessage(msg)
        }
        val bundle = intent.extras
        val text = bundle?.get("out")
        Log.e(tagTest, "thread off service here...........$text")// lay duoc intent roi

        val pendingIntent: PendingIntent =
            Intent(this, HelloService::class.java).let { notificationIntent ->
                PendingIntent.getActivities(this, 0, arrayOf(notificationIntent), 0)
            }
        val notificationIntent: Notification =  NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("name")
            .setContentText("singer")
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .addAction(R.drawable.ic_baseline_skip_previous_24, "previous", null)//#0
            .addAction(R.drawable.ic_baseline_pause_24, "pause", null)//#1
            .addAction(R.drawable.ic_baseline_skip_next_24, "next", null)//#2
            .addAction(R.drawable.ic_baseline_close_24, "next", null)//#2
            .setSound(null)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notificationIntent)

        // If we get killed, after returning from here, not restart
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show()
    }

}