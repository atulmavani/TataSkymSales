package com.zplus.tataskymsales.utility

import android.content.Context

object SharedPreference {

    private var preferences: android.content.SharedPreferences? = null
    private var editor: android.content.SharedPreferences.Editor? = null

    //region Shared Preference
    fun CreatePreference(context: Context, preferenceName: String) {
        preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        editor = preferences?.edit()
        editor?.apply()
    }
    //endregion

    fun SavePreference(preferenceKey: String, preferenceValue: String) {
        editor?.putString(preferenceKey, preferenceValue)
        editor?.apply()
    }

    fun GetPreference(context: Context, preferenceName: String, preferenceKey: String): String? {
        var text: String? = null
        try {
            preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            text = preferences?.getString(preferenceKey, null)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        return text
    }

    fun ClearPreference(context: Context, preferenceName: String) {
        preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        editor = preferences?.edit()
        editor?.clear()
        editor?.apply()
    }

    fun RemovePreference(context: Context, preferenceName: String, preferenceKey: String) {
        preferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        editor = preferences?.edit()
        editor?.remove(preferenceKey)
        editor?.apply()
    }

}