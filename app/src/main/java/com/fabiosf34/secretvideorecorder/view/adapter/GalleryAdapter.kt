package com.fabiosf34.secretvideorecorder.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.RowVideoBinding
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.listeners.OnVideoListener
import com.fabiosf34.secretvideorecorder.view.viewHolder.GalleryViewHolder

class GalleryAdapter : RecyclerView.Adapter<GalleryViewHolder>() {

    private var videoList: List<Video> = listOf()
    private lateinit var listener: OnVideoListener

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GalleryViewHolder {
        val item = RowVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(item, listener)
    }

    override fun onBindViewHolder(
        holder: GalleryViewHolder,
        position: Int
    ) {
        if (videoList.isNotEmpty()) {
                holder.bind(videoList.sortedBy { it.title }[position])
        }
        else Toast.makeText(
            holder.itemView.context,
            getString(holder.itemView.context, R.string.empty_video_list),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun getItemCount(): Int {
        return videoList.count()
    }

    fun attachListener(videoListener: OnVideoListener) {
        listener = videoListener
    }

    fun updateVideos(list: List<Video>) {
        videoList = list
        notifyItemRangeChanged(0, list.size)
        Log.d("GalleryAdapter", "updateVideos called with list size: ${list.size}")
    }

    fun deleteAllVideos() {
        val size = videoList.size
        if (size == 0) return

        videoList = listOf()
        notifyItemRangeRemoved(0, size)
    }

}