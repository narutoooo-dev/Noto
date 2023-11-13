package com.noto.app.data.model

import com.noto.app.domain.model.*
import kotlinx.datetime.Instant

data object DomainMappers {

    val NotoColor = Mapper<NotoColor, Int> { it.ordinal }
    val Instant = Mapper<Instant, String> { it.toString() }
    val Layout = Mapper<Layout, Int> { it.ordinal }
    val NoteListSortingType = Mapper<NoteListSortingType, Int> { it.ordinal }
    val SortingOrder = Mapper<SortingOrder, Int> { it.ordinal }
    val Grouping = Mapper<Grouping, Int> { it.ordinal }
    val NewNoteCursorPosition = Mapper<NewNoteCursorPosition, Int> { it.ordinal }
    val GroupingOrder = Mapper<GroupingOrder, Int> { it.ordinal }
    val FilteringType = Mapper<FilteringType, Int> { it.ordinal }
    val OpenNotesIn = Mapper<OpenNotesIn, Int> { it.ordinal }

}