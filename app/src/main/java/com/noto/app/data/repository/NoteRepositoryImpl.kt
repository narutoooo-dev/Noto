package com.noto.app.data.repository

import com.noto.app.data.database.InstantConverter
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

    override suspend fun createNote(note: Note, overridePosition: Boolean) = withContext(dispatcher) {
        val position = if (overridePosition) getNotePosition(note.folderId) else note.position
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
            id,
            folderId,
            title,
            body,
            position,
            InstantConverter.toDate(creationDate)!!,
            isPinned,
            isArchived,
            InstantConverter.toDate(reminderDate),
            isVaulted,
            InstantConverter.toDate(accessDate)!!,
            scrollingPosition,
        )
    }

    private fun Note.toLocalNote(): LocalNote {
        return LocalNote(
            id,
            folderId,
            title,
            body,
            position,
            InstantConverter.toString(creationDate)!!,
            isPinned,
            isArchived,
            InstantConverter.toString(reminderDate),
            isVaulted,
            InstantConverter.toString(accessDate)!!,
            scrollingPosition,
        )
    }

}
