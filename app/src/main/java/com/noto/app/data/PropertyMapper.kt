package com.noto.app.data

import com.noto.app.domain.*
import kotlinx.datetime.Instant

class PropertyMapper {

    fun mapDomainNotoColorToLocalNotoColor(notoColor: NotoColor): Int {
        return notoColor.ordinal
    }

    fun mapLocalNotoColorToDomainNotoColor(localNotoColor: Int): NotoColor {
        return NotoColor.entries.first { notoColor -> notoColor.ordinal == localNotoColor }
    }

    fun mapDomainInstantToLocalInstant(instant: Instant): String {
        return instant.toString()
    }

    fun mapLocalInstantToDomainInstant(localInstant: String): Instant {
        return Instant.parse(localInstant)
    }

    fun mapDomainLayoutToLocalLayout(layout: Layout): Int {
        return layout.ordinal
    }

    fun mapLocalLayoutToDomainLayout(localLayout: Int): Layout {
        return Layout.entries.first { layoutManager -> layoutManager.ordinal == localLayout }
    }

    fun mapDomainNoteListSortingTypeToLocalNoteListSortingType(noteListSortingType: NoteListSortingType): Int {
        return noteListSortingType.ordinal
    }

    fun mapLocalNoteListSortingTypeToDomainNoteListSortingType(localNoteListSortingType: Int): NoteListSortingType {
        return NoteListSortingType.entries.first { sortingType -> sortingType.ordinal == localNoteListSortingType }
    }

    fun mapDomainSortingOrderToLocalSortingOrder(sortingOrder: SortingOrder): Int {
        return sortingOrder.ordinal
    }

    fun mapLocalSortingOrderToDomainSortingOrder(localSortingOrder: Int): SortingOrder {
        return SortingOrder.entries.first { sortingOrder -> sortingOrder.ordinal == localSortingOrder }
    }

    fun mapDomainGroupingToLocalGrouping(grouping: Grouping): Int {
        return grouping.ordinal
    }

    fun mapLocalGroupingToDomainGrouping(localGrouping: Int): Grouping {
        return Grouping.entries.first { grouping -> grouping.ordinal == localGrouping }
    }

    fun mapDomainNewNoteCursorPositionToLocalNewNoteCursorPosition(newNoteCursorPosition: NewNoteCursorPosition): Int {
        return newNoteCursorPosition.ordinal
    }

    fun mapLocalNewNoteCursorPositionToDomainNewNoteCursorPosition(localNewNoteCursorPosition: Int): NewNoteCursorPosition {
        return NewNoteCursorPosition.entries.first { position -> position.ordinal == localNewNoteCursorPosition }
    }

    fun mapDomainGroupingOrderToLocalGroupingOrder(groupingOrder: GroupingOrder): Int {
        return groupingOrder.ordinal
    }

    fun mapLocalGroupingOrderToDomainGroupingOrder(localGroupingOrder: Int): GroupingOrder {
        return GroupingOrder.entries.first { groupingOrder -> groupingOrder.ordinal == localGroupingOrder }
    }

    fun mapDomainFilteringTypeToLocalFilteringType(filteringType: FilteringType): Int {
        return filteringType.ordinal
    }

    fun mapLocalFilteringTypeToDomainFilteringType(localFilteringType: Int): FilteringType {
        return FilteringType.entries.first { filteringType -> filteringType.ordinal == localFilteringType }
    }

    fun mapDomainOpenNotesInToLocalOpenNotesIn(openNotesIn: OpenNotesIn): Int {
        return openNotesIn.ordinal
    }

    fun mapLocalOpenNotesInToDomainOpenNotesIn(localOpenNotesIn: Int): OpenNotesIn {
        return OpenNotesIn.entries.first { openNotesIn -> openNotesIn.ordinal == localOpenNotesIn }
    }

}