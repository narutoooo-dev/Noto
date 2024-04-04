package com.noto.app.domain

data class Translation(
    val language: Language,
    val iconId: Int,
    val translators: List<Translator>,
) {
    data class Translator(val nameId: Int, val urlId: Int?) {
        companion object
    }

    companion object
}