package com.example.baocaocuoiky.data.local

import com.example.baocaocuoiky.data.model.*
import java.util.UUID

// Đặt ngay phía trên dòng: object AppDataStore {
data class CategoryItem(
    val id: String,
    val name: String,
    val description: String,
    var isActive: Boolean = true
)

object AppDataStore {

    val registeredAccounts = mutableListOf(
        Pair(
            User(
                userId = "u1",
                fullName = "Nguyễn Tấn Thắng",
                studentId = "2415141122119",
                className = "126LTTD01",
                email = "2415141122119@sv.ute.udn.vn",
                phone = "0796658054",
                zaloPhone = "0796658054",
                greenPoints = 130,
                role = "STUDENT"
            ),
            "123456"
        ),
        Pair(
            User(
                userId = "admin1",
                fullName = "Cán Bộ Quản Trị CTSV",
                studentId = "ADMIN-UTE",
                className = "Phòng Công tác Sinh viên",
                email = "admin@ute.udn.vn",
                phone = "02363822571",
                zaloPhone = "02363822571",
                greenPoints = 999,
                role = "ADMIN"
            ),
            "admin123"
        ),
        Pair(
            User(
                userId = "u2",
                fullName = "Minh Thư",
                studentId = "2415053122001",
                className = "K25CNTT",
                email = "minhthuk25@sv.ute.udn.vn",
                phone = "0905123456",
                zaloPhone = "0905123456",
                greenPoints = 85,
                role = "STUDENT"
            ),
            "123456"
        ),
        Pair(
            User(
                userId = "u3",
                fullName = "Trần Anh",
                studentId = "2315053122088",
                className = "K24KTPM",
                email = "trananhk24@sv.ute.udn.vn",
                phone = "0796658054",
                zaloPhone = "0796658054",
                greenPoints = 120,
                role = "STUDENT"
            ),
            "123456"
        )
    )

