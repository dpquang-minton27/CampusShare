package com.campusshare.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusshare.app.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// UI state is independent of Firebase SDK types.
data class AuthUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val information: String? = null,
    val authenticated: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutable = MutableStateFlow(AuthUiState())
    val state = mutable.asStateFlow()

    fun clearMessage() {
        mutable.value = mutable.value.copy(error = null, information = null)
    }
    fun submit(register: Boolean, name: String, email: String, password: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = AuthUiState(busy = true)
            runCatching {
                if (register) repository.signUp(name, email, password)
                else repository.signIn(email, password)
            }.onSuccess {
                mutable.value = AuthUiState(authenticated = true)
            }.onFailure {
                mutable.value = AuthUiState(error = it.message ?: "Không thể đăng nhập. Hãy thử lại.")
            }
        }
    }
    fun resetPassword(email: String) {
        if (mutable.value.busy) return
        viewModelScope.launch {
            mutable.value = AuthUiState(busy = true)
            runCatching { repository.sendPasswordReset(email) }
                .onSuccess { mutable.value = AuthUiState(
                    information = "Nếu email hợp lệ, hướng dẫn đặt lại mật khẩu sẽ được gửi."
                ) }
                .onFailure { mutable.value = AuthUiState(
                    error = it.message ?: "Chưa gửi được hướng dẫn đặt lại mật khẩu."
                ) }
        }
    }
    companion object {
        fun factory(repo: AuthRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(AuthViewModel::class.java))
                return AuthViewModel(repo) as T
            }
        }
    }
}
