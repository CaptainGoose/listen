package com.goose.player.adapter

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.goose.player.R
import com.goose.player.entity.Song

/**
 *Created by Gxxxse on 20.07.2019.
 */
class SongsListAdapter(private val dataset: ArrayList<Song>): RecyclerView.Adapter<SongsListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val album: ImageView = itemView.findViewById(R.id.albumImage)
        private val artist: TextView = itemView.findViewById(R.id.artist)
        private val songName: TextView = itemView.findViewById(R.id.songName)

        fun bind(song: Song){
            artist.text = song.artist
            songName.text = song.name
            Glide.with(itemView).load(Uri.parse(song.path)).into(album)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataset.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position])
    }

}