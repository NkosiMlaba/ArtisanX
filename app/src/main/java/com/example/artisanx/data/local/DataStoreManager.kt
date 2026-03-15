package com.example.artisanx.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.artisanx.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = Constants.PREFERENCES_NAME)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val roleKey = stringPreferencesKey(Constants.KEY_USER_ROLE)

    val userRoleFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[roleKey]
    }

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[roleKey] = role
        }
    }

    suspend fun clearRole() {
        context.dataStore.edit { preferences ->
            preferences.remove(roleKey)
        }
    }
}
