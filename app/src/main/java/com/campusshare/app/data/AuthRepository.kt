package com.campusshare.app.data

import com.campusshare.app.domain.model.User
import com.campusshare.app.domain.model.ProfileEdit

sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
}

/**
 * Keep synchronous session observation for the existing BC2 MainActivity.
 * Remote network actions are suspending, called by ViewModel, never on the UI thread.
 */
interface AuthRepository {
    val currentUser: User?
    val emailVerified: Boolean
    fun observeAuthState(observer: (AuthState) -> Unit): () -> Unit
    suspend fun signIn(email: String, password: String): User
    suspend fun signUp(fullName: String, email: String, password: String): User
    suspend fun updateMyProfile(edit: ProfileEdit): User
    suspend fun sendVerificationEmail()
    suspend fun reloadEmailVerification(): Boolean
    suspend fun sendPasswordReset(email: String)
    fun logout()
}
