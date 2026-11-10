package com.campusshare.app.data

import com.campusshare.app.data.fake.*
import com.campusshare.app.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeWorkflowTest {
    private suspend fun expectIllegal(action: suspend () -> Any?) {
        var failed = false
        try { action() } catch (_: IllegalStateException) { failed = true }
        assertTrue("Expected IllegalStateException", failed)
    }

    @Test fun exactlyOneAcceptedRequestAndSiblingsRejected() = runBlocking {
        val auth = FakeAuthRepository(SeedData.users)
        val store = FakeStore(initialPosts = SeedData.posts, initialRequests = emptyList(), initialFavorites = emptyList())
        val posts = FakePostRepository(store, auth)
        val requests = FakeRequestRepository(store, auth)
        auth.signIn("an@sv.example.edu", "Campus123")
        val postId = posts.createPost(PostDraft("books", "Sách công nghệ", "Sách còn rất tốt", PostType.GIVE_AWAY,
            meetingLocation = "Thư viện trường"))
        auth.logout()
        auth.signIn("binh@sv.example.edu", "Campus123")
        val b = requests.sendRequest(postId, "Em xin cuốn sách này")
        expectIllegal { requests.sendRequest(postId, "Gửi lần hai") }
        auth.logout()
        auth.signIn("chi@sv.example.edu", "Campus123")
        val c = requests.sendRequest(postId, "Em cũng cần sách")
        auth.logout()
        auth.signIn("an@sv.example.edu", "Campus123")
        requests.acceptRequest(b)
        val post = posts.observePost(postId).first()!!
        assertEquals(PostStatus.TAKEN, post.status)
        assertEquals(b, post.acceptedRequestId)
        assertEquals(RequestStatus.ACCEPTED, store.requests.value.single { it.id == b }.status)
        assertEquals(RequestStatus.REJECTED, store.requests.value.single { it.id == c }.status)
        expectIllegal { requests.acceptRequest(c) }
    }

    @Test fun cannotApplyToOwnPostOrModifyAnotherOwnersPost() = runBlocking {
        val auth = FakeAuthRepository(SeedData.users)
        val store = FakeStore(initialRequests = emptyList())
        val posts = FakePostRepository(store, auth)
        val requests = FakeRequestRepository(store, auth)
        auth.signIn("an@sv.example.edu", "Campus123")
        expectIllegal { requests.sendRequest("post-1", "Em xin") }
        auth.logout()
        auth.signIn("binh@sv.example.edu", "Campus123")
        expectIllegal { posts.hidePost("post-1") }
    }
    @Test fun favoritesAreUniquePerUserAndPost() = runBlocking {
        val auth = FakeAuthRepository(SeedData.users)
        val store = FakeStore(initialFavorites = emptyList())
        val favorites = FakeFavoriteRepository(store, auth)
        auth.signIn("an@sv.example.edu", "Campus123")
        favorites.setFavorite("post-1", true)
        favorites.setFavorite("post-1", true)
        assertEquals(1, favorites.observeFavorites("u1").first().size)
        favorites.setFavorite("post-1", false)
        assertFalse(favorites.observeIsFavorite("post-1").first())
    }
}
