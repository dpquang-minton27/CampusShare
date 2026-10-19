package com.campusshare.app.data

import com.campusshare.app.domain.model.User
import org.junit.Assert.*
import org.junit.Test

class FakeAuthRepositoryTest {
    private fun repository() = FakeAuthRepository(listOf(User("u1", "Nguyễn An", "an@sv.example.edu")))

    @Test fun loginNormalizesEmailButDoesNotTrimPassword() {
        val repo = repository()
        assertTrue(repo.login(" AN@SV.EXAMPLE.EDU ", "wrong").isFailure)
        assertNull(repo.currentUser)
        assertTrue(repo.login(" AN@SV.EXAMPLE.EDU ", "Campus123").isSuccess)
        assertEquals("u1", repo.currentUser?.id)
        repo.logout()
        assertTrue(repo.login("an@sv.example.edu", " Campus123 ").isFailure)
        assertNull(repo.currentUser)
    }

    @Test fun registrationRejectsInvalidAndDuplicateAccounts() {
        val repo = repository()
        assertTrue(repo.register(" ", "new@sv.edu", "secret1").isFailure)
        assertTrue(repo.register("Quang", "invalid", "secret1").isFailure)
        assertTrue(repo.register("Quang", "new@sv.edu", "123").isFailure)
        assertTrue(repo.register("Quang", " AN@sv.example.edu ", "secret1").isFailure)
        assertNull(repo.currentUser)
        val user = repo.register(" Quang ", " NEW@sv.edu ", "secret1").getOrThrow()
        assertEquals("Quang", user.fullName)
        assertEquals("new@sv.edu", user.email)
        repo.logout()
        assertTrue(repo.login("new@sv.edu", "secret1").isSuccess)
        assertEquals(user.id, repo.currentUser?.id)
    }

    @Test fun profileUpdateKeepsIdentityAndOnlyChangesCurrentAccount() {
        val repo = repository()
        assertTrue(repo.updateProfile("Quang").isFailure)
        val a = repo.login("an@sv.example.edu", "Campus123").getOrThrow()
        val updated = repo.updateProfile("Tên mới").getOrThrow()
        assertEquals(a.id, updated.id)
        assertEquals(a.email, updated.email)
        assertTrue(repo.updateProfile(" ").isFailure)
        assertEquals(updated, repo.currentUser)
        repo.logout()
        repo.register("Người B", "b@sv.edu", "secret1").getOrThrow()
        repo.updateProfile("Tên B").getOrThrow()
        repo.logout()
        assertEquals(updated, repo.login("an@sv.example.edu", "Campus123").getOrThrow())
    }

    @Test fun authStateEmitsLoginProfileLogoutAndSupportsUnsubscribe() {
        val repo = repository()
        val events = mutableListOf<AuthState>()
        val stop = repo.observeAuthState { events.add(it) }
        assertEquals(listOf(AuthState.SignedOut), events)
        repo.login("an@sv.example.edu", "Campus123").getOrThrow()
        assertEquals(repo.currentUser, (events.last() as AuthState.SignedIn).user)
        repo.updateProfile("Đổi tên").getOrThrow()
        assertEquals("Đổi tên", (events.last() as AuthState.SignedIn).user.fullName)
        repo.logout()
        assertEquals(AuthState.SignedOut, events.last())
        val count = events.size
        stop()
        repo.login("an@sv.example.edu", "Campus123").getOrThrow()
        assertEquals(count, events.size)
    }

    @Test fun newRepositoryDoesNotRestoreMockSessionOrNewAccount() {
        val first = repository()
        first.register("Quang", "q@sv.edu", "secret1").getOrThrow()
        val restarted = repository()
        assertNull(restarted.currentUser)
        assertTrue(restarted.login("q@sv.edu", "secret1").isFailure)
    }
}
