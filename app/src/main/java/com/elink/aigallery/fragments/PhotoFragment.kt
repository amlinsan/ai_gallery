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
import androidx.navigation.fragment.navArgs
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.databinding.FragmentPhotoBinding
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.ui.gallery.PhotoPagerAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PhotoFragment : Fragment() {

    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private val args: PhotoFragmentArgs by navArgs()
    private val viewModel: GalleryViewModel by activityViewModels {
        GalleryViewModel.Factory(MediaRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = PhotoPagerAdapter()
        binding.photoPager.adapter = adapter

        binding.btnDelete.setOnClickListener {
            val position = binding.photoPager.currentItem
            val item = adapter.currentList.getOrNull(position) ?: return@setOnClickListener
            viewModel.requestDelete(listOf(item))
        }

        var hasSetInitial = false
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPhotoList.collectLatest { items ->
                    adapter.submitList(items)
                    if (!hasSetInitial && items.isNotEmpty()) {
                        val target = args.initialPosition.coerceIn(0, items.lastIndex)
                        binding.photoPager.setCurrentItem(target, false)
                        hasSetInitial = true
                    } else if (hasSetInitial && items.isEmpty()) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
