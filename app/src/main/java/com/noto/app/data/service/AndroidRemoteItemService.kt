package com.noto.app.data.service

import androidx.work.Constraints
import androidx.work.NetworkType

interface AndroidRemoteItemService {

    fun buildConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

}