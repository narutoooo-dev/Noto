package com.noto.app.domain.note

import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getMainNotes(): Flow<List<Note>>

    fun getArchivedNotes(): Flow<List<Note>>

    fun getMainNotesByFolderId(folderId: Long): Flow<List<Note>>

    fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>>

    fun getNoteById(noteId: Long): Flow<Note>

    suspend fun createNote(note: Note): Result<Long>

    suspend fun updateNote(note: Note): Result<Unit>

    suspend fun deleteNote(note: Note): Result<Unit>

    suspend fun clearNotes()

}