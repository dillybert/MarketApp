package kz.market.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefs {
    private const val PREFS_NAME = "market_pref"
    lateinit var prefs: SharedPreferences
        private set

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    inline fun <reified T> set(key: String, value: T) {
        prefs.edit(commit = true) {
            when (T::class) {
                String::class  -> putString(key, value as String)
                Int::class     -> putInt(key, value as Int)
                Long::class    -> putLong(key, value as Long)
                Float::class   -> putFloat(key, value as Float)
                Boolean::class -> putBoolean(key, value as Boolean)
                Set::class     -> {
                    @Suppress("UNCHECKED_CAST")
                    putStringSet(key, value as Set<String>)
                }
                else           -> error("Unsupported type ${T::class}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> get(key: String, default: T): T = with(prefs) {
        when (T::class) {
            String::class  -> getString(key, default as String) as T
            Int::class     -> getInt(key, default as Int) as T
            Long::class    -> getLong(key, default as Long) as T
            Float::class   -> getFloat(key, default as Float) as T
            Boolean::class -> getBoolean(key, default as Boolean) as T
            Set::class     -> getStringSet(key, default as Set<String>) as T
            else           -> error("Unsupported type ${T::class}")
        }
    }


    fun remove(key: String) {
        if (!this::prefs.isInitialized) {
            throw IllegalStateException("SharedPreferences not initialized")
        }

        prefs.edit(commit = true) { remove(key) }
    }

    fun contains(key: String): Boolean {
        if (!this::prefs.isInitialized) {
            throw IllegalStateException("SharedPreferences not initialized")
        }

        return prefs.contains(key)
    }

    fun clear() {
        if (!this::prefs.isInitialized) {
            throw IllegalStateException("SharedPreferences not initialized")
        }

        prefs.edit(commit = true) { clear() }
    }
}