    val posts = mutableListOf(
        Post(
            postId = "p1",
            ownerId = "u2",
            ownerName = "Trần Minh Anh",
            ownerClass = "24KTPM01 - Khoa CNS",
            ownerPhone = "0912345678",
            categoryId = "books",
            categoryName = "Giáo trình",
            title = "Giáo trình Giải tích 1 & Đại số tuyến tính",
            description = "Sách còn mới 90%, không rách trang, có ghi chú công thức ôn thi cuối kỳ. Tặng lại cho bạn khóa dưới.",
            conditionPercent = 90,
            pickupLocation = "Thư viện ĐH SPKT - Tầng 2",
            imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
            postType = PostType.GIVE_AWAY,
            postStatus = PostStatus.AVAILABLE,
            requestCount = 2
        ),
        Post(
            postId = "p2",
            ownerId = "u3",
            ownerName = "Lê Hoàng Nam",
            ownerClass = "23ĐTVT02 - Khoa Điện",
            ownerPhone = "0935112233",
            categoryId = "tech",
            categoryName = "Thiết bị điện tử",
            title = "Máy tính bỏ túi Casio fx-580VN X",
            description = "Máy chính hãng còn tem, pin mới thay, nút bấm nhạy. Muốn đổi lấy giáo trình Tiếng Anh B1 hoặc tặng bạn cần.",
            conditionPercent = 85,
            pickupLocation = "Sảnh Khu A - ĐH SPKT",
            imageUrl = "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?w=600",
            postType = PostType.EXCHANGE,
            postStatus = PostStatus.AVAILABLE,
            exchangeNote = "Đổi giáo trình Tiếng Anh B1",
            requestCount = 1
        ),
        Post(
            postId = "p3",
            ownerId = "u4",
            ownerName = "Phạm Thảo Vy",
            ownerClass = "24XD01 - Khoa Xây dựng",
            ownerPhone = "0977889900",
            categoryId = "dorm",
            categoryName = "Đồ dùng KTX",
            title = "Quạt tích điện mini để bàn KTX",
            description = "Quạt 3 tốc độ gió, sạc cổng Type-C tiện lợi, chạy êm khi mất điện. Tặng bạn sinh viên ở KTX.",
            conditionPercent = 95,
            pickupLocation = "Cổng Ký túc xá UTE",
            imageUrl = "https://images.unsplash.com/photo-1618941716939-553df3c6c278?w=600",
            postType = PostType.GIVE_AWAY,
            postStatus = PostStatus.AVAILABLE,
            requestCount = 0
        ),
        Post(
            postId = "p4",
            ownerId = "u1",
            ownerName = "Nguyễn Tấn Thắng",
            ownerClass = "126LTTD01",
            ownerPhone = "0796658054",
            categoryId = "tech",
            categoryName = "Thiết bị điện tử",
            title = "Chuột không dây Logitech M185",
            description = "Chuột dùng bền, đầy đủ đầu thu USB. Mình nâng cấp chuột mới nên tặng lại cho bạn sinh viên năm nhất.",
            conditionPercent = 80,
            pickupLocation = "Căng tin trường",
            imageUrl = "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600",
            postType = PostType.GIVE_AWAY,
            postStatus = PostStatus.AVAILABLE,
            requestCount = 1
        ),
        Post(
            postId = "p_demo_1",
            ownerId = "u2",
            ownerName = "Minh Thư",
            ownerClass = "K25CNTT",
            ownerPhone = "0905123456",
            categoryId = "dorm",
            categoryName = "Đồ dùng KTX",
            title = "Quạt tích điện mini",
            description = "Quạt mini 3 tốc độ, sạc USB Type-C, dùng khi mất điện rất tiện. Tặng bạn ở KTX.",
            conditionPercent = 90,
            pickupLocation = "Cổng KTX UTE",
            imageUrl = "https://images.unsplash.com/photo-1618941716939-553df3c6c278?w=600",
            postType = PostType.GIVE_AWAY,
            postStatus = PostStatus.AVAILABLE,
            requestCount = 1
        ),
        Post(
            postId = "p_demo_2",
            ownerId = "u3",
            ownerName = "Trần Anh",
            ownerClass = "K24KTPM",
            ownerPhone = "0796658054",
            categoryId = "books",
            categoryName = "Giáo trình",
            title = "Sách Giải Tích 1",
            description = "Giáo trình Giải Tích 1 năm nhất, có ghi chú đầy đủ. Đã trao cho sinh viên.",
            conditionPercent = 85,
            pickupLocation = "Thư viện trường",
            imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
            postType = PostType.GIVE_AWAY,
            postStatus = PostStatus.TAKEN,
            requestCount = 1
        ),
        Post(
            postId = "p_demo_3",
            ownerId = "u2",
            ownerName = "Minh Thư",
            ownerClass = "K25CNTT",
            ownerPhone = "0905123456",
            categoryId = "tech",
            categoryName = "Thiết bị điện tử",
            title = "Máy tính Casio fx-570",
            description = "Máy tính bỏ túi Casio fx-570VN Plus, còn mới, tặng sinh viên năm nhất.",
            conditionPercent = 95,
            pickupLocation = "Căng tin trường",
            imageUrl = "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?w=600",
            postType = PostType.GIVE_AWAY,
            postStatus = PostStatus.AVAILABLE,
            requestCount = 1
        )
    )

    val favoritePostIds = mutableSetOf("p1")

