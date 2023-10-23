package com.noto.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

suspend fun OutputStream.writeText(text: String) = withContext(Dispatchers.IO) {
    use { outputStream ->
        outputStream.write(text.toByteArray())
    }
}

suspend fun InputStream.readText(): String = withContext(Dispatchers.IO) {
    reader().use { inputStreamReader ->
        inputStreamReader.readText()
    }
}