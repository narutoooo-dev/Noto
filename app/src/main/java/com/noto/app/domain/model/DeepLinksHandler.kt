package com.noto.app.domain.model

import android.content.Intent

interface DeepLinksHandler {

    fun handleDeepLinks(
        intent: Intent,
        onFinishCreatingAccount: (id: String, email: String) -> Unit,
        onFinishUpdatingEmail: (email: String) -> Unit,
    )

}