package com.example.baocaocuoiky.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.baocaocuoiky.MainActivity
import com.example.baocaocuoiky.R
import com.example.baocaocuoiky.data.local.ProfileLocalStore
import com.example.baocaocuoiky.data.local.SessionManager
import com.example.baocaocuoiky.databinding.FragmentProfileBinding
import com.example.baocaocuoiky.ui.auth.LoginActivity
import com.example.baocaocuoiky.ui.detail.RequestsActivity
import com.example.baocaocuoiky.ui.home.MyPostsActivity

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val pickAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val ctx = context ?: return@registerForActivityResult
            val user = SessionManager.getCurrentUser(ctx) ?: return@registerForActivityResult
            if (uri != null) {
                if (!ProfileLocalStore.grantAvatarRead(ctx, uri)) {
                    Toast.makeText(ctx, "Không thể giữ quyền đọc ảnh đã chọn.", Toast.LENGTH_SHORT).show()
                    return@registerForActivityResult
                }
                ProfileLocalStore.saveAvatar(ctx, user.userId, uri)
                renderAvatar(user.userId)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Profile is a bottom navigation tab, not a pushed screen.
        // Back icon returns to Home instead of accidentally closing MainActivity.
        binding.btnBack.setOnClickListener {
            (activity as? MainActivity)?.openHomeTab()
        }
        binding.layoutAvatar.setOnClickListener {
            pickAvatarLauncher.launch(arrayOf("image/*"))
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
            // This must navigate to the Figma full-screen EditProfileActivity.
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
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
        if (_binding != null) renderUserInfo()
    }

    private fun renderUserInfo() {
        val user = SessionManager.getCurrentUser(requireContext()) ?: return
        binding.tvFullName.text = user.fullName
        binding.tvStudentId.text = "MSV : ${user.studentId}"
        binding.tvClassName.text = "LỚP : ${user.className}"
        binding.tvEmail.text = user.email
        binding.tvPhone.text = user.phone.ifBlank {
            user.zaloPhone.ifBlank { "Chưa cập nhật" }
        }
        renderAvatar(user.userId)
    }

    private fun renderAvatar(uid: String) {
        if (_binding == null) return
        val uri = ProfileLocalStore.avatarUri(requireContext(), uid)
        Glide.with(this)
            .load(uri ?: R.drawable.logo_ute)
            .placeholder(R.drawable.logo_ute)
            .error(R.drawable.logo_ute)
            .into(binding.imgAvatar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
