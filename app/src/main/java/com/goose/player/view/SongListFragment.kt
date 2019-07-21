package com.goose.player.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goose.player.R
import com.goose.player.RecycleTouchListener
import com.goose.player.adapter.SongsListAdapter
import com.goose.player.entity.Song
import com.goose.player.extensions.toast
import com.goose.player.interfaces.ClickListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_song_list.*

class SongListFragment : Fragment(), ClickListener {
    var songsList = ArrayList<Song>()

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
            layoutManager = LinearLayoutManager(context)
            adapter = SongsListAdapter(songsList)
        }
    }

    override fun onClick(view: View, position: Int) {
        showPlayerAndPlaySong(songsList[position])
    }

    private fun showPlayerAndPlaySong(song: Song) {
        //todo add listener
        with(activity as MainActivity){
            showPlayer()
            stopSong()
            playSong(song)
        }
    }

    override fun onLongClick(view: View, position: Int) {
        toast(position.toString() + position.toString())
    }
}
