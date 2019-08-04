package com.goose.player.utils

import android.Manifest.permission_group.STORAGE
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.goose.player.entity.Song
import java.util.*

/**
 *Created by Gxxxse on 03.08.2019.
 */
object StorageUtil {

    fun storeSongList(context: Context, arrayList: ArrayList<Song>) {
        val preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        val json = Gson().toJson(arrayList)
        editor.putString("audioArrayList", json)
        editor.apply()
    }

    fun loadSongList(context: Context): ArrayList<Song> {
        val preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val json = preferences.getString("audioArrayList", "")
        val type = object :TypeToken<ArrayList<Song>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun storeSongIndex(index: Int, context: Context) {
        val preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt("audioIndex", index)
        editor.apply()
    }

    fun loadAudioIndex(context: Context): Int {
        val preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)
        return preferences.getInt("audioIndex", 0)
    }

}