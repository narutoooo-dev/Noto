package com.noto.app.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.domain.model.*
import com.noto.app.domain.repository.*
import com.noto.app.getOrDefault
import com.noto.app.label.LabelItemModel
import com.noto.app.map
import com.noto.app.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

private const val AutoScrollDuration = 2000L

class FolderViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val labelRepository: LabelRepository,
    private val settingsRepository: SettingsRepository,
    private val vaultRepository: VaultRepository,
    private val folderId: Long,
    private val selectedNoteIds: LongArray = longArrayOf(),
) : ViewModel() {

    val folder = folderRepository.getFolderById(folderId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Folder.Default)

    private val mutableNotes = MutableStateFlow<UiState<List<NoteItemModel>>>(UiState.Loading)
    val notes get() = mutableNotes.asStateFlow()

    private val mutableArchivedNotes = MutableStateFlow<UiState<List<NoteItemModel>>>(UiState.Loading)
    val archivedNotes get() = mutableArchivedNotes.asStateFlow()

    private val mutableLabels = MutableStateFlow(emptyList<LabelItemModel>())
    val labels get() = mutableLabels.asStateFlow()

    val font = settingsRepository.font
        .stateIn(viewModelScope, SharingStarted.Lazily, Font.Nunito)

    private val mutableIsSearchEnabled = MutableStateFlow(false)
    val isSearchEnabled get() = mutableIsSearchEnabled.asStateFlow()

    private val mutableSearchTerm = MutableStateFlow("")
    val searchTerm get() = mutableSearchTerm.asStateFlow()

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val mutableIsSelection = MutableStateFlow(false)
    val isSelection get() = mutableIsSelection.asStateFlow()

    val quickExit = settingsRepository.quickExit
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var sortSelectedLabels = true

    val selectionLabels = combine(notes, labels) { notes, labels ->
        val selectedLabels = notes.getOrDefault(emptyList()).filter { it.isSelected }.map { it.note.labels }.flatten()
        labels.map { model -> LabelItemModel(model.label, selectedLabels.contains(model.label)) }
            .let {
                if (it.isNotEmpty() && sortSelectedLabels) {
                    sortSelectedLabels = false
                    it.sortedWith(SelectedLabelsComparator)
                } else {
                    it
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val mutablePreviewNotePosition = MutableStateFlow(0)
    val previewNotePosition get() = mutablePreviewNotePosition.asStateFlow()

    private var currentPosition = mutablePreviewNotePosition.value

    private var isUserScrolling = false

    val selectedNotes
        get() = notes.value
            .getOrDefault(emptyList())
            .filter { it.isSelected }
            .sortedBy { it.selectionOrder }

    val selectedArchivedNotes
        get() = archivedNotes.value
            .getOrDefault(emptyList())
            .filter { it.isSelected }
            .sortedBy { it.selectionOrder }

    private val previewAutoScroll = settingsRepository.previewAutoScroll
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        combine(
            noteRepository.getMainNotesByFolderId(folderId)
                .filterNotNull(),
            noteRepository.getArchivedNotesByFolderId(folderId)
                .filterNotNull(),
        ) { notes, archivedNotes ->
            val selectedNoteIds = selectedNoteIds.toList()
                .ifEmpty { selectedNotes.map { it.note.id } }
                .toLongArray()
            val draggedNoteIds = mutableNotes.value
                .getOrDefault(emptyList())
                .filter { it.isDragged }
                .map { it.note.id }
                .toLongArray()
            mutableNotes.value = notes.mapToNoteItemModel(selectedNoteIds, draggedNoteIds)
                .sortedBy { selectedNoteIds.indexOf(it.note.id) }
                .let { UiState.Success(it) }
            mutableArchivedNotes.value = archivedNotes.mapToNoteItemModel().let { UiState.Success(it) }
        }.launchIn(viewModelScope)

        labelRepository.getLabelsByFolderId(folderId)
            .filterNotNull()
            .map {
                it.sortedBy { it.position }.map { label ->
                    val isSelected =
                        labels.value.find { model -> model.label.id == label.id }?.isSelected
                            ?: false
                    LabelItemModel(label, isSelected)
                }
            }
            .onEach { mutableLabels.value = it }
            .launchIn(viewModelScope)

        notes
            .onEach { notesState ->
                val isNoneSelected = notesState.getOrDefault(emptyList()).none { it.isSelected }
                if (isNoneSelected) {
                    disableSelection()
                    deselectAllNotes()
                }
            }
            .launchIn(viewModelScope)

        archivedNotes
            .onEach { notesState ->
                val isNoneSelected = notesState.getOrDefault(emptyList()).none { it.isSelected }
                if (isNoneSelected) {
                    disableSelection()
                    deselectAllArchivedNotes()
                }
            }
            .launchIn(viewModelScope)

        notes.combine(previewAutoScroll) { state, isEnabled ->
            if (isEnabled) {
                val models = state.getOrDefault(emptyList()).filter { it.isSelected }
                if (models.isNotEmpty()) {
                    while (true) {
                        if (isUserScrolling) {
                            delay(1000L)
                            isUserScrolling = false
                        }
                        delay(AutoScrollDuration)
                        val nextPosition = if (currentPosition == models.lastIndex) {
                            0
                        } else {
                            currentPosition + 1
                        }
                        mutablePreviewNotePosition.value = nextPosition
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun updateFolderScrollingPosition(scrollingPosition: Int) = viewModelScope.launch {
        if (folder.value.id != 0L) {
            folderRepository.updateFolder(folder.value.copy(scrollingPosition = scrollingPosition))
        }
    }

    fun deleteFolder() = viewModelScope.launch {
        folderRepository.deleteFolder(folder.value)
        folder.value.childFolders.forEach { childFolder ->
            folderRepository.updateFolder(childFolder.copy(parentFolder = folder.value.parentFolder))
        }
    }

    fun toggleFolderIsArchived() = viewModelScope.launch {
        folderRepository.updateFolder(
            folder.value.copy(
                isArchived = !folder.value.isArchived,
                isVaulted = false,
                parentFolder = null,
            )
        )
        folder.value.childFolders.forEachRecursively { folder, _ ->
            launch {
                folderRepository.updateFolder(
                    folder.copy(
                        isArchived = !folder.isArchived,
                        isVaulted = false
                    )
                )
            }
        }
    }

    fun toggleFolderIsVaulted() = viewModelScope.launch {
        if (folder.value.isVaulted) {
            vaultRepository.removeFolderFromVault(folderId).getOrThrow()
        } else {
            vaultRepository.addFolderToVault(folderId).getOrThrow()
        }
    }

    fun toggleFolderIsPinned() = viewModelScope.launch {
        folderRepository.updateFolder(folder.value.copy(isPinned = !folder.value.isPinned))
    }

    fun updateNotePosition(note: Note, position: Int) = viewModelScope.launch {
        noteRepository.updateNote(note.copy(position = position))
    }

    fun selectLabel(id: Long) {
        mutableLabels.value = labels.value
            .map { model -> if (model.label.id == id) model.copy(isSelected = true) else model }
    }

    fun deselectLabel(id: Long) {
        mutableLabels.value = labels.value
            .map { model -> if (model.label.id == id) model.copy(isSelected = false) else model }
    }

    fun clearLabelSelection() {
        mutableLabels.value = labels.value
            .map { model -> model.copy(isSelected = false) }
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

    fun enableSelection() {
        mutableIsSelection.value = true
    }

    fun disableSelection() {
        mutableIsSelection.value = false
    }

    fun enableDragging(id: Long) {
        mutableNotes.value = notes.value.map {
            it.map { model ->
                model.copy(isSelected = false, selectionOrder = -1, isDragged = model.note.id == id)
            }
        }
    }

    fun disableDragging() {
        mutableNotes.value = notes.value.map {
            it.map { model ->
                model.copy(isDragged = false)
            }
        }
    }

    fun selectNote(id: Long) {
        mutableNotes.value = notes.value.map {
            val selectionOrder = it.maxOf { it.selectionOrder }.plus(1)
            it.map { model ->
                if (model.note.id == id)
                    model.copy(isSelected = true, selectionOrder = selectionOrder)
                else
                    model
            }
        }
    }

    fun deselectNote(id: Long) {
        mutableNotes.value = notes.value.map {
            it.map { model ->
                if (model.note.id == id)
                    model.copy(isSelected = false, selectionOrder = -1)
                else
                    model
            }
        }
    }

    fun selectAllNotes() {
        var selectionOrder = -1
        val filteredNotes = notes.value.getOrDefault(emptyList())
            .filterByLabels(labels.value.filterSelected(), folder.value.filteringType)
            .filterBySearchTerm(searchTerm.value)

        mutableNotes.value = notes.value.map {
            it.map { model ->
                model.copy(
                    isSelected = if (model.isSelected) true else filteredNotes.contains(model),
                    selectionOrder = selectionOrder++,
                )
            }
        }
    }

    fun deselectAllNotes() {
        mutableNotes.value = notes.value.map {
            it.map { model ->
                model.copy(isSelected = false, selectionOrder = -1)
            }
        }
    }

    fun mergeSelectedNotes() = viewModelScope.launch {
        val title = selectedNotes.joinToString(LineSeparator) { it.note.title }.trim()
        val body = selectedNotes.joinToString(LineSeparator) { it.note.body }.trim()
        val isPinned = selectedNotes.any { it.note.isPinned }
        val labels = selectedNotes.flatMap { it.note.labels }
        val note = Note.Default.copy(
            folderId = folderId,
            title = title,
            body = body,
            isPinned = isPinned,
            labels = labels,
        )
        noteRepository.createNote(note)
    }

    fun pinSelectedNotes() = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                noteRepository.updateNote(model.note.copy(isPinned = true))
            }
        }
    }

    fun unpinSelectedNotes() = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                noteRepository.updateNote(model.note.copy(isPinned = false))
            }
        }
    }

    fun archiveSelectedNotes() = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                noteRepository.updateNote(model.note.copy(isArchived = true, reminderDate = null))
            }
        }
    }

    fun duplicateSelectedNotes() = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                noteRepository.createNote(model.note.copy(id = 0, reminderDate = null, creationDate = Clock.System.now()))
            }
        }
    }

    fun moveSelectedNotes(folderId: Long) = viewModelScope.launch {
        selectedNotes.forEach { model ->
            // Create or get labels for the destination folder.
            val labels = model.note.labels.map { label -> label.copy(id = labelRepository.getOrCreateLabel(folderId, label)) }
            noteRepository.updateNote(model.note.copy(folderId = folderId, labels = labels))
        }
    }

    fun copySelectedNotes(folderId: Long) = viewModelScope.launch {
        selectedNotes.forEach { model ->
            // Create or get labels for the destination folder.
            val labels = model.note.labels.map { label -> label.copy(id = labelRepository.getOrCreateLabel(folderId, label)) }
            noteRepository.createNote(model.note.copy(id = 0, folderId = folderId, creationDate = Clock.System.now(), labels = labels))
        }
    }

    fun deleteSelectedNotes() = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                noteRepository.deleteNote(model.note)
            }
        }
    }

    fun setCurrentNotePosition(position: Int) {
        currentPosition = position
    }

    fun setIsUserScrolling(isScrolling: Boolean) {
        isUserScrolling = isScrolling
    }

    fun selectLabelForSelectedNotes(label: Label) = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                val selectedLabels = model.note.labels + label
                val updatedNote = model.note.copy(labels = selectedLabels)
                noteRepository.updateNote(updatedNote)
            }
        }
    }

    fun deselectLabelForSelectedNotes(label: Label) = viewModelScope.launch {
        selectedNotes.forEach { model ->
            launch {
                val selectedLabels = model.note.labels.filterNot { it.id == label.id }
                val updatedNote = model.note.copy(labels = selectedLabels)
                noteRepository.updateNote(updatedNote)
            }
        }
    }

    fun selectArchivedNote(id: Long) {
        mutableArchivedNotes.value = archivedNotes.value.map {
            val selectionOrder = it.maxOf { it.selectionOrder }.plus(1)
            it.map { model ->
                if (model.note.id == id)
                    model.copy(isSelected = true, selectionOrder = selectionOrder)
                else
                    model
            }
        }
    }

    fun deselectArchivedNote(id: Long) {
        mutableArchivedNotes.value = archivedNotes.value.map {
            it.map { model ->
                if (model.note.id == id)
                    model.copy(isSelected = false, selectionOrder = -1)
                else
                    model
            }
        }
    }

    fun deselectAllArchivedNotes() {
        mutableArchivedNotes.value = archivedNotes.value.map {
            it.map { model ->
                model.copy(isSelected = false, selectionOrder = -1)
            }
        }
    }

    fun unarchiveSelectedArchivedNotes() = viewModelScope.launch {
        selectedArchivedNotes.forEach { model ->
            launch {
                noteRepository.updateNote(model.note.copy(isArchived = false))
            }
        }
    }

    fun deleteSelectedArchivedNotes() = viewModelScope.launch {
        selectedArchivedNotes.forEach { model ->
            launch {
                noteRepository.deleteNote(model.note)
            }
        }
    }

    fun updateFolderNotesView(
        filteringType: FilteringType,
        sortingType: NoteListSortingType,
        sortingOrder: SortingOrder,
        groupingType: Grouping,
        groupingOrder: GroupingOrder,
    ) = viewModelScope.launch {
        folderRepository.updateFolder(
            folder.value.copy(
                filteringType = filteringType,
                sortingType = sortingType,
                sortingOrder = if (sortingType == NoteListSortingType.Manual) SortingOrder.Ascending else sortingOrder,
                grouping = groupingType,
                groupingOrder = groupingOrder,
            )
        )
    }

}