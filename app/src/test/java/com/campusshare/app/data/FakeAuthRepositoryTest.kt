package com.campusshare.app.data

import com.campusshare.app.domain.model.ProfileEdit
import com.campusshare.app.domain.model.User
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FakeAuthRepositoryTest {
    private fun repo() = FakeAuthRepository(listOf(User("u1", "Nguyễn An", "an@sv.example.edu")))

    @Test fun authStateAndSession() = runBlocking {
        val auth = repo()
        val events = mutableListOf<AuthState>()
        val unsubscribe = auth.observeAuthState { events.add(it) }
        assertEquals(AuthState.SignedOut, events.first())
        assertEquals("u1", auth.signIn(" AN@SV.EXAMPLE.EDU ", "Campus123").id)
        assertTrue(auth.emailVerified)
        assertTrue(events.last() is AuthState.SignedIn)
        assertTrue(auth.login("an@sv.example.edu", " Campus123 ").isFailure)
        unsubscribe()
        auth.logout()
        assertNull(auth.currentUser)
    }
    @Test fun profileAndRegistration() = runBlocking {
        val auth = repo()
        assertTrue(auth.register("Quang", "quang@sv.edu", "123").isFailure)
        assertTrue(auth.register("Quang", "an@sv.example.edu", "Campus123").isFailure)
        val signed = auth.signUp("Quang", "quang@sv.edu", "Campus123")
        val updated = auth.updateMyProfile(ProfileEdit("Quang Updated", "0123456789", "https://zalo.me/test"))
        assertEquals(signed.id, updated.id)
        assertEquals("0123456789", updated.phone)
        auth.logout()
        assertEquals(signed.id, auth.signIn("quang@sv.edu", "Campus123").id)
    }
}
