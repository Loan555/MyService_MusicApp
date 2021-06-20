package com.loan555.myservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.fragment.app.commit
import com.loan555.myservice.fragment.ListSongFragment
import com.loan555.myservice.fragment.PlayFragment
import com.loan555.myservice.model.Audio
import com.loan555.myservice.model.AudioList
import com.loan555.myservice.permission.MyPermission
import com.loan555.myservice.permission.STORAGE_REQUEST_CODE
import com.loan555.myservice.service.MyServiceClass
import com.loan555.myservice.service.tagTest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_play.*
import kotlinx.android.synthetic.main.fragment_songs_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

const val LOGO = R.drawable.musical_note_icon
var currentFragment: String = ""

class MainActivity : AppCompatActivity(),
    ListSongFragment.ClickSongListener {
    private var audioList = AudioList(this)
    private val permissions = MyPermission(this, this)

    private lateinit var mService: MyServiceClass
    var mBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val conn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MyServiceClass.LocalBinder
            mService = binder.getService()
            mBound = true
            Log.e(tagTest, "connect bound service $mBound")
            //read data here
            loadData(mService.audioList)
            audioList = mService.audioList
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MyServiceClass::class.java).also {
            bindService(it, conn, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar_main)

        supportFragmentManager.commit {
            val mSongsFragment = ListSongFragment(audioList, this@MainActivity)
            setReorderingAllowed(true)
            replace(R.id.fragment_container_view_main, mSongsFragment)
            addToBackStack("home")
        }

        if (audioList.get().size == 0) {
            loadData(audioList)
        }

        // su ly su kien pause
        play_pause.setOnClickListener {
            if (mBound) {
                // Call a method from the LocalService.
                // However, if this call were something that might hang, then this request should
                // occur in a separate thread to avoid slowing down the activity performance.
                mService.handMusic(it)
            } else {
                Toast.makeText(this, "service is not ready", Toast.LENGTH_SHORT).show()
            }
        }

        skip_next.setOnClickListener {
            if (mBound) {
                try {
                    if (mService.songPlaying != null) {
                        mService.nextClick(
                            mService.songPlaying,
                            mService.statePlay,
                            music_playing
                        )
                    }
                } catch (e: Exception) {
                    Log.e(tagTest, "skip next error: ${e.message}")
                }
            } else {
                Toast.makeText(this, "service is not ready", Toast.LENGTH_SHORT).show()
            }
        }
        skip_back.setOnClickListener {
            if (mBound) {
                try {
                    if (mService.songPlaying != null) {
                        mService.skipBackClick(
                            mService.songPlaying,
                            mService.statePlay,
                            music_playing
                        )
                        Log.e(tagTest, "back: ")
                    }
                } catch (e: Exception) {
                    Log.e(tagTest, "skip next error: ${e.message}")
                }
            } else {
                Toast.makeText(this, "service is not ready", Toast.LENGTH_SHORT).show()
            }
        }

        music_playing.setOnClickListener {
            supportFragmentManager.commit {
                val mPlayFragment = PlayFragment(mService)
                setReorderingAllowed(true)
                replace(R.id.fragment_container_view_main, mPlayFragment)
                addToBackStack("play")
            }
            music_playing.visibility = View.GONE
            mService.myItemViewFragment = play
        }
    }

    var cout = 0
    override fun onBackPressed() {
        when (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name) {
            "home" -> {
                cout++
                if (cout == 1) {
                    Toast.makeText(
                        this,
                        "Nhấn back thêm 1 lần nữa để thoát",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                val thread = Thread {
                    Thread.sleep(1000)
                    cout = 0
                }
                thread.start()
                if (cout == 2)
                    finish()
            }
            "play" -> {
                supportFragmentManager.popBackStack()
                music_playing.visibility = View.VISIBLE
                mService.initViewPlay(mService.songPlaying, music_playing)
                currentFragment = "home"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.actionbar_main, menu)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Toast.makeText(this, "Allow...", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    //permission denied
                    Toast.makeText(this, "Storage permission required...", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }
    }

    private fun loadData(listData: AudioList) {
        if (permissions.checkStoragePermission()) {
            GlobalScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    try {
                        listData.getList()
                        return@async 0 // success
                    } catch (e: Exception) {
                        return@async -1 // error
                    }
                }
                if (result.await() == 0) {
                    Toast.makeText(this@MainActivity, "Load data done", Toast.LENGTH_SHORT)
                        .show()
                    list.adapter?.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@MainActivity, "Load data error..", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else {
            permissions.requestStoragePermission()
        }
    }

    override fun click(item: Audio) {
        playAudio(item)
    }

    private fun playAudio(item: Audio) {
        mService.playAudio(
            item,
            music_playing
        )
    }
}