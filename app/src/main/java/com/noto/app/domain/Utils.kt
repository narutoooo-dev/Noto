package com.noto.app.domain

import android.text.format.DateUtils
import com.noto.app.domain.folder.Folder
import com.noto.app.domain.label.Label
import com.noto.app.domain.label.LabelRepository
import com.noto.app.domain.note.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*
import java.text.Collator
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.days

const val LineSeparator = "\n\n"

fun String?.firstLineOrEmpty() = this?.lines()?.firstOrNull()?.trim().orEmpty()

fun String?.takeAfterFirstLineOrEmpty() = this?.lines()?.drop(1)?.joinToString("\n")?.trim().orEmpty()

fun String.takeLines(n: Int) = lines().take(n).joinToString("\n")

val CharSequence.wordsCount
    get() = if (isBlank()) 0 else split("\\s+".toRegex()).filter { it.isNotBlank() }.size

fun Note.format(): String = """
    $title
    
    $body
""".trimIndent()
    .trim()

val Note.isValid
    get() = title.isNotBlank() || body.isNotBlank()

@Suppress("FunctionName")
fun Note.Companion.Comparator(sortingOrder: SortingOrder, sortingType: NoteListSortingType): Comparator<Note> {
    val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
    val isCollatorEnabled = sortingType == NoteListSortingType.Alphabetical
    val selector: (Note) -> Comparable<*> = { note ->
        when (sortingType) {
            NoteListSortingType.Manual -> note.position
            NoteListSortingType.CreationDate -> note.creationDate
            NoteListSortingType.Alphabetical -> note.title.ifBlank { note.body }
            NoteListSortingType.AccessDate -> note.accessDate
        }
    }
    return compareByDescending<Note> { note -> note.isPinned }
        .let {
            when (sortingOrder) {
                SortingOrder.Ascending -> if (isCollatorEnabled) it.thenBy(collator, selector) else it.thenBy(selector)
                SortingOrder.Descending -> if (isCollatorEnabled) it.thenByDescending(collator, selector) else it.thenByDescending(selector)
            }
        }
}

@Suppress("DEPRECATION", "FunctionName")
fun Folder.Companion.Comparator(sortingOrder: SortingOrder, sortingType: FolderListSortingType): Comparator<Folder> {
    val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
    val isCollatorEnabled = sortingType == FolderListSortingType.Alphabetical
    val selector: (Folder) -> Comparable<*> = { pair ->
        when (sortingType) {
            FolderListSortingType.Manual -> pair.position
            FolderListSortingType.CreationDate -> pair.creationDate
            FolderListSortingType.Alphabetical -> pair.title
        }
    }
    return compareByDescending<Folder> { pair -> pair.isPinned }
        .let {
            when (sortingOrder) {
                SortingOrder.Ascending -> if (isCollatorEnabled) it.thenBy(collator, selector) else it.thenBy(selector)
                SortingOrder.Descending -> if (isCollatorEnabled) it.thenByDescending(collator, selector) else it.thenByDescending(selector)
            }
        }
}

val Note.isRecent
    get() = accessDate >= Clock.System.now().minus(7.days)

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K?, V>.filterNotNullKeys() = filterKeys { it != null } as Map<K, V>

fun Map<Label, Boolean>.filterSelected() = filterValues { it }.map { it.key }

val Folder.isGeneral
    get() = id == Folder.GeneralFolderId

fun List<Folder>.forEachRecursively(depth: Int = 1, block: (Folder, depth: Int) -> Unit) {
    forEach { entry ->
        block(entry, depth)
        entry.childFolders.forEachRecursively(depth + 1, block)
    }
}

fun List<Folder>.countRecursively(): Int {
    var count = count()
    forEach { entry ->
        count += entry.childFolders.countRecursively()
    }
    return count
}

fun List<Folder>.filterRecursively(predicate: (Folder) -> Boolean): List<Folder> {
    return filter(predicate).map {
        it.copy(childFolders = it.childFolders.filterRecursively(predicate))
    }
}

