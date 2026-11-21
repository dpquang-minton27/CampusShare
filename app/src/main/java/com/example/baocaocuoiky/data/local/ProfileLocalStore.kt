package com.example.baocaocuoiky.data.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.baocaocuoiky.data.model.User

/**
 * Local BC3/demo profile overrides. This is NOT a replacement for a remote database.
 * Student ID and email are intentionally not editable here.
 */
object ProfileLocalStore {
    private const val PREF_NAME = "campusshare_profile_overrides"
    private fun key(uid: String, field: String) = "${uid}_$field"

    fun applySaved(context: Context, base: User): User {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(key(base.userId, "fullName"))) return base
        return base.copy(
            fullName = prefs.getString(key(base.userId, "fullName"), base.fullName) ?: base.fullName,
            className = prefs.getString(key(base.userId, "className"), base.className) ?: base.className,
            phone = prefs.getString(key(base.userId, "phone"), base.phone) ?: base.phone,
            zaloPhone = prefs.getString(key(base.userId, "zaloPhone"), base.zaloPhone) ?: base.zaloPhone
        )
    }

    fun saveProfile(context: Context, user: User) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key(user.userId, "fullName"), user.fullName)
            .putString(key(user.userId, "className"), user.className)
            .putString(key(user.userId, "phone"), user.phone)
            .putString(key(user.userId, "zaloPhone"), user.zaloPhone)
            .apply()
    }

    fun avatarUri(context: Context, uid: String): Uri? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(key(uid, "avatarUri"), null)
            ?.let(Uri::parse)

    /** Call only for a SAF OpenDocument URI. */
    fun grantAvatarRead(context: Context, uri: Uri): Boolean = try {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        true
    } catch (_: SecurityException) {
        false
    }

    fun saveAvatar(context: Context, uid: String, uri: Uri) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(key(uid, "avatarUri"), uri.toString()).apply()
    }
}
