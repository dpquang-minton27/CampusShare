package com.campusshare.app.data.firebase

import com.campusshare.app.data.ContactShareRepository
import com.campusshare.app.domain.model.UserContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

/**
 * Explicit opt-in. Contact values remain private until a party creates their own share.
 * The recipient can read only if the linked Request is ACCEPTED, enforced by Rules.
 */
class FirebaseContactShareRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ContactShareRepository {
    private fun uid() = requireNotNull(auth.currentUser?.uid) { "Vui lòng đăng nhập." }
    override suspend fun shareMyContact(requestId: String) {
        val me = uid()
        val request = db.collection("requests").document(requestId).get().await()
        check(request.exists() && request.getString("status") == "ACCEPTED")
        val ownerId = requireNotNull(request.getString("ownerId"))
        val requesterId = requireNotNull(request.getString("requesterId"))
        check(me == ownerId || me == requesterId) { "Không phải người tham gia." }
        val other = if (me == ownerId) requesterId else ownerId
        val contact = db.collection("userContacts").document(me).get().await()
        db.collection("contactShares").document("${requestId}__${me}").set(mapOf(
            "requestId" to requestId,
            "senderId" to me,
            "recipientId" to other,
            "phone" to contact.getString("phone"),
            "zaloUrl" to contact.getString("zaloUrl"),
            "updatedAt" to FieldValue.serverTimestamp()
        )).await()
    }
    override suspend fun getOtherPartyContact(requestId: String): UserContact? {
        val me = uid()
        val request = db.collection("requests").document(requestId).get().await()
        check(request.exists() && request.getString("status") == "ACCEPTED")
        val owner = request.getString("ownerId")
        val requester = request.getString("requesterId")
        val other = when(me) {
            owner -> requester
            requester -> owner
            else -> error("Bạn không tham gia giao dịch.")
        } ?: return null
        val snapshot = try {
            db.collection("contactShares").document("${requestId}__${other}").get().await()
        } catch (e: FirebaseFirestoreException) {
            // Firestore denies non-existing docs when Rules require resource.data.
            // This interface treats an absent/non-readable share as unavailable.
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) return null
            throw e
        }
        if (!snapshot.exists()) return null
        return UserContact(snapshot.getString("phone"), snapshot.getString("zaloUrl"))
    }
}
