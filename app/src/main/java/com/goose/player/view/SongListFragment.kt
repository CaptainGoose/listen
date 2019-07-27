package com.goose.player.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.goose.player.MediaPlayerController
import com.goose.player.R
import com.goose.player.adapter.RecycleTouchListener
import com.goose.player.adapter.SongsListAdapter
import com.goose.player.entity.Song
import com.goose.player.extensions.setGone
import com.goose.player.interfaces.ClickListener
import com.goose.player.utils.FileHelper.getAllAudioFromDevice
import kotlinx.android.synthetic.main.fragment_song_list.*

class SongListFragment : androidx.fragment.app.Fragment(), ClickListener, SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener {

    private var songsList = ArrayList<Song>()
    private var songsListAdapter = SongsListAdapter(ArrayList())
    private var playerController: MediaPlayerController? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_song_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        showPlayerBtn.setOnClickListener { (activity as MainActivity).showPlayer() }
        refresher.setOnRefreshListener(this)
        getMusicBtn.setOnClickListener(this)
    }

    private fun getData() {
        songsList = getAllAudioFromDevice(context!!)
        songsListAdapter.dataset = songsList
        songsListAdapter.notifyDataSetChanged()
        refresher.isRefreshing = false
        getMusicBtn.setGone()
    }

    private fun initRecyclerView() {
        songsListRecyclerView.apply {
            addOnItemTouchListener(RecycleTouchListener(this@SongListFragment, context, this))
            setHasFixedSize(true)
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            this.adapter = songsListAdapter
        }
    }

    override fun onClick(view: View, position: Int) {
        showPlayerAndPlaySong(songsList[position])
        saveActualSongPosition(position)
    }

    private fun showPlayerAndPlaySong(song: Song) {
        (activity as MainActivity).showPlayer()
        with(playerController!!) {
            playNewSong(song)
        }
    }

    override fun onRefresh() {
        getData()
    }

    private fun saveActualSongPosition(position: Int) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("actualSongPosition", position)
            apply()
        }
    }

    override fun onLongClick(view: View, position: Int) {
    }

    override fun onClick(v: View?) {
        getData()
    }

    fun setMediaController(controller: MediaPlayerController) {
        playerController = controller
    }
}
