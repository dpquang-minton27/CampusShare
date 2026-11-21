package com.example.baocaocuoiky.data.model

object RequestStatus {
    const val PENDING = "PENDING"     // Chờ duyệt
    const val ACCEPTED = "ACCEPTED"   // Được nhận
    const val REJECTED = "REJECTED"   // Bị từ chối
    const val CANCELLED = "CANCELLED" // Đã hủy
}

data class ExchangeRequest(
    val requestId: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val postImageUrl: String = "",
    val pickupLocation: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val requesterStudentId: String = "",
    val requesterClass: String = "",
    val requesterPhone: String = "",
    val message: String = "",
    var requestStatus: String = RequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val rejectReason: String = ""
)