    // Có sẵn 1 yêu cầu từ sinh viên khác gửi đến bài p4 của u1 để test tab "Nhận được"
    val requests = mutableListOf(
        ExchangeRequest(
            requestId = "req_demo_1",
            postId = "p4",
            postTitle = "Chuột không dây Logitech M185",
            postImageUrl = "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600",
            pickupLocation = "Căng tin trường",
            ownerId = "u1",
            ownerName = "Nguyễn Tấn Thắng",
            ownerPhone = "0796658054",
            requesterId = "u2",
            requesterName = "Trần Minh Anh",
            requesterStudentId = "2415053122108",
            requesterClass = "24KTPM01",
            requesterPhone = "0912345678",
            message = "Em chào anh Thắng, em năm nhất chưa có chuột học thực hành ạ. Em có thể nhận ở căng tin vào 11h30 trưa nay được không ạ?",
            requestStatus = RequestStatus.PENDING,
            createdAt = System.currentTimeMillis() - 45 * 60 * 1000L
        ),
        // 3 yêu cầu u1 đã gửi (PENDING, ACCEPTED, REJECTED)
        ExchangeRequest(
            requestId = "req_sent_1",
            postId = "p_demo_1",
            postTitle = "Quạt tích điện mini",
            postImageUrl = "https://images.unsplash.com/photo-1618941716939-553df3c6c278?w=600",
            pickupLocation = "Cổng KTX UTE",
            ownerId = "u2",
            ownerName = "Minh Thư",
            ownerPhone = "0905123456",
            requesterId = "u1",
            requesterName = "Nguyễn Tấn Thắng",
            requesterStudentId = "2415141122119",
            requesterClass = "126LTTD01",
            requesterPhone = "0796658054",
            message = "Em ở KTX phòng 304, mất điện thường xuyên nên rất cần quạt tích điện ạ. Em có thể nhận chiều nay được không ạ?",
            requestStatus = RequestStatus.PENDING,
            createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000L
        ),
        ExchangeRequest(
            requestId = "req_sent_2",
            postId = "p_demo_2",
            postTitle = "Sách Giải Tích 1",
            postImageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600",
            pickupLocation = "Thư viện trường",
            ownerId = "u3",
            ownerName = "Trần Anh",
            ownerPhone = "0796658054",
            requesterId = "u1",
            requesterName = "Nguyễn Tấn Thắng",
            requesterStudentId = "2415141122119",
            requesterClass = "126LTTD01",
            requesterPhone = "0796658054",
            message = "Kỳ này em học môn này, em xin sách để ôn thi cuối kỳ ạ. Em cảm ơn anh nhiều!",
            requestStatus = RequestStatus.ACCEPTED,
            createdAt = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        ),
        ExchangeRequest(
            requestId = "req_sent_3",
            postId = "p_demo_3",
            postTitle = "Máy tính Casio fx-570",
            postImageUrl = "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?w=600",
            pickupLocation = "Căng tin trường",
            ownerId = "u2",
            ownerName = "Minh Thư",
            ownerPhone = "0905123456",
            requesterId = "u1",
            requesterName = "Nguyễn Tấn Thắng",
            requesterStudentId = "2415141122119",
            requesterClass = "126LTTD01",
            requesterPhone = "0796658054",
            message = "Em xin máy tính để học toán ạ",
            requestStatus = RequestStatus.REJECTED,
            rejectReason = "Em đã trao cho bạn cần hơn rồi ạ",
            createdAt = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
        ),
        // 1 yêu cầu u3 gửi tới bài đăng của u1
        ExchangeRequest(
            requestId = "req_recv_1",
            postId = "p4",
            postTitle = "Chuột không dây Logitech M185",
            postImageUrl = "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=600",
            pickupLocation = "Căng tin trường",
            ownerId = "u1",
            ownerName = "Nguyễn Tấn Thắng",
            ownerPhone = "0796658054",
            requesterId = "u3",
            requesterName = "Trần Anh",
            requesterStudentId = "2315053122088",
            requesterClass = "K24KTPM",
            requesterPhone = "0796658054",
            message = "Em đang học môn lập trình kỳ 1, chưa có chuột để làm bài thực hành. Em có thể nhận chuột này được không ạ?",
            requestStatus = RequestStatus.PENDING,
            createdAt = System.currentTimeMillis() - 30 * 60 * 1000L
        )
    )

    fun login(email: String, password: String): User? {
        return registeredAccounts.firstOrNull {
            it.first.email.equals(email.trim(), ignoreCase = true) && it.second == password
        }?.first
    }

    fun register(newUser: User, password: String): Boolean {
        val exists = registeredAccounts.any { it.first.email.equals(newUser.email, ignoreCase = true) }
        if (exists) return false
        registeredAccounts.add(Pair(newUser, password))
        return true
    }

    fun updateUser(updated: User) {
        val index = registeredAccounts.indexOfFirst { it.first.userId == updated.userId }
        if (index != -1) {
            val oldPass = registeredAccounts[index].second
            registeredAccounts[index] = Pair(updated, oldPass)
        }
    }

