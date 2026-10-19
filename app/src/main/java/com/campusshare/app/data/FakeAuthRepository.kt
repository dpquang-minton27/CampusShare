package com.campusshare.app.data

import com.campusshare.app.domain.model.User
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

/** BC2 ONLY: accounts and session live in memory and reset on process death.
 * No network, disk persistence or production authentication is implemented.
 */
class FakeAuthRepository(initialUsers: List<User> = emptyList()) : AuthRepository {
    private data class Account(val user: User, val salt: String, val digest: ByteArray)
    private val accounts = linkedMapOf<String, Account>()
    private val observers = linkedSetOf<(AuthState) -> Unit>()
    override var currentUser: User? = null
        private set

    init {
        initialUsers.forEach { user ->
            val salt = UUID.randomUUID().toString()
            accounts[key(user.email)] = Account(user, salt, digest(salt, DEMO_PASSWORD))
        }
    }

    override fun observeAuthState(observer: (AuthState) -> Unit): () -> Unit {
        observers.add(observer)
        observer(state())
        return { observers.remove(observer); Unit }
    }

    override fun login(email: String, password: String): Result<User> = runCatching {
        validateEmail(email)
        require(password.isNotEmpty()) { "Vui lòng nhập mật khẩu." }
        val account = accounts[key(email)]
        require(account != null && MessageDigest.isEqual(account.digest, digest(account.salt, password))) {
            "Email hoặc mật khẩu chưa đúng."
        }
        currentUser = account.user
        publish()
        account.user
    }

    override fun register(fullName: String, email: String, password: String): Result<User> = runCatching {
        val name = validName(fullName)
        validateEmail(email)
        require(password.length >= 6) { "Mật khẩu cần ít nhất 6 ký tự." }
        val normalized = key(email)
        require(normalized !in accounts) { "Email này đã được đăng ký trong bản demo." }
        val user = User(UUID.randomUUID().toString(), name, normalized)
        val salt = UUID.randomUUID().toString()
        accounts[normalized] = Account(user, salt, digest(salt, password))
        currentUser = user
        publish()
        user
    }

    override fun updateProfile(fullName: String): Result<User> = runCatching {
        val user = requireNotNull(currentUser) { "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại." }
        val updated = user.copy(fullName = validName(fullName))
        val account = requireNotNull(accounts[key(user.email)])
        accounts[key(user.email)] = account.copy(user = updated)
        currentUser = updated
        publish()
        updated
    }

    override fun logout() {
        currentUser = null
        publish()
    }

    private fun state(): AuthState = currentUser?.let { AuthState.SignedIn(it) } ?: AuthState.SignedOut
    private fun publish() { observers.toList().forEach { it(state()) } }
    private fun key(email: String) = email.trim().lowercase(Locale.ROOT)
    private fun validateEmail(email: String) {
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) {
            "Vui lòng nhập email hợp lệ."
        }
    }
    private fun validName(name: String): String {
        val value = name.trim()
        require(value.length in 2..80) { "Họ tên cần từ 2 đến 80 ký tự." }
        return value
    }
    private fun digest(salt: String, password: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest("$salt:$password".toByteArray(Charsets.UTF_8))

    companion object { const val DEMO_PASSWORD = "Campus123" }
}
