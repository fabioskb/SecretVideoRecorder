package com.fabiosf34.secretvideorecorder.view.viewHolder

import android.widget.ImageView
import androidx.core.content.ContextCompat.getString
import androidx.recyclerview.widget.RecyclerView
import com.fabiosf34.secretvideorecorder.R
import com.fabiosf34.secretvideorecorder.databinding.RowVideoBinding
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.listeners.OnVideoListener
import com.fabiosf34.secretvideorecorder.model.utilities.ImagesUtils
import com.fabiosf34.secretvideorecorder.model.utilities.Utils

class GalleryViewHolder(private val bind: RowVideoBinding, val listener: OnVideoListener) :
    RecyclerView.ViewHolder(bind.root) {

    private val imagesUtils = ImagesUtils(itemView.context)

    fun bind(video: Video) {
        val imageView: ImageView = bind.videoImgView

        imagesUtils.loadVideoThumbnail(video.uri, imageView)

        bind.videoTextView.text = video.title

        bind.deleteImgView.setOnClickListener {
            Utils.AppUtils.dialog(
                bind.root.context,
                R.string.remove_video,
                R.string.remove_video_confirmation,
                getString(itemView.context, R.string.yes),
                getString(itemView.context, R.string.no),
                { dialog, which ->
                    listener.onDelete(
                        video.id, video.uri
                    )
                }
            )
        }

        bind.videoTextView.setOnClickListener {
            listener.onClick(video.uri)
        }

        bind.videoImgView.setOnClickListener {
            listener.onClick(video.uri)
        }

//        bind.rowLayout.setOnLongClickListener {
//            AlertDialog.Builder(itemView.context)
//                .setTitle(getString(itemView.context, R.string.remove_video))
//                .setMessage(
//                    "${
//                        getString(
//                            itemView.context,
//                            R.string.remove_video_confirmation
//                        )
//                    } ${video.title}?"
//                )
//                .setPositiveButton(
//                    getString(itemView.context, R.string.yes),
//                    object : DialogInterface.OnClickListener {
//                        override fun onClick(dialog: DialogInterface, which: Int) {
//                            listener.onDelete(
//                                video.id, video.uri
//                            )
//                        }
//                    })
//                .setNegativeButton(getString(itemView.context, R.string.no), null)
//                .create()
//                .show()
//            true
//        }
    }
}