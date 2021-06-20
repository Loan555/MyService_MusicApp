package com.loan555.myservice.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.loan555.myservice.service.MyServiceClass
import com.loan555.myservice.service.tagTest

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.e(tagTest, "onReceive")
        val actionMusic = intent?.getIntExtra("action_music", 0)
        if (actionMusic != 0){
            val bundle = intent?.getBundleExtra("bundle_song")
            Log.d(tagTest, "bundle get tu start service = $bundle")

            val intentService = Intent(context, MyServiceClass::class.java)
            intentService.putExtra("action_music_service", actionMusic)
            intentService.putExtra("bundle_song", bundle)
            Log.e(tagTest, "co thay doi tu notification $actionMusic")
            context?.startService(intentService)
        }
    }
}