    fun toggleFavorite(postId: String): Boolean {
        return if (favoritePostIds.contains(postId)) {
            favoritePostIds.remove(postId)
            false
        } else {
            favoritePostIds.add(postId)
            true
        }
    }

    fun sendRequest(post: Post, requester: User, message: String): ExchangeRequest? {
        val alreadySent = requests.any { it.postId == post.postId && it.requesterId == requester.userId }
        if (alreadySent || post.ownerId == requester.userId || post.postStatus != PostStatus.AVAILABLE) {
            return null
        }
        val req = ExchangeRequest(
            requestId = "req_${UUID.randomUUID().toString().take(6)}",
            postId = post.postId,
            postTitle = post.title,
            postImageUrl = post.imageUrl,
            pickupLocation = post.pickupLocation,
            ownerId = post.ownerId,
            ownerName = post.ownerName,
            ownerPhone = post.ownerPhone,
            requesterId = requester.userId,
            requesterName = requester.fullName,
            requesterStudentId = requester.studentId,
            requesterClass = requester.className,
            requesterPhone = requester.phone,
            message = message.trim(),
            requestStatus = RequestStatus.PENDING
        )
        requests.add(0, req)

        val pIdx = posts.indexOfFirst { it.postId == post.postId }
        if (pIdx != -1) {
            posts[pIdx] = posts[pIdx].copy(requestCount = posts[pIdx].requestCount + 1)
        }
        return req
    }

    fun cancelRequest(requestId: String) {
        val req = requests.firstOrNull { it.requestId == requestId } ?: return
        if (req.requestStatus == RequestStatus.PENDING) {
            req.requestStatus = RequestStatus.CANCELLED
        }
    }

    fun rejectRequest(requestId: String) {
        val req = requests.firstOrNull { it.requestId == requestId } ?: return
        if (req.requestStatus == RequestStatus.PENDING) {
            req.requestStatus = RequestStatus.REJECTED
        }
    }

    /** Chấp nhận 1 người nhận duy nhất: khóa bài đăng và tự động từ chối các yêu cầu còn lại */
    fun acceptRequest(requestId: String): Boolean {
        val winner = requests.firstOrNull { it.requestId == requestId } ?: return false
        val postIdx = posts.indexOfFirst { it.postId == winner.postId }
        if (postIdx == -1) return false

        val post = posts[postIdx]
        if (post.postStatus != PostStatus.AVAILABLE || winner.requestStatus != RequestStatus.PENDING) {
            return false
        }

        val nextPostStatus = if (post.postType == PostType.GIVE_AWAY) {
            PostStatus.TAKEN
        } else {
            PostStatus.EXCHANGED
        }
        posts[postIdx] = post.copy(postStatus = nextPostStatus)

        requests.forEach { item ->
            if (item.postId == post.postId) {
                if (item.requestId == winner.requestId) {
                    item.requestStatus = RequestStatus.ACCEPTED
                } else if (item.requestStatus == RequestStatus.PENDING) {
                    item.requestStatus = RequestStatus.REJECTED
                }
            }
        }

        // Cộng 20 Điểm Sống Xanh cho chủ bài đăng khi giao dịch thành công
        val ownerIdx = registeredAccounts.indexOfFirst { it.first.userId == post.ownerId }
        if (ownerIdx != -1) {
            val (ownerUser, pass) = registeredAccounts[ownerIdx]
            registeredAccounts[ownerIdx] = Pair(
                ownerUser.copy(greenPoints = ownerUser.greenPoints + 20),
                pass
            )
        }
        return true
    }

    val categories = mutableListOf(
        CategoryItem("books", "Giáo trình", "Sách giáo khoa, tài liệu ôn thi, đề cương các khoa"),
        CategoryItem("tech", "Thiết bị điện tử", "Máy tính bỏ túi, chuột, tai nghe, cáp sạc"),
        CategoryItem("dorm", "Đồ dùng KTX", "Quạt mini, đèn bàn, kệ sách, đồ sinh hoạt"),
        CategoryItem("study", "Dụng cụ học tập", "Balo, thước kỹ thuật, bảng vẽ, dụng cụ thực hành")
    )