fun List<Folder>.findRecursively(predicate: (Folder) -> Boolean): Folder? {
    val item: Folder? = firstOrNull(predicate)
    if (item != null)
        return item
    else
        forEach {
            val result = it.childFolders.findRecursively(predicate)
            if (result != null)
                return result
        }
    return null
}

fun Flow<CharSequence?>.asSearchFlow() = filterNotNull()
    .map { it.trim().toString() }

suspend fun LabelRepository.getOrCreateLabel(folderId: Long, label: Label): Long {
    val folderLabels = getLabelsByFolderId(folderId).first()
    val existingLabel = folderLabels.firstOrNull { it.title == label.title }?.id
    return existingLabel ?: createLabel(label.copy(id = 0, folderId = folderId)).getOrDefault(0L)
}

fun CharSequence.indicesOf(string: String, startIndex: Int = 0, ignoreCase: Boolean = false): List<IntRange> {
    val indices = mutableListOf<IntRange>()
    var index = this.indexOf(string, startIndex, ignoreCase)
    while (index >= 0) {
        indices += index..(index + string.length)
        index = this.indexOf(string, startIndex = index + 1, ignoreCase)
    }
    return indices
}

fun CharSequence.capitalizeFirstLetter(): String {
    var isCapitalized = false
    val builder = StringBuilder(this)
    for ((index, char) in this.withIndex()) {
        if (char.isLetter()) {
            if (!isCapitalized) {
                isCapitalized = true
                builder[index] = char.uppercaseChar()
            } else {
                break
            }
        }
    }
    return builder.toString()
}

fun LocalDate.format(lowercaseTimeSpan: Boolean = false): String {
    val timeZone = TimeZone.currentSystemDefault()
    val currentInstant = Clock.System.now()
    val currentDateTime = currentInstant.toLocalDateTime(timeZone)
    return if (this.year == currentDateTime.year) {
        val format = "EEE, d MMM"
        val formattedDate = this.toJavaLocalDate().format(DateTimeFormatter.ofPattern(format))
        val milliseconds = atStartOfDayIn(timeZone).toEpochMilliseconds()
        val currentMilliseconds = currentDateTime.toInstant(timeZone).toEpochMilliseconds()
        val timeSpan = DateUtils.getRelativeTimeSpanString(milliseconds, currentMilliseconds, DateUtils.DAY_IN_MILLIS).toString()
            .let { if (lowercaseTimeSpan) it.lowercase() else it }
        when {
            this.isToday -> timeSpan
            this.isThisWeek -> "$timeSpan ($formattedDate)"
            else -> formattedDate
        }
    } else {
        val format = "EEE, d MMM yyyy"
        val formattedDate = this.toJavaLocalDate().format(DateTimeFormatter.ofPattern(format))
        formattedDate
    }
}

fun LocalTime.format(is24HourFormat: Boolean): String {
    val format = if (is24HourFormat) "HH:mm" else "h:mm a"
    val pattern = DateTimeFormatter.ofPattern(format)
    return toJavaLocalTime().format(pattern)
}

fun Instant.toLocalDate() = toLocalDateTime(TimeZone.currentSystemDefault()).date
fun Instant.toLocalTime() = toLocalDateTime(TimeZone.currentSystemDefault()).time
val LocalDate.isToday: Boolean
    get() {
        val currentDate = Clock.System.now().toLocalDate()
        return this.dayOfYear == currentDate.dayOfYear
    }

val LocalDate.isThisWeek: Boolean
    get() {
        val currentDate = Clock.System.now().toLocalDate()
        return this.dayOfYear >= currentDate.dayOfYear.minus(6)
    }

val Instant.isNow: Boolean
    get() {
        val currentInstant = Clock.System.now()
        val secondsUntilNow = this.until(currentInstant, DateTimeUnit.SECOND)
        return secondsUntilNow in 0..60
    }