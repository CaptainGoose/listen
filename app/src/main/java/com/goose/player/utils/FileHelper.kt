package com.goose.player.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.goose.player.entity.Song


/**
 *Created by Gxxxse on 20.07.2019.
 */
object FileHelper {

    fun getAllAudioFromDevice(context: Context): ArrayList<Song> {
        val tempAudioList = ArrayList<Song>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val name = cursor.getString(1)
                val artist = cursor.getString(2)
                val duration = cursor.getString(3)
                val album = getAlbumArt(context, cursor.getString(4))
                val hours = (duration.toInt() / 1000 / 60 / 60).toString()
                val minutes = (duration.toInt() / 1000 / 60).toString()
                val seconds = (duration.toInt() / 1000 - minutes.toInt() * 60).toString()
                val audioModel = Song(path, name, album, artist, duration.toInt(), hours, minutes, seconds)
                tempAudioList.add(audioModel)
            }
            cursor.close()
        }
        return tempAudioList
    }

    private fun getAlbumArt(context: Context, albumId: String): Bitmap? {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
            MediaStore.Audio.Albums._ID + "=?",
            arrayOf(albumId), null)

        if (cursor.moveToFirst()) {
            val albumArtPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
            return BitmapFactory.decodeFile(albumArtPath)
        }
        return null
    }
}