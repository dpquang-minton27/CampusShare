package com.campusshare.app.data.fake

import com.campusshare.app.data.*
import com.campusshare.app.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.awaitClose

/** A single, shared in-memory state. Fake instances must not be created per screen. */
class FakeStore(
    initialPosts: List<Post> = SeedData.posts,
    initialRequests: List<Request> = SeedData.requests,
    initialFavorites: List<Favorite> = SeedData.favorites
) {
    internal val monitor = Any()
    internal val posts = MutableStateFlow(initialPosts)
    internal val requests = MutableStateFlow(initialRequests)
    internal val favorites = MutableStateFlow(initialFavorites)
}

class FakeUserRepository(private val auth: AuthRepository) : UserRepository {
    override suspend fun getMyProfile(): User = requireNotNull(auth.currentUser) { "Vui lòng đăng nhập." }
    override suspend fun saveMyProfile(edit: ProfileEdit): User = auth.updateMyProfile(edit)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observePublicUser(uid: String): Flow<User?> = callbackFlow {
        val fake = auth as? FakeAuthRepository
        if (fake == null) {
            trySend(SeedData.users.find { it.id == uid }?.copy(phone = null, zaloUrl = null))
            close()
        } else {
            val unsubscribe = auth.observeAuthState { trySend(fake.lookupPublicUser(uid)) }
            awaitClose { unsubscribe() }
        }
    }
}

class FakeCategoryRepository : CategoryRepository {
    override fun observeActiveCategories(): Flow<List<Category>> =
        flowOf(SeedData.categories.filter { it.active }.sortedBy { it.sortOrder })
}

class FakePostRepository(private val store: FakeStore, private val auth: AuthRepository) : PostRepository {
    override fun observeAvailablePosts(): Flow<List<Post>> = store.posts.map { all ->
        all.filter { it.status == PostStatus.AVAILABLE }.sortedByDescending { it.createdAtMillis }
    }
    override fun observeMyPosts(ownerId: String): Flow<List<Post>> = store.posts.map { all ->
        all.filter { it.ownerId == ownerId }.sortedByDescending { it.createdAtMillis }
    }
    override fun observePost(postId: String): Flow<Post?> = store.posts.map { all ->
        all.find { it.id == postId }
    }
    override suspend fun createPost(draft: PostDraft): String {
        val uid = requireNotNull(auth.currentUser?.id) { "Vui lòng đăng nhập." }
        val input = DataValidation.post(draft)
        require(SeedData.categories.any { it.id == input.categoryId && it.active })
        val id = java.util.UUID.randomUUID().toString()
        synchronized(store.monitor) {
            val timestamp = System.currentTimeMillis()
            store.posts.value = listOf(Post(
                id, uid, input.categoryId, input.title, input.description, input.type,
                imageUrls = input.imageUrls, conditionPercent = input.conditionPercent,
                meetingLocation = input.meetingLocation, exchangeNote = input.exchangeNote,
                createdAtMillis = timestamp, updatedAtMillis = timestamp
            )) + store.posts.value
        }
        return id
    }
    override suspend fun editPost(postId: String, draft: PostDraft) {
        val uid = requireNotNull(auth.currentUser?.id)
        val input = DataValidation.post(draft)
        require(SeedData.categories.any { it.id == input.categoryId && it.active })
        synchronized(store.monitor) {
            val post = store.posts.value.firstOrNull { it.id == postId } ?: error("Không tìm thấy bài đăng.")
            check(post.ownerId == uid) { "Bạn không phải chủ bài đăng." }
            check(post.status == PostStatus.AVAILABLE) { "Chỉ sửa được bài còn sẵn sàng." }
            store.posts.value = store.posts.value.map {
                if (it.id != postId) it else post.copy(
                    categoryId = input.categoryId, title = input.title, description = input.description,
                    type = input.type, imageUrls = input.imageUrls, conditionPercent = input.conditionPercent,
                    meetingLocation = input.meetingLocation, exchangeNote = input.exchangeNote,
                    updatedAtMillis = System.currentTimeMillis()
                )
            }
        }
    }
    override suspend fun hidePost(postId: String) {
        val uid = requireNotNull(auth.currentUser?.id)
        synchronized(store.monitor) {
            val post = store.posts.value.find { it.id == postId } ?: error("Không tìm thấy bài đăng.")
            check(post.ownerId == uid && post.status == PostStatus.AVAILABLE)
            store.posts.value = store.posts.value.map {
                if (it.id == postId) it.copy(status = PostStatus.HIDDEN, updatedAtMillis = System.currentTimeMillis()) else it
            }
        }
    }
}

class FakeFavoriteRepository(private val store: FakeStore, private val auth: AuthRepository) : FavoriteRepository {
    override fun observeFavorites(userId: String): Flow<List<Favorite>> = store.favorites.map { items ->
        items.filter { it.userId == userId }
    }
    override fun observeIsFavorite(postId: String): Flow<Boolean> {
        val uid = requireNotNull(auth.currentUser?.id)
        return store.favorites.map { items -> items.any { it.userId == uid && it.postId == postId } }
    }
    override suspend fun setFavorite(postId: String, saved: Boolean) {
        val uid = requireNotNull(auth.currentUser?.id)
        synchronized(store.monitor) {
            check(store.posts.value.any { it.id == postId })
            val id = DataValidation.favoriteId(uid, postId)
            store.favorites.value = if (saved) {
                if (store.favorites.value.any { it.id == id }) store.favorites.value
                else store.favorites.value + Favorite(id, uid, postId, System.currentTimeMillis())
            } else store.favorites.value.filterNot { it.id == id }
        }
    }
}

