package com.example.baocaocuoiky.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.baocaocuoiky.MainActivity
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.ProfileLocalStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.User
import com.example.baocaocuoiky.databinding.ActivityLoginBinding
import com.example.baocaocuoiky.ui.admin.AdminDashboardActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedUser = SessionManager.getCurrentUser(this)
        if (savedUser != null) {
            val restored = ProfileLocalStore.applySaved(this, savedUser)
            AppDataStore.updateUser(restored)
            SessionManager.saveUser(this, restored)
            routeByRole(restored)
            return
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Xử lý nút show/hide mật khẩu
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.btnTogglePassword.setImageResource(R.drawable.login_ic_eye_off)
            } else {
                binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnTogglePassword.setImageResource(R.drawable.login_ic_eye)
            }
            binding.edtPassword.setSelection(binding.edtPassword.text.length)
        }
        // Xử lý nút đăng nhập
        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString()
            if (email.isBlank()) {
                binding.edtEmail.error = "Vui lòng nhập email!"
                return@setOnClickListener
            }
            if (password.isBlank()) {
                binding.edtPassword.error = "Vui lòng nhập mật khẩu!"
                return@setOnClickListener
            }
            val user = AppDataStore.login(email, password)
            if (user != null) {
                if (user.isLocked) {
                    Toast.makeText(this, "Tài khoản của bạn đã bị Admin tạm khóa!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                // Restore editable local profile data after a process restart or logout.
                val restored = ProfileLocalStore.applySaved(this, user)
                AppDataStore.updateUser(restored)
                SessionManager.saveUser(this, restored)
                Toast.makeText(this, "Xin chào ${restored.fullName}!", Toast.LENGTH_SHORT).show()
                routeByRole(restored)
            } else {
                Toast.makeText(this, "Email hoặc mật khẩu chưa đúng!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun routeByRole(user: User) {
        val target = if (user.role == "ADMIN") AdminDashboardActivity::class.java
            else MainActivity::class.java
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
