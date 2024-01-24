package com.noto.app.crypto.tink

import com.google.crypto.tink.Aead

interface TinkKeysetGenerator {

    val keysetEncryptionAead: Aead

    fun generateEncryptedKeyset(): String

}