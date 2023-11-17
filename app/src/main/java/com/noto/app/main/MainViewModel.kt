package com.noto.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.domain.model.Folder
import com.noto.app.domain.model.FolderListSortingType
import com.noto.app.domain.model.SortingOrder
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.Comparator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val sortingType = settingsRepository.sortingType
        .stateIn(viewModelScope, SharingStarted.Lazily, FolderListSortingType.CreationDate)

    val sortingOrder = settingsRepository.sortingOrder
        .stateIn(viewModelScope, SharingStarted.Lazily, SortingOrder.Descending)

    val folders = combine(
        folderRepository.getMainFolders(),
        sortingType,
        sortingOrder,
    ) { folders, sortingType, sortingOrder ->
        folders.sortedWith(Folder.Comparator(sortingOrder, sortingType))
    }
        .map { UiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    val archivedFolders = combine(
        folderRepository.getArchivedFolders(),
        sortingType,
        sortingOrder,
    ) { folders, sortingType, sortingOrder ->
        folders.sortedWith(Folder.Comparator(sortingOrder, sortingType))
    }
        .map { UiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    val vaultedFolders = combine(
        folderRepository.getVaultedFolders(),
        sortingType,
        sortingOrder,
    ) { folders, sortingType, sortingOrder ->
        folders.sortedWith(Folder.Comparator(sortingOrder, sortingType))
    }
        .map { UiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.Lazily, UiState.Loading)

    val isVaultOpen = settingsRepository.isVaultOpen
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val vaultPasscode = settingsRepository.vaultPasscode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isShowNotesCount = settingsRepository.isShowNotesCount
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val allNotes = noteRepository.getAllNotes()
        .combine(folderRepository.getAllNotVaultedFolders()) { notes, folders ->
            notes.filter { note -> folders.any { folder -> folder.id == note.folderId } }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateFoldersView(sortingType: FolderListSortingType, sortingOrder: SortingOrder) = viewModelScope.launch {
        settingsRepository.updateSortingType(sortingType)
        if (sortingType == FolderListSortingType.Manual)
            settingsRepository.updateSortingOrder(SortingOrder.Ascending)
        else
            settingsRepository.updateSortingOrder(sortingOrder)
    }

    fun updateFolderPosition(folder: Folder, position: Int) = viewModelScope.launch {
        folderRepository.updateFolder(folder.copy(position = position))
    }

    fun updateFolderParentId(folder: Folder, parentFolder: Folder?) = viewModelScope.launch {
        folderRepository.updateFolder(folder.copy(parentFolder = parentFolder))
    }

    fun openVault() = viewModelScope.launch {
        settingsRepository.updateIsVaultOpen(true)
    }

    fun closeVault() = viewModelScope.launch {
        settingsRepository.updateIsVaultOpen(false)
    }

}