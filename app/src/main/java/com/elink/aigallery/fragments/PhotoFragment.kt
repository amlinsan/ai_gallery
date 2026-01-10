package com.elink.aigallery.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.elink.aigallery.R
import com.elink.aigallery.ai.SelfieSegmenterHelper
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.databinding.BottomSheetBgReplaceBinding
import com.elink.aigallery.databinding.DialogBgReplacePreviewBinding
import com.elink.aigallery.databinding.FragmentPhotoBinding
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.ui.gallery.PhotoPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoFragment : Fragment() {

    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private val args: PhotoFragmentArgs by navArgs()
    private val viewModel: GalleryViewModel by activityViewModels {
        GalleryViewModel.Factory(requireContext())
    }
    private lateinit var photoAdapter: PhotoPagerAdapter
    private var isProcessing = false

    private val pickBackgroundImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { startBackgroundReplace(BackgroundOption.Image(it)) }
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
        photoAdapter = PhotoPagerAdapter()
        binding.photoPager.adapter = photoAdapter

        binding.btnDelete.setOnClickListener {
            val position = binding.photoPager.currentItem
            val item = photoAdapter.currentList.getOrNull(position) ?: return@setOnClickListener
            viewModel.requestDelete(listOf(item))
        }

        binding.btnBgReplace.setOnClickListener {
            if (!isProcessing) {
                showBackgroundReplaceSheet()
            }
        }

        var hasSetInitial = false
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentPhotoList.collectLatest { items ->
                    photoAdapter.submitList(items)
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

    private fun showBackgroundReplaceSheet() {
        val sheetBinding = BottomSheetBgReplaceBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(sheetBinding.root)

        sheetBinding.btnPickBgImage.setOnClickListener {
            dialog.dismiss()
            pickBackgroundImage.launch("image/*")
        }
        sheetBinding.btnPickBgColor.setOnClickListener {
            sheetBinding.bgColorTitle.isVisible = true
            sheetBinding.colorOptionRow.isVisible = true
        }

        sheetBinding.colorOptionBlue.setOnClickListener {
            dialog.dismiss()
            startBackgroundReplace(BackgroundOption.Color(requireContext().getColor(R.color.bg_replace_color_blue)))
        }
        sheetBinding.colorOptionGreen.setOnClickListener {
            dialog.dismiss()
            startBackgroundReplace(BackgroundOption.Color(requireContext().getColor(R.color.bg_replace_color_green)))
        }
        sheetBinding.colorOptionOrange.setOnClickListener {
            dialog.dismiss()
            startBackgroundReplace(BackgroundOption.Color(requireContext().getColor(R.color.bg_replace_color_orange)))
        }
        sheetBinding.colorOptionPink.setOnClickListener {
            dialog.dismiss()
            startBackgroundReplace(BackgroundOption.Color(requireContext().getColor(R.color.bg_replace_color_pink)))
        }
        sheetBinding.colorOptionGray.setOnClickListener {
            dialog.dismiss()
            startBackgroundReplace(BackgroundOption.Color(requireContext().getColor(R.color.bg_replace_color_gray)))
        }

        dialog.show()
    }

    private fun startBackgroundReplace(option: BackgroundOption) {
        val item = getCurrentItem() ?: return
        if (isProcessing) return
        isProcessing = true
        binding.bgReplaceOverlay.isVisible = true

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val foreground = decodeScaledBitmap(item.path, MAX_BG_REPLACE_EDGE)
            if (foreground == null) {
                notifyReplaceFailed()
                return@launch
            }
            val background = when (option) {
                is BackgroundOption.Color -> createSolidBackground(foreground.width, foreground.height, option.color)
                is BackgroundOption.Image -> loadBackgroundFromUri(option.uri, foreground.width, foreground.height)
            }
            if (background == null) {
                foreground.recycle()
                notifyReplaceFailed()
                return@launch
            }

            val segmenter = SelfieSegmenterHelper.getInstance(requireContext())
            val mask = segmenter.segment(foreground)
            if (mask == null) {
                foreground.recycle()
                background.recycle()
                notifyReplaceFailed()
                return@launch
            }

            val output = composeBackground(foreground, background, mask)
            foreground.recycle()
            background.recycle()

            withContext(Dispatchers.Main) {
                binding.bgReplaceOverlay.isVisible = false
                isProcessing = false
                showPreviewDialog(output)
            }
        }
    }

    private suspend fun notifyReplaceFailed() {
        withContext(Dispatchers.Main) {
            binding.bgReplaceOverlay.isVisible = false
            isProcessing = false
            Toast.makeText(requireContext(), R.string.bg_replace_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPreviewDialog(output: Bitmap) {
        val previewBinding = DialogBgReplacePreviewBinding.inflate(LayoutInflater.from(requireContext()))
        previewBinding.bgReplacePreviewImage.setImageBitmap(output)
        var consumed = false
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.bg_replace_preview_title)
            .setView(previewBinding.root)
            .setNegativeButton(R.string.action_cancel) { _, _ ->
                consumed = true
                output.recycle()
            }
            .setPositiveButton(R.string.action_save) { _, _ ->
                consumed = true
                viewModel.saveBackgroundReplacedImage(output) { success ->
                    val message = if (success) {
                        R.string.bg_replace_save_success
                    } else {
                        R.string.bg_replace_save_failed
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    output.recycle()
                }
            }
            .create()
        dialog.setOnDismissListener {
            if (!consumed) {
                output.recycle()
            }
        }
        dialog.show()
    }

    private fun getCurrentItem(): MediaItem? {
        val position = binding.photoPager.currentItem
        return photoAdapter.currentList.getOrNull(position)
    }

    private fun decodeScaledBitmap(path: String, maxEdge: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, options)
            val sampleSize = calculateInSampleSize(options, maxEdge)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(path, decodeOptions)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxEdge: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > maxEdge || width > maxEdge) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= maxEdge && halfWidth / inSampleSize >= maxEdge) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun loadBackgroundFromUri(uri: Uri, width: Int, height: Int): Bitmap? {
        return try {
            val resolver = requireContext().contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }
            val sampleSize = calculateInSampleSize(bounds, maxOf(width, height))
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            resolver.openInputStream(uri)?.use { input ->
                val original = BitmapFactory.decodeStream(input, null, decodeOptions) ?: return null
                val scaled = Bitmap.createScaledBitmap(original, width, height, true)
                if (scaled != original) {
                    original.recycle()
                }
                scaled
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun createSolidBackground(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(color)
        return bitmap
    }

    private fun composeBackground(
        foreground: Bitmap,
        background: Bitmap,
        mask: SelfieSegmenterHelper.SegmentationMask
    ): Bitmap {
        val width = foreground.width
        val height = foreground.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val fgPixels = IntArray(width * height)
        val bgPixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        foreground.getPixels(fgPixels, 0, width, 0, 0, width, height)
        background.getPixels(bgPixels, 0, width, 0, 0, width, height)

        for (y in 0 until height) {
            val maskY = (y * mask.height) / height
            for (x in 0 until width) {
                val maskX = (x * mask.width) / width
                val maskIndex = maskY * mask.width + maskX
                val alpha = mask.values[maskIndex].coerceIn(0f, 1f)

                val index = y * width + x
                val fg = fgPixels[index]
                val bg = bgPixels[index]
                val outR = ((android.graphics.Color.red(fg) * alpha) +
                    (android.graphics.Color.red(bg) * (1f - alpha))).toInt()
                val outG = ((android.graphics.Color.green(fg) * alpha) +
                    (android.graphics.Color.green(bg) * (1f - alpha))).toInt()
                val outB = ((android.graphics.Color.blue(fg) * alpha) +
                    (android.graphics.Color.blue(bg) * (1f - alpha))).toInt()
                outPixels[index] = android.graphics.Color.rgb(outR, outG, outB)
            }
        }
        result.setPixels(outPixels, 0, width, 0, 0, width, height)
        return result
    }

    private sealed class BackgroundOption {
        data class Image(val uri: Uri) : BackgroundOption()
        data class Color(val color: Int) : BackgroundOption()
    }

    companion object {
        private const val MAX_BG_REPLACE_EDGE = 1440
    }
}
