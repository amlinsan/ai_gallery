package com.elink.aigallery.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elink.aigallery.R
import com.elink.aigallery.databinding.FragmentPersonGridBinding
import com.elink.aigallery.ui.gallery.GalleryViewModel
import com.elink.aigallery.ui.gallery.PersonAdapter
import kotlinx.coroutines.launch

class PersonGridFragment : Fragment() {

    private var _binding: FragmentPersonGridBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GalleryViewModel by activityViewModels {
        GalleryViewModel.Factory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = PersonAdapter(
            onClick = { person ->
                viewModel.selectPerson(person)
                findNavController().navigate(R.id.action_person_grid_to_grid)
            },
            onEditClick = { person ->
                showRenameDialog(person)
            }
        )
        binding.personList.layoutManager = LinearLayoutManager(requireContext())
        binding.personList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.personAlbums.collect { albums ->
                    adapter.submitList(albums)
                    binding.personList.isVisible = albums.isNotEmpty()
                    binding.emptyMessage.isVisible = albums.isEmpty()
                }
            }
        }
    }

    private fun showRenameDialog(person: com.elink.aigallery.data.model.PersonAlbum) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rename_input, null)
        val input = view.findViewById<android.widget.EditText>(R.id.rename_input)
        
        val currentName = if (person.name == "Unknown") "" else person.name
        input.setText(currentName)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_rename_title)
            .setView(view)
            .setPositiveButton(R.string.action_ok) { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.renamePerson(person.personId, newName)
                }
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
