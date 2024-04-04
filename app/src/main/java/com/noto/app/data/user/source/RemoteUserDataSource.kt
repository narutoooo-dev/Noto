package com.noto.app.data.user.source

import com.noto.app.data.user.model.RemoteUser

interface RemoteUserDataSource {

    suspend fun getUser(): RemoteUser

    suspend fun updateName(id: String, name: String)

}