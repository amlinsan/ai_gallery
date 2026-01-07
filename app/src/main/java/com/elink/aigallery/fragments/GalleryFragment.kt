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
import androidx.recyclerview.widget.LinearLayoutManager
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.databinding.FragmentGalleryBinding
import com.elink.aigallery.ui.gallery.FolderAdapter
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.R
import kotlinx.coroutines.launch

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private var hasLoadedOnce = false

    private val viewModel: GalleryViewModel by viewModels {
        GalleryViewModel.Factory(MediaRepository(requireContext()))
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (hasMediaPermissions()) {
                startScan()
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
        val adapter = FolderAdapter { folder ->
            Toast.makeText(
                requireContext(),
                "打开文件夹：${folder.folderName}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.folderList.layoutManager = LinearLayoutManager(requireContext())
        binding.folderList.adapter = adapter
        binding.requestPermissionButton.setOnClickListener {
            permissionLauncher.launch(requiredPermissions())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.folders.collect { folders ->
                    adapter.submitList(folders)
                    hasLoadedOnce = true
                    updateContentState(hasData = folders.isNotEmpty())
                }
            }
        }

        if (hasMediaPermissions()) {
            startScan()
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

    private fun updateContentState(hasData: Boolean) {
        val hasPermission = hasMediaPermissions()
        binding.folderList.isVisible = hasPermission && hasData
        binding.loading.isVisible = hasPermission && !hasData && !hasLoadedOnce
        binding.emptyState.isVisible = !hasPermission || (hasLoadedOnce && !hasData)
        binding.requestPermissionButton.isVisible = !hasPermission
        binding.emptyMessage.text = if (hasPermission) {
            getString(R.string.gallery_empty_hint)
        } else {
            getString(R.string.gallery_permission_hint)
        }
    }
}
