package com.noto.app.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.components.material.TextFieldStatus
import com.noto.app.domain.model.*
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.toUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewFolderViewModel(private val folderRepository: FolderRepository, private val folderId: Long) : ViewModel() {

    private val mutableState = MutableStateFlow<UiState<Long>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    private val mutableFolder = MutableStateFlow(Folder.Default)
    val folder get() = mutableFolder.asStateFlow()

    private val mutableTitleStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val titleStatus get() = mutableTitleStatus.asStateFlow()

    init {
        folderRepository.getFolderById(folderId)
            .filterNotNull()
            .onEach { mutableFolder.value = it }
            .launchIn(viewModelScope)
    }

    fun createOrUpdateFolder() = viewModelScope.launch {
        val folder = folder.value
        mutableState.value = UiState.Loading
        mutableState.value = if (folder.title.isBlank() && folderId != Folder.GeneralFolderId) {
            UiState.Failure(NotoException.Model.TitleIsRequired)
        } else {
            if (folderId == 0L) {
                folderRepository.createFolder(folder)
                    .toUiState()
            } else {
                folderRepository.updateFolder(folder)
                    .map { folder.id }
                    .toUiState()
            }
        }
    }

    fun setParentFolder(parentId: Long?) = viewModelScope.launch {
        val parentFolder = if (parentId != null && parentId != 0L)
            folderRepository.getFolderById(parentId).firstOrNull()
        else
            null
        mutableFolder.value = folder.value.copy(parentFolder = parentFolder)
    }

    fun setNotoColor(notoColor: NotoColor) {
        mutableFolder.value = folder.value.copy(color = notoColor)
    }

    fun setTitle(title: String) {
        mutableFolder.value = folder.value.copy(title = title)
    }

    fun setTitleStatus(status: TextFieldStatus) {
        mutableTitleStatus.value = status
    }

    fun setLayout(layout: Layout) {
        mutableFolder.value = folder.value.copy(layout = layout)
    }

    fun setNewNoteCursorPosition(newNoteCursorPosition: NewNoteCursorPosition) {
        mutableFolder.value = folder.value.copy(newNoteCursorPosition = newNoteCursorPosition)
    }

    fun setOpenNotesIn(openNotesIn: OpenNotesIn) {
        mutableFolder.value = folder.value.copy(openNotesIn = openNotesIn)
    }

    fun setNotePreviewSize(notePreviewSize: Float) {
        mutableFolder.value = folder.value.copy(notePreviewSize = notePreviewSize.toInt())
    }

    fun toggleIsShowNoteCreationDate() {
        mutableFolder.value = folder.value.copy(isShowNoteCreationDate = !folder.value.isShowNoteCreationDate)
    }

}