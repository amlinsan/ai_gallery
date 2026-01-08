package com.elink.aigallery.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.databinding.ItemPhotoPagerBinding
import java.io.File

class PhotoPagerAdapter :
    ListAdapter<MediaItem, PhotoPagerAdapter.PhotoViewHolder>(DiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoPagerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PhotoViewHolder(
        private val binding: ItemPhotoPagerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem) {
            val imageView = binding.photoItemView
            imageView.setImageDrawable(null)
            imageView.load(File(item.path)) {
                crossfade(true)
                listener(onSuccess = { _, _ ->
                    imageView.resetZoom()
                })
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
