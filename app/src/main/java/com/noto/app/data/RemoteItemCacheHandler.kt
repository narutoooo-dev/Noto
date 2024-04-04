package com.noto.app.data

interface RemoteItemCacheHandler<T : Any> {

    suspend fun cacheRemoteItems(remoteItems: List<T>)

}