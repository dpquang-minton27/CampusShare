package com.campusshare.app.data

import com.campusshare.app.domain.model.User

sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
}

/** Replace the implementation for BC3; UI only depends on this contract. */
interface AuthRepository {
    val currentUser: User?
    fun observeAuthState(observer: (AuthState) -> Unit): () -> Unit
    fun login(email: String, password: String): Result<User>
    fun register(fullName: String, email: String, password: String): Result<User>
    fun updateProfile(fullName: String): Result<User>
    fun logout()
}