    fun adminToggleHidePost(postId: String): Post? {
        val idx = posts.indexOfFirst { it.postId == postId }
        if (idx == -1) return null
        val old = posts[idx]
        val nextStatus = if (old.postStatus == PostStatus.HIDDEN) {
            PostStatus.AVAILABLE
        } else {
            PostStatus.HIDDEN
        }
        val updated = old.copy(postStatus = nextStatus)
        posts[idx] = updated
        return updated
    }

    fun adminDeletePost(postId: String) {
        posts.removeAll { it.postId == postId }
        requests.removeAll { it.postId == postId }
        favoritePostIds.remove(postId)
    }

    fun addCategory(name: String, description: String): CategoryItem {
        val item = CategoryItem(
            id = "cat_${System.currentTimeMillis().toString().takeLast(4)}",
            name = name.trim(),
            description = description.trim().ifBlank { "Danh mục đồ dùng sinh viên UTE" }
        )
        categories.add(item)
        return item
    }

    fun findUserByStudentIdOrEmail(input: String): User? {
        val key = input.trim().lowercase()
        return registeredAccounts.firstOrNull { (user, _) ->
            user.studentId.lowercase() == key || user.email.lowercase() == key
        }?.first
    }

    fun updatePassword(userId: String, newPassword: String): Boolean {
        val idx = registeredAccounts.indexOfFirst { it.first.userId == userId }
        if (idx != -1) {
            val user = registeredAccounts[idx].first
            registeredAccounts[idx] = user to newPassword
            return true
        }
        return false
    }

    fun adminApprovePost(postId: String) {
        val idx = posts.indexOfFirst { it.postId == postId }
        if (idx != -1) {
            posts[idx] = posts[idx].copy(postStatus = PostStatus.AVAILABLE)
        }
    }

    fun adminToggleLockUser(userId: String): Boolean {
        val idx = registeredAccounts.indexOfFirst { it.first.userId == userId }
        if (idx != -1) {
            val (user, pass) = registeredAccounts[idx]
            val updated = user.copy(isLocked = !user.isLocked)
            registeredAccounts[idx] = updated to pass
            return updated.isLocked
        }
        return false
    }

    init {
        // Tài khoản Admin
        if (registeredAccounts.none { it.first.role == "ADMIN" }) {
            registeredAccounts.add(
                User(
                    userId = "admin_1",
                    fullName = "Quản Trị Viên UTE",
                    studentId = "ADMIN",
                    className = "Phòng CTSV",
                    email = "admin@ute.udn.vn",
                    phone = "02363822571",
                    zaloPhone = "02363822571",
                    greenPoints = 999,
                    role = "ADMIN"
                ) to "admin123"
            )
        }

        // Tài khoản sinh viên bị khóa mẫu (để test lọc "Đã khóa" & nút "Mở khóa")
        if (registeredAccounts.none { it.first.userId == "u_locked" }) {
            registeredAccounts.add(
                User(
                    userId = "u_locked",
                    fullName = "Lê Văn Hoàng",
                    studentId = "2315053122045",
                    className = "23T2",
                    email = "2315053122045@sv.ute.udn.vn",
                    phone = "0905999888",
                    zaloPhone = "0905999888",
                    greenPoints = 10,
                    role = "STUDENT",
                    isLocked = true
                ) to "123456"
            )
        }

        // Bài đăng đang CHỜ DUYỆT mẫu (để test nút "Duyệt bài" và "Gỡ bài")
        if (posts.none { it.postId == "p_pending_1" }) {
            posts.add(
                0,
                Post(
                    postId = "p_pending_1",
                    ownerId = "u1",
                    ownerName = "Nguyễn Tấn Thắng",
                    categoryId = "books",
                    title = "Giáo trình Lập trình Android cơ bản",
                    description = "Sách còn mới, không rách trang, có ghi chú bài tập chương 1-5. Tặng lại cho bạn sinh viên khóa dưới cần học môn này.",
                    conditionPercent = 90,
                    postType = PostType.GIVE_AWAY,
                    postStatus = PostStatus.PENDING,
                    pickupLocation = "Sảnh Khu A UTE",
                    imageUrl = "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=500",
                    createdAt = System.currentTimeMillis() - 10 * 60000L
                )
            )
        }
    }
}