package com.noto.app.ui.label

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.NotoException
import com.noto.app.domain.folder.Folder
import com.noto.app.domain.folder.FolderRepository
import com.noto.app.domain.label.Label
import com.noto.app.domain.label.LabelRepository
import com.noto.app.ui.UiState
import com.noto.app.ui.component.material.TextFieldStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NewLabelViewModel(
    private val folderRepository: FolderRepository,
    private val labelRepository: LabelRepository,
    private val folderId: Long,
    private val labelId: Long,
) : ViewModel() {

    val folder = folderRepository.getFolderById(folderId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, Folder.Default)

    private val mutableLabel = MutableStateFlow(Label.Default.copy(id = labelId, folderId = folderId))
    val label get() = mutableLabel.asStateFlow()

    private val mutableTitleStatus = MutableStateFlow<TextFieldStatus>(TextFieldStatus.Empty)
    val titleStatus get() = mutableTitleStatus.asStateFlow()

    private val mutableState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val state get() = mutableState.asStateFlow()

    init {
        labelRepository.getLabelById(labelId)
            .filterNotNull()
            .onEach { mutableLabel.value = it }
            .launchIn(viewModelScope)
    }

    fun createOrUpdateLabel() = viewModelScope.launch {
        if (label.value.title.isNotBlank()) {
            mutableState.value = UiState.Loading
            if (labelId == 0L) {
                labelRepository.createLabel(label.value)
            } else {
                labelRepository.updateLabel(label.value)
            }
            mutableState.value = UiState.Success(Unit)
        } else {
            mutableState.value = UiState.Failure(NotoException.Model.TitleIsRequired)
        }
    }

    fun setTitle(title: String) {
        mutableLabel.value = label.value.copy(title = title)
    }

    fun setTitleStatus(status: TextFieldStatus) {
        mutableTitleStatus.value = status
    }

}