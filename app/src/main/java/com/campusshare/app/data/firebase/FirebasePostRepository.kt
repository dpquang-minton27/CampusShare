package com.campusshare.app.data.firebase

import com.campusshare.app.data.PostRepository
import com.campusshare.app.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebasePostRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : PostRepository {
    private fun uid(): String = requireNotNull(auth.currentUser?.uid) { "Vui lòng đăng nhập." }
    override fun observeAvailablePosts(): Flow<List<Post>> = db.collection("posts")
        .whereEqualTo("status", PostStatus.AVAILABLE.name)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .observe().map { it.documents.map { doc -> doc.toPost() } }

    override fun observeMyPosts(ownerId: String): Flow<List<Post>> = db.collection("posts")
        .whereEqualTo("ownerId", ownerId)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .observe().map { it.documents.map { doc -> doc.toPost() } }

    override fun observePost(postId: String): Flow<Post?> = db.collection("posts")
        .document(postId).observe().map { snapshot ->
            snapshot?.takeIf { it.exists() }?.toPost()
        }

    override suspend fun createPost(draft: PostDraft): String {
        val uid = uid()
        val input = DataValidation.post(draft)
        val category = db.collection("categories").document(input.categoryId).get().await()
        require(category.exists() && category.getBoolean("active") == true) { "Danh mục không khả dụng." }
        val ref = db.collection("posts").document()
        ref.set(mapOf(
            "ownerId" to uid,
            "categoryId" to input.categoryId,
            "title" to input.title,
            "description" to input.description,
            "type" to input.type.name,
            "status" to PostStatus.AVAILABLE.name,
            "imageUrls" to input.imageUrls,
            "conditionPercent" to input.conditionPercent,
            "meetingLocation" to input.meetingLocation,
            "exchangeNote" to input.exchangeNote,
            "acceptedRequestId" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )).await()
        return ref.id
    }

    override suspend fun editPost(postId: String, draft: PostDraft) {
        val ownerId = uid()
        val input = DataValidation.post(draft)
        val category = db.collection("categories").document(input.categoryId).get().await()
        require(category.exists() && category.getBoolean("active") == true)
        val ref = db.collection("posts").document(postId)
        db.runTransaction { tx ->
            val old = tx.get(ref)
            check(old.exists() && old.getString("ownerId") == ownerId) { "Bạn không phải chủ tin." }
            check(old.getString("status") == PostStatus.AVAILABLE.name) { "Tin này đã khóa chỉnh sửa." }
            tx.update(ref, mapOf(
                "categoryId" to input.categoryId,
                "title" to input.title,
                "description" to input.description,
                "type" to input.type.name,
                "imageUrls" to input.imageUrls,
                "conditionPercent" to input.conditionPercent,
                "meetingLocation" to input.meetingLocation,
                "exchangeNote" to input.exchangeNote,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            Unit
        }.await()
    }

    override suspend fun hidePost(postId: String) {
        val ownerId = uid()
        val ref = db.collection("posts").document(postId)
        db.runTransaction { tx ->
            val old = tx.get(ref)
            check(old.exists() && old.getString("ownerId") == ownerId)
            check(old.getString("status") == PostStatus.AVAILABLE.name)
            tx.update(ref, mapOf(
                "status" to PostStatus.HIDDEN.name,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            Unit
        }.await()
    }
}
