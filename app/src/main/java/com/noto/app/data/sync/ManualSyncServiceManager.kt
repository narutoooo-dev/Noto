package com.noto.app.data.sync

import com.noto.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ManualSyncServiceManager(
    private val folderSyncService: FolderSyncService,
    private val noteSyncService: NoteSyncService,
    private val labelSyncService: LabelSyncService,
    private val settingsRepository: SettingsRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) {

    suspend fun runManualSyncServices() = runCatching {
        withContext(coroutineDispatcher) {
            val timestamp = settingsRepository.lastSyncTimestamp.first().toString()
            folderSyncService.runManualFolderSyncService(timestamp).getOrThrow()
            noteSyncService.runManualNoteSyncService(timestamp).getOrThrow()
            labelSyncService.runManualLabelSyncService(timestamp).getOrThrow()
            noteSyncService.runManualNoteLabelSyncService(timestamp).getOrThrow()
        }
    }.onSuccess { settingsRepository.updateLastSyncTimestamp(Clock.System.now()) }

}