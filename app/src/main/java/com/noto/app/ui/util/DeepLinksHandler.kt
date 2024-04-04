package com.noto.app.ui.util

import android.content.Intent

interface DeepLinksHandler {

    fun handleDeepLinks(
        intent: Intent,
        onFinishCreatingAccount: (id: String, email: String) -> Unit,
        onFinishUpdatingEmail: (email: String) -> Unit,
    ): Result<Unit>

}