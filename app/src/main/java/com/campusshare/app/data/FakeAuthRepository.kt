package com.campusshare.app.data

import com.campusshare.app.domain.model.*
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/** In-memory only; preserves BC2 direct-call methods for its existing unit tests. */
class FakeAuthRepository(initialUsers: List<User> = emptyList()) : AuthRepository {
    private data class Account(val user: User, val salt: String, val digest: ByteArray)
    private val accounts = linkedMapOf<String, Account>()
    private val observers = linkedSetOf<(AuthState) -> Unit>()
    override val emailVerified: Boolean get() = currentUser != null
    override var currentUser: User? = null
        private set

    init {
        initialUsers.forEach { user ->
            val salt = UUID.randomUUID().toString()
            accounts[key(user.email)] = Account(user, salt, digest(salt, DEMO_PASSWORD))
        }
    }
    override fun observeAuthState(observer: (AuthState) -> Unit): () -> Unit {
        observers += observer
        observer(state())
        return { observers.remove(observer); Unit }
    }
    // Keep source compatibility with the BC2 FakeAuthRepositoryTest and handoff.
    fun login(email: String, password: String): Result<User> = runCatching {
        val account = accounts[key(DataValidation.email(email))]
        require(account != null && MessageDigest.isEqual(account.digest, digest(account.salt, password))) {
            "Email hoặc mật khẩu chưa đúng."
        }
        currentUser = account.user
        publish()
        account.user
    }
    fun register(fullName: String, email: String, password: String): Result<User> = runCatching {
        val normalized = key(DataValidation.email(email))
        val name = DataValidation.name(fullName)
        DataValidation.password(password)
        require(normalized !in accounts) { "Email đã được đăng ký trong mock." }
        val user = User(UUID.randomUUID().toString(), name, normalized)
        val salt = UUID.randomUUID().toString()
        accounts[normalized] = Account(user, salt, digest(salt, password))
        currentUser = user
        publish()
        user
    }
    fun updateProfile(fullName: String): Result<User> = runCatching {
        val current = requireNotNull(currentUser) { "Vui lòng đăng nhập." }
        replaceProfile(current.copy(fullName = DataValidation.name(fullName)))
    }
    private fun replaceProfile(value: User): User {
        val entry = requireNotNull(accounts[key(value.email)])
        accounts[key(value.email)] = entry.copy(user = value)
        currentUser = value
        publish()
        return value
    }
    fun lookupPublicUser(uid: String): User? = accounts.values.firstOrNull { it.user.id == uid }
        ?.user?.copy(phone = null, zaloUrl = null)

    override suspend fun signIn(email: String, password: String) = login(email, password).getOrThrow()
    override suspend fun signUp(fullName: String, email: String, password: String) =
        register(fullName, email, password).getOrThrow()
    override suspend fun updateMyProfile(edit: ProfileEdit): User {
        val input = DataValidation.profile(edit)
        val old = requireNotNull(currentUser) { "Vui lòng đăng nhập." }
        return replaceProfile(old.copy(
            fullName = input.fullName, phone = input.phone, zaloUrl = input.zaloUrl,
            updatedAtMillis = System.currentTimeMillis()
        ))
    }
    override suspend fun sendVerificationEmail() = Unit
    override suspend fun reloadEmailVerification() = emailVerified
    override suspend fun sendPasswordReset(email: String) { DataValidation.email(email) }
    override fun logout() { currentUser = null; publish() }
    private fun state(): AuthState = currentUser?.let(AuthState::SignedIn) ?: AuthState.SignedOut
    private fun publish() { observers.toList().forEach { it(state()) } }
    private fun key(value: String) = value.trim().lowercase(Locale.ROOT)
    private fun digest(salt: String, password: String) = MessageDigest.getInstance("SHA-256")
        .digest("$salt:$password".toByteArray(Charsets.UTF_8))
    companion object { const val DEMO_PASSWORD = "Campus123" }
}
