package com.campusshare.app.data.firebase

import com.campusshare.app.data.UserRepository
import com.campusshare.app.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : UserRepository {
    override suspend fun getMyProfile(): User {
        val sdk = requireNotNull(auth.currentUser) { "Vui lòng đăng nhập." }
        val publicRef = db.collection("users").document(sdk.uid)
        val contactsRef = db.collection("userContacts").document(sdk.uid)
        var public = publicRef.get().await()
        if (!public.exists()) {
            publicRef.set(mapOf(
                "id" to sdk.uid,
                "fullName" to (sdk.displayName?.takeIf(String::isNotBlank) ?: "Sinh viên"),
                "avatarUrl" to null,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
            public = publicRef.get().await()
        }
        var contact = contactsRef.get().await()
        if (!contact.exists()) {
            contactsRef.set(mapOf(
                "email" to sdk.email,
                "phone" to null,
                "zaloUrl" to null,
                "updatedAt" to FieldValue.serverTimestamp()
            )).await()
            contact = contactsRef.get().await()
        }
        return User(
            id = sdk.uid,
            fullName = public.getString("fullName") ?: sdk.displayName ?: "Sinh viên",
            email = sdk.email.orEmpty(),
            phone = contact.getString("phone"),
            zaloUrl = contact.getString("zaloUrl"),
            avatarUrl = public.getString("avatarUrl"),
            createdAtMillis = public.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
            updatedAtMillis = public.getTimestamp("updatedAt")?.toDate()?.time ?: 0L
        )
    }
    override suspend fun saveMyProfile(edit: ProfileEdit): User {
        val input = DataValidation.profile(edit)
        val sdk = requireNotNull(auth.currentUser) { "Vui lòng đăng nhập." }
        val batch = db.batch()
        batch.set(db.collection("users").document(sdk.uid), mapOf(
            "id" to sdk.uid,
            "fullName" to input.fullName,
            "updatedAt" to FieldValue.serverTimestamp()
        ), SetOptions.merge())
        batch.set(db.collection("userContacts").document(sdk.uid), mapOf(
            "email" to sdk.email,
            "phone" to input.phone,
            "zaloUrl" to input.zaloUrl,
            "updatedAt" to FieldValue.serverTimestamp()
        ), SetOptions.merge())
        batch.commit().await()
        // Profile is already committed even if FirebaseAuth display-name update later fails.
        sdk.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(input.fullName).build()).await()
        return getMyProfile()
    }
    override fun observePublicUser(uid: String): Flow<User?> =
        db.collection("users").document(uid).observe().map { document ->
            document?.takeIf { it.exists() }?.toPublicUser()
        }
}
