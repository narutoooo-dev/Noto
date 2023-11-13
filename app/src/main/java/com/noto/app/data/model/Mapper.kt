package com.noto.app.data.model

fun interface Mapper<in I, out O> {

    fun map(input: I): O

}