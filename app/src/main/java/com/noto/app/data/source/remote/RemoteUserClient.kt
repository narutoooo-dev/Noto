package com.noto.app.data.source.remote

import com.noto.app.data.model.remote.RemoteUser
import com.noto.app.data.model.remote.RestErrorResponse
import com.noto.app.domain.source.remote.RemoteUserDataSource
import com.noto.app.util.Constants
import com.noto.app.util.getOrElse
import com.noto.app.util.unhandledError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody

class RemoteUserClient(private val client: HttpClient) : RemoteUserDataSource {

    override suspend fun getUser(): RemoteUser {
        return client.get("/rest/v1/users") {
            parameter(Constants.Select, "*")
        }.getOrElse<List<RemoteUser>> { response ->
            val errorResponse = response.body<RestErrorResponse>()
            unhandledError(errorResponse.message)
        }.first()
    }

    override suspend fun updateName(id: String, name: String) {
        return client.patch("/rest/v1/users") {
            parameter(Constants.Id, FilterKeys eq id)
            setBody(
                mapOf(Constants.Name to name)
            )
        }.getOrElse { response ->
            val errorResponse = response.body<RestErrorResponse>()
            unhandledError(errorResponse.message)
        }
    }
}