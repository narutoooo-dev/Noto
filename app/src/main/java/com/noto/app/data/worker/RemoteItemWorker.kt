package com.noto.app.data.worker

import com.noto.app.util.CoroutineDispatcherQualifier
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface RemoteItemWorker : KoinComponent {

    val coroutineDispatcher get() = get<CoroutineDispatcher>(CoroutineDispatcherQualifier)

    companion object {
        const val NewItemId = 0L
    }

}