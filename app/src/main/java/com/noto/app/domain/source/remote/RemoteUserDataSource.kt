package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteUser

interface RemoteUserDataSource {

    suspend fun getUser(): RemoteUser

    suspend fun updateName(id: String, name: String)

}