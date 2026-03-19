package com.jds6600.sequencer.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jds6600.sequencer.domain.model.ConnectionConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "connection_prefs")

@Singleton
class ConnectionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_HOST  = stringPreferencesKey("last_host")
    private val KEY_PORT  = intPreferencesKey("last_port")
    private val KEY_TOKEN = stringPreferencesKey("last_token")

    suspend fun save(config: ConnectionConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HOST]  = config.host
            prefs[KEY_PORT]  = config.port
            prefs[KEY_TOKEN] = config.token
        }
    }

    /** Returns null if nothing was ever saved. */
    suspend fun load(): ConnectionConfig? {
        return context.dataStore.data.map { prefs ->
            val host = prefs[KEY_HOST] ?: return@map null
            ConnectionConfig(
                host  = host,
                port  = prefs[KEY_PORT]  ?: 8080,
                token = prefs[KEY_TOKEN] ?: ""
            )
        }.firstOrNull()
    }
}
