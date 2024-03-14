package com.noto.app.data.sync

import com.noto.app.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ManualSyncServiceManager(
    private val folderSyncService: FolderSyncService,
    private val settingsRepository: SettingsRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) {

    suspend fun runManualSyncServices() = runCatching {
        withContext(coroutineDispatcher) {
            val timestamp = settingsRepository.lastSyncTimestamp.first().toString()
            folderSyncService.runManualFolderSyncService(timestamp).getOrThrow()
        }
    }.onSuccess { settingsRepository.updateLastSyncTimestamp(Clock.System.now()) }

}