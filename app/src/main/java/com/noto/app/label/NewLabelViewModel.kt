package com.noto.app.label

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.UiState
import com.noto.app.components.TextFieldStatus
import com.noto.app.domain.model.Folder
import com.noto.app.domain.model.Label
import com.noto.app.domain.model.NotoException
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.LabelRepository
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