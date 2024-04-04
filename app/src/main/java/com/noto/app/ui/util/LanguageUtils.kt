package com.noto.app.ui.util

import android.content.Context
import androidx.core.os.LocaleListCompat
import com.noto.app.domain.Language
import java.text.Collator
import java.util.Locale

fun Language.Companion.Comparator(context: Context): Comparator<Language> {
    val collator = Collator.getInstance().apply { strength = Collator.PRIMARY }
    return compareByDescending<Language> { it == Language.System }
        .thenBy { it in Language.Deprecated }
        .thenBy(collator) { it ->
            val localizedContext = context.localize(it)
            localizedContext.stringResource(it.toStringResourceId())
        }
}

@Suppress("DEPRECATION")
fun Language.toLocale(): Locale = when (this) {
    Language.System -> Locale.getDefault()
    Language.English -> Locale("en")
    Language.Turkish -> Locale("tr")
    Language.Arabic -> Locale("ar")
    Language.Indonesian -> Locale("in")
    Language.Russian -> Locale("ru")
    Language.Tamil -> Locale("ta")
    Language.Spanish -> Locale("es")
    Language.French -> Locale("fr")
    Language.German -> Locale("de")
    Language.Italian -> Locale("it")
    Language.Czech -> Locale("cs")
    Language.Lithuanian -> Locale("lt")
    Language.SimplifiedChinese -> Locale("zh")
    Language.Portuguese -> Locale("pt")
    Language.Korean -> Locale("ko")
}

fun List<Language>.toLocalListCompat(): LocaleListCompat {
    return try {
        val locales = this.map { it.toLocale() }.toTypedArray()
        LocaleListCompat.create(*locales)
    } catch (e: Exception) {
        LocaleListCompat.getEmptyLocaleList()
    }
}

@Suppress("DEPRECATION")
fun LocaleListCompat.toLanguages(): List<Language> {
    return toLanguageTags().split(',').map { tag ->
        when {
            tag.startsWith("en", ignoreCase = true) -> Language.English
            tag.startsWith("tr", ignoreCase = true) -> Language.Turkish
            tag.startsWith("ar", ignoreCase = true) -> Language.Arabic
            tag.startsWith("in", ignoreCase = true) || tag.startsWith("id", ignoreCase = true) -> Language.Indonesian
            tag.startsWith("ru", ignoreCase = true) -> Language.Russian
            tag.startsWith("ta", ignoreCase = true) -> Language.Tamil
            tag.startsWith("es", ignoreCase = true) -> Language.Spanish
            tag.startsWith("fr", ignoreCase = true) -> Language.French
            tag.startsWith("de", ignoreCase = true) -> Language.German
            tag.startsWith("it", ignoreCase = true) -> Language.Italian
            tag.startsWith("cs", ignoreCase = true) -> Language.Czech
            tag.startsWith("lt", ignoreCase = true) -> Language.Lithuanian
            tag.startsWith("zh", ignoreCase = true) -> Language.SimplifiedChinese
            tag.startsWith("pt", ignoreCase = true) -> Language.Portuguese
            tag.startsWith("ko", ignoreCase = true) -> Language.Korean
            else -> Language.System
        }
    }
}