package com.campusshare.app.domain.model

enum class PostType { GIVE_AWAY, EXCHANGE }
enum class PostStatus { AVAILABLE, TAKEN, EXCHANGED, HIDDEN }
enum class RequestStatus { PENDING, ACCEPTED, REJECTED, CANCELLED }

data class Category(
    val id: String,
    val name: String,
    val iconKey: String = "category",
    val sortOrder: Int = 0,
    val active: Boolean = true
)

data class Post(
    val id: String,
    val ownerId: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val type: PostType,
    val status: PostStatus = PostStatus.AVAILABLE,
    val imageUrls: List<String> = emptyList(),
    val conditionPercent: Int = 100,
    val meetingLocation: String = "",
    val exchangeNote: String? = null,
    val acceptedRequestId: String? = null,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

// Form input only: caller must not provide an arbitrary ownerId, status or acceptedRequestId.
data class PostDraft(
    val categoryId: String,
    val title: String,
    val description: String,
    val type: PostType,
    val imageUrls: List<String> = emptyList(),
    val conditionPercent: Int = 100,
    val meetingLocation: String,
    val exchangeNote: String? = null
)

data class Request(
    val id: String,
    val postId: String,
    val ownerId: String,
    val requesterId: String,
    val message: String,
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

data class Favorite(
    val id: String,
    val userId: String,
    val postId: String,
    val createdAtMillis: Long = 0L
)

// UI must use this when old PENDING documents have not yet been reconciled.
fun Request.effectiveStatus(post: Post?): RequestStatus = when {
    status != RequestStatus.PENDING -> status
    post?.acceptedRequestId != null && post.acceptedRequestId != id -> RequestStatus.REJECTED
    else -> status
}