class FakeRequestRepository(private val store: FakeStore, private val auth: AuthRepository) : RequestRepository {
    override fun observeSent(requesterId: String): Flow<List<Request>> = store.requests.map { list ->
        list.filter { it.requesterId == requesterId }.sortedByDescending { it.createdAtMillis }
    }
    override fun observeReceived(ownerId: String): Flow<List<Request>> = store.requests.map { list ->
        list.filter { it.ownerId == ownerId }.sortedByDescending { it.createdAtMillis }
    }
    override suspend fun sendRequest(postId: String, message: String): String {
        val uid = requireNotNull(auth.currentUser?.id)
        val text = DataValidation.requestMessage(message)
        val id = DataValidation.requestId(postId, uid)
        synchronized(store.monitor) {
            val post = store.posts.value.find { it.id == postId } ?: error("Bài đăng không tồn tại.")
            check(post.status == PostStatus.AVAILABLE && post.ownerId != uid)
            check(store.requests.value.none { it.id == id }) { "Đã gửi yêu cầu cho bài này." }
            store.requests.value = store.requests.value + Request(
                id, postId, post.ownerId, uid, text, createdAtMillis = System.currentTimeMillis()
            )
        }
        return id
    }
    override suspend fun cancelRequest(requestId: String) = transition(requestId, RequestStatus.CANCELLED, false)
    override suspend fun rejectRequest(requestId: String) = transition(requestId, RequestStatus.REJECTED, true)
    private fun transition(requestId: String, next: RequestStatus, ownerAction: Boolean) {
        val uid = requireNotNull(auth.currentUser?.id)
        synchronized(store.monitor) {
            val request = store.requests.value.find { it.id == requestId } ?: error("Không tìm thấy yêu cầu.")
            check(request.status == RequestStatus.PENDING)
            check(if (ownerAction) request.ownerId == uid else request.requesterId == uid)
            store.requests.value = store.requests.value.map {
                if (it.id == requestId) it.copy(status = next, updatedAtMillis = System.currentTimeMillis()) else it
            }
        }
    }
    override suspend fun acceptRequest(requestId: String) {
        val uid = requireNotNull(auth.currentUser?.id)
        synchronized(store.monitor) {
            val request = store.requests.value.find { it.id == requestId } ?: error("Không tìm thấy yêu cầu.")
            val post = store.posts.value.find { it.id == request.postId } ?: error("Không tìm thấy bài.")
            check(post.ownerId == uid && request.ownerId == uid && request.status == RequestStatus.PENDING)
            check(post.status == PostStatus.AVAILABLE && post.acceptedRequestId == null)
            val next = if (post.type == PostType.GIVE_AWAY) PostStatus.TAKEN else PostStatus.EXCHANGED
            store.posts.value = store.posts.value.map {
                if (it.id == post.id) it.copy(status = next, acceptedRequestId = requestId,
                    updatedAtMillis = System.currentTimeMillis()) else it
            }
            store.requests.value = store.requests.value.map {
                when {
                    it.id == requestId -> it.copy(status = RequestStatus.ACCEPTED, updatedAtMillis = System.currentTimeMillis())
                    it.postId == post.id && it.status == RequestStatus.PENDING ->
                        it.copy(status = RequestStatus.REJECTED, updatedAtMillis = System.currentTimeMillis())
                    else -> it
                }
            }
        }
    }
    override suspend fun reconcileRejected(postId: String) {
        val uid = requireNotNull(auth.currentUser?.id)
        synchronized(store.monitor) {
            val post = store.posts.value.find { it.id == postId } ?: error("Không tìm thấy bài đăng.")
            check(post.ownerId == uid)
            val winner = post.acceptedRequestId ?: return
            store.requests.value = store.requests.value.map {
                if (it.postId == postId && it.id != winner && it.status == RequestStatus.PENDING)
                    it.copy(status = RequestStatus.REJECTED, updatedAtMillis = System.currentTimeMillis())
                else it
            }
        }
    }
}

class FakeContactShareRepository(private val auth: AuthRepository, private val store: FakeStore) : ContactShareRepository {
    private val shared = mutableMapOf<String, UserContact>()
    override suspend fun shareMyContact(requestId: String) {
        val user = requireNotNull(auth.currentUser)
        val req = store.requests.value.find { it.id == requestId } ?: error("Không có yêu cầu.")
        check(req.status == RequestStatus.ACCEPTED)
        check(user.id == req.ownerId || user.id == req.requesterId)
        shared["${requestId}__${user.id}"] = UserContact(user.phone, user.zaloUrl)
    }
    override suspend fun getOtherPartyContact(requestId: String): UserContact? {
        val uid = requireNotNull(auth.currentUser?.id)
        val req = store.requests.value.find { it.id == requestId } ?: error("Không có yêu cầu.")
        check(req.status == RequestStatus.ACCEPTED)
        val other = when(uid) { req.ownerId -> req.requesterId; req.requesterId -> req.ownerId; else -> error("Không có quyền.") }
        return shared["${requestId}__${other}"]
    }
}
