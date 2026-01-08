package com.elink.aigallery.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elink.aigallery.R
import com.elink.aigallery.databinding.FragmentTabContentBinding
import com.elink.aigallery.ui.gallery.CategoryAdapter
import com.elink.aigallery.ui.gallery.FolderAdapter
import com.elink.aigallery.ui.gallery.GalleryViewModel
import kotlinx.coroutines.launch

class GalleryTabFragment : Fragment() {

    private var _binding: FragmentTabContentBinding? = null
    private val binding get() = _binding!!
    private var type: Int = TYPE_FOLDERS

    private val viewModel: GalleryViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt(ARG_TYPE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.contentList.layoutManager = LinearLayoutManager(requireContext())

        if (type == TYPE_FOLDERS) {
            val adapter = FolderAdapter { folder ->
                viewModel.selectFolder(folder)
                findNavController().navigate(R.id.action_gallery_to_grid)
            }
            binding.contentList.adapter = adapter
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.folders.collect {
                        adapter.submitList(it)
                    }
                }
            }
        } else {
            val adapter = CategoryAdapter { category ->
                viewModel.selectCategory(category)
                findNavController().navigate(R.id.action_gallery_to_grid)
            }
            binding.contentList.adapter = adapter
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.categories.collect {
                        adapter.submitList(it)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TYPE_FOLDERS = 0
        const val TYPE_CATEGORIES = 1
        private const val ARG_TYPE = "type"

        fun newInstance(type: Int) = GalleryTabFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TYPE, type)
            }
        }
    }
}
