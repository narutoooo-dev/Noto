package com.noto.app.ui.label

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.folder.Folder
import com.noto.app.domain.folder.FolderRepository
import com.noto.app.domain.label.Label
import com.noto.app.domain.label.LabelRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LabelViewModel(
    private val folderRepository: FolderRepository,
    private val labelRepository: LabelRepository,
    private val folderId: Long,
    private val labelId: Long,
) : ViewModel() {

    val folder = folderRepository.getFolderById(folderId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, Folder.Default)

    val labels = labelRepository.getLabelsByFolderId(folderId)
        .filterNotNull()
        .map { it.sortedBy { it.position } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val label = labelRepository.getLabelById(labelId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Label.Default.copy(id = labelId, folderId = folderId))

    fun updateLabelPosition(label: Label, position: Int) = viewModelScope.launch {
        labelRepository.updateLabel(label.copy(position = position))
    }

    fun deleteLabel() = viewModelScope.launch {
        labelRepository.deleteLabel(label.value)
    }

}