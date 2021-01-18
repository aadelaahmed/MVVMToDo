package com.codinginflow.mvvmtodo.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.provider.ContactsContract
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import com.codinginflow.mvvmtodo.utils.PreferenceKeys.CURRENT_THEME_STATE_PREFERENCE_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.concurrent.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class FilterPreference(
    val sortQuery: String,
    val hideCompleted: Boolean,
    val currentThemeState: Boolean
) {

}

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {
    val dataStore: DataStore<Preferences> = context.createDataStore(name =Constants.PREFERENCE_NAME)
    val preferenceFlow = dataStore.data.catch { exception ->
        if (exception is IOException)
            emit(emptyPreferences())
        else
            throw exception
    }.map { preferences ->
        val sortQuery =
            preferences[PreferenceKeys.SORT_QUERY_PREFERENCE_KEY] ?: Constants.SORT_BY_DATE
        val hideCompleted = preferences[PreferenceKeys.HIDE_COMPLETED_PREFERENCE_KEY] ?: false
        val currentThemeState = preferences[CURRENT_THEME_STATE_PREFERENCE_KEY] ?: false
        FilterPreference(sortQuery, hideCompleted,currentThemeState)
    }

    suspend fun updateSortOrder(sortQuery: String) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[PreferenceKeys.SORT_QUERY_PREFERENCE_KEY] = sortQuery
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[PreferenceKeys.HIDE_COMPLETED_PREFERENCE_KEY] = hideCompleted
        }
    }

    suspend fun updateDarkMode(currentThemeState : Boolean)
    {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[CURRENT_THEME_STATE_PREFERENCE_KEY] = currentThemeState
        }
    }

}

private object PreferenceKeys {
    val CURRENT_THEME_STATE_PREFERENCE_KEY = preferencesKey<Boolean>("CURRENT_THEME_STATE_PREFERENCE_KEY")
    val SORT_QUERY_PREFERENCE_KEY = preferencesKey<String>("SORT_QUERY_PREFERENCE_KEY")
    val HIDE_COMPLETED_PREFERENCE_KEY = preferencesKey<Boolean>("HIDE_COMPLETED_PREFERENCE_KEY")

}