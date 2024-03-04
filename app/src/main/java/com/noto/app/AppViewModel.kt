package com.noto.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.data.sync.FolderSyncService
import com.noto.app.domain.model.*
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.Constants
import com.noto.app.util.firstLineOrEmpty
import com.noto.app.util.isGeneral
import com.noto.app.util.takeAfterFirstLineOrEmpty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Use [Flow.distinctUntilChanged] with [SharedFlow] or [Flow.shareIn]
 * to update and emit only the value you specified. Otherwise, everytime you update
 * a value, it will re-emit every value to every flow that uses [Flow.shareIn].
 * */

class AppViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val folderSyncService: FolderSyncService,
) : ViewModel() {

    val userStatus = settingsRepository.userStatus
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val theme = settingsRepository.theme
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val icon = settingsRepository.icon
        .stateIn(viewModelScope, SharingStarted.Eagerly, Icon.Futuristic)

    val vaultTimeout = settingsRepository.vaultTimeout
        .stateIn(viewModelScope, SharingStarted.Eagerly, VaultTimeout.Immediately)

    /**
     * If the activity gets destroyed, a new work will be enqueued everytime the app runs. This way, we check if there has been any scheduled work before,
     * so we don't cancel an already existing one unless they don't match with [vaultTimeout] property above.
     * */
    val scheduledVaultTimeout = settingsRepository.scheduledVaultTimeout
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val isVaultOpen = settingsRepository.isVaultOpen
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val lastVersion = settingsRepository.lastVersion
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val mainInterfaceId = settingsRepository.mainInterfaceId
        .stateIn(viewModelScope, SharingStarted.Eagerly, Constants.AllFoldersId)

    val autoBackupLocation = settingsRepository.autoBackupLocation
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val autoBackupDuration = settingsRepository.autoBackupDuration
        .stateIn(viewModelScope, SharingStarted.Eagerly, AutoBackupDuration.Daily)

    val scheduledAutoBackupDuration = settingsRepository.scheduledAutoBackupDuration
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    var shouldNavigateToMainFragment = true
        private set

    val currentIcon = viewModelScope.async { settingsRepository.icon.first() }

    var currentTheme: Theme? = null
        private set

    private var syncServicesJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val quickNoteFolder = settingsRepository.quickNoteFolderId
        .flatMapConcat { folderRepository.getFolderById(it) }
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.Eagerly, Int.MAX_VALUE)

    private val mutableQuickNote = MutableStateFlow<Note?>(null)
    val quickNote get() = mutableQuickNote.asStateFlow()

    init {
        createGeneralFolder()
        vaultTimeout
            .onEach { timeout -> if (timeout == VaultTimeout.Immediately) closeVault() }
            .launchIn(viewModelScope)
    }

    fun closeVault() = viewModelScope.launch {
        settingsRepository.updateIsVaultOpen(false)
    }

    fun setScheduledVaultTimeout(vaultTimeout: VaultTimeout?) = viewModelScope.launch {
        settingsRepository.updateScheduledVaultTimeout(vaultTimeout)
    }

    fun setScheduledAutoBackupDuration(autoBackupDuration: AutoBackupDuration?) = viewModelScope.launch {
        settingsRepository.updateScheduledAutoBackupDuration(autoBackupDuration)
    }

    private fun createGeneralFolder() = viewModelScope.launch {
        combine(
            userStatus
                .map { it != UserStatus.New }
                .distinctUntilChanged(),
            folderRepository.getMainFolders()
                .map { it.firstOrNull { folder -> folder.isGeneral } == null }
                .distinctUntilChanged()
        ) { isIntroFinished, isGeneralFolderNotCreated ->
            if (isIntroFinished && isGeneralFolderNotCreated) folderRepository.createGeneralFolder()
        }.launchIn(viewModelScope)
    }

    fun setShouldNavigateToMainFragment(value: Boolean) {
        shouldNavigateToMainFragment = value
    }

    fun setCurrentTheme(theme: Theme) {
        currentTheme = theme
    }

    fun createQuickNote(content: String) = viewModelScope.launch {
        val title = content.firstLineOrEmpty()
        val body = content.takeAfterFirstLineOrEmpty()
        val folderId = quickNoteFolder.first().id
        val note = Note.Default.copy(folderId = folderId, title = title, body = body)
        noteRepository.createNote(note).onSuccess(::setQuickNote)
    }

    fun setQuickNote(noteId: Long) = viewModelScope.launch {
        mutableQuickNote.value = noteRepository.getNoteById(noteId).first()
    }

    fun startSyncServices() {
        syncServicesJob = viewModelScope.launch {
            folderSyncService.startFolderSyncService()
        }
    }

    fun stopSyncServices() {
        syncServicesJob?.cancel()
        viewModelScope.launch {
            folderSyncService.stopFolderSyncService()
        }
    }

}