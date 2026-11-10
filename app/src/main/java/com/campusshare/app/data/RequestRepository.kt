package com.campusshare.app.data

import com.campusshare.app.domain.model.Request
import kotlinx.coroutines.flow.Flow

interface RequestRepository {
    fun observeSent(requesterId: String): Flow<List<Request>>
    fun observeReceived(ownerId: String): Flow<List<Request>>
    suspend fun sendRequest(postId: String, message: String): String
    suspend fun cancelRequest(requestId: String)
    suspend fun rejectRequest(requestId: String)
    suspend fun acceptRequest(requestId: String)
    // Idempotent; can be invoked again on launch to repair pending siblings.
    suspend fun reconcileRejected(postId: String)
}
