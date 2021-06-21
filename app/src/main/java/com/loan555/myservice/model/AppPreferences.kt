package com.loan555.myservice.model

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AppPreferences {
    private const val NAME = "SpinKotlin"
    private const val MODE = Context.MODE_PRIVATE
    lateinit var preferences: SharedPreferences

    private val LAST_SONG = Pair("lastSong", -1)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    var lastSongIDPlay: Long
        get() = preferences.getLong(LAST_SONG.first, LAST_SONG.second.toLong())
        set(value) = preferences.edit {
            this.putLong(LAST_SONG.first, value)
        }
}