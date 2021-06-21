package com.loan555.myservice.model

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import com.loan555.myservice.LOGO
import com.loan555.myservice.MainActivity
import com.loan555.myservice.R
import com.loan555.myservice.adapter.TAG
import com.loan555.myservice.service.tagTest
import java.io.Serializable
import java.text.FieldPosition

class AudioList(private val context: Context) {
    private val audioList = mutableListOf<Audio>()

    fun playAudio(audio: Audio) {
        val mPlayer = MediaPlayer.create(context, audio.uri)
        try {
            mPlayer.prepare()
            mPlayer.start()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    fun getSize(): Int = audioList.size

    fun getItem(position: Int): Audio {
        return audioList[position]
    }

    fun getPositionByID(id: Long): Int{
        var position = -1
        for (i in 0 until audioList.size){
            if (id == audioList[i].id){
                position = i
                return position
            }
        }
        return position
    }

    fun printList() {
        var listToString = ""
        audioList.forEach {
            listToString += it.toString() + "\n"
        }
        Log.d(tagTest, "Audio list ${audioList.size} = $listToString")
    }

    fun getList() {
        val collection: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val protection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = null
        val selectionArgs = null
        val sortOder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = context.applicationContext.contentResolver.query(
            collection,
            protection,
            selection,
            selectionArgs,
            sortOder
        )
        query?.use { cursor ->
            audioList.clear()
            val options = BitmapFactory.Options()
            options.outHeight = 480
            options.outWidth = 640
            val bitmap =
                BitmapFactory.decodeResource(context.resources, LOGO, options)
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            while (cursor.moveToNext()) {
                try {
                    // Get values of columns for a given audio.
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val artists = cursor.getString(artistsColumn)
                    val duration = cursor.getInt(durationColumn)
                    val size = cursor.getInt(sizeColumn)
                    val title = cursor.getString(titleColumn)
                    val albums = cursor.getString(albumsColumn)

                    //load content Uri
                    val contentUri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    // Load thumbnail of a specific media item.
                    var thumbnail = bitmap
                    if (SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            thumbnail = context.applicationContext.contentResolver.loadThumbnail(
                                contentUri, Size(640, 480), null
                            )
                        } catch (e: java.lang.Exception) {

                        }
                    }

                    // Stores column values and the contentUri in a local object
                    // that represents the media file.
                    audioList += Audio(id, contentUri, name, artists, duration, size, thumbnail, title, albums)
                } catch (e: Exception) {
                    Log.e("error", "${e.message}")
                }
            }
        }
    }

    fun get() = audioList
}

data class Audio(
    val id: Long,
    val uri: Uri,
    val name: String,
    val artists: String,
    val duration: Int,
    val size: Int,
    val bitmap: Bitmap,
    val title: String,
    val albums: String
) : Serializable

data class AudioTest(
    val id: Long,
    val name: String,
    val artists: String,
    val duration: Int,
    val size: Int,
    val title: String,
    val albums: String
) : Serializable