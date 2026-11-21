package com.example.baocaocuoiky.data.local

import android.content.Context
import com.example.baocaocuoiky.data.model.User

object SessionManager {
    private const val PREF_NAME = "campus_share_session"

    fun saveUser(context: Context, user: User) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString("userId", user.userId)
            putString("fullName", user.fullName)
            putString("studentId", user.studentId)
            putString("className", user.className)
            putString("email", user.email)
            putString("phone", user.phone)
            putString("zaloPhone", user.zaloPhone)
            putInt("greenPoints", user.greenPoints)
            putString("role", user.role)
            apply()
        }
    }

    fun getCurrentUser(context: Context): User? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString("userId", null) ?: return null
        return User(
            userId = id,
            fullName = prefs.getString("fullName", "") ?: "",
            studentId = prefs.getString("studentId", "") ?: "",
            className = prefs.getString("className", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            phone = prefs.getString("phone", "") ?: "",
            zaloPhone = prefs.getString("zaloPhone", "") ?: "",
            greenPoints = prefs.getInt("greenPoints", 100),
            role = prefs.getString("role", "STUDENT") ?: "STUDENT"
        )
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}