package com.noto.app.ui.util

import com.noto.app.domain.*
import com.noto.app.domain.folder.Folder
import com.noto.app.domain.label.Label
import com.noto.app.domain.note.Note
import com.noto.app.ui.component.model.FolderItem
import com.noto.app.ui.folder.NoteItemModel
import com.noto.app.ui.label.LabelItemModel
import kotlinx.datetime.LocalDate
import java.text.Collator

fun List<Folder>.mapRecursivelyToFolderItem(depth: Int = 0, transform: (Folder, Int, List<FolderItem>) -> FolderItem): List<FolderItem> {
    return map { folder ->
        transform(
            folder,
            depth,
            folder.childFolders.mapRecursivelyToFolderItem(depth + 1, transform)
        )
    }
}

fun List<FolderItem>.mapRecursively(transform: (FolderItem) -> FolderItem): List<FolderItem> {
    return map {
        transform(it.copy(childItems = it.childItems.mapRecursively(transform)))
    }
}

@Suppress("FunctionName")
fun NoteItemModel.Companion.Comparator(sortingOrder: SortingOrder, sortingType: NoteListSortingType): Comparator<NoteItemModel> {
    val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
    val isCollatorEnabled = sortingType == NoteListSortingType.Alphabetical
    val selector: (NoteItemModel) -> Comparable<*> = { model ->
        when (sortingType) {
            NoteListSortingType.Manual -> model.note.position
            NoteListSortingType.CreationDate -> model.note.creationDate
            NoteListSortingType.Alphabetical -> model.note.title.ifBlank { model.note.body }
            NoteListSortingType.AccessDate -> model.note.accessDate
        }
    }
    return compareByDescending<NoteItemModel> { model -> model.note.isPinned }
        .let {
            when (sortingOrder) {
                SortingOrder.Ascending -> if (isCollatorEnabled) it.thenBy(collator, selector) else it.thenBy(selector)
                SortingOrder.Descending -> if (isCollatorEnabled) it.thenByDescending(collator, selector) else it.thenByDescending(selector)
            }
        }
}

fun List<NoteItemModel>.filterByLabels(selectedLabels: List<Label>, filteringType: FilteringType) = filter { model ->
    if (selectedLabels.isNotEmpty()) {
        when (filteringType) {
            FilteringType.Inclusive -> model.note.labels.any { label -> selectedLabels.any { it == label } }
            FilteringType.Exclusive -> model.note.labels.containsAll(selectedLabels)
            FilteringType.Strict -> model.note.labels == selectedLabels
        }
    } else {
        true
    }
}

fun List<NoteItemModel>.filterBySearchTerm(searchTerm: CharSequence) = filter { model ->
    model.note.title.contains(searchTerm, ignoreCase = true) || model.note.body.contains(searchTerm, ignoreCase = true)
}

fun List<NoteItemModel>.groupByCreationDate(
    sortingType: NoteListSortingType,
    sortingOrder: SortingOrder,
    groupingOrder: GroupingOrder,
): List<Pair<LocalDate, List<NoteItemModel>>> = groupBy { model -> model.note.creationDate.toLocalDate() }
    .sorted(sortingType, sortingOrder, groupingOrder)

fun List<NoteItemModel>.groupByAccessDate(
    sortingType: NoteListSortingType,
    sortingOrder: SortingOrder,
    groupingOrder: GroupingOrder,
): List<Pair<LocalDate, List<NoteItemModel>>> = groupBy { model -> model.note.accessDate.toLocalDate() }
    .sorted(sortingType, sortingOrder, groupingOrder)

private fun Map<LocalDate, List<NoteItemModel>>.sorted(
    sortingType: NoteListSortingType,
    sortingOrder: SortingOrder,
    groupingOrder: GroupingOrder,
): List<Pair<LocalDate, List<NoteItemModel>>> =
    mapValues { it.value.sortedWith(NoteItemModel.Comparator(sortingOrder, sortingType)).sortedByDescending { model -> model.note.isPinned } }
        .map { it.toPair() }
        .let {
            if (groupingOrder == GroupingOrder.Descending)
                it.sortedByDescending { it.first }
            else
                it.sortedBy { it.first }
        }

fun List<NoteItemModel>.groupByLabels(
    sortingType: NoteListSortingType,
    sortingOrder: SortingOrder,
    groupingOrder: GroupingOrder,
): List<Pair<List<Label>, List<NoteItemModel>>> = map { model -> model.note.labels to model.copy(note = model.note.copy(labels = emptyList())) }
    .groupBy({ it.first }, { it.second })
    .mapValues { it.value.sortedWith(NoteItemModel.Comparator(sortingOrder, sortingType)).sortedByDescending { it.note.isPinned } }
    .map { it.toPair() }
    .let {
        if (groupingOrder == GroupingOrder.Descending)
            it.sortedByDescending { it.first.firstOrNull()?.position }
        else
            it.sortedBy { it.first.firstOrNull()?.position }
    }

fun List<Note>.mapToNoteItemModel(
    selectedNoteIds: LongArray = longArrayOf(),
    draggedNoteIds: LongArray = longArrayOf(),
): List<NoteItemModel> {
    return map { note ->
        NoteItemModel(
            note.copy(labels = note.labels.sortedBy { it.position }),
            isSelected = selectedNoteIds.contains(note.id),
            isDragged = draggedNoteIds.contains(note.id),
        )
    }
}

val SelectedLabelsComparator = compareByDescending<LabelItemModel> { it.isSelected }.thenBy { it.label.position }
fun List<LabelItemModel>.filterSelected() = filter { it.isSelected }.map { it.label }
val LabelDefaultStrokeWidth = 2.dp