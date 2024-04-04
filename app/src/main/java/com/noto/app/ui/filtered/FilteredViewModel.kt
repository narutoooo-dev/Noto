package com.noto.app.ui.filtered

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.*
import com.noto.app.domain.FilteredItem
import com.noto.app.domain.folder.Folder
import com.noto.app.domain.folder.FolderRepository
import com.noto.app.domain.note.NoteRepository
import com.noto.app.domain.settings.SettingsRepository
import com.noto.app.ui.UiState
import com.noto.app.ui.folder.NoteItemModel
import com.noto.app.ui.getOrDefault
import com.noto.app.ui.map
import com.noto.app.ui.util.Comparator
import com.noto.app.ui.util.filterBySearchTerm
import com.noto.app.ui.util.mapToNoteItemModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

typealias NotesGroupedByFolder = Map<Folder, List<NoteItemModel>>
typealias NotesGroupedByDate = Map<LocalDate, List<Pair<Folder, NoteItemModel>>>

class FilteredViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val filteredItem: FilteredItem,
) : ViewModel() {

    private val mutableNotesGroupedByFolder = MutableStateFlow<UiState<NotesGroupedByFolder>>(UiState.Loading)
    val notesGroupedByFolder get() = mutableNotesGroupedByFolder.asStateFlow()

    private val mutableNotesGroupedByDate = MutableStateFlow<UiState<NotesGroupedByDate>>(UiState.Loading)
    val notesGroupedByDate get() = mutableNotesGroupedByDate.asStateFlow()

    private val mutableNotesGroupedByFolderVisibility = MutableStateFlow(emptyMap<Folder, Boolean>())
    val notesGroupedByFolderVisibility get() = mutableNotesGroupedByFolderVisibility.asStateFlow()

    private val mutableNotesGroupedByDateVisibility = MutableStateFlow(emptyMap<LocalDate, Boolean>())
    val notesGroupedByDateVisibility get() = mutableNotesGroupedByDateVisibility.asStateFlow()

    val font = settingsRepository.font
        .stateIn(viewModelScope, SharingStarted.Lazily, Font.Nunito)

    private val mutableIsSearchEnabled = MutableStateFlow(false)
    val isSearchEnabled get() = mutableIsSearchEnabled.asStateFlow()

    private val mutableSearchTerm = MutableStateFlow("")
    val searchTerm get() = mutableSearchTerm.asStateFlow()

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val scrollingPosition = settingsRepository.getFilteredNotesScrollingPosition(filteredItem)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val quickExit = settingsRepository.quickExit
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val mutableIsSelection = MutableStateFlow(false)
    val isSelection get() = mutableIsSelection.asStateFlow()

    val selectedNotesGroupedByFolder
        get() = notesGroupedByFolder.value
            .getOrDefault(emptyMap())
            .flatMap { it.value }
            .filter { it.isSelected }
            .sortedBy { it.selectionOrder }

    init {
        combine(
            folderRepository.getMainFolders(),
            noteRepository.getMainNotes(),
            noteRepository.getArchivedNotes(),
            searchTerm,
        ) { folders, notes, archivedNotes, searchTerm ->
            when (filteredItem) {
                FilteredItem.All -> {
                    mutableNotesGroupedByFolderVisibility.value = folders.associateWith { notesGroupedByFolderVisibility.value[it] ?: true }
                    mutableNotesGroupedByFolder.value = notes
                        .mapToNoteItemModel()
                        .filterBySearchTerm(searchTerm)
                        .groupBy { model ->
                            folders.firstOrNull { folder ->
                                folder.id == model.note.folderId
                            }
                        }
                        .filterNotNullKeys()
                        .filterValues { it.isNotEmpty() }
                        .mapValues { it.value.sortedWith(NoteItemModel.Comparator(it.key.sortingOrder, it.key.sortingType)) }
                        .toList()
                        .sortedBy { it.first.position }
                        .sortedByDescending { it.first.isGeneral }
                        .toMap()
                        .let { UiState.Success(it) }
                }

                FilteredItem.Recent -> {
                    mutableNotesGroupedByDateVisibility.value = notes
                        .map { it.accessDate.toLocalDate() }
                        .associateWith { notesGroupedByDateVisibility.value[it] ?: true }
                    mutableNotesGroupedByDate.value = notes
                        .filter { note -> note.isRecent }
                        .mapToNoteItemModel()
                        .filterBySearchTerm(searchTerm)
                        .mapNotNull { model ->
                            val folder = folders.firstOrNull { it.id == model.note.folderId }
                            if (folder != null)
                                folder to model
                            else
                                null
                        }
                        .groupBy { pair -> pair.second.note.accessDate.toLocalDate() }
                        .filterValues { it.isNotEmpty() }
                        .mapValues { it.value.sortedByDescending { it.second.note.accessDate } }
                        .toSortedMap(compareByDescending { it })
                        .let { UiState.Success(it) }
                }

                FilteredItem.Scheduled -> {
                    mutableNotesGroupedByDateVisibility.value = notes
                        .mapNotNull { it.reminderDate?.toLocalDate() }
                        .associateWith { notesGroupedByDateVisibility.value[it] ?: true }
                    mutableNotesGroupedByDate.value = notes
                        .filter { note -> note.reminderDate != null }
                        .mapToNoteItemModel()
                        .filterBySearchTerm(searchTerm)
                        .mapNotNull { model ->
                            val folder = folders.firstOrNull { it.id == model.note.folderId }
                            if (folder != null)
                                folder to model
                            else
                                null
                        }
                        .groupBy { pair -> pair.second.note.accessDate.toLocalDate() }
                        .filterValues { it.isNotEmpty() }
                        .mapValues { it.value.sortedByDescending { it.second.note.accessDate } }
                        .toSortedMap(compareByDescending { it })
                        .let { UiState.Success(it) }
                }

                FilteredItem.Archived -> {
                    mutableNotesGroupedByFolderVisibility.value = folders.associateWith { notesGroupedByFolderVisibility.value[it] ?: true }
                    mutableNotesGroupedByFolder.value = archivedNotes
                        .mapToNoteItemModel()
                        .filterBySearchTerm(searchTerm)
                        .groupBy { model ->
                            folders.firstOrNull { folder ->
                                folder.id == model.note.folderId
                            }
                        }
                        .filterNotNullKeys()
                        .filterValues { it.isNotEmpty() }
                        .mapValues { it.value.sortedWith(NoteItemModel.Comparator(it.key.sortingOrder, it.key.sortingType)) }
                        .toList()
                        .sortedBy { it.first.position }
                        .sortedByDescending { it.first.isGeneral }
                        .toMap()
                        .let { UiState.Success(it) }
                }
            }
        }.launchIn(viewModelScope)

        notesGroupedByFolder
            .onEach { notesState ->
                val isNoneSelected = notesState.getOrDefault(emptyMap()).none { it.value.none { it.isSelected } }
                if (isNoneSelected) {
                    disableSelection()
                    deselectAllNotes()
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleVisibilityForFolder(folderId: Long) {
        mutableNotesGroupedByFolderVisibility.value = notesGroupedByFolderVisibility.value.mapValues {
            if (it.key.id == folderId)
                !it.value
            else
                it.value
        }
    }

    fun toggleVisibilityForDate(date: LocalDate) {
        mutableNotesGroupedByDateVisibility.value = notesGroupedByDateVisibility.value.mapValues {
            if (it.key == date)
                !it.value
            else
                it.value
        }
    }


    fun enableSearch() {
        mutableIsSearchEnabled.value = true
    }

    fun disableSearch() {
        mutableIsSearchEnabled.value = false
        setSearchTerm("")
    }

    fun setSearchTerm(searchTerm: String) {
        mutableSearchTerm.value = searchTerm
    }

    fun expandAll() {
        when (filteredItem) {
            FilteredItem.All, FilteredItem.Archived -> {
                mutableNotesGroupedByFolderVisibility.value = notesGroupedByFolderVisibility.value.mapValues { true }
            }

            FilteredItem.Recent, FilteredItem.Scheduled -> {
                mutableNotesGroupedByDateVisibility.value = notesGroupedByDateVisibility.value.mapValues { true }
            }
        }
    }

    fun collapseAll() {
        when (filteredItem) {
            FilteredItem.All, FilteredItem.Archived -> {
                mutableNotesGroupedByFolderVisibility.value = notesGroupedByFolderVisibility.value.mapValues { false }
            }

            FilteredItem.Recent, FilteredItem.Scheduled -> {
                mutableNotesGroupedByDateVisibility.value = notesGroupedByDateVisibility.value.mapValues { false }
            }
        }
    }

    fun updateScrollingPosition(scrollingPosition: Int) = viewModelScope.launch {
        settingsRepository.updateFilteredNotesScrollingPosition(filteredItem, scrollingPosition)
    }

    fun selectNote(id: Long) {
        mutableNotesGroupedByFolder.value = notesGroupedByFolder.value.map {
            it.map { entry ->
                val selectionOrder = entry.value.maxOf { it.selectionOrder }.plus(1)
                entry.key to entry.value.map { model ->
                    if (model.note.id == id)
                        model.copy(isSelected = true, selectionOrder = selectionOrder)
                    else
                        model
                }
            }.toMap()
        }
    }

    fun deselectNote(id: Long) {
        mutableNotesGroupedByFolder.value = notesGroupedByFolder.value.map {
            it.map { entry ->
                entry.key to entry.value.map { model ->
                    if (model.note.id == id)
                        model.copy(isSelected = false, selectionOrder = -1)
                    else
                        model
                }
            }.toMap()
        }
    }

    fun deselectAllNotes() {
        mutableNotesGroupedByFolder.value = notesGroupedByFolder.value.map {
            it.map { entry ->
                entry.key to entry.value.map { model ->
                    model.copy(isSelected = false, selectionOrder = -1)
                }
            }.toMap()
        }
    }

    fun unarchiveSelectedNotes() = viewModelScope.launch {
        selectedNotesGroupedByFolder.forEach { model ->
            launch {
                noteRepository.updateNote(model.note.copy(isArchived = false))
            }
        }
    }

    fun deleteSelectedNotes() = viewModelScope.launch {
        selectedNotesGroupedByFolder.forEach { model ->
            launch {
                noteRepository.deleteNote(model.note)
            }
        }
    }

    fun enableSelection() {
        mutableIsSelection.value = true
    }

    fun disableSelection() {
        mutableIsSelection.value = false
    }

}
