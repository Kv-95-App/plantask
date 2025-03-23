package kv.apps.taskmanager.data.repositoryImpl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val KEEP_LOGGED_IN_KEY = booleanPreferencesKey("keep_logged_in")
    }

    override suspend fun saveUserSession(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { preferences ->
                    preferences[USER_ID_KEY] = userId
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun getUserSession(): Flow<String?> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences -> preferences[USER_ID_KEY] }
    }

    override suspend fun clearUserSession() {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { preferences ->
                    preferences.remove(USER_ID_KEY)
                    preferences.remove(KEEP_LOGGED_IN_KEY)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun saveKeepLoggedIn(keepLoggedIn: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                dataStore.edit { preferences ->
                    preferences[KEEP_LOGGED_IN_KEY] = keepLoggedIn
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun isKeepLoggedIn(): Flow<Boolean> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences -> preferences[KEEP_LOGGED_IN_KEY] ?: false }
    }
}