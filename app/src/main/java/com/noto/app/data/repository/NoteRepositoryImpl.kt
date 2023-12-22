package com.noto.app.data.repository

import com.noto.app.data.model.local.LocalLabel
import com.noto.app.data.model.local.LocalNote
import com.noto.app.data.model.local.LocalNoteLabel
import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.domain.model.Note
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteNoteService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val remoteNoteService: RemoteNoteService,
    private val settingsRepository: SettingsRepository,
    private val noteMapper: NoteMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override fun getMainNotes(): Flow<List<Note>> = localNoteDataSource.getMainLocalNotes()
        .toDomainNotes(folderId = null)
        .flowOn(coroutineDispatcher)

    override fun getArchivedNotes(): Flow<List<Note>> = localNoteDataSource.getArchivedLocalNotes()
        .toDomainNotes(folderId = null)
        .flowOn(coroutineDispatcher)

    override fun getMainNotesByFolderId(folderId: Long): Flow<List<Note>> = flow {
        if (settingsRepository.isUserLoggedIn.first()) {
            val remoteFolderId = localFolderDataSource.getLocalFolderById(folderId).firstOrNull()?.remoteId
            if (remoteFolderId != null) remoteNoteService.getRemoteNotesByFolderId(remoteFolderId)
        }
        localNoteDataSource.getLocalNotesByFolderId(folderId)
            .toDomainNotes(folderId)
            .also { emitAll(it) }
    }.flowOn(coroutineDispatcher)

    override fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>> = localNoteDataSource.getArchivedLocalNotesByFolderId(folderId)
        .toDomainNotes(folderId)
        .flowOn(coroutineDispatcher)

    override fun getNoteById(noteId: Long): Flow<Note> = combine(
        localNoteDataSource.getLocalNoteById(noteId)
            .filterNotNull(),
        localLabelDataSource.getMainLocalLabels(),
        localNoteLabelDataSource.getNoteLabelsByNoteId(noteId),
    ) { note, labels, noteLabels ->
        val selectedLabels = labels.filter { it.folderId == note.folderId }
            .filteredSelected(noteLabels, note.id)
        noteMapper.mapLocalNoteToDomainNote(note, selectedLabels)
    }.flowOn(coroutineDispatcher)

    override suspend fun createNote(note: Note): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val position = getNotePosition(note.folderId)
            val positionedNote = note.copy(position = position)
            val localNote = noteMapper.mapDomainNoteToLocalNote(positionedNote)
            val localNoteId = localNoteDataSource.createLocalNote(localNote)

            val noteLabels = note.labels.map { label -> LocalNoteLabel(noteId = localNoteId, labelId = label.id) }
            noteLabels.forEach { launch { localNoteLabelDataSource.createNoteLabel(it) } }

            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.createRemoteNote(localNote.remoteId)

            localNoteId
        }
    }

    override suspend fun updateNote(note: Note) = runCatching {
        withContext(coroutineDispatcher) {
            val localNote = noteMapper.mapDomainNoteToLocalNote(note)
            localNoteDataSource.updateLocalNote(localNote)

            val databaseNoteLabels = localNoteLabelDataSource.getNoteLabelsByNoteId(note.id).first()
            val updatedNoteLabels = note.labels.map { LocalNoteLabel(noteId = note.id, labelId = it.id) }
            val isSameNoteLabels = databaseNoteLabels.map { it.copy(id = 0L) } == updatedNoteLabels

            if (!isSameNoteLabels) {
                val databaseLabelIds = databaseNoteLabels.map { it.labelId }
                val updatedLabelIds = updatedNoteLabels.map { it.labelId }

                val oldNoteLabels = databaseNoteLabels.filter { it.labelId !in updatedLabelIds }
                val newNoteLabels = updatedNoteLabels.filter { it.labelId !in databaseLabelIds }

                newNoteLabels.forEach { launch { localNoteLabelDataSource.createNoteLabel(it) } }
                oldNoteLabels.forEach { launch { localNoteLabelDataSource.deleteNoteLabel(it) } }
            }

            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.updateRemoteNote(localNote.remoteId)
        }
    }

    override suspend fun deleteNote(note: Note) = runCatching {
        withContext(coroutineDispatcher) {
            val localNote = noteMapper.mapDomainNoteToLocalNote(note)
            localNoteDataSource.deleteLocalNote(localNote)
            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.deleteRemoteNote(localNote.remoteId)
        }
    }

    override suspend fun clearNotes() = withContext(coroutineDispatcher) {
        localNoteDataSource.clearLocalNotes()
    }

    private suspend fun getNotePosition(folderId: Long) = withContext(coroutineDispatcher) {
        localNoteDataSource.getLocalNotesByFolderId(folderId)
            .filterNotNull()
            .first()
            .count()
    }

    private fun List<LocalLabel>.filteredSelected(noteLabels: List<LocalNoteLabel>, noteId: Long) = filter { label ->
        noteLabels.any { noteLabel ->
            noteLabel.labelId == label.id && noteLabel.noteId == noteId
        }
    }

    private fun Flow<List<LocalNote>>.toDomainNotes(folderId: Long?): Flow<List<Note>> = combine(
        this,
        if (folderId != null) localLabelDataSource.getLocalLabelsByFolderId(folderId) else localLabelDataSource.getMainLocalLabels(),
        localNoteLabelDataSource.getAllNoteLabels(),
    ) { notes, labels, noteLabels ->
        notes.map { note ->
            val selectedLabels = labels.filteredSelected(noteLabels, note.id)
            noteMapper.mapLocalNoteToDomainNote(note, selectedLabels)
        }
    }

}
