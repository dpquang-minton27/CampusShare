package com.campusshare.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusshare.app.data.AuthRepository
import com.campusshare.app.data.UserRepository
import com.campusshare.app.domain.model.ProfileEdit
import com.campusshare.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val loading: Boolean = false,
    val emailVerified: Boolean = false,
    val version: Int = 0,
    val message: String? = null,
    val error: String? = null
)

class ProfileViewModel(
    private val auth: AuthRepository,
    private val users: UserRepository
) : ViewModel() {
    private val mutable = MutableStateFlow(ProfileUiState())
    val state = mutable.asStateFlow()
    init { refresh() }

    fun refresh() {
        if (mutable.value.loading) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(loading = true, message = null, error = null)
            runCatching { users.getMyProfile() }
                .onSuccess { user -> mutable.value = ProfileUiState(
                    user = user, loading = false, emailVerified = auth.emailVerified,
                    version = mutable.value.version + 1
                ) }
                .onFailure { mutable.value = mutable.value.copy(loading = false,
                    error = it.message ?: "Không tải được hồ sơ.") }
        }
    }
    fun save(fullName: String, phone: String?, zaloUrl: String?) {
        if (mutable.value.loading) return
        viewModelScope.launch {
            mutable.value = mutable.value.copy(loading = true, message = null, error = null)
            runCatching { users.saveMyProfile(ProfileEdit(fullName, phone, zaloUrl)) }
                .onSuccess { user -> mutable.value = mutable.value.copy(
                    loading = false, user = user, version = mutable.value.version + 1,
                    message = "Đã lưu hồ sơ.") }
                .onFailure { mutable.value = mutable.value.copy(loading = false,
                    error = it.message ?: "Không lưu được hồ sơ.") }
        }
    }
    fun sendVerification() {
        viewModelScope.launch {
            mutable.value = mutable.value.copy(loading = true, message = null, error = null)
            runCatching { auth.sendVerificationEmail() }
                .onSuccess { mutable.value = mutable.value.copy(loading = false,
                    message = "Đã gửi email xác thực. Hãy kiểm tra hộp thư.") }
                .onFailure { mutable.value = mutable.value.copy(loading = false,
                    error = it.message ?: "Không gửi được email xác thực.") }
        }
    }
    fun checkVerification() {
        viewModelScope.launch {
            mutable.value = mutable.value.copy(loading = true, message = null, error = null)
            runCatching { auth.reloadEmailVerification() }
                .onSuccess { verified -> mutable.value = mutable.value.copy(
                    loading = false, emailVerified = verified,
                    message = if (verified) "Email đã được xác thực." else "Email chưa xác thực.") }
                .onFailure { mutable.value = mutable.value.copy(loading = false,
                    error = it.message ?: "Không kiểm tra được email.") }
        }
    }
    companion object {
        fun factory(auth: AuthRepository, users: UserRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
                return ProfileViewModel(auth, users) as T
            }
        }
    }
}
