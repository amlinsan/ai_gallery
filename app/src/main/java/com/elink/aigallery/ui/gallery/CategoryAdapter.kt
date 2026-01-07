package com.elink.aigallery.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.elink.aigallery.data.model.CategoryAlbum
import com.elink.aigallery.databinding.ItemCategoryBinding
import java.io.File

class CategoryAdapter(
    private val onClick: (CategoryAlbum) -> Unit
) : ListAdapter<CategoryAlbum, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onClick: (CategoryAlbum) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: CategoryAlbum) {
            val coverPath = category.items.firstOrNull()?.path
            if (coverPath != null) {
                binding.coverImage.load(File(coverPath)) {
                    crossfade(true)
                }
            } else {
                binding.coverImage.setImageDrawable(null)
            }
            binding.categoryName.text = category.title
            binding.categoryCount.text = category.items.size.toString()
            binding.root.setOnClickListener { onClick(category) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryAlbum>() {
            override fun areItemsTheSame(
                oldItem: CategoryAlbum,
                newItem: CategoryAlbum
            ): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(
                oldItem: CategoryAlbum,
                newItem: CategoryAlbum
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
