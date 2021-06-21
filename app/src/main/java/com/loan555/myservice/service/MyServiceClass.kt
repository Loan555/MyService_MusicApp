package com.loan555.myservice.service

import android.app.PendingIntent
import android.app.Service
import android.content.*
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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.loan555.myservice.*
import com.loan555.myservice.adapter.TAG
import com.loan555.myservice.broadcast.MyBroadcastReceiver
import com.loan555.myservice.model.AppPreferences
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
const val ACTION_PAUSE = 1
private const val ACTION_STOP = 3
private const val ACTION_NEXT = 2
private const val ACTION_BACK = -2
const val ACTION_HISTORY = 4
class MyServiceClass : Service() {

    var alarmTime = 0
    var statePlay: Int = 0
    var audioList = AudioList(this)

    var mPlayer: MediaPlayer = MediaPlayer()
    var isPlaying = false

    // Binder given to clients
    private val binder = LocalBinder()

    // Random number generator
    private val mGenerator = Random

    var lastClick = 0

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

    lateinit var songPlaying: Audio

    var myItemView: View? = null
    var myItemViewFragment: View? = null

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
        Log.d(tagTest, "service on create")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(tagTest, "service on start command")
        try {
            val action = intent?.getIntExtra("action_music_service", 0)
            if (action != 0)
                handActionMusic(action)// put intent o day se lam thay doi notification duoc

            var bundle: Bundle? = null
            bundle =
                intent?.getBundleExtra("bundle_song")// intent dang bij nullval intent = Intent(this, MyServiceClass::class.java)
            if (bundle != null) {
                val song = bundle.get("song") as AudioTest
                val audio = convertToAudio(song)
                val name = audio.title
                val singer = audio.artists
                val thumbnail = audio.bitmap
                when(action){
                    0->startMusic(audio)
                    4->songHistory(audio)
                }


                val pendingIntent =
                    PendingIntent.getActivity(this, 0, intent, flags)
                val mediaSession = MediaSessionCompat(this, "tag")

                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(name)
                    .setContentText(singer)
                    .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                    .setLargeIcon(thumbnail)
                    .addAction(
                        R.drawable.ic_baseline_skip_previous_24,
                        "previous",
                        getPendingIntent(this, ACTION_BACK, bundle)
                    )//#0
                    .addAction(
                        getImgBtn(),
                        "pause",
                        getPendingIntent(this, ACTION_PAUSE, bundle)
                    )//#1
                    .addAction(
                        R.drawable.ic_baseline_skip_next_24,
                        "next",
                        getPendingIntent(this, ACTION_NEXT, bundle)
                    )//#2
                    .addAction(
                        R.drawable.ic_baseline_close_24,
                        "next",
                        getPendingIntent(this, ACTION_STOP, bundle)
                    )//#2
                    .setStyle(
                        androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2 /* #1: pause button \*/)
                            .setMediaSession(mediaSession.sessionToken)
                    )
                    .setSound(null)
                    .setContentIntent(pendingIntent)
                    .build()
                startForeground(ONGOING_NOTIFICATION_ID, notification)
                AppPreferences.init(this)
                AppPreferences.lastSongIDPlay = audio.id
            } else {
                Log.d(tagTest, "bundel null ")
            }
        } catch (e: Exception) {
            Log.e(tagTest, "sent notification error: ${e.message}")
        }
        return START_NOT_STICKY
    }

    private fun songHistory(audio: Audio) {
        try {
            mPlayer = if (mPlayer == null) {
                MediaPlayer.create(this, audio.uri)
            } else {
                mPlayer.release()
                MediaPlayer.create(this, audio.uri)
            }
            mPlayer.start()
            if (!isPlaying)
                mPlayer.pause()
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

    private fun getImgBtn(): Int {
        return if (isPlaying)
            R.drawable.ic_baseline_pause_24
        else R.drawable.ic_baseline_play_arrow_24
    }

    private fun handActionMusic(actionMusic: Int?) {// cap nhat trang thai cho cac nut
        Log.e(tagTest, "handActionMusic $actionMusic")
        when (actionMusic) {
            0 -> {
                //khong co su kienj nao
            }
            1 -> {
                if (isPlaying) {
                    try {
                        Log.d(tagTest, "pause")
                        isPlaying = false
                        mPlayer.pause()
                        myItemView?.findViewById<View>(R.id.play_pause)
                            ?.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
                        if (myItemViewFragment != null) {
                            myItemViewFragment?.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
                        }
                    } catch (e: Exception) {
                        Log.e(tagTest, "error pause: ${e.message}")
                    }
                } else {
                    try {
                        Log.d(tagTest, "play")
                        isPlaying = true
                        mPlayer.start()
                        myItemView?.findViewById<View>(R.id.play_pause)
                            ?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
                        if (myItemViewFragment != null) {
                            myItemViewFragment?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
                        }
                    } catch (e: Exception) {
                        Log.e(tagTest, "error play: ${e.message}")
                    }
                }
            }
            2 -> {
                Log.d(tagTest, "next")
                nextClick(songPlaying, statePlay)
            }
            -2 -> {
                Log.d(tagTest, "back")
                skipBackClick(songPlaying, statePlay)
            }
            3 -> {
                Log.d(tagTest, "stop")
                stopSelf()
            }
        }
    }

    fun handBtnPlayImg() {
        if (isPlaying) {
            myItemViewFragment?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
            myItemView?.findViewById<View>(R.id.play_pause)
                ?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
        } else {
            myItemViewFragment?.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
            myItemView?.findViewById<View>(R.id.play_pause)
                ?.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    private fun getPendingIntent(context: Context, action: Int, bundle: Bundle): PendingIntent? {
        Log.e(tagTest, "getPendingIntent")
        val intent = Intent(this, MyBroadcastReceiver::class.java)
        intent.putExtra("action_music", action)
        intent.putExtra("bundle_song", bundle)// cai nay chi push len de cho du lieu khoi null
        return PendingIntent.getBroadcast(// lay intent cua receiver
            context.applicationContext,
            action,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "service onBind")
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tagTest, "service on destroy")
        mPlayer.release()
        AppPreferences.init(this)
        AppPreferences.lastSongIDPlay = songPlaying.id
        Log.d(tagTest, "save : ${AppPreferences.lastSongIDPlay}")
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
        song: Audio
    ) {
        isPlaying = true
        clickStartService(song)
        songPlaying = song
        if (currentFragment == "home")
            initViewPlay(song)
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
        intent.putExtra("bundle_song", bundle)
        startService(intent)
    }

    fun initViewPlay(
        item: Audio
    ) {
        myItemView?.visibility = View.VISIBLE
        val imgSong = myItemView?.findViewById<ImageView>(R.id.img_playing)
        val nameSong = myItemView?.findViewById<TextView>(R.id.nameSong_playing)
        val singerSong = myItemView?.findViewById<TextView>(R.id.singer_playing)
        val pauseBtn = myItemView?.findViewById<ImageButton>(R.id.play_pause)
        imgSong?.setImageBitmap(item.bitmap)
//        lastClick++
//        GlobalScope.launch(Dispatchers.IO) {
//            val threadPlaying = lastClick
//            var rotat = 0f
//            while (threadPlaying == lastClick) {
//                Thread.sleep(50)
//                // Call a method from the LocalService.
//                // However, if this call were something that might hang, then this request should
//                // occur in a separate thread to avoid slowing down the activity performance.
//                if (isPlaying) {
//                    rotat += 1f * 360 / 500
//                    rotat %= 360
//                    imgSong?.rotation = rotat
//                }
//            }
//        }
        nameSong?.text = item.title
        singerSong?.text = item.artists
        if (isPlaying)
            pauseBtn?.setBackgroundResource(R.drawable.ic_baseline_pause_24)
        else
            pauseBtn?.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
    }

    fun handMusic() {
        val bundleReceiver = Bundle()
        val audio = songPlaying
        val audioTest = AudioTest(
            audio.id,
            audio.name,
            audio.artists,
            audio.duration,
            audio.size,
            audio.title,
            audio.albums
        )
        bundleReceiver.putSerializable("song", audioTest)
        val intentReceiver = Intent(this, MyBroadcastReceiver::class.java)
        intentReceiver.putExtra("action_music", ACTION_PAUSE)
        intentReceiver.putExtra("bundle_song", bundleReceiver)
        val bundle = intentReceiver.getBundleExtra("bundle_song")
        Log.d(tagTest, "sddfsd lay tu rec: $bundle ")
        sendBroadcast(intentReceiver)
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
                nexPosition = if (positionNow == audioList.getSize() - 1) {
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
                    playAudio(nextSongPlay)
                } else
                    handMusic()
            } catch (e: Exception) {
                Log.e(TAG, "play music error: ${e.message}")
            }
    }

    fun nextClick(
        audioPlaying: Audio,
        statePlay: Int
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
                playAudio(nextSongPlay)
            } catch (e: Exception) {
                Log.e(TAG, "play music error: ${e.message}")
            }
    }

    fun skipBackClick(
        audioPlaying: Audio,
        statePlay: Int
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
                playAudio(backSongPlay)
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