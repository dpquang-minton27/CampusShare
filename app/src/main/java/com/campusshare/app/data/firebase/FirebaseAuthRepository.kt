package com.campusshare.app.data.firebase

import com.campusshare.app.data.AuthRepository
import com.campusshare.app.data.AuthState
import com.campusshare.app.domain.model.DataValidation
import com.campusshare.app.domain.model.ProfileEdit
import com.campusshare.app.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : AuthRepository {
    // Important: remains available immediately when Firebase Auth has a persisted session.
    // Full name and private contact fields must be loaded via UserRepository.
    override val emailVerified: Boolean get() = auth.currentUser?.isEmailVerified == true
    override val currentUser: User?
        get() = auth.currentUser?.asSessionUser()

    override fun observeAuthState(observer: (AuthState) -> Unit): () -> Unit {
        val listener = FirebaseAuth.AuthStateListener { sdk ->
            val state = sdk.currentUser?.asSessionUser()?.let(AuthState::SignedIn)
                ?: AuthState.SignedOut
            observer(state)
        }
        auth.addAuthStateListener(listener)
        return { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): User {
        val normalized = DataValidation.email(email)
        require(password.isNotBlank()) { "Hãy nhập mật khẩu." }
        val sdkUser = requireNotNull(auth.signInWithEmailAndPassword(normalized, password).await().user)
        ensureProfile(sdkUser)
        return requireNotNull(currentUser)
    }

    override suspend fun signUp(fullName: String, email: String, password: String): User {
        val name = DataValidation.name(fullName)
        val normalized = DataValidation.email(email)
        DataValidation.password(password)
        val sdkUser = requireNotNull(auth.createUserWithEmailAndPassword(normalized, password).await().user)
        sdkUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        ensureProfile(sdkUser, name)
        // Sending verification mail can fail transiently. Allow explicit resend in the UI.
        runCatching { sdkUser.sendEmailVerification().await() }
        return requireNotNull(currentUser)
    }

    private suspend fun ensureProfile(sdkUser: FirebaseUser, fallbackName: String? = null) {
        val uid = sdkUser.uid
        val publicRef = db.collection("users").document(uid)
        if (!publicRef.get().await().exists()) {
            val name = fallbackName ?: sdkUser.displayName?.takeIf { it.isNotBlank() }
                ?: sdkUser.email?.substringBefore('@') ?: "Sinh viên"
            publicRef.set(mapOf(
                "id" to uid,
                "fullName" to name,
                "avatarUrl" to null,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
        }
        val contactsRef = db.collection("userContacts").document(uid)
        if (!contactsRef.get().await().exists()) {
            contactsRef.set(mapOf(
                "email" to sdkUser.email,
                "phone" to null,
                "zaloUrl" to null,
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
        }
    }
    override suspend fun updateMyProfile(edit: ProfileEdit): User =
        FirebaseUserRepository(auth, db).saveMyProfile(edit)

    override suspend fun sendVerificationEmail() {
        requireNotNull(auth.currentUser) { "Vui lòng đăng nhập." }.sendEmailVerification().await()
    }
    override suspend fun reloadEmailVerification(): Boolean {
        auth.currentUser?.reload()?.await()
        // Rules read the ID token claim, not only the local SDK emailVerified property.
        auth.currentUser?.getIdToken(true)?.await()
        return emailVerified
    }
    override suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(DataValidation.email(email)).await()
    }
    override fun logout() { auth.signOut() }

    private fun FirebaseUser.asSessionUser() = User(
        id = uid,
        fullName = displayName?.takeIf(String::isNotBlank) ?: email?.substringBefore('@') ?: "Sinh viên",
        email = email.orEmpty(),
        avatarUrl = photoUrl?.toString(),
        createdAtMillis = metadata?.creationTimestamp ?: 0L,
        updatedAtMillis = metadata?.lastSignInTimestamp ?: 0L
    )
}
