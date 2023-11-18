package com.noto.app.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noto.app.domain.model.Folder
import com.noto.app.domain.model.Font
import com.noto.app.domain.model.Label
import com.noto.app.domain.model.Note
import com.noto.app.domain.repository.FolderRepository
import com.noto.app.domain.repository.LabelRepository
import com.noto.app.domain.repository.NoteRepository
import com.noto.app.domain.repository.SettingsRepository
import com.noto.app.util.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

private val ExtraDatePeriod = 1.days

class NoteViewModel(
    private val folderRepository: FolderRepository,
    private val noteRepository: NoteRepository,
    private val labelRepository: LabelRepository,
    private val settingsRepository: SettingsRepository,
    private val folderId: Long,
    private val noteId: Long,
    private val body: String?,
    private var labelsIds: LongArray,
) : ViewModel() {

    private val mutableNote = MutableStateFlow(
        Note.Default.copy(
            id = noteId,
            folderId = folderId,
            title = body.firstLineOrEmpty(),
            body = body.takeAfterFirstLineOrEmpty()
        )
    )
    val note get() = mutableNote.asStateFlow()

    private val mutableTitleHistory = MutableSharedFlow<Triple<Int, Int, String>>(replay = Int.MAX_VALUE)
    val titleHistory get() = mutableTitleHistory.asSharedFlow()

    private val mutableBodyHistory = MutableSharedFlow<Triple<Int, Int, String>>(replay = Int.MAX_VALUE)
    val bodyHistory get() = mutableBodyHistory.asSharedFlow()

    private val mutableIsUndoOrRedo = MutableStateFlow(false)
    val isUndoOrRedo get() = mutableIsUndoOrRedo.asStateFlow()

    val folder = folderRepository.getFolderById(folderId)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, Folder.Default)

    val font = settingsRepository.font
        .stateIn(viewModelScope, SharingStarted.Lazily, Font.Nunito)

    val labels = labelRepository.getLabelsByFolderId(folderId)
        .filterNotNull()
        .map { it.sortedBy { label -> label.position } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isRememberScrollingPosition = settingsRepository.isRememberScrollingPosition
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val mutableIsTrackingTitleCursorPosition = MutableStateFlow(false)
    val isTrackingTitleCursorPosition get() = mutableIsTrackingTitleCursorPosition.asStateFlow()

    private val mutableIsTrackingBodyCursorPosition = MutableStateFlow(false)
    val isTrackingBodyCursorPosition get() = mutableIsTrackingBodyCursorPosition.asStateFlow()

    private var titleCursorStartPosition = 0
    private var titleCursorEndPosition = 0

    private var bodyCursorStartPosition = 0
    private var bodyCursorEndPosition = 0

    private val mutableReminderDateTime = MutableStateFlow(Clock.System.now().plus(ExtraDatePeriod))
    val reminderDateTime get() = mutableReminderDateTime.asStateFlow()

    private val mutableIsFindInNoteEnabled = MutableStateFlow(false)
    val isFindInNoteEnabled get() = mutableIsFindInNoteEnabled.asStateFlow()

    private val mutableFindInNoteTerm = MutableStateFlow("")
    val findInNoteTerm get() = mutableFindInNoteTerm.asStateFlow()

    private val mutableFindInNoteIndices = MutableStateFlow(emptyMap<IntRange, Boolean>())
    val findInNoteIndices get() = mutableFindInNoteIndices.asStateFlow()

    val continuousSearch = settingsRepository.continuousSearch
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    var isTextHighlighted: Boolean = false
        private set

    init {
        combine(
            noteRepository.getNoteById(noteId)
                .onStart { emit(note.value) }
                .filterNotNull(),
            labelRepository.getLabelsByFolderId(folderId)
                .filterNotNull(),
        ) { note, labels ->
            if (note.id == 0L) { // New note
                if (labelsIds.isNotEmpty()) { // Apply selected labels.
                    val selectedLabels = labels.filter { it.id in labelsIds }
                    mutableNote.value = note.copy(labels = selectedLabels)
                    labelsIds = longArrayOf() // Setting this value to empty array so it can be used only once.
                } else { // Copy old selected labels after the labels properties have changed.
                    val currentLabelIds = this.note.value.labels.map { it.id }
                    val selectedLabels = labels.filter { it.id in currentLabelIds }
                    mutableNote.value = note.copy(labels = selectedLabels)
                }
            } else { // Existing note; Do not apply or copy labels.
                mutableNote.value = note
            }
            if (note.reminderDate != null) mutableReminderDateTime.value = note.reminderDate
        }.launchIn(viewModelScope)
    }

    fun createOrUpdateNote(title: String, body: String, trimContent: Boolean) = viewModelScope.launch {
        val note = note.value.copy(
            title = title.takeUnless { trimContent } ?: title.trim(),
            body = body.takeUnless { trimContent } ?: body.trim(),
        )
        if (note.isValid) {
            if (note.id == 0L) {
                noteRepository.createNote(note)
                    .onSuccess { newNoteId ->
                        noteRepository.getNoteById(newNoteId)
                            .filterNotNull()
                            .onEach { createdNote -> mutableNote.value = createdNote }
                            .launchIn(viewModelScope)
                    }
            } else {
                noteRepository.updateNote(note)
            }
        }
    }

    fun deleteNote() = viewModelScope.launch {
        noteRepository.deleteNote(note.value)
    }

    fun toggleNoteIsArchived() = viewModelScope.launch {
        noteRepository.updateNote(note.value.copy(isArchived = !note.value.isArchived, reminderDate = null))
    }

    fun toggleNoteIsPinned() = viewModelScope.launch {
        noteRepository.updateNote(note.value.copy(isPinned = !note.value.isPinned))
    }

    fun setNoteReminder() = viewModelScope.launch {
        noteRepository.updateNote(note.value.copy(reminderDate = reminderDateTime.value))
    }

    fun cancelNoteReminder() = viewModelScope.launch {
        noteRepository.updateNote(note.value.copy(reminderDate = null))
    }

    fun moveNote(folderId: Long) = viewModelScope.launch {
        // Create or get labels for the destination folder.
        val labels = note.value.labels.map { label -> label.copy(id = labelRepository.getOrCreateLabel(folderId, label)) }
        noteRepository.updateNote(note.value.copy(folderId = folderId, labels = labels))
    }

    fun copyNote(folderId: Long) = viewModelScope.launch {
        // Create or get labels for the destination folder.
        val labels = note.value.labels.map { label -> label.copy(id = labelRepository.getOrCreateLabel(folderId, label)) }
        noteRepository.createNote(note.value.copy(id = 0, folderId = folderId, creationDate = Clock.System.now(), labels = labels))
    }

    fun duplicateNote() = viewModelScope.launch {
        noteRepository.createNote(note.value.copy(id = 0, reminderDate = null, creationDate = Clock.System.now()))
    }

    fun selectLabel(label: Label) = viewModelScope.launch {
        val selectedLabels = note.value.labels + label
        val updatedNote = note.value.copy(labels = selectedLabels)
        if (note.value.id == 0L) mutableNote.value = updatedNote else noteRepository.updateNote(updatedNote)
    }

    fun unselectLabel(label: Label) = viewModelScope.launch {
        val selectedLabels = note.value.labels.filterNot { it.id == label.id }
        val updatedNote = note.value.copy(labels = selectedLabels)
        if (note.value.id == 0L) mutableNote.value = updatedNote else noteRepository.updateNote(updatedNote)
    }

    fun updateNoteScrollingPosition(scrollingPosition: Int) = viewModelScope.launch {
        noteRepository.updateNote(note.value.copy(scrollingPosition = scrollingPosition))
    }

    fun updateNoteAccessDate() = viewModelScope.launch {
        noteRepository.getNoteById(noteId)
            .firstOrNull()
            ?.let { note -> noteRepository.updateNote(note.copy(accessDate = Clock.System.now())) }
    }

    fun emitNewTitleOnly(title: String) = viewModelScope.launch {
        val value = Triple(titleCursorStartPosition, titleCursorEndPosition, title)
        val isNew = bodyHistory.replayCache.none { it.third == title }
        if (isNew) mutableTitleHistory.emit(value)
        setIsTrackingTitleCursorPosition(false)
    }

    fun emitNewBodyOnly(body: String) = viewModelScope.launch {
        val value = Triple(bodyCursorStartPosition, bodyCursorEndPosition, body)
        val isNew = bodyHistory.replayCache.none { it.third == body }
        if (isNew) mutableBodyHistory.emit(value)
        setIsTrackingBodyCursorPosition(false)
    }

    fun undoTitle(): Triple<Int, Int, String> {
        val value = titleHistory.replayCache.getPreviousValueOrCurrent(note.value.title)
        setIsUndoOrRedo()
        setNoteTitle(value.third)
        return value
    }

    fun redoTitle(): Triple<Int, Int, String> {
        val value = titleHistory.replayCache.getNextValueOrCurrent(note.value.title)
        setIsUndoOrRedo()
        setNoteTitle(value.third)
        return value
    }

    fun undoBody(): Triple<Int, Int, String> {
        val value = bodyHistory.replayCache.getPreviousValueOrCurrent(note.value.body)
        setIsUndoOrRedo()
        setNoteBody(value.third)
        return value
    }

    fun redoBody(): Triple<Int, Int, String> {
        val value = bodyHistory.replayCache.getNextValueOrCurrent(note.value.body)
        setIsUndoOrRedo()
        setNoteBody(value.third)
        return value
    }

    fun setIsUndoOrRedo() {
        mutableIsUndoOrRedo.value = true
    }

    fun resetIsUndoOrRedo() {
        mutableIsUndoOrRedo.value = false
    }

    fun setNoteTitle(title: String) {
        mutableNote.value = note.value.copy(title = title)
    }

    fun setNoteBody(body: String) {
        mutableNote.value = note.value.copy(body = body)
    }

    fun setIsTrackingTitleCursorPosition(value: Boolean) {
        mutableIsTrackingTitleCursorPosition.value = value
    }

    fun setIsTrackingBodyCursorPosition(value: Boolean) {
        mutableIsTrackingBodyCursorPosition.value = value
    }

    fun setTitleCursorStartPosition(position: Int) {
        titleCursorStartPosition = position
    }

    fun setTitleCursorEndPosition(position: Int) {
        titleCursorEndPosition = position
    }

    fun setBodyCursorStartPosition(position: Int) {
        bodyCursorStartPosition = position
    }

    fun setBodyCursorEndPosition(position: Int) {
        bodyCursorEndPosition = position
    }

    fun setReminderDate(epochMilliseconds: Long) {
        val currentDateTime = reminderDateTime.value.toLocalDateTime(TimeZone.currentSystemDefault())
        val updatedDateTime = Instant.fromEpochMilliseconds(epochMilliseconds).toLocalDateTime(TimeZone.UTC).let {
            LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, currentDateTime.hour, currentDateTime.minute, it.second, it.nanosecond)
        }
        mutableReminderDateTime.value = updatedDateTime.toInstant(TimeZone.currentSystemDefault())
    }

    fun setReminderTime(hour: Int, minute: Int) {
        val currentDateTime = reminderDateTime.value.toLocalDateTime(TimeZone.currentSystemDefault())
        val updatedDateTime = currentDateTime.let {
            LocalDateTime(it.year, it.monthNumber, it.dayOfMonth, hour, minute, it.second, it.nanosecond)
        }
        mutableReminderDateTime.value = updatedDateTime.toInstant(TimeZone.currentSystemDefault())
    }

    fun enableFindInNote() {
        mutableIsFindInNoteEnabled.value = true
    }

    fun disableFindInNote() {
        mutableIsFindInNoteEnabled.value = false
        setFindInNoteTerm("", "")
    }

    fun setFindInNoteTerm(term: String, body: String) {
        val currentIndex = findInNoteIndices.value.toList().indexOfFirst { it.second }.coerceAtLeast(0)
        mutableFindInNoteTerm.value = term
        mutableFindInNoteIndices.value = if (term.isBlank()) {
            emptyMap()
        } else {
            body.indicesOf(term, ignoreCase = true)
                .mapIndexed { index, intRange -> intRange to (index == currentIndex) }
                .toMap()
        }
    }

    fun selectNextFindInNoteIndex() {
        val values = findInNoteIndices.value.toList()
        val currentIndex = values.indexOfFirst { it.second }
        val intRange = values.getOrNull(currentIndex + 1)?.first

        if (intRange != null) {
            mutableFindInNoteIndices.value = findInNoteIndices.value.map {
                it.key to (it.key == intRange)
            }.toMap()
        }
    }

    fun selectPreviousFindInNoteIndex() {
        val values = findInNoteIndices.value.toList()
        val currentIndex = values.indexOfFirst { it.second }
        val intRange = values.getOrNull(currentIndex - 1)?.first

        if (intRange != null) {
            mutableFindInNoteIndices.value = findInNoteIndices.value.map {
                it.key to (it.key == intRange)
            }.toMap()
        }
    }

    fun setIsTextHighlighted(isHighlighted: Boolean) {
        isTextHighlighted = isHighlighted
    }

    private fun List<Triple<Int, Int, String>>.getPreviousValueOrCurrent(currentValue: String): Triple<Int, Int, String> {
        val lastIndex = lastIndex.coerceAtLeast(0)
        return indexOfLast { it.third == currentValue }.minus(1).coerceIn(0, lastIndex).let(this::get)
    }

    private fun List<Triple<Int, Int, String>>.getNextValueOrCurrent(currentValue: String): Triple<Int, Int, String> {
        val lastIndex = lastIndex.coerceAtLeast(0)
        return indexOfFirst { it.third == currentValue }.plus(1).coerceIn(0, lastIndex).let(this::get)
    }
}