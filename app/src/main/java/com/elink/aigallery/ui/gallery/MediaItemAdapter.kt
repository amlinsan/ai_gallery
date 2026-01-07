package com.elink.aigallery.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.databinding.ItemMediaBinding
import java.io.File

class MediaItemAdapter(
    private val onClick: (MediaItem) -> Unit
) : ListAdapter<MediaItem, MediaItemAdapter.MediaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MediaViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MediaViewHolder(
        private val binding: ItemMediaBinding,
        private val onClick: (MediaItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem) {
            binding.mediaImage.load(File(item.path)) {
                crossfade(true)
            }
            binding.root.setOnClickListener { onClick(item) }
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
