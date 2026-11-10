package com.campusshare.app.data.fake

import com.campusshare.app.domain.model.*

/** Only fiction/test fixtures. Never treat these identifiers as real Firebase Auth UIDs. */
object SeedData {
    val users = listOf(
        User("u1", "Nguyễn An", "an@sv.example.edu"),
        User("u2", "Trần Bình", "binh@sv.example.edu"),
        User("u3", "Lê Chi", "chi@sv.example.edu")
    )
    val categories = listOf(
        Category("books", "Sách và giáo trình", "menu_book", 1),
        Category("study", "Đồ dùng học tập", "school", 2),
        Category("dorm", "Đồ ký túc xá", "home", 3),
        Category("electronics", "Thiết bị điện tử", "devices", 4)
    )
    private val specs = listOf(
        Triple("Giáo trình Giải tích 1", "books", PostType.GIVE_AWAY),
        Triple("Sách nhập môn Kotlin", "books", PostType.EXCHANGE),
        Triple("Máy tính Casio", "study", PostType.EXCHANGE),
        Triple("Bộ thước kỹ thuật", "study", PostType.GIVE_AWAY),
        Triple("Đèn bàn học", "dorm", PostType.GIVE_AWAY),
        Triple("Kệ sách nhỏ", "dorm", PostType.EXCHANGE),
        Triple("Tai nghe có dây", "electronics", PostType.GIVE_AWAY),
        Triple("Chuột máy tính", "electronics", PostType.EXCHANGE),
        Triple("Giáo trình Tiếng Anh", "books", PostType.GIVE_AWAY),
        Triple("Quạt để bàn", "dorm", PostType.GIVE_AWAY)
    )
    val posts: List<Post> = specs.mapIndexed { i, (title, category, type) ->
        Post(
            id = "post-${i + 1}",
            ownerId = users[i % users.size].id,
            categoryId = category,
            title = title,
            description = "Đồ dùng sinh viên còn sử dụng tốt, ưu tiên gặp ở thư viện.",
            type = type,
            status = when (i) {
                4 -> PostStatus.TAKEN
                5 -> PostStatus.EXCHANGED
                9 -> PostStatus.HIDDEN
                else -> PostStatus.AVAILABLE
            },
            conditionPercent = 75 + (i % 5) * 5,
            meetingLocation = "Thư viện trường",
            exchangeNote = if (type == PostType.EXCHANGE) "Trao đổi đồ dùng học tập" else null,
            acceptedRequestId = when(i) {
                4 -> "post-5__u1"
                5 -> "post-6__u2"
                else -> null
            },
            createdAtMillis = 1_780_000_000_000L + i * 60_000L,
            updatedAtMillis = 1_780_000_000_000L + i * 60_000L
        )
    }
    val requests = listOf(
        Request("post-1__u2", "post-1", "u1", "u2", "Em cần học Giải tích", RequestStatus.PENDING),
        Request("post-1__u3", "post-1", "u1", "u3", "Cho em đăng ký nhận", RequestStatus.PENDING),
        Request("post-2__u1", "post-2", "u2", "u1", "Xin hỏi trao đổi được không", RequestStatus.CANCELLED),
        Request("post-5__u1", "post-5", "u2", "u1", "Em muốn xin đèn học", RequestStatus.ACCEPTED),
        Request("post-5__u3", "post-5", "u2", "u3", "Em muốn xin", RequestStatus.REJECTED),
        Request("post-6__u2", "post-6", "u3", "u2", "Em có đồ muốn đổi", RequestStatus.ACCEPTED)
    )
    val favorites = listOf(
        Favorite("u1__post-2", "u1", "post-2"),
        Favorite("u2__post-1", "u2", "post-1"),
        Favorite("u3__post-3", "u3", "post-3")
    )
}
