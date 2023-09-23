package com.noto.app.data.source.remote

object FilterKeys {
    infix fun eq(value: String) = "eq.$value"
}