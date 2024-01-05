package com.noto.app.data.source.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSettingsDataSource(val storage: DataStore<Preferences>) {

    fun <T> getOrDefault(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return storage.data.map { preferences -> preferences[key] ?: defaultValue }
    }

    fun <T> getOrNull(key: Preferences.Key<T>): Flow<T?> {
        return storage.data.map { preferences -> preferences[key] }
    }

    inline fun <reified E : Enum<E>> getEnumOrDefault(key: Preferences.Key<String>, defaultValue: E): Flow<E> {
        return storage.data
            .map { preferences -> preferences[key] }
            .map { if (it != null) enumValueOf(it) else defaultValue }
    }

    inline fun <reified E : Enum<E>, T : E?> getEnumOrNull(key: Preferences.Key<String>): Flow<T?> {
        return storage.data
            .map { preferences -> preferences[key] }
            .map { if (it != null) enumValueOf<E>(it) as T else null }
    }

    suspend fun <T> set(key: Preferences.Key<T>, value: T?) {
        storage.edit { preferences ->
            if (value != null)
                preferences[key] = value
            else
                preferences.remove(key)
        }
    }

    suspend fun clear() {
        storage.edit { it.clear() }
    }

}