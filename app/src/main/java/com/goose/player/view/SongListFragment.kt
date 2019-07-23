package com.goose.player.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goose.player.MediaPlayerController
import com.goose.player.R
import com.goose.player.adapter.RecycleTouchListener
import com.goose.player.adapter.SongsListAdapter
import com.goose.player.entity.Song
import com.goose.player.extensions.toast
import com.goose.player.interfaces.ClickListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_song_list.*

class SongListFragment : androidx.fragment.app.Fragment(), ClickListener {
    private var songsList = ArrayList<Song>()
    private var playerController: MediaPlayerController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initRecyclerView()
    }

    private fun initData() {
        songsList = getAllAudioFromDevice(context!!)
    }

    private fun initRecyclerView() {
        songsListRecyclerView.apply {
            addOnItemTouchListener(RecycleTouchListener(this@SongListFragment, context, this))
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            adapter = SongsListAdapter(songsList)
        }
    }

    override fun onClick(view: View, position: Int) {
        showPlayerAndPlaySong(songsList[position])
        saveActualSongPosition(position)
    }

    private fun showPlayerAndPlaySong(song: Song) {
        (activity as MainActivity).showPlayer()
        with(playerController!!){
            playNewSong(song)
        }
    }

    private fun saveActualSongPosition(position: Int) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("actualSongPosition", position)
            apply()
        }
    }

    override fun onLongClick(view: View, position: Int) {
        toast(position.toString() + position.toString())
    }

    fun setMediaController(controller: MediaPlayerController){
        playerController = controller
    }
}
