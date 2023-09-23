package com.noto.app.domain.source.remote

import com.noto.app.data.model.remote.RemoteUser

interface RemoteUserDataSource {

    suspend fun getUser(): RemoteUser

    suspend fun createUser(id: String, name: String, passwordParameters: String)

    suspend fun updateName(id: String, name: String)

}