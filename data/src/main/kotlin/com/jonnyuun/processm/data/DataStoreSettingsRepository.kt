package com.jonnyuun.processm.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jonnyuun.processm.domain.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "droidsypm_settings")

class DataStoreSettingsRepository(
    private val context: Context,
) : SettingsRepository {

    private val key = longPreferencesKey("sampling_interval_ms")

    override val samplingIntervalMs: Flow<Long> =
        context.dataStore.data.map { prefs -> prefs[key] ?: 2_000L }

    override suspend fun setSamplingIntervalMs(value: Long) {
        context.dataStore.edit { prefs -> prefs[key] = value }
    }
}
