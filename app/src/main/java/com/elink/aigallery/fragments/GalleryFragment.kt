package com.elink.aigallery.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.elink.aigallery.R
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.databinding.FragmentGalleryBinding
import com.elink.aigallery.ui.gallery.GalleryPagerAdapter
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.ui.gallery.MediaItemAdapter
import com.elink.aigallery.worker.TaggingWorkScheduler
import androidx.appcompat.app.AlertDialog
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private var hasLoadedOnce = false

    private val viewModel: GalleryViewModel by activityViewModels {
        GalleryViewModel.Factory(MediaRepository(requireContext()))
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            if (hasMediaPermissions()) {
                startScan()
                TaggingWorkScheduler.schedule(requireContext().applicationContext)
            } else {
                Toast.makeText(requireContext(), "请授予相册访问权限", Toast.LENGTH_SHORT).show()
            }
            updateContentState(hasData = false)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup ViewPager2
        val pagerAdapter = GalleryPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // Setup Floating Bar
        updateTabState(0) // Initial State
        binding.btnTabGallery.setOnClickListener {
            binding.viewPager.currentItem = 0
        }
        binding.btnTabSmart.setOnClickListener {
            binding.viewPager.currentItem = 1
        }

        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabState(position)
            }
        })

        // Setup Search Adapter
        lateinit var mediaAdapter: MediaItemAdapter
        mediaAdapter = MediaItemAdapter(
            onClick = { item ->
                val currentList = mediaAdapter.currentList
                viewModel.setCurrentPhotoList(currentList)
                val index = currentList.indexOfFirst { it.id == item.id }
                val safeIndex = if (index >= 0) index else 0
                val action = GalleryFragmentDirections.actionGalleryToPhoto()
                    .setInitialPosition(safeIndex)
                findNavController().navigate(action)
            },
            onLongClick = { item ->
                showDeleteConfirmDialog(item)
            }
        )
        
        binding.searchList.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.searchList.adapter = mediaAdapter

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.updateSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText.orEmpty())
                return true
            }
        })

        binding.requestPermissionButton.setOnClickListener {
            permissionLauncher.launch(requiredPermissions())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.folders.collect { folders ->
                        hasLoadedOnce = true
                        updateContentState(hasData = folders.isNotEmpty())
                    }
                }
                launch {
                    viewModel.searchResults.collect { results ->
                        mediaAdapter.submitList(results)
                        updateContentState(
                            hasData = true, // Simplified, we check search query in updateContentState
                            hasSearchResults = results.isNotEmpty()
                        )
                    }
                }
            }
        }

        if (hasMediaPermissions()) {
            startScan()
            TaggingWorkScheduler.schedule(requireContext().applicationContext)
        } else {
            updateContentState(hasData = false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startScan() {
        binding.loading.isVisible = true
        viewModel.scanLocalMedia()
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

    private fun requiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            else -> emptyArray()
        }
    }

    private fun hasMediaPermissions(): Boolean {
        val context = requireContext()
        return when {
            Build.VERSION.SDK_INT >= 34 -> {
                val hasImages = isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                val hasVideo = isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
                val hasSelected =
                    isGranted(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                hasImages || hasVideo || hasSelected
            }
            Build.VERSION.SDK_INT >= 33 -> {
                val hasImages = isGranted(context, Manifest.permission.READ_MEDIA_IMAGES)
                val hasVideo = isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
                hasImages || hasVideo
            }
            else -> false
        }
    }

    private fun isGranted(context: android.content.Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateContentState(
        hasData: Boolean,
        hasSearchResults: Boolean = false
    ) {
        val hasPermission = hasMediaPermissions()
        val isSearching = !binding.searchView.query.isNullOrBlank()
        
        binding.searchView.isVisible = true // Always show search in header now (design choice) or toggle based on tab? 
        // Per design, search is in header. It should probably handle both tabs.
        
        if (isSearching) {
            binding.viewPager.isVisible = false
            binding.searchList.isVisible = hasPermission && hasSearchResults
            binding.emptyState.isVisible = !hasPermission || (hasLoadedOnce && !hasSearchResults)
            binding.emptyMessage.text = if (!hasPermission) getString(R.string.gallery_permission_hint) else getString(R.string.gallery_search_empty_hint)
        } else {
            binding.searchList.isVisible = false
            binding.viewPager.isVisible = hasPermission && hasData
            binding.emptyState.isVisible = !hasPermission || (hasLoadedOnce && !hasData)
            binding.emptyMessage.text = if (!hasPermission) getString(R.string.gallery_permission_hint) else getString(R.string.gallery_empty_hint)
        }

        binding.loading.isVisible = hasPermission && !hasLoadedOnce
        binding.requestPermissionButton.isVisible = !hasPermission
    }

    private fun updateTabState(position: Int) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.icon_tint_selected)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.icon_tint_unselected)

        if (position == 0) {
            binding.btnTabGallery.setColorFilter(selectedColor)
            binding.btnTabSmart.setColorFilter(unselectedColor)
        } else {
            binding.btnTabGallery.setColorFilter(unselectedColor)
            binding.btnTabSmart.setColorFilter(selectedColor)
        }
    }
}
