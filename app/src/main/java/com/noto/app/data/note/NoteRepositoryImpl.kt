package com.noto.app.data.note

import com.noto.app.data.folder.source.LocalEncryptedFolderDataSource
import com.noto.app.data.label.LabelMapper
import com.noto.app.data.label.source.LocalEncryptedLabelDataSource
import com.noto.app.data.label.source.LocalLabelDataSource
import com.noto.app.data.note.label.NoteLabelMapper
import com.noto.app.data.note.label.source.LocalEncryptedNoteLabelDataSource
import com.noto.app.data.note.label.source.LocalNoteLabelDataSource
import com.noto.app.data.note.model.LocalEncryptedNote
import com.noto.app.data.note.model.LocalNote
import com.noto.app.data.note.source.LocalEncryptedNoteDataSource
import com.noto.app.data.note.source.LocalNoteDataSource
import com.noto.app.data.note.source.RemoteNoteDataSource
import com.noto.app.domain.note.Note
import com.noto.app.domain.note.NoteRepository
import com.noto.app.domain.settings.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(
    private val localNoteDataSource: LocalNoteDataSource,
    private val localLabelDataSource: LocalLabelDataSource,
    private val localNoteLabelDataSource: LocalNoteLabelDataSource,
    private val localEncryptedFolderDataSource: LocalEncryptedFolderDataSource,
    private val localEncryptedNoteDataSource: LocalEncryptedNoteDataSource,
    private val localEncryptedLabelDataSource: LocalEncryptedLabelDataSource,
    private val localEncryptedNoteLabelDataSource: LocalEncryptedNoteLabelDataSource,
    private val remoteNoteDataSource: RemoteNoteDataSource,
    private val remoteNoteService: RemoteNoteService,
    private val settingsRepository: SettingsRepository,
    private val noteMapper: NoteMapper,
    private val labelMapper: LabelMapper,
    private val noteLabelMapper: NoteLabelMapper,
    private val coroutineDispatcher: CoroutineDispatcher,
) : NoteRepository {

    override fun getMainNotes(): Flow<List<Note>> = localNoteDataSource.getMainLocalNotes()
        .mapLocalNotesToDomainNotes(folderId = null)
        .flowOn(coroutineDispatcher)

    override fun getArchivedNotes(): Flow<List<Note>> = localNoteDataSource.getArchivedLocalNotes()
        .mapLocalNotesToDomainNotes(folderId = null)
        .flowOn(coroutineDispatcher)

    override fun getMainNotesByFolderId(folderId: Long): Flow<List<Note>> = combine(
        localNoteDataSource.getMainLocalNotesByFolderId(folderId)
            .mapLocalNotesToDomainNotes(folderId),
        localEncryptedNoteDataSource.getMainLocalEncryptedNotesByFolderId(folderId)
            .mapLocalEncryptedNotesToDomainNotes(folderId),
    ) { notes, encryptedNotes ->
        val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
        if (isLocalFolderVaulted) encryptedNotes else notes
    }.flowOn(coroutineDispatcher)

    override fun getArchivedNotesByFolderId(folderId: Long): Flow<List<Note>> = combine(
        localNoteDataSource.getArchivedLocalNotesByFolderId(folderId)
            .mapLocalNotesToDomainNotes(folderId),
        localEncryptedNoteDataSource.getArchivedLocalEncryptedNotesByFolderId(folderId)
            .mapLocalEncryptedNotesToDomainNotes(folderId),
    ) { notes, encryptedNotes ->
        val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(folderId)
        if (isLocalFolderVaulted) encryptedNotes else notes
    }.flowOn(coroutineDispatcher)

    override fun getNoteById(noteId: Long): Flow<Note> = combine(
        getNonEncryptedNoteById(noteId),
        getEncryptedNoteById(noteId),
    ) { note, encryptedNote -> note ?: encryptedNote }
        .filterNotNull().flowOn(coroutineDispatcher)

    override suspend fun createNote(note: Note): Result<Long> = runCatching {
        withContext(coroutineDispatcher) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(note.folderId)
            val position = getNotePosition(note.folderId)
            val positionedNote = note.copy(position = position)
            val localNote = noteMapper.mapDomainNoteToLocalNote(positionedNote)
            val localNoteId = if (isLocalFolderVaulted) {
                val localEncryptedNote = noteMapper.mapLocalNoteToLocalEncryptedNote(localNote)
                localEncryptedNoteDataSource.createLocalEncryptedNote(localEncryptedNote)
            } else {
                localNoteDataSource.createLocalNote(localNote)
            }

            val noteLabels = note.labels.map { label -> noteLabelMapper.mapDomainLabelToLocalNoteLabel(label, localNoteId) }
            if (isLocalFolderVaulted) {
                noteLabels.map { noteLabelMapper.mapLocalNoteLabelToLocalEncryptedNoteLabel(it) }
                    .forEach { launch { localEncryptedNoteLabelDataSource.createLocalEncryptedNoteLabel(it) } }
            } else {
                noteLabels.forEach { launch { localNoteLabelDataSource.createLocalNoteLabel(it) } }
            }

            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.createRemoteNote(localNote.remoteId)

            localNoteId
        }
    }

    override suspend fun updateNote(note: Note) = runCatching {
        withContext(coroutineDispatcher) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(note.folderId)
            val localNote = noteMapper.mapDomainNoteToLocalNote(note)
            if (isLocalFolderVaulted) {
                val localEncryptedNote = noteMapper.mapLocalNoteToLocalEncryptedNote(localNote)
                localEncryptedNoteDataSource.updateLocalEncryptedNote(localEncryptedNote)
            } else {
                localNoteDataSource.updateLocalNote(localNote)
            }

            val databaseNoteLabels = if (isLocalFolderVaulted) {
                localEncryptedNoteLabelDataSource.getLocalEncryptedNoteLabelsByNoteId(note.id).first()
                    .map { noteLabelMapper.mapLocalEncryptedNoteLabelToLocalNoteLabel(it) }
            } else {
                localNoteLabelDataSource.getLocalNoteLabelsByNoteId(note.id).first()
            }
            val updatedNoteLabels = note.labels.map { noteLabelMapper.mapDomainLabelToLocalNoteLabel(it, note.id) }
            val databaseLabelIds = databaseNoteLabels.map { it.labelId }
            val updatedLabelIds = updatedNoteLabels.map { it.labelId }
            val oldNoteLabels = databaseNoteLabels.filter { it.labelId !in updatedLabelIds }
            val newNoteLabels = updatedNoteLabels.filter { it.labelId !in databaseLabelIds }
            val oldRemoteNoteLabelIds = oldNoteLabels.map { it.remoteId }
            val newRemoteNoteLabelIds = newNoteLabels.map { it.remoteId }

            if (isLocalFolderVaulted) {
                oldNoteLabels.map { noteLabelMapper.mapLocalNoteLabelToLocalEncryptedNoteLabel(it) }
                    .forEach { launch { localEncryptedNoteLabelDataSource.deleteLocalEncryptedNoteLabel(it) } }
                newNoteLabels.map { noteLabelMapper.mapLocalNoteLabelToLocalEncryptedNoteLabel(it) }
                    .forEach { launch { localEncryptedNoteLabelDataSource.createLocalEncryptedNoteLabel(it) } }
            } else {
                oldNoteLabels.forEach { launch { localNoteLabelDataSource.deleteLocalNoteLabel(it) } }
                newNoteLabels.forEach { launch { localNoteLabelDataSource.createLocalNoteLabel(it) } }
            }

            if (settingsRepository.isUserLoggedIn.first()) {
                remoteNoteService.updateRemoteNote(localNote.remoteId, oldRemoteNoteLabelIds, newRemoteNoteLabelIds)
            }
        }
    }

    override suspend fun deleteNote(note: Note) = runCatching {
        withContext(coroutineDispatcher) {
            val isLocalFolderVaulted = localEncryptedFolderDataSource.checkIfLocalEncryptedFolderExistsById(note.folderId)
            val localNote = noteMapper.mapDomainNoteToLocalNote(note)
            if (isLocalFolderVaulted) {
                val localEncryptedNote = noteMapper.mapLocalNoteToLocalEncryptedNote(localNote)
                localEncryptedNoteDataSource.deleteLocalEncryptedNote(localEncryptedNote)
            } else {
                localNoteDataSource.deleteLocalNote(localNote)
            }
            if (settingsRepository.isUserLoggedIn.first()) remoteNoteService.deleteRemoteNote(localNote.remoteId)
        }
    }

    override suspend fun clearNotes() = withContext(coroutineDispatcher) {
        localNoteDataSource.clearLocalNotes()
        localNoteLabelDataSource.clearLocalNoteLabels()
        localEncryptedNoteDataSource.clearLocalEncryptedNotes()
        localEncryptedNoteLabelDataSource.clearLocalEncryptedNoteLabels()
    }

    private suspend fun getNotePosition(folderId: Long) = withContext(coroutineDispatcher) {
        localNoteDataSource.getMainLocalNotesByFolderId(folderId)
            .filterNotNull()
            .first()
            .count()
    }

    private fun Flow<List<LocalNote>>.mapLocalNotesToDomainNotes(folderId: Long?): Flow<List<Note>> = combine(
        this,
        if (folderId != null) localLabelDataSource.getLocalLabelsByFolderId(folderId) else localLabelDataSource.getMainLocalLabels(),
        localNoteLabelDataSource.getAllLocalNoteLabels(),
    ) { localNotes, localLabels, localNoteLabels ->
        localNotes
            .associateWith { localNote ->
                localNoteLabels
                    .filter { localNoteLabel -> localNoteLabel.noteId == localNote.id }
                    .mapNotNull { localNoteLabel -> noteLabelMapper.mapLocalNoteLabelToDomainLabel(localNoteLabel, localLabels) }
            }
            .map { noteMapper.mapLocalNoteToDomainNote(it.key, it.value) }
    }

    private fun Flow<List<LocalEncryptedNote>>.mapLocalEncryptedNotesToDomainNotes(folderId: Long?): Flow<List<Note>> = combine(
        this,
        if (folderId != null) localEncryptedLabelDataSource.getLocalEncryptedLabelsByFolderId(folderId) else localEncryptedLabelDataSource.getAllLocalEncryptedLabels(),
        localEncryptedNoteLabelDataSource.getAllLocalEncryptedNoteLabels(),
    ) { localEncryptedNotes, localEncryptedLabels, localEncryptedNoteLabels ->
        val localLabels = localEncryptedLabels.map { labelMapper.mapLocalEncryptedLabelToLocalLabel(it) }
        localEncryptedNotes
            .map { noteMapper.mapLocalEncryptedNoteToLocalNote(it) }
            .associateWith { localNote ->
                localEncryptedNoteLabels
                    .filter { localEncryptedNoteLabel -> localEncryptedNoteLabel.noteId == localNote.id }
                    .map { localEncryptedNoteLabel -> noteLabelMapper.mapLocalEncryptedNoteLabelToLocalNoteLabel(localEncryptedNoteLabel) }
                    .mapNotNull { localNoteLabel -> noteLabelMapper.mapLocalNoteLabelToDomainLabel(localNoteLabel, localLabels) }
            }
            .map { noteMapper.mapLocalNoteToDomainNote(it.key, it.value) }
    }

    private fun getEncryptedNoteById(noteId: Long): Flow<Note?> = combine(
        localEncryptedNoteDataSource.getLocalEncryptedNoteById(noteId),
        localEncryptedLabelDataSource.getAllLocalEncryptedLabels(),
        localEncryptedNoteLabelDataSource.getLocalEncryptedNoteLabelsByNoteId(noteId),
    ) { localEncryptedNote, localEncryptedLabels, localEncryptedNoteLabels ->
        val localLabels = localEncryptedLabels
            .filter { localEncryptedLabel -> localEncryptedLabel.folderId == localEncryptedNote?.folderId }
            .map { localEncryptedLabel -> labelMapper.mapLocalEncryptedLabelToLocalLabel(localEncryptedLabel) }
        val domainLabels = localEncryptedNoteLabels
            .map { localEncryptedNoteLabel -> noteLabelMapper.mapLocalEncryptedNoteLabelToLocalNoteLabel(localEncryptedNoteLabel) }
            .mapNotNull { localEncryptedNoteLabel -> noteLabelMapper.mapLocalNoteLabelToDomainLabel(localEncryptedNoteLabel, localLabels) }
        localEncryptedNote
            ?.let { noteMapper.mapLocalEncryptedNoteToLocalNote(it) }
            ?.let { noteMapper.mapLocalNoteToDomainNote(it, domainLabels) }
    }

    private fun getNonEncryptedNoteById(noteId: Long): Flow<Note?> = combine(
        localNoteDataSource.getLocalNoteById(noteId),
        localLabelDataSource.getMainLocalLabels(),
        localNoteLabelDataSource.getLocalNoteLabelsByNoteId(noteId),
    ) { localNote, localLabels, localNoteLabels ->
        val localFolderLabels = localLabels
            .filter { localLabel -> localLabel.folderId == localNote?.folderId }
        val labels = localNoteLabels
            .mapNotNull { localNoteLabel -> noteLabelMapper.mapLocalNoteLabelToDomainLabel(localNoteLabel, localFolderLabels) }
        localNote?.let { noteMapper.mapLocalNoteToDomainNote(it, labels) }
    }

}
