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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.elink.aigallery.R
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.databinding.FragmentGalleryBinding
import com.elink.aigallery.ui.gallery.CategoryAdapter
import com.elink.aigallery.ui.gallery.FolderAdapter
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.ui.gallery.MediaItemAdapter
import com.elink.aigallery.worker.TaggingWorkScheduler
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private var hasLoadedOnce = false
    private var currentTab = TAB_FOLDERS

    private val viewModel: GalleryViewModel by viewModels {
        GalleryViewModel.Factory(MediaRepository(requireContext()))
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
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
        val folderAdapter = FolderAdapter { folder ->
            Toast.makeText(
                requireContext(),
                "打开文件夹：${folder.folderName}",
                Toast.LENGTH_SHORT
            ).show()
        }

        val mediaAdapter = MediaItemAdapter {
            Toast.makeText(requireContext(), "打开图片", Toast.LENGTH_SHORT).show()
        }

        val categoryAdapter = CategoryAdapter { category ->
            Toast.makeText(
                requireContext(),
                "打开分类：${category.title}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.contentList.layoutManager = LinearLayoutManager(requireContext())
        binding.contentList.adapter = folderAdapter
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.updateSearchQuery(query.orEmpty())
                updateAdapters(folderAdapter, mediaAdapter, categoryAdapter)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.updateSearchQuery(newText.orEmpty())
                updateAdapters(folderAdapter, mediaAdapter, categoryAdapter)
                return true
            }
        })

        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(R.string.gallery_tab_folders),
            true
        )
        binding.tabLayout.addTab(
            binding.tabLayout.newTab().setText(R.string.gallery_tab_categories)
        )
        binding.tabLayout.addOnTabSelectedListener(object :
            com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                currentTab = if (tab.position == 1) TAB_CATEGORIES else TAB_FOLDERS
                updateAdapters(folderAdapter, mediaAdapter, categoryAdapter)
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}

            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        binding.requestPermissionButton.setOnClickListener {
            permissionLauncher.launch(requiredPermissions())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.folders.collect { folders ->
                        folderAdapter.submitList(folders)
                        hasLoadedOnce = true
                        updateContentState(
                            hasData = folders.isNotEmpty(),
                            hasSearchResults = mediaAdapter.currentList.isNotEmpty(),
                            hasCategories = categoryAdapter.currentList.isNotEmpty()
                        )
                    }
                }
                launch {
                    viewModel.searchResults.collect { results ->
                        mediaAdapter.submitList(results)
                        updateContentState(
                            hasData = folderAdapter.currentList.isNotEmpty(),
                            hasSearchResults = results.isNotEmpty(),
                            hasCategories = categoryAdapter.currentList.isNotEmpty()
                        )
                    }
                }
                launch {
                    viewModel.categories.collect { categories ->
                        categoryAdapter.submitList(categories)
                        updateContentState(
                            hasData = folderAdapter.currentList.isNotEmpty(),
                            hasSearchResults = mediaAdapter.currentList.isNotEmpty(),
                            hasCategories = categories.isNotEmpty()
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

        updateAdapters(folderAdapter, mediaAdapter, categoryAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun startScan() {
        binding.loading.isVisible = true
        viewModel.scanLocalMedia()
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
            else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
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
                (hasImages && hasVideo) || hasSelected
            }
            Build.VERSION.SDK_INT >= 33 -> {
                isGranted(context, Manifest.permission.READ_MEDIA_IMAGES) &&
                    isGranted(context, Manifest.permission.READ_MEDIA_VIDEO)
            }
            else -> isGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
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
        hasSearchResults: Boolean = false,
        hasCategories: Boolean = false
    ) {
        val hasPermission = hasMediaPermissions()
        val showSearch = currentTab == TAB_FOLDERS
        binding.searchView.isVisible = showSearch

        val shouldShowContent = when (currentTab) {
            TAB_CATEGORIES -> hasPermission && hasCategories
            else -> if (binding.searchView.query.isNullOrBlank()) {
                hasPermission && hasData
            } else {
                hasPermission && hasSearchResults
            }
        }

        binding.contentList.isVisible = shouldShowContent
        binding.loading.isVisible = hasPermission && !hasLoadedOnce
        binding.emptyState.isVisible = !hasPermission || (hasLoadedOnce && !shouldShowContent)
        binding.requestPermissionButton.isVisible = !hasPermission
        binding.emptyMessage.text = when {
            !hasPermission -> getString(R.string.gallery_permission_hint)
            currentTab == TAB_CATEGORIES -> getString(R.string.gallery_category_empty_hint)
            binding.searchView.query.isNullOrBlank() -> getString(R.string.gallery_empty_hint)
            else -> getString(R.string.gallery_search_empty_hint)
        }
    }

    private fun updateAdapters(
        folderAdapter: FolderAdapter,
        mediaAdapter: MediaItemAdapter,
        categoryAdapter: CategoryAdapter
    ) {
        when (currentTab) {
            TAB_CATEGORIES -> {
                binding.contentList.layoutManager = LinearLayoutManager(requireContext())
                binding.contentList.adapter = categoryAdapter
            }
            else -> {
                val query = binding.searchView.query?.toString().orEmpty()
                if (query.isBlank()) {
                    binding.contentList.layoutManager = LinearLayoutManager(requireContext())
                    binding.contentList.adapter = folderAdapter
                } else {
                    binding.contentList.layoutManager = GridLayoutManager(requireContext(), 3)
                    binding.contentList.adapter = mediaAdapter
                }
            }
        }
        updateContentState(
            hasData = folderAdapter.currentList.isNotEmpty(),
            hasSearchResults = mediaAdapter.currentList.isNotEmpty(),
            hasCategories = categoryAdapter.currentList.isNotEmpty()
        )
    }

    companion object {
        private const val TAB_FOLDERS = 0
        private const val TAB_CATEGORIES = 1
    }
}
