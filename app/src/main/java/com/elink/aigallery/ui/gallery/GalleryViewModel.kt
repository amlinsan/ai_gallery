package com.elink.aigallery.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.elink.aigallery.data.db.FolderWithImages
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.data.model.CategoryAlbum
import com.elink.aigallery.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val repository: MediaRepository
) : ViewModel() {

    val folders: StateFlow<List<FolderWithImages>> = repository.observeFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val searchQuery = MutableStateFlow("")

    val searchResults: StateFlow<List<MediaItem>> =
        searchQuery
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isEmpty()) flowOf(emptyList()) else repository.searchImages(query)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val personFlow = repository.observeImagesByLabel("Person")
    private val foodFlow = repository.observeImagesByLabel("Food")
    private val natureFlow = combine(
        repository.observeImagesByLabel("Nature"),
        repository.observeImagesByLabel("Sky")
    ) { nature, sky ->
        (nature + sky).distinctBy { it.id }
    }

    val categories: StateFlow<List<CategoryAlbum>> =
        combine(personFlow, foodFlow, natureFlow) { person, food, nature ->
            listOf(
                CategoryAlbum(title = "人物", items = person),
                CategoryAlbum(title = "美食", items = food),
                CategoryAlbum(title = "风景", items = nature)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun scanLocalMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncImages()
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    class Factory(
        private val repository: MediaRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(repository) as T
        }
    }
}
