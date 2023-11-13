package com.noto.app.data.repository

import com.noto.app.data.model.DomainMappers
import com.noto.app.data.model.LocalMappers
import com.noto.app.data.model.local.LocalNote
import com.noto.app.domain.model.FolderIdWithNotesCount
import com.noto.app.domain.model.Note
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.source.local.LocalNoteDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val dataSource: LocalNoteDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> = dataSource.getAllNotes()
        .map { it.map { it.toDomainNote() } }
        .flowOn(dispatcher)

    override fun getAllMainNotes(): Flow<List<Note>> = dataSource.getAllMainNotes()
        .map { it.map { it.toDomainNote() } }
        .flowOn(dispatcher)

    override fun getNotesByFolderId(folderId: Long): Flow<List<Note>> = dataSource.getNotesByFolderId(folderId)
        .map { it.map { it.toDomainNote() } }
        .flowOn(dispatcher)

    override fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>> = dataSource.getArchivedNotesByFolderId(folderId)
        .map { it.map { it.toDomainNote() } }
        .flowOn(dispatcher)

    override fun getNoteById(noteId: Long): Flow<Note> = dataSource.getNoteById(noteId)
        .filterNotNull()
        .map { it.toDomainNote() }
        .flowOn(dispatcher)

    override fun getFolderNotesCount(): Flow<List<FolderIdWithNotesCount>> =
        dataSource.getFoldersNotesCount().flowOn(dispatcher)

    override suspend fun createNote(note: Note) = withContext(dispatcher) {
        val position = getNotePosition(note.folderId)
        dataSource.createNote(note.copy(position = position).toLocalNote())
    }

    override suspend fun updateNote(note: Note) = withContext(dispatcher) {
        dataSource.updateNote(note.toLocalNote())
    }

    override suspend fun deleteNote(note: Note) = withContext(dispatcher) {
        dataSource.deleteNote(note.toLocalNote())
    }

    override suspend fun clearNotes() = withContext(dispatcher) {
        dataSource.clearNotes()
    }

    private suspend fun getNotePosition(folderId: Long) = withContext(dispatcher) {
        dataSource.getNotesByFolderId(folderId)
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

    private fun Note.toLocalNote(): LocalNote {
        return LocalNote(
            id = id,
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
