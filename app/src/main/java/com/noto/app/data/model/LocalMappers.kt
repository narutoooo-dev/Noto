package com.noto.app.data.model

import com.noto.app.domain.model.*
import kotlinx.datetime.Instant

data object LocalMappers {

    val NotoColor = Mapper<Int, NotoColor> {
        com.noto.app.domain.model.NotoColor.entries.first { notoColor -> notoColor.ordinal == it }
    }
    val Instant = Mapper<String, Instant> {
        kotlinx.datetime.Instant.parse(it)
    }
    val Layout = Mapper<Int, Layout> {
        com.noto.app.domain.model.Layout.entries.first { layoutManager -> layoutManager.ordinal == it }
    }
    val NoteListSortingType = Mapper<Int, NoteListSortingType> {
        com.noto.app.domain.model.NoteListSortingType.entries.first { sortingType -> sortingType.ordinal == it }
    }
    val SortingOrder = Mapper<Int, SortingOrder> {
        com.noto.app.domain.model.SortingOrder.entries.first { sortingOrder -> sortingOrder.ordinal == it }
    }
    val Grouping = Mapper<Int, Grouping> {
        com.noto.app.domain.model.Grouping.entries.first { grouping -> grouping.ordinal == it }
    }
    val NewNoteCursorPosition = Mapper<Int, NewNoteCursorPosition> {
        com.noto.app.domain.model.NewNoteCursorPosition.entries.first { position -> position.ordinal == it }
    }
    val GroupingOrder = Mapper<Int, GroupingOrder> {
        com.noto.app.domain.model.GroupingOrder.entries.first { groupingOrder -> groupingOrder.ordinal == it }
    }
    val FilteringType = Mapper<Int, FilteringType> {
        com.noto.app.domain.model.FilteringType.entries.first { filteringType -> filteringType.ordinal == it }
    }
    val OpenNotesIn = Mapper<Int, OpenNotesIn> {
        com.noto.app.domain.model.OpenNotesIn.entries.first { openNotesIn -> openNotesIn.ordinal == it }
    }

}