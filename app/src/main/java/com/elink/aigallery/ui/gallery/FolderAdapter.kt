package com.elink.aigallery.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.elink.aigallery.data.db.FolderWithImages
import com.elink.aigallery.databinding.ItemFolderBinding
import java.io.File

class FolderAdapter(
    private val onClick: (FolderWithImages) -> Unit
) : ListAdapter<FolderWithImages, FolderAdapter.FolderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FolderViewHolder(
        private val binding: ItemFolderBinding,
        private val onClick: (FolderWithImages) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: FolderWithImages) {
            val coverPath = folder.items.firstOrNull()?.path
            if (coverPath != null) {
                binding.coverImage.load(File(coverPath)) {
                    crossfade(true)
                }
            } else {
                binding.coverImage.setImageDrawable(null)
            }
            binding.folderName.text = folder.folderName
            binding.folderCount.text = folder.items.size.toString()
            binding.root.setOnClickListener { onClick(folder) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<FolderWithImages>() {
            override fun areItemsTheSame(
                oldItem: FolderWithImages,
                newItem: FolderWithImages
            ): Boolean {
                return oldItem.folderName == newItem.folderName
            }

            override fun areContentsTheSame(
                oldItem: FolderWithImages,
                newItem: FolderWithImages
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
