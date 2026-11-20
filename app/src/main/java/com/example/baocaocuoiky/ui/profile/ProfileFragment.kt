package com.example.baocaocuoiky.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.baocaocuoiky.data.local.AppDataStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.databinding.FragmentProfileBinding
import com.example.baocaocuoiky.ui.auth.LoginActivity
import com.example.baocaocuoiky.ui.detail.RequestsActivity
import com.example.baocaocuoiky.ui.home.MyPostsActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val pickAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null && _binding != null) {
                binding.imgAvatar.setImageURI(uri)
                Toast.makeText(requireContext(), "Đã cập nhật ảnh đại diện!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.layoutAvatar.setOnClickListener {
            pickAvatarLauncher.launch("image/*")
        }

        binding.btnMenuMyPosts.setOnClickListener {
            startActivity(Intent(requireContext(), MyPostsActivity::class.java))
        }

        binding.btnMenuSentRequests.setOnClickListener {
            startActivity(Intent(requireContext(), RequestsActivity::class.java).apply {
                putExtra("EXTRA_INITIAL_TAB", 0)
            })
        }

        binding.btnMenuReceivedRequests.setOnClickListener {
            startActivity(Intent(requireContext(), RequestsActivity::class.java).apply {
                putExtra("EXTRA_INITIAL_TAB", 1)
            })
        }

        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.clearSession(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        renderUserInfo()
    }

    private fun renderUserInfo() {
        val user = SessionManager.getCurrentUser(requireContext()) ?: return
        binding.tvFullName.text = user.fullName
        binding.tvStudentId.text = "MSV : ${user.studentId}"
        binding.tvClassName.text = "LỚP : ${user.className}"
        binding.tvEmail.text = user.email
        binding.tvPhone.text = user.phone.ifBlank { "0796658054" }
    }

    private fun showEditProfileDialog() {
        val user = SessionManager.getCurrentUser(requireContext()) ?: return

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }
        val edtName = EditText(requireContext()).apply {
            hint = "Họ và tên"
            setText(user.fullName)
        }
        val edtClass = EditText(requireContext()).apply {
            hint = "Lớp học phần (VD: 126LTTD01)"
            setText(user.className)
        }
        val edtPhone = EditText(requireContext()).apply {
            hint = "Số điện thoại / Zalo"
            setText(user.phone)
        }
        container.addView(edtName)
        container.addView(edtClass)
        container.addView(edtPhone)

        AlertDialog.Builder(requireContext())
            .setTitle("Chỉnh Sửa Hồ Sơ")
            .setView(container)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Lưu") { _, _ ->
                val newName = edtName.text.toString().trim()
                val newClass = edtClass.text.toString().trim()
                val newPhone = edtPhone.text.toString().trim()

                if (newName.isBlank()) {
                    Toast.makeText(requireContext(), "Họ tên không được để trống!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val updated = user.copy(
                    fullName = newName,
                    className = newClass.ifBlank { user.className },
                    phone = newPhone.ifBlank { user.phone },
                    zaloPhone = newPhone.ifBlank { user.zaloPhone }
                )
                AppDataStore.updateUser(updated)
                SessionManager.saveUser(requireContext(), updated)
                renderUserInfo()
                Toast.makeText(requireContext(), "Đã lưu thông tin hồ sơ!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
