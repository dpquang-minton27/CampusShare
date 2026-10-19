package com.campusshare.app.data

import com.campusshare.app.domain.model.User
import com.campusshare.app.domain.model.Category
import com.campusshare.app.domain.model.Post
import com.campusshare.app.domain.model.PostStatus
import com.campusshare.app.domain.model.PostType
/** Shared for every screen. Do not construct a repository per Activity. */
object CampusShareData {
    val users = listOf(
        User("u1", "Nguyễn An", "an@sv.example.edu"),
        User("u2", "Trần Bình", "binh@sv.example.edu"),
        User("u3", "Lê Chi", "chi@sv.example.edu")
    )
    val authRepository: AuthRepository = FakeAuthRepository(users)
    val categories = listOf(
        Category("books", "Sách và giáo trình"),
        Category("study", "Đồ dùng học tập"),
        Category("dorm", "Đồ ký túc xá"),
        Category("electronics", "Thiết bị điện tử")
    )

    private val sampleItems = listOf(
        Triple("Giáo trình Giải tích 1", "books", PostType.GIVE_AWAY),
        Triple("Sách nhập môn Kotlin", "books", PostType.EXCHANGE),
        Triple("Máy tính cầm tay", "study", PostType.EXCHANGE),
        Triple("Bộ bút và thước", "study", PostType.GIVE_AWAY),
        Triple("Đèn bàn học", "dorm", PostType.GIVE_AWAY),
        Triple("Kệ sách nhỏ", "dorm", PostType.EXCHANGE),
        Triple("Tai nghe có dây", "electronics", PostType.GIVE_AWAY),
        Triple("Chuột máy tính", "electronics", PostType.EXCHANGE),
        Triple("Giáo trình Tiếng Anh", "books", PostType.GIVE_AWAY),
        Triple("Quạt để bàn", "dorm", PostType.GIVE_AWAY)
    )

    val posts: List<Post> = sampleItems.mapIndexed { index, item ->
        Post(
            id = "post-${index + 1}",
            ownerId = users[index % users.size].id,
            categoryId = item.second,
            title = item.first,
            description = "Đồ dùng sinh viên còn sử dụng tốt.",
            type = item.third,
            status = if (index == 9) {
                PostStatus.TAKEN
            } else {
                PostStatus.AVAILABLE
            }
        )
    }

    val postRepository: PostRepository = FakePostRepository(posts)
}
