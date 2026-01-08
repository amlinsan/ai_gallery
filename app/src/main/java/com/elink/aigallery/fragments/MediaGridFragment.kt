package com.elink.aigallery.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.databinding.FragmentMediaGridBinding
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.ui.gallery.MediaItemAdapter
import com.elink.aigallery.R
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.launch

class MediaGridFragment : Fragment() {

    private var _binding: FragmentMediaGridBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GalleryViewModel by activityViewModels {
        GalleryViewModel.Factory(MediaRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MediaItemAdapter(
            onClick = { item ->
                val action = MediaGridFragmentDirections.actionGridToPhoto(item.path)
                findNavController().navigate(action)
            },
            onLongClick = { item ->
                showDeleteConfirmDialog(item)
            }
        )
        binding.gridList.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.gridList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedAlbumItems.collect { items ->
                    adapter.submitList(items)
                }
            }
        }
    }

    private fun showDeleteConfirmDialog(item: com.elink.aigallery.data.db.MediaItem) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.action_delete)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.requestDelete(listOf(item))
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
