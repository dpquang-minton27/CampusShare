package com.campusshare.app.data.firebase

import com.campusshare.app.domain.model.*
import com.google.firebase.firestore.DocumentSnapshot

/** Explicit mapping isolates Firestore types from Trọng/Thắng ViewModels. */
internal fun DocumentSnapshot.toPublicUser(): User = User(
    id = id,
    fullName = getString("fullName").orEmpty(),
    email = "", // private email is read from FirebaseAuth or userContacts for the owner.
    avatarUrl = getString("avatarUrl"),
    createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
    updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
)
internal fun DocumentSnapshot.toCategory(): Category = Category(
    id = id,
    name = getString("name").orEmpty(),
    iconKey = getString("iconKey") ?: "category",
    sortOrder = getLong("sortOrder")?.toInt() ?: 0,
    active = getBoolean("active") ?: true
)
internal fun DocumentSnapshot.toPost(): Post = Post(
    id = id,
    ownerId = requireNotNull(getString("ownerId")) { "Missing ownerId in $id" },
    categoryId = requireNotNull(getString("categoryId")) { "Missing categoryId in $id" },
    title = getString("title").orEmpty(),
    description = getString("description").orEmpty(),
    type = PostType.valueOf(requireNotNull(getString("type"))),
    status = PostStatus.valueOf(requireNotNull(getString("status"))),
    imageUrls = (get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    conditionPercent = getLong("conditionPercent")?.toInt() ?: 100,
    meetingLocation = getString("meetingLocation").orEmpty(),
    exchangeNote = getString("exchangeNote"),
    acceptedRequestId = getString("acceptedRequestId"),
    createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
    updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
)
internal fun DocumentSnapshot.toRequest(): Request = Request(
    id = id,
    postId = requireNotNull(getString("postId")),
    ownerId = requireNotNull(getString("ownerId")),
    requesterId = requireNotNull(getString("requesterId")),
    message = getString("message").orEmpty(),
    status = RequestStatus.valueOf(requireNotNull(getString("status"))),
    createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
    updatedAtMillis = getTimestamp("updatedAt")?.toDate()?.time ?: 0L
)
internal fun DocumentSnapshot.toFavorite(): Favorite = Favorite(
    id = id,
    userId = requireNotNull(getString("userId")),
    postId = requireNotNull(getString("postId")),
    createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L
)
