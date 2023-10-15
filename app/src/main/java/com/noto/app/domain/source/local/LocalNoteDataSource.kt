package com.noto.app.domain.source.local

import com.noto.app.data.model.local.LocalNote
import com.noto.app.domain.model.FolderIdWithNotesCount
import kotlinx.coroutines.flow.Flow

interface LocalNoteDataSource {

    fun getAllNotes(): Flow<List<LocalNote>>

    fun getAllMainNotes(): Flow<List<LocalNote>>

    fun getNotesByFolderId(folderId: Long): Flow<List<LocalNote>>

    fun getArchivedNotesByFolderId(folderId: Long): Flow<List<LocalNote>>

    fun getNoteById(noteId: Long): Flow<LocalNote>

    fun getFoldersNotesCount(): Flow<List<FolderIdWithNotesCount>>

    suspend fun createNote(note: LocalNote): Long

    suspend fun updateNote(note: LocalNote)

    suspend fun deleteNote(note: LocalNote)

    suspend fun clearNotes()
}