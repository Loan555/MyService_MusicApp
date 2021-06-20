package com.loan555.myservice.fragment

import android.app.Service
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import com.loan555.myservice.*
import com.loan555.myservice.service.MyServiceClass
import com.loan555.myservice.service.tagTest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_play.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

class PlayFragment(private val mService: MyServiceClass) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mService.myItemViewFragment = play

        currentFragment = "play"
        handShuffle(loop)

        initSeekBar()
        var currentProcess = 0
        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentProcess = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mService.mPlayer.seekTo(currentProcess)
            }

        })
        play.setOnClickListener {
            try {
                mService.handMusic()
            } catch (e: Exception) {
                Toast.makeText(this.requireContext(), "service is not ready", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        skip_next_play.setOnClickListener {
            try {
                try {
                    if (mService.songPlaying != null) {
                        mService.nextClick(
                            mService.songPlaying,
                            mService.statePlay
                        )
                        mService.handBtnPlayImg()
                    }
                } catch (e: Exception) {
                    Log.e(tagTest, "skip next error: ${e.message}")
                }
            } catch (e: Exception) {
                Toast.makeText(this.requireContext(), "service is not ready", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        skip_back_play.setOnClickListener {
            try {
                try {
                    if (mService.songPlaying != null) {
                        mService.skipBackClick(
                            mService.songPlaying,
                            mService.statePlay
                        )
                        Log.e(tagTest, "back: ")
                        mService.handBtnPlayImg()
                    }
                } catch (e: Exception) {
                    Log.e(tagTest, "skip next error: ${e.message}")
                }
            } catch (e: Exception) {
                Toast.makeText(this.requireContext(), "service is not ready", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        loop.setOnClickListener {
            mService.statePlay = (mService.statePlay + 1) % 4
            handShuffle(it)
        }
        time_btn.setOnClickListener {
            Toast.makeText(
                this.requireContext(),
                "Rảnh thì làm thêm chức năng này",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mService.myItemViewFragment = null
    }

    private fun handAlarm(view: View?) {
        if (mService.alarmTime > 0) {
            view?.setBackgroundResource(R.drawable.ic_baseline_alarm_on_24)
        } else {
            view?.setBackgroundResource(R.drawable.ic_baseline_timer_24)
        }
    }

    private fun handShuffle(view: View?) {// thay doi trang thai nut shuffle
        when (mService.statePlay) {
            //0 la tuan tu roi ket thuc
            //1 la lap lai list
            //2 la phat ngau nhien
            //3 lap lai 1 bai
            0 -> {
                view?.setBackgroundResource(R.drawable.ic_baseline_repeat_24)
            }
            1 -> {
                view?.setBackgroundResource(R.drawable.ic_baseline_repeat_24_color)
            }
            2 -> {
                view?.setBackgroundResource(R.drawable.ic_baseline_shuffle_24)
            }
            3 -> {
                view?.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24)
            }
        }
    }

    private fun initSeekBar() {
        try {
            var currentSongID = mService.songPlaying.id
            val time = mService.songPlaying!!.duration
            nameSong.text = mService.songPlaying.title
            nameSinger.text = mService.songPlaying.artists
            img_src.setImageBitmap(mService.songPlaying.bitmap)
            if (mService.isPlaying) {
                play.setBackgroundResource(R.drawable.ic_baseline_pause_24)
            } else play.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
            seek_bar!!.max = time
            time_sum.text = "${(time / 60 / 1000)}:${time / 1000 % 60}"
            try {
                var handler = Handler()
                val postDelayed = handler.postDelayed(object : Runnable {
                    override fun run() {
                        try {
                            if (currentSongID != mService.songPlaying.id) {
                                currentSongID = mService.songPlaying.id
                                nameSong.text = mService.songPlaying.title
                                img_src.setImageBitmap(mService.songPlaying.bitmap)
                                nameSinger.text = mService.songPlaying.artists
                                val time = mService.songPlaying!!.duration
                                time_sum.text = "${(time / 60 / 1000)}:${time / 1000 % 60}"
                                seek_bar!!.max = time
                            }
                            val time = mService.mPlayer.currentPosition
                            seek_bar!!.progress = mService.mPlayer.currentPosition
                            time_draw!!.text = "${time / 1000 / 60}:${time / 1000 % 60}"
                            handler.postDelayed(this, 1000)
                        } catch (e: Exception) {
                            Log.e(tagTest, "error seek bar thread ${e.message}")
                        }
                    }
                }, 0)
            } catch (e: Exception) {
                Log.e(tagTest, "error seek bar thread ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(tagTest, "error seek bar ${e.message}")
        }
    }
}