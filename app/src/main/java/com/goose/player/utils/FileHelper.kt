package com.goose.player.utils

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.goose.player.entity.Song

/**
 *Created by Gxxxse on 20.07.2019.
 */
object FileHelper {

    fun getAllAudioFromDevice(context: Context): ArrayList<Song> {
        val tempAudioList = ArrayList<Song>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.ArtistColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DURATION
        )
        val c = context.contentResolver.query(uri, projection, null, null, null)

        if (c != null) {
            while (c.moveToNext()) {
                val path = c.getString(0)
                val name = c.getString(1)
                val album = c.getString(2)
                val artist = c.getString(3)
                val duration = c.getString(4)
                val hours = (duration.toInt() / 1000 / 60 / 60).toString()
                val minutes = (duration.toInt() / 1000 / 60).toString()
                val seconds = (duration.toInt() / 1000 - minutes.toInt() * 60).toString()

                val audioModel = Song(path, name, album, artist, duration.toInt(), hours, minutes, seconds)

                Log.e("Name :$name", " Album :$album")
                Log.e("Path :$path", " Artist :$artist")

                tempAudioList.add(audioModel)
            }
            c.close()
        }
        return tempAudioList
    }

}