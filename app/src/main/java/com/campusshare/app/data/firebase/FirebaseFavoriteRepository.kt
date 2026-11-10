package com.campusshare.app.data.firebase

import com.campusshare.app.data.FavoriteRepository
import com.campusshare.app.domain.model.DataValidation
import com.campusshare.app.domain.model.Favorite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseFavoriteRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : FavoriteRepository {
    private fun uid() = requireNotNull(auth.currentUser?.uid) { "Vui lòng đăng nhập." }
    override fun observeFavorites(userId: String): Flow<List<Favorite>> =
        db.collection("favorites").whereEqualTo("userId", userId)
            .observe().map { it.documents.map { doc -> doc.toFavorite() } }

    override fun observeIsFavorite(postId: String): Flow<Boolean> =
        observeFavorites(uid()).map { list -> list.any { it.postId == postId } }
    override suspend fun setFavorite(postId: String, saved: Boolean) {
        val userId = uid()
        val ref = db.collection("favorites").document(DataValidation.favoriteId(userId, postId))
        val existing = db.collection("favorites")
            .whereEqualTo("userId", userId)
            .whereEqualTo("postId", postId)
            .get().await()
        if (saved) {
            if (!existing.isEmpty) return
            ref.set(mapOf(
                "userId" to userId,
                "postId" to postId,
                "createdAt" to FieldValue.serverTimestamp()
            )).await()
        } else {
            if (!existing.isEmpty) ref.delete().await()
        }
    }
}
