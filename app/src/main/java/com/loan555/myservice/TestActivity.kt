package com.loan555.myservice

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.loan555.myservice.service.HelloService
import com.loan555.myservice.service.MyBoundService
import com.loan555.myservice.service.tagTest
import kotlinx.android.synthetic.main.activity_test.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity() {
    private lateinit var mService: MyBoundService
    private var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MyBoundService.LocalBinder
            mService = binder.getService()
            mBound = true
            Log.e(tagTest, "connect bound service $mBound")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        btn_start.setOnClickListener {
            startMyService()
        }
        btn_stop.setOnClickListener {
            stopMyService()
        }
        btn_start_fs.setOnClickListener {
            Intent(this, HelloService::class.java).also { intent ->
                startService(intent)
            }
        }
        btn_start_bs.setOnClickListener {
            // Bind to LocalService
            if (mBound) {
                // Call a method from the LocalService.
                // However, if this call were something that might hang, then this request should
                // occur in a separate thread to avoid slowing down the activity performance.
                val num: Int = mService.randomNumber
                Toast.makeText(this, "random = $num", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MyBoundService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    private fun startMyService() {
        Intent(this, HelloService::class.java).also { intent ->
            val text = input_text.text.toString()
            val bundle = Bundle()
            bundle.putString("out", text)
            intent.putExtras(bundle)

            startService(intent)
        }
    }

    private fun stopMyService() {
        Intent(this, HelloService::class.java).also { intent ->
            stopService(intent)
        }
    }
}