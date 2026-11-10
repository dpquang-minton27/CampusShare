import com.campusshare.app.data.FakeAuthRepository
import com.campusshare.app.data.fake.*
import com.campusshare.app.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val auth = FakeAuthRepository(SeedData.users)
    val store = FakeStore(initialRequests = emptyList(), initialFavorites = emptyList())
    val posts = FakePostRepository(store, auth)
    val requests = FakeRequestRepository(store, auth)
    check(auth.signIn("an@sv.example.edu", "Campus123").id == "u1")
    val p = posts.createPost(PostDraft("books", "Sách Kiến trúc", "Giáo trình còn mới",
        PostType.GIVE_AWAY, meetingLocation = "Thư viện"))
    auth.logout()
    auth.signIn("binh@sv.example.edu", "Campus123")
    val r1 = requests.sendRequest(p, "Em muốn xin tài liệu")
    auth.logout()
    auth.signIn("chi@sv.example.edu", "Campus123")
    val r2 = requests.sendRequest(p, "Em xin một bản")
    auth.logout()
    auth.signIn("an@sv.example.edu", "Campus123")
    requests.acceptRequest(r1)
    val updated = posts.observePost(p).first()!!
    check(updated.status == PostStatus.TAKEN)
    check(updated.acceptedRequestId == r1)
    check(store.requests.value.first { it.id == r2 }.status == RequestStatus.REJECTED)
    check(store.requests.value.count { it.postId == p && it.status == RequestStatus.ACCEPTED } == 1)
    println("PASS: model + validators + Fake Auth/Post/Request transaction")
}
