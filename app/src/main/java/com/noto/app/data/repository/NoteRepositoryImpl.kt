package com.noto.app.data.repository

import com.noto.app.data.model.DomainMappers
import com.noto.app.data.model.LocalMappers
import com.noto.app.data.model.local.LocalNote
import com.noto.app.domain.model.FolderIdWithNotesCount
import com.noto.app.domain.model.Note
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.domain.service.RemoteNoteService
import com.noto.app.domain.source.local.LocalFolderDataSource
import com.noto.app.domain.source.local.LocalNoteDataSource
import com.noto.app.domain.source.remote.RemoteNoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

class NoteRepositoryImpl(
    private val localFolderDataSource: LocalFolderDataSource,
    private val localNoteDataSource: LocalNoteDataSource,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val remoteNoteService: RemoteNoteService,
    private val settingsRepository: SettingsRepository,
    private val coroutineDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> = localNoteDataSource.getAllLocalNotes()
        .map { it.map { it.toDomainNote() } }
        .flowOn(coroutineDispatcher)

    override fun getAllMainNotes(): Flow<List<Note>> = localNoteDataSource.getAllMainLocalNotes()
        .map { it.map { it.toDomainNote() } }
        .flowOn(coroutineDispatcher)

    override fun getNotesByFolderId(folderId: Long): Flow<List<Note>> = flow {
        if (settingsRepository.isUserLoggedIn.first()) {
            val remoteFolderId = localFolderDataSource.getLocalFolderById(folderId).firstOrNull()?.remoteId
            if (remoteFolderId != null) remoteNoteService.getRemoteNoteByRemoteFolderId(remoteFolderId)
        }
        localNoteDataSource.getLocalNotesByFolderId(folderId)
            .map { it.map { it.toDomainNote() } }
            .also { emitAll(it) }
    }.flowOn(coroutineDispatcher)

    override fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>> = localNoteDataSource.getArchivedLocalNotesByFolderId(folderId)
        .map { it.map { it.toDomainNote() } }
        .flowOn(coroutineDispatcher)

    override fun getNoteById(noteId: Long): Flow<Note> = localNoteDataSource.getLocalNoteById(noteId)
        .filterNotNull()
        .map { it.toDomainNote() }
        .flowOn(coroutineDispatcher)

    override fun getFolderNotesCount(): Flow<List<FolderIdWithNotesCount>> =
        localNoteDataSource.getFoldersLocalNotesCount().flowOn(coroutineDispatcher)

    override suspend fun createNote(note: Note): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val position = getNotePosition(note.folderId)
            val positionedNote = note.copy(position = position)
            val localNote = positionedNote.toLocalNote()
            val localNoteId = localNoteDataSource.createLocalNote(localNote)
            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.createRemoteNote(localNote.remoteId)
            localNoteId
        }
    }.onFailure { throw it }

    override suspend fun updateNote(note: Note) = runCatching {
        withContext(coroutineDispatcher) {
            val localNote = note.toLocalNote()
            localNoteDataSource.updateLocalNote(localNote)
            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.updateRemoteNote(localNote.remoteId)
        }
    }

    override suspend fun deleteNote(note: Note) = runCatching {
        withContext(coroutineDispatcher) {
            val localNote = note.toLocalNote()
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

    private fun LocalNote.toDomainNote(): Note {
        return Note(
            id = id,
            folderId = folderId,
            title = title,
            body = body,
            position = position,
            creationDate = LocalMappers.Instant.map(creationDate),
            isPinned = isPinned,
            isArchived = isArchived,
            reminderDate = reminderDate?.let(LocalMappers.Instant::map),
            isVaulted = isVaulted,
            accessDate = LocalMappers.Instant.map(accessDate),
            scrollingPosition = scrollingPosition,
        )
    }

    private suspend fun Note.toLocalNote(): LocalNote {
        val localNote = localNoteDataSource.getLocalNoteById(id).firstOrNull()
        val remoteId = localNote?.remoteId ?: UUID.randomUUID().toString()
        return LocalNote(
            id = id,
            remoteId = remoteId,
            folderId = folderId,
            title = title,
            body = body,
            position = position,
            creationDate = DomainMappers.Instant.map(creationDate),
            isPinned = isPinned,
            isArchived = isArchived,
            reminderDate = reminderDate?.let(DomainMappers.Instant::map),
            isVaulted = isVaulted,
            accessDate = DomainMappers.Instant.map(accessDate)!!,
            scrollingPosition = scrollingPosition,
        )
    }

}
