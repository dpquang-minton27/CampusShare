package com.example.baocaocuoiky.data.model

object PostType {
    const val GIVE_AWAY = "GIVE_AWAY" // Cho tặng 0đ
    const val EXCHANGE = "EXCHANGE"   // Trao đổi đồ dùng
}

object PostStatus {
    const val AVAILABLE = "AVAILABLE" // Đang sẵn sàng
    const val TAKEN = "TAKEN"         // Đã cho xong
    const val EXCHANGED = "EXCHANGED" // Đã đổi xong
    const val HIDDEN = "HIDDEN"       // Đã ẩn
}

data class Post(
    val postId: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerClass: String = "",
    val ownerPhone: String = "",
    val categoryId: String = "",      // "books", "tech", "dorm", "study"
    val categoryName: String = "",
    val title: String = "",
    val description: String = "",
    val conditionPercent: Int = 90,
    val pickupLocation: String = "",
    val imageUrl: String = "",
    val postType: String = PostType.GIVE_AWAY,
    val postStatus: String = PostStatus.AVAILABLE,
    val exchangeNote: String = "",
    val requestCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)