package com.noto.app.ui.util

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.noto.app.R
import com.noto.app.domain.isNow
import com.noto.app.domain.isThisWeek
import com.noto.app.domain.isToday
import com.noto.app.domain.toLocalDate
import kotlinx.datetime.*
import java.time.format.DateTimeFormatter

fun Instant.format(context: Context, includeDateTimeInThisWeek: Boolean = true): String {
    val timeZone = TimeZone.currentSystemDefault()
    val localDate = this.toLocalDate()
    val localDateTime = this.toLocalDateTime(timeZone)
    val is24HourFormat = DateFormat.is24HourFormat(context)
    val currentInstant = Clock.System.now()
    val currentDateTime = currentInstant.toLocalDateTime(timeZone)
    return if (localDateTime.year == currentDateTime.year) {
        val format = if (is24HourFormat)
            "EEE, d MMM HH:mm"
        else
            "EEE, d MMM h:mm a"
        val formattedDateTime = localDateTime.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(format))
        val milliseconds = this.toEpochMilliseconds()
        val currentMilliseconds = currentDateTime.toInstant(timeZone).toEpochMilliseconds()
        val timeSpan = DateUtils.getRelativeTimeSpanString(milliseconds, currentMilliseconds, DateUtils.SECOND_IN_MILLIS).toString()
        when {
            this.isNow -> context.stringResource(R.string.just_now)
            localDate.isToday -> timeSpan
            localDate.isThisWeek -> if (includeDateTimeInThisWeek) "$timeSpan ($formattedDateTime)" else timeSpan
            else -> formattedDateTime
        }
    } else {
        val format = if (is24HourFormat)
            "EEE, d MMM yyyy HH:mm"
        else
            "EEE, d MMM yyyy h:mm a"

        val formattedDateTime = localDateTime.toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(format))
        formattedDateTime
    }
}