package com.example.baocaocuoiky.ui.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        val currentUser = SessionManager.getCurrentUser(this) ?: run {
            finish()
            return
        }

        // Hiển thị dữ liệu hiện tại lên form
        binding.edtFullName.setText(currentUser.fullName)
        binding.edtStudentId.setText(currentUser.studentId)
        binding.edtClassName.setText(currentUser.className)
        binding.edtPhone.setText(currentUser.phone)
        binding.edtZalo.setText(currentUser.zaloPhone)

        binding.btnSave.setOnClickListener {
            val newName = binding.edtFullName.text.toString().trim()
            if (newName.isBlank()) {
                binding.edtFullName.error = "Tên không được để trống!"
                return@setOnClickListener
            }

            val updatedUser = currentUser.copy(
                fullName = newName,
                studentId = binding.edtStudentId.text.toString().trim(),
                className = binding.edtClassName.text.toString().trim(),
                phone = binding.edtPhone.text.toString().trim(),
                zaloPhone = binding.edtZalo.text.toString().trim()
            )

            AppDataStore.updateUser(updatedUser)
            SessionManager.saveUser(this, updatedUser)

            Toast.makeText(this, "Đã lưu hồ sơ!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}