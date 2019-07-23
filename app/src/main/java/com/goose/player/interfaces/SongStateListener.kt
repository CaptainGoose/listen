package com.goose.player.interfaces

import com.goose.player.entity.Song

/**
 *Created by Gxxxse on 21.07.2019.
 */
interface SongStateListener {
    fun onSongPlay(song: Song)
    fun onSongPause()
    fun onSongResume()
    fun onSongRelease()
    fun onSeekBarPositionChange(progress: Int)
}