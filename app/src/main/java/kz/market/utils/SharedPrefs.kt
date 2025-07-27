package kz.market.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefs {
    private const val PREFS_NAME = "market_pref"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun <T> set(key: String, value: T) {
        prefs.edit {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                is Set<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    putStringSet(key, value as? Set<String>)
                }
                else -> throw IllegalArgumentException("Unsupported type: ${value?.javaClass}")
            }
            apply()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T {
        return when (default) {
            is String -> prefs.getString(key, default) as T
            is Int -> prefs.getInt(key, default) as T
            is Long -> prefs.getLong(key, default) as T
            is Float -> prefs.getFloat(key, default) as T
            is Boolean -> prefs.getBoolean(key, default) as T
            is Set<*> -> prefs.getStringSet(key, default as? Set<String>) as T
            else -> throw IllegalArgumentException("Unsupported type: ${default?.javaClass}")
        }
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    fun contains(key: String): Boolean {
        return prefs.contains(key)
    }

    fun clear() {
        prefs.edit { clear() }
    }
}