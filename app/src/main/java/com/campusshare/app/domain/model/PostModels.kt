package com.campusshare.app.domain.model

enum class PostType {
    GIVE_AWAY, EXCHANGE
}

enum class PostStatus {
    AVAILABLE, TAKEN, EXCHANGED, HIDDEN
}

enum class RequestStatus {
    PENDING, ACCEPTED, REJECTED, CANCELLED
}

data class Category(
    val id: String,
    val name: String
)

data class Post(
    val id: String,
    val ownerId: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val type: PostType,
    val status: PostStatus = PostStatus.AVAILABLE,
    val imageUrl: String? = null
)

data class Favorite(
    val userId: String,
    val postId: String
)

data class Request(
    val id: String,
    val postId: String,
    val requesterId: String,
    val message: String,
    val status: RequestStatus = RequestStatus.PENDING
)