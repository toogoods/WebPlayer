package com.puxin.webplayer.ui.videoplayer.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.puxin.webplayer.R
import com.puxin.webplayer.aty.MainActivity
import com.puxin.webplayer.logic.model.Episode
import kotlinx.android.synthetic.main.menu_item.view.*

class VideoAdapter(private val episodeList: List<Episode>): RecyclerView.Adapter<VideoAdapter.ViewHolder>() {

    var mPosition = 0

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val itemText : TextView = view.itemText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false))

    override fun getItemCount() = episodeList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val episode = episodeList[position]

        holder.itemText.text = "${MainActivity.data?.title} 第${episode.num_str}集"
        if(position == mPosition) holder.itemView.requestFocus()
    }
}