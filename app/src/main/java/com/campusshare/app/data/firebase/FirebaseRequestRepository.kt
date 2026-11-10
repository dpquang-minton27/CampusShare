package com.campusshare.app.data.firebase

import com.campusshare.app.data.RequestRepository
import com.campusshare.app.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseRequestRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : RequestRepository {
    private fun uid() = requireNotNull(auth.currentUser?.uid) { "Vui lòng đăng nhập." }

    override fun observeSent(requesterId: String): Flow<List<Request>> =
        db.collection("requests").whereEqualTo("requesterId", requesterId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .observe().map { it.documents.map { doc -> doc.toRequest() } }

    override fun observeReceived(ownerId: String): Flow<List<Request>> =
        db.collection("requests").whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .observe().map { it.documents.map { doc -> doc.toRequest() } }

    override suspend fun sendRequest(postId: String, message: String): String {
        val requesterId = uid()
        val safeMessage = DataValidation.requestMessage(message)
        val requestId = DataValidation.requestId(postId, requesterId)
        val postRef = db.collection("posts").document(postId)
        val requestRef = db.collection("requests").document(requestId)
        db.runTransaction { tx ->
            // All reads must precede any transaction writes.
            val post = tx.get(postRef)
            check(post.exists()) { "Bài đăng không tồn tại." }
            check(post.getString("status") == PostStatus.AVAILABLE.name) { "Món đồ không còn nhận yêu cầu." }
            val ownerId = requireNotNull(post.getString("ownerId"))
            check(ownerId != requesterId) { "Không thể xin đồ của chính mình." }
            // Deterministic ID + Rules allow create but forbid unauthorized overwrite.
            tx.set(requestRef, mapOf(
                "postId" to postId,
                "ownerId" to ownerId,
                "requesterId" to requesterId,
                "message" to safeMessage,
                "status" to RequestStatus.PENDING.name,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            Unit
        }.await()
        return requestId
    }

    override suspend fun cancelRequest(requestId: String) {
        updatePending(requestId, RequestStatus.CANCELLED, byOwner = false)
    }
    override suspend fun rejectRequest(requestId: String) {
        updatePending(requestId, RequestStatus.REJECTED, byOwner = true)
    }
    private suspend fun updatePending(requestId: String, status: RequestStatus, byOwner: Boolean) {
        val actor = uid()
        val ref = db.collection("requests").document(requestId)
        db.runTransaction { tx ->
            val old = tx.get(ref)
            check(old.exists() && old.getString("status") == RequestStatus.PENDING.name)
            val expected = if (byOwner) old.getString("ownerId") else old.getString("requesterId")
            check(actor == expected) { "Không có quyền thay đổi yêu cầu." }
            tx.update(ref, mapOf("status" to status.name, "updatedAt" to FieldValue.serverTimestamp()))
            Unit
        }.await()
    }

    override suspend fun acceptRequest(requestId: String) {
        val actor = uid()
        val requestRef = db.collection("requests").document(requestId)
        // Resolve the referenced Post without trusting UI-supplied ownerId or postId.
        val initialRequest = requestRef.get().await()
        check(initialRequest.exists()) { "Không tìm thấy yêu cầu." }
        val postId = requireNotNull(initialRequest.getString("postId"))
        val postRef = db.collection("posts").document(postId)

        db.runTransaction { tx ->
            val request = tx.get(requestRef)
            val post = tx.get(postRef)
            check(request.exists() && post.exists())
            check(request.getString("postId") == postId)
            check(request.getString("ownerId") == actor && post.getString("ownerId") == actor)
            check(request.getString("status") == RequestStatus.PENDING.name)
            check(post.getString("status") == PostStatus.AVAILABLE.name)
            check(post.getString("acceptedRequestId") == null)
            val newStatus = when(post.getString("type")) {
                PostType.GIVE_AWAY.name -> PostStatus.TAKEN
                PostType.EXCHANGE.name -> PostStatus.EXCHANGED
                else -> error("Loại bài đăng không hợp lệ.")
            }
            tx.update(postRef, mapOf(
                "status" to newStatus.name,
                "acceptedRequestId" to requestId,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            tx.update(requestRef, mapOf(
                "status" to RequestStatus.ACCEPTED.name,
                "updatedAt" to FieldValue.serverTimestamp()
            ))
            Unit
        }.await()

        // Two-document selection was committed. Rejection of siblings is a separate,
        // retryable operation, NOT part of the atomicity guarantee above.
        runCatching { reconcileRejected(postId) }
    }

    override suspend fun reconcileRejected(postId: String) {
        val actor = uid()
        val post = db.collection("posts").document(postId).get().await()
        check(post.exists() && post.getString("ownerId") == actor) { "Không có quyền." }
        val winnerId = post.getString("acceptedRequestId") ?: return
        check(post.getString("status") != PostStatus.AVAILABLE.name)
        val collection = db.collection("requests")
        // Limit avoids Firestore's 500-writes-per-batch limit. Repeat until empty.
        repeat(50) {
            // The query must prove every result belongs to the signed-in owner.
            val snapshot = collection.whereEqualTo("ownerId", actor)
                .whereEqualTo("postId", postId)
                .whereEqualTo("status", RequestStatus.PENDING.name)
                .limit(300).get().await()
            val other = snapshot.documents.filter { it.id != winnerId }
            if (other.isEmpty()) return
            val batch = db.batch()
            other.forEach { doc ->
                batch.update(doc.reference, mapOf(
                    "status" to RequestStatus.REJECTED.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                ))
            }
            batch.commit().await()
        }
        // Explicit failure rather than falsely claiming all siblings were persisted.
        error("Quá nhiều yêu cầu chờ: cần tiếp tục đồng bộ từ chối.")
    }
}
