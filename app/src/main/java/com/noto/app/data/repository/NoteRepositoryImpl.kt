package com.noto.app.data.repository

import com.noto.app.data.model.local.LocalNote
import com.noto.app.data.model.mapper.NoteLabelMapper
import com.noto.app.data.model.mapper.NoteMapper
import com.noto.app.domain.model.Note
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteNoteService
import com.noto.app.domain.source.local.LocalLabelDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.local.LocalNoteLabelDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val remoteNoteService: RemoteNoteService,
    private val settingsRepository: SettingsRepository,
    private val noteMapper: NoteMapper,
    private val noteLabelMapper: NoteLabelMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override fun getMainNotes(): Flow<List<Note>> = localNoteDataSource.getMainLocalNotes()
        .toDomainNotes(folderId = null)
        .flowOn(coroutineDispatcher)

    override fun getArchivedNotes(): Flow<List<Note>> = localNoteDataSource.getArchivedLocalNotes()
        .toDomainNotes(folderId = null)
        .flowOn(coroutineDispatcher)

    override fun getMainNotesByFolderId(folderId: Long): Flow<List<Note>> = localNoteDataSource.getLocalNotesByFolderId(folderId)
        .toDomainNotes(folderId)
        .flowOn(coroutineDispatcher)

    override fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>> = localNoteDataSource.getArchivedLocalNotesByFolderId(folderId)
        .toDomainNotes(folderId)
        .flowOn(coroutineDispatcher)

    override fun getNoteById(noteId: Long): Flow<Note> = combine(
        localNoteDataSource.getLocalNoteById(noteId)
            .filterNotNull(),
        localLabelDataSource.getMainLocalLabels(),
        localNoteLabelDataSource.getNoteLabelsByNoteId(noteId),
    ) { localNote, localLabels, localNoteLabels ->
        val localFolderLabels = localLabels.filter { it.folderId == localNote.folderId }
        val labels = localNoteLabels.map { localNoteLabel ->
            noteLabelMapper.mapLocalNoteLabelToDomainLabel(localNoteLabel, localFolderLabels)
        }
        noteMapper.mapLocalNoteToDomainNote(localNote, labels)
    }.flowOn(coroutineDispatcher)

    override suspend fun createNote(note: Note): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val position = getNotePosition(note.folderId)
            val positionedNote = note.copy(position = position)
            val localNote = noteMapper.mapDomainNoteToLocalNote(positionedNote)
            val localNoteId = localNoteDataSource.createLocalNote(localNote)

            val noteLabels = note.labels.map { label -> noteLabelMapper.mapDomainLabelToLocalNoteLabel(label, localNoteId) }
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
            val updatedNoteLabels = note.labels.map { noteLabelMapper.mapDomainLabelToLocalNoteLabel(it, note.id) }
            val databaseLabelIds = databaseNoteLabels.map { it.labelId }
            val updatedLabelIds = updatedNoteLabels.map { it.labelId }
            val oldNoteLabels = databaseNoteLabels.filter { it.labelId !in updatedLabelIds }
            val newNoteLabels = updatedNoteLabels.filter { it.labelId !in databaseLabelIds }
            val oldRemoteNoteLabelIds = oldNoteLabels.map { it.remoteId }
            val newRemoteNoteLabelIds = newNoteLabels.map { it.remoteId }

            oldNoteLabels.forEach { launch { localNoteLabelDataSource.deleteNoteLabel(it) } }
            newNoteLabels.forEach { launch { localNoteLabelDataSource.createNoteLabel(it) } }

            if (settingsRepository.isUserLoggedIn.first()) {
                remoteNoteService.updateRemoteNote(localNote.remoteId, oldRemoteNoteLabelIds, newRemoteNoteLabelIds)
            }
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

    private fun Flow<List<LocalNote>>.toDomainNotes(folderId: Long?): Flow<List<Note>> = combine(
        this,
        if (folderId != null) localLabelDataSource.getLocalLabelsByFolderId(folderId) else localLabelDataSource.getMainLocalLabels(),
        localNoteLabelDataSource.getAllNoteLabels(),
    ) { localNotes, localLabels, localNoteLabels ->
        localNotes.map { localNote ->
            val labels = localNoteLabels.filter { it.noteId == localNote.id }
                .map { localNoteLabel -> noteLabelMapper.mapLocalNoteLabelToDomainLabel(localNoteLabel, localLabels) }
            noteMapper.mapLocalNoteToDomainNote(localNote, labels)
        }
    }

}
