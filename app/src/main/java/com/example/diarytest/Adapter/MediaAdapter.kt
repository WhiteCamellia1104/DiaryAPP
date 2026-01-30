package com.example.diarytest.Adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.diarytest.Data.MediaItem
import com.example.diarytest.Activity.ImagePreviewActivity
import com.example.diarytest.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MediaAdapter(
    private val context: Context,
    private var mediaList: MutableList<MediaItem>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mediaThumbnail: ImageView = itemView.findViewById(R.id.mediaThumbnail)
        val mediaTypeTextView: TextView = itemView.findViewById(R.id.mediaTypeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem = mediaList[position]
        holder.mediaTypeTextView.text = mediaItem.type

        when (mediaItem.type) {
            "Image" -> {
                loadImage(holder.mediaThumbnail, mediaItem.uri)
            }
            "Video" -> {
                loadVideoThumbnail(holder.mediaThumbnail, mediaItem.uri)
            }
            "File" -> {
                holder.mediaThumbnail.setImageResource(R.drawable.ic_file)
            }
        }

        holder.itemView.setOnClickListener {
            when (mediaItem.type) {
                "Image" -> previewImage(mediaItem.uri)
                "Video" -> previewVideo(mediaItem.uri)
                "File" -> openFile(mediaItem.uri)
            }
        }

        // Set long-press listener (for deletion)
        holder.itemView.setOnLongClickListener {
            showDeleteConfirmationDialog(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    private fun loadImage(imageView: ImageView, uri: String) {
        try {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(Uri.parse(uri))
            val drawable = Drawable.createFromStream(inputStream, null)
            imageView.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
            imageView.setImageResource(R.drawable.ic_launcher_foreground) // Use a placeholder map
        }
    }


    private fun loadVideoThumbnail(imageView: ImageView, uri: String) {
        val videoUri = Uri.parse(uri)
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val thumbnail = retriever.getFrameAtTime(0) // Get the first frame as a thumbnail
            imageView.setImageBitmap(thumbnail)
        } catch (e: Exception) {
            e.printStackTrace()
            imageView.setImageResource(R.drawable.ic_video_placeholder) // Default video placeholder map
        } finally {
            retriever.release()
        }
    }

    private fun previewImage(uri: String) {
        val intent = Intent(context, ImagePreviewActivity::class.java).apply {
            putExtra("imageUri", uri)
        }
        context.startActivity(intent)
    }

    private fun previewVideo(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setDataAndType(Uri.parse(uri), "video/*")
        }
        context.startActivity(intent)
    }

    private fun openFile(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(uri), "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(context).apply {
            setTitle("remove media")
            setMessage("Are you sure you want to delete this media entry？")
            setPositiveButton("Yes") { _, _ ->
                onDeleteClick(position)
            }
            setNegativeButton("No", null)
        }.show()
    }

}
