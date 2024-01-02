package com.noto.app.data.cache

sealed interface RemoteItemCacheHandler<T : Any> {

    suspend fun cacheRemoteItems(remoteItems: List<T>)

}