package com.elink.aigallery.ui.gallery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.elink.aigallery.R
import com.elink.aigallery.data.db.FolderWithImages
import com.elink.aigallery.data.db.MediaItem
import com.elink.aigallery.data.model.CategoryAlbum
import com.elink.aigallery.data.model.CategoryType
import com.elink.aigallery.data.model.PersonAlbum
import com.elink.aigallery.data.repository.MediaRepository
import com.elink.aigallery.data.repository.PersonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GalleryViewModel(
    private val repository: MediaRepository,
    private val personRepository: PersonRepository,
    private val categoryTitles: CategoryTitles
) : ViewModel() {

    val folders: StateFlow<List<FolderWithImages>> = repository.observeFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val searchQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<MediaItem>> =
        searchQuery
            .map { it.trim() }
            .distinctUntilChanged()
            .flatMapLatest { query ->
                if (query.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val mappedQuery = mapChineseToEnglish(query)
                    repository.searchImages(query, mappedQuery)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun mapChineseToEnglish(query: String): String {
        return when (query) {
            "人物" -> "person"
            "人" -> "person"
            "猫" -> "cat"
            "狗" -> "dog"
            "美食", "食物", "吃" -> "food"
            "风景", "自然" -> "nature"
            "天空" -> "sky"
            "花" -> "flower"
            "车", "汽车" -> "car"
            else -> query
        }
    }

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
                CategoryAlbum(
                    title = categoryTitles.people,
                    type = CategoryType.PEOPLE,
                    items = person
                ),
                CategoryAlbum(
                    title = categoryTitles.food,
                    type = CategoryType.FOOD,
                    items = food
                ),
                CategoryAlbum(
                    title = categoryTitles.nature,
                    type = CategoryType.NATURE,
                    items = nature
                )
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val personAlbums: StateFlow<List<PersonAlbum>> =
        personRepository.observePersonAlbums()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedAlbumItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val selectedAlbumItems: StateFlow<List<MediaItem>> = _selectedAlbumItems

    private val _selectedAlbumTitle = MutableStateFlow("")
    val selectedAlbumTitle: StateFlow<String> = _selectedAlbumTitle

    private val _currentPhotoList = MutableStateFlow<List<MediaItem>>(emptyList())
    val currentPhotoList: StateFlow<List<MediaItem>> = _currentPhotoList

    fun setCurrentPhotoList(items: List<MediaItem>) {
        _currentPhotoList.value = items.toList()
    }

    fun selectFolder(folder: FolderWithImages) {
        _selectedAlbumTitle.value = folder.folderName
        _selectedAlbumItems.value = folder.items
    }

    fun selectCategory(category: CategoryAlbum) {
        _selectedAlbumTitle.value = category.title
        _selectedAlbumItems.value = category.items
    }

    fun selectPerson(person: PersonAlbum) {
        _selectedAlbumTitle.value = person.name
        viewModelScope.launch(Dispatchers.IO) {
            val items = personRepository.getMediaByPerson(person.personId)
            withContext(Dispatchers.Main) {
                _selectedAlbumItems.value = items
            }
        }
    }

    private val _deletePendingIntent = MutableStateFlow<android.app.PendingIntent?>(null)
    val deletePendingIntent: StateFlow<android.app.PendingIntent?> = _deletePendingIntent

    private var pendingDeleteItems: List<MediaItem> = emptyList()

    fun requestDelete(items: List<MediaItem>) {
        if (items.isEmpty()) return
        pendingDeleteItems = items
        _deletePendingIntent.value = repository.createDeleteRequest(items)
    }

    fun requestDeletePath(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getMediaItemByPath(path)?.let { item ->
                requestDelete(listOf(item))
            }
        }
    }

    fun onDeleteConfirmed() {
        val itemsToDelete = pendingDeleteItems
        if (itemsToDelete.isEmpty()) {
            _deletePendingIntent.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val deleteIds = itemsToDelete.map { it.id }
            repository.deleteFromDb(deleteIds)
            val deleteIdSet = deleteIds.toSet()
            withContext(Dispatchers.Main) {
                _selectedAlbumItems.value =
                    _selectedAlbumItems.value.filterNot { deleteIdSet.contains(it.id) }
                _currentPhotoList.value =
                    _currentPhotoList.value.filterNot { deleteIdSet.contains(it.id) }
                pendingDeleteItems = emptyList()
                _deletePendingIntent.value = null
            }
        }
    }

    fun consumeDeleteIntent() {
        _deletePendingIntent.value = null
    }

    fun scanLocalMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncImages()
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    data class CategoryTitles(
        val people: String,
        val food: String,
        val nature: String
    )

    class Factory(
        context: Context
    ) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext
        private val repository = MediaRepository(appContext)
        private val personRepository = PersonRepository(appContext)
        private val categoryTitles = CategoryTitles(
            people = appContext.getString(R.string.category_people),
            food = appContext.getString(R.string.category_food),
            nature = appContext.getString(R.string.category_nature)
        )

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GalleryViewModel(repository, personRepository, categoryTitles) as T
        }
    }
}
