package com.example.baocaocuoiky.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.baocaocuoiky.MainActivity
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.data.model.User
import com.example.baocaocuoiky.databinding.ActivityLoginBinding
import com.example.baocaocuoiky.ui.admin.AdminDashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tự động điều hướng đúng giao diện nếu đã đăng nhập trước đó
        val savedUser = SessionManager.getCurrentUser(this)
        if (savedUser != null) {
            routeByRole(savedUser)
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvFillDemo.setOnClickListener {
            binding.edtEmail.setText("2415141122119@sv.ute.udn.vn")
            binding.edtPassword.setText("123456")
            Toast.makeText(this, "Đã điền tài khoản Sinh viên!", Toast.LENGTH_SHORT).show()
        }

        binding.tvFillAdmin.setOnClickListener {
            binding.edtEmail.setText("admin@ute.udn.vn")
            binding.edtPassword.setText("123456")
            Toast.makeText(this, "Đã điền tài khoản Quản trị viên (Admin)!", Toast.LENGTH_SHORT).show()
        }

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
                SessionManager.saveUser(this, user)
                Toast.makeText(this, "Xin chào ${user.fullName}!", Toast.LENGTH_SHORT).show()
                routeByRole(user)
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
        val targetActivity = if (user.role == "ADMIN") {
            AdminDashboardActivity::class.java
        } else {
            MainActivity::class.java
        }
        startActivity(Intent(this, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
