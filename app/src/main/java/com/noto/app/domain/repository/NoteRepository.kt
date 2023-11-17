package com.noto.app.domain.repository

import com.noto.app.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    fun getMainNotes(): Flow<List<Note>>

    fun getMainNotesByFolderId(folderId: Long): Flow<List<Note>>

    fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>>

    fun getNoteById(noteId: Long): Flow<Note>

    suspend fun createNote(note: Note): Result<Long>

    suspend fun updateNote(note: Note): Result<Unit>

    suspend fun deleteNote(note: Note): Result<Unit>

    suspend fun clearNotes()

}