package com.campusshare.app.domain.model

// A single user can be both a giver (Post.ownerId) and a requester.
// phone and zaloUrl are private; never store them in a publicly readable users/{uid} document.
data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val zaloUrl: String? = null,
    val avatarUrl: String? = null,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

data class ProfileEdit(
    val fullName: String,
    val phone: String?,
    val zaloUrl: String?
)

data class UserContact(
    val phone: String? = null,
    val zaloUrl: String? = null
)
