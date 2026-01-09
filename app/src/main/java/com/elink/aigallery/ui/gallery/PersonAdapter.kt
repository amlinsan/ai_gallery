package com.elink.aigallery.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.elink.aigallery.R
import com.elink.aigallery.data.model.PersonAlbum
import com.elink.aigallery.databinding.ItemPersonBinding
import java.io.File

class PersonAdapter(
    private val onClick: (PersonAlbum) -> Unit,
    private val onEditClick: (PersonAlbum) -> Unit
) : ListAdapter<PersonAlbum, PersonAdapter.PersonViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonViewHolder(binding, onClick, onEditClick)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PersonViewHolder(
        private val binding: ItemPersonBinding,
        private val onClick: (PersonAlbum) -> Unit,
        private val onEditClick: (PersonAlbum) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(person: PersonAlbum) {
            val coverPath = person.coverPath
            if (coverPath != null) {
                binding.personCover.load(File(coverPath)) {
                    crossfade(true)
                }
            } else {
                binding.personCover.setImageDrawable(null)
            }

            val name = if (person.name.isBlank() || person.name == UNKNOWN_NAME) {
                binding.root.context.getString(R.string.person_fallback_name, person.personId)
            } else {
                person.name
            }
            binding.personName.text = name
            binding.personCount.text = person.mediaCount.toString()
            binding.root.setOnClickListener { onClick(person) }
            binding.nameContainer.setOnClickListener {
                onEditClick(person)
            }
        }
    }

    companion object {
        private const val UNKNOWN_NAME = "Unknown"

        private val DiffCallback = object : DiffUtil.ItemCallback<PersonAlbum>() {
            override fun areItemsTheSame(oldItem: PersonAlbum, newItem: PersonAlbum): Boolean {
                return oldItem.personId == newItem.personId
            }

            override fun areContentsTheSame(oldItem: PersonAlbum, newItem: PersonAlbum): Boolean {
                return oldItem == newItem
            }
        }
    }
}
