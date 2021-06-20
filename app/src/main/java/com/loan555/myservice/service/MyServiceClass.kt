package com.loan555.myservice.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.loan555.myservice.*
import com.loan555.myservice.adapter.TAG
import com.loan555.myservice.broadcast.MyBroadcastReceiver
import com.loan555.myservice.model.Audio
import com.loan555.myservice.model.AudioList
import com.loan555.myservice.model.AudioTest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_songs_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.random.Random

const val ONGOING_NOTIFICATION_ID = 1

class MyServiceClass : Service() {
    val br: BroadcastReceiver = MyBroadcastReceiver()
    var alarmTime = 0
    var statePlay: Int = 0
    var audioList = AudioList(this)

    var mPlayer: MediaPlayer = MediaPlayer()
    var isPlaying = false

    // Binder given to clients
    private val binder = LocalBinder()

    // Random number generator
    private val mGenerator = Random

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    lateinit var songPlaying: Audio

    private lateinit var myItemView: View

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MyServiceClass = this@MyServiceClass
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Log.d(TAG, "service on create")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service on start command")
        try {
            val bundle = intent?.extras// intent dang bij null
            if (bundle != null) {
                val song = bundle.get("song") as AudioTest
                val audio = convertToAudio(song)
                val name = audio.title
                val singer = audio.artists
                val thumbnail = audio.bitmap

                val pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val mediaSession = MediaSessionCompat(this, "tag")
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(name)
                    .setContentText(singer)
                    .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                    .setLargeIcon(thumbnail)
                    .addAction(R.drawable.ic_baseline_skip_previous_24, "previous", null)//#0
                    .addAction(R.drawable.ic_baseline_pause_24, "pause", null)//#1
                    .addAction(R.drawable.ic_baseline_skip_next_24, "next", null)//#2
                    .addAction(R.drawable.ic_baseline_close_24, "next", null)//#2
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2 /* #1: pause button \*/)
                            .setMediaSession(mediaSession.sessionToken)
                    )
                    .setSound(null)
                    .setContentIntent(pendingIntent)
                    .build()
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
//                    if (isPlaying)
//                    notification.actions[1].actionIntent = pendingIntent
//                }
                startForeground(ONGOING_NOTIFICATION_ID, notification)
                startMusic(audio)
            }
        } catch (e: Exception) {
            Log.e(TAG, "sent notification error: ${e.message}")
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "service onBind")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "service on destroy")
        mPlayer.release()
    }

    private fun startMusic(audio: Audio) {
        try {
            isPlaying = true
            mPlayer = if (mPlayer == null) {
                MediaPlayer.create(this, audio.uri)
            } else {
                mPlayer.release()
                MediaPlayer.create(this, audio.uri)
            }
            mPlayer.start()
            mPlayer.setOnCompletionListener {
                Log.e(tagTest, "chayj het bai roi")
                nextAuto(audio, statePlay, myItemView)
            }
            mPlayer.setOnErrorListener { _, _, _ ->
                nextAuto(audio, statePlay, myItemView)
                true
            }
            mPlayer.setOnSeekCompleteListener {
                Log.e(tagTest, "di chuyen seekbar xong")
            }
        } catch (e: Exception) {
            Log.e(TAG, "play music error: ${e.message}")
        }
    }

    fun playAudio(
        song: Audio,
        itemPlaying: View?
    ) {
        if (currentFragment == "home")
            initViewPlay(song, itemPlaying)
        clickStartService(song)
        songPlaying = song
        if (itemPlaying != null) {
            myItemView = itemPlaying
        }
    }

    private fun clickStartService(audio: Audio) {
        val intent = Intent(this, MyServiceClass::class.java)
        val bundle = Bundle()
        val audioTest = AudioTest(
            audio.id,
            audio.name,
            audio.artists,
            audio.duration,
            audio.size,
            audio.title,
            audio.albums
        )
        bundle.putSerializable("song", audioTest)
        intent.putExtras(bundle)
        ContextCompat.startForegroundService(this, intent)
    }

    fun initViewPlay(
        item: Audio,
        itemPlaying: View?
    ) {
        itemPlaying?.visibility = View.VISIBLE
        val imgSong = itemPlaying?.findViewById<ImageView>(R.id.img_playing)
        val nameSong = itemPlaying?.findViewById<TextView>(R.id.nameSong_playing)
        val singerSong = itemPlaying?.findViewById<TextView>(R.id.singer_playing)
        val pauseBtn = itemPlaying?.findViewById<ImageButton>(R.id.play_pause)
        imgSong?.setImageBitmap(item.bitmap)
//        lastClick++
//        GlobalScope.launch(Dispatchers.IO) {
//            val threadPlaying = lastClick
//            var rotat = 0f
//            while (threadPlaying == lastClick) {
//                Thread.sleep(100)
//                // Call a method from the LocalService.
//                // However, if this call were something that might hang, then this request should
//                // occur in a separate thread to avoid slowing down the activity performance.
//                if (isPlaying) {
//                    rotat += 1f * 360 / 100
//                    rotat %= 360
//                    imgSong?.rotation = rotat
//                }
//            }
//        }
        nameSong?.text = item.title
        singerSong?.text = item.artists
        pauseBtn?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
    }

    fun handMusic(view: View?) {
        when (isPlaying) {
            false -> {// dang dung thi chay
                resumeMusic()
                view?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
            }
            true -> {//dang chay thi dung
                pauseMusic()
                view?.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }
    }

    private fun pauseMusic() {
        try {
            if (mPlayer != null) {
                mPlayer.pause()
                isPlaying = false
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    private fun resumeMusic() {
        try {
            if (mPlayer != null) {
                mPlayer.start()
                isPlaying = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Can't start media! ${e.message}")
        }
    }

    private fun convertToAudio(audioTest: AudioTest): Audio {
        val options = BitmapFactory.Options()
        options.outHeight = 480
        options.outWidth = 640
        var thumbnail =
            BitmapFactory.decodeResource(this.resources, LOGO, options)
        val contentUri =
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioTest.id)

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                thumbnail =
                    this.applicationContext.contentResolver.loadThumbnail(
                        contentUri, Size(640, 480), null
                    )
            }
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "img not found")
        }
        return Audio(
            audioTest.id,
            contentUri,
            audioTest.name,
            audioTest.artists,
            audioTest.duration,
            audioTest.size,
            thumbnail,
            audioTest.title,
            audioTest.albums
        )
    }

    private fun nextSong(positionNow: Int, statePlay: Int): Int {
        var nexPosition = -1
        //0 la tuan tu roi ket thuc
        //1 la lap lai list
        //2 la phat ngau nhien
        //3 lap lai 1 bai
        when (statePlay) {
            0 -> {
                nexPosition = if (positionNow == audioList.getSize() - 1)
                    -1
                else positionNow + 1
            }
            1 -> {
                nexPosition = if (positionNow == audioList.getSize() -1 ) {
                    0
                } else {
                    positionNow + 1
                }
            }
            2 -> {
                nexPosition = Random.nextInt(audioList.getSize())
            }
            3 -> {
                nexPosition = positionNow
            }
        }
        return nexPosition
    }

    private fun nextAuto(
        audioPlaying: Audio,
        statePlay: Int,
        itemPlaying: View?
    ) {
        if (audioList.getSize() > 0)
            try {
                var nextSongPlay: Audio
                val positionNow = audioList.getPositionByID(audioPlaying.id)
                val next = nextSong(positionNow, statePlay)
                if (next != -1) {
                    nextSongPlay = audioList.getItem(next)
                    playAudio(nextSongPlay, itemPlaying)
                } else
                    if (currentFragment == "home")
                        handMusic(itemPlaying?.findViewById(R.id.play_pause))
                    else if (currentFragment == "play"){
                        handMusic(itemPlaying?.findViewById(R.id.play_pause))
                    }
            } catch (e: Exception) {
                Log.e(TAG, "play music error: ${e.message}")
            }
    }

    fun nextClick(
        audioPlaying: Audio,
        statePlay: Int,
        itemPlaying: View?
    ) {
        if (audioList.getSize() > 0)
            try {
                var nextSongPlay =
                    audioList.getItem(0) // neu nextsong == -1 thi chayj lai danh sach
                val positionNow = audioList.getPositionByID(audioPlaying.id)
                val next = nextSong(positionNow, statePlay)
                if (next != -1) {
                    nextSongPlay = audioList.getItem(next)
                }
                playAudio(nextSongPlay, itemPlaying)
            } catch (e: Exception) {
                Log.e(TAG, "play music error: ${e.message}")
            }
    }

    fun setAlarm() {//time la so phut
        var result = false
        var count = 0
        while (count < alarmTime) {
            Thread.sleep(5000)
            count += 5
        }
        mPlayer?.pause()
    }

    fun skipBackClick(
        audioPlaying: Audio,
        statePlay: Int,
        viewPlaying: View?
    ) {
        if (audioList.getSize() > 0)
            try {
                var backSongPlay =
                    audioList.getItem(audioList.getSize() - 1) // neu nextsong == -1 thi chayj lai danh sach
                val positionNow = audioList.getPositionByID(audioPlaying.id)

                Log.e(TAG, "positionNow : $positionNow")

                val back = backSong(positionNow, statePlay, audioList)
                if (back != -1) {
                    backSongPlay = audioList.getItem(back)
                }
                playAudio(backSongPlay, viewPlaying)
            } catch (e: Exception) {
                Log.e(TAG, "play music error: ${e.message}")
            }
    }

    private fun backSong(positionNow: Int, statePlay: Int, audioList: AudioList): Int {
        var backPosition = -1
        //0 la tuan tu roi ket thuc
        //1 la lap lai list
        //2 la phat ngau nhien
        //3 lap lai 1 bai
        when (statePlay) {
            0 -> {
                backPosition = if (positionNow == 0)
                    -1
                else positionNow - 1
            }
            1 -> {
                backPosition = if (positionNow == 0) {
                    audioList.getSize() - 1
                } else {
                    positionNow - 1
                }
            }
            2 -> {
                backPosition = Random.nextInt(audioList.getSize())
            }
            3 -> {
                backPosition = positionNow
            }
        }
        return backPosition
    }

}