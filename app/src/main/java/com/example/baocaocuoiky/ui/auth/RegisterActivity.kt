package com.example.baocaocuoiky.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.model.User
import com.example.baocaocuoiky.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, statusBarInsets.top, 0, 0)
            insets
        }

        binding.btnRegister.setOnClickListener {
            handleRegister()
        }

        binding.tvLoginNow.setOnClickListener {
            finish()
        }
    }

    private fun handleRegister() {
        val fullName = binding.edtFullName.text.toString().trim()
        val studentId = binding.edtStudentId.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim().lowercase()
        val phone = binding.edtPhone.text.toString().trim()
        val password = binding.edtPassword.text.toString()
        val confirmPassword = binding.edtConfirmPassword.text.toString()

        if (fullName.isEmpty()) {
            binding.edtFullName.error = "Vui lòng nhập họ và tên"
            return
        }
        if (studentId.isEmpty()) {
            binding.edtStudentId.error = "Vui lòng nhập mã số sinh viên"
            return
        }
        if (!email.endsWith("@sv.ute.udn.vn")) {
            binding.edtEmail.error = "Vui lòng dùng email sinh viên (@sv.ute.udn.vn)"
            return
        }
        if (AppDataStore.findUserByStudentIdOrEmail(email) != null ||
            AppDataStore.findUserByStudentIdOrEmail(studentId) != null
        ) {
            binding.edtEmail.error = "Tài khoản hoặc MSSV này đã được đăng ký"
            return
        }
        if (phone.length < 9) {
            binding.edtPhone.error = "Vui lòng nhập số điện thoại hợp lệ"
            return
        }
        if (password.length < 6) {
            binding.edtPassword.error = "Mật khẩu phải từ 6 ký tự trở lên"
            return
        }
        if (password != confirmPassword) {
            binding.edtConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            return
        }

        val prefix = if (studentId.length >= 2) studentId.take(2) else "24"
        val newUser = User(
            userId = "u_${System.currentTimeMillis()}",
            fullName = fullName,
            studentId = studentId,
            className = "${prefix}KTPM01",
            email = email,
            phone = phone,
            zaloPhone = phone,
            greenPoints = 50,
            role = "STUDENT"
        )

        AppDataStore.registeredAccounts.add(newUser to password)
        Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show()
        finish()
    }